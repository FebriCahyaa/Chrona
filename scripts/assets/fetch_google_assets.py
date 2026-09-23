from __future__ import annotations

import json
from pathlib import Path
from urllib.request import Request, urlopen

API_ROOT = "https://api.github.com/repos"
HEADERS = {"Accept": "application/vnd.github+json", "User-Agent": "Chrona-Asset-Fetcher"}

def get_json(url: str) -> dict:
    with urlopen(Request(url, headers=HEADERS), timeout=30) as response:
        return json.load(response)

def get_bytes(url: str) -> bytes:
    with urlopen(Request(url, headers=HEADERS), timeout=60) as response:
        return response.read()

def latest_asset(owner: str, repo: str, predicate) -> str:
    tree = get_json(f"{API_ROOT}/{owner}/{repo}/git/trees/main?recursive=1")
    paths = [item["path"] for item in tree["tree"] if item.get("type") == "blob"]
    matches = sorted(path for path in paths if predicate(path))
    if not matches:
        raise RuntimeError(f"No matching asset found in {owner}/{repo}")
    matches.sort(key=lambda path: (0 if "[" in path else 1, 0 if "variablefont" in path.lower() else 1, path.lower()))
    return matches[0]

def save(url: str, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(get_bytes(url))

def main() -> int:
    root = Path(__file__).resolve().parents[2]
    font_path = latest_asset(
        "googlefonts",
        "googlesans-flex",
        lambda p: p.lower().endswith(".ttf") and "googlesansflex" in p.lower(),
    )
    symbols_path = latest_asset(
        "google",
        "material-design-icons",
        lambda p: p.lower().endswith(".ttf") and "materialsymbolsrounded" in p.lower(),
    )
    save(
        f"https://raw.githubusercontent.com/googlefonts/googlesans-flex/main/{font_path}",
        root / ".generated/assets/fonts/GoogleSansFlex.ttf",
    )
    save(
        f"https://raw.githubusercontent.com/google/material-design-icons/main/{symbols_path}",
        root / ".generated/assets/fonts/MaterialSymbolsRounded.ttf",
    )
    metadata = {
        "google_sans_flex": font_path,
        "material_symbols_rounded": symbols_path,
    }
    (root / ".generated/assets/fonts/metadata.json").write_text(
        json.dumps(metadata, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
