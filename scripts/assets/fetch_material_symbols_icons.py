"""Fetch Material Symbols (Rounded) icons as Android vector drawables.

google/material-design-icons publishes Material Symbols for Android as one
XML VectorDrawable per icon, style and size under symbols/android/<icon>/
materialsymbolsrounded/<icon>_24px.xml, not as a single combined font (that
layout is under variablefont/ and font/ instead, and font/ is the older,
separate "Material Icons" set). This fetches the default (weight 400, fill
0, grade 0) 24dp rounded drawable for each icon named in
material_symbols_icons.txt into app/src/main/res/drawable/ic_symbol_<icon>.xml.

Usage: python3 scripts/assets/fetch_material_symbols_icons.py
"""

from __future__ import annotations

import re
from pathlib import Path

from fetch_google_assets import default_branch, get_bytes

OWNER, REPO = "google", "material-design-icons"
ICON_NAME = re.compile(r"^[a-z][a-z0-9_]*$")

def read_icon_names(manifest: Path) -> list[str]:
    names = []
    for line in manifest.read_text(encoding="utf-8").splitlines():
        name = line.split("#", 1)[0].strip()
        if not name:
            continue
        if not ICON_NAME.match(name):
            raise ValueError(f"Invalid icon name in {manifest}: {name!r}")
        names.append(name)
    return names

def fetch_icon(branch: str, name: str, destination: Path) -> None:
    url = (
        f"https://raw.githubusercontent.com/{OWNER}/{REPO}/{branch}/"
        f"symbols/android/{name}/materialsymbolsrounded/{name}_24px.xml"
    )
    content = get_bytes(url)
    if not content.lstrip().startswith(b"<"):
        raise RuntimeError(f"Unexpected (non-XML) response fetching {name!r} from {url}")
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_bytes(content)

def main() -> int:
    root = Path(__file__).resolve().parents[2]
    names = read_icon_names(root / "scripts/assets/material_symbols_icons.txt")
    if not names:
        print("No Material Symbols icons listed in material_symbols_icons.txt; nothing to fetch.")
        return 0
    branch = default_branch(OWNER, REPO)
    drawable_dir = root / "app/src/main/res/drawable"
    for name in names:
        destination = drawable_dir / f"ic_symbol_{name}.xml"
        fetch_icon(branch, name, destination)
        print(f"Fetched {name} -> {destination.relative_to(root)}")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
