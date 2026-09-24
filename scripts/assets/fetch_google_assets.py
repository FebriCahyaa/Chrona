"""Refresh Google Sans Flex, the text typeface bundled under assets/fonts.

Material Symbols icons are fetched per-icon as Android vector drawables by
fetch_material_symbols_icons.py instead; see that script's docstring.
"""

from __future__ import annotations

import json
import os
from pathlib import Path
from urllib.request import Request, urlopen

API_ROOT = "https://api.github.com/repos"
HEADERS = {"Accept": "application/vnd.github+json", "User-Agent": "Chrona-Asset-Fetcher"}

def api_headers() -> dict:
    # Authenticated requests get a much higher api.github.com rate limit;
    # CI passes GITHUB_TOKEN, local runs work fine without it.
    token = os.environ.get("GITHUB_TOKEN", "").strip()
    return {**HEADERS, "Authorization": f"Bearer {token}"} if token else HEADERS

def get_json(url: str) -> dict:
    with urlopen(Request(url, headers=api_headers()), timeout=30) as response:
        return json.load(response)

def get_bytes(url: str) -> bytes:
    with urlopen(Request(url, headers=HEADERS), timeout=60) as response:
        return response.read()

def default_branch(owner: str, repo: str) -> str:
    # Don't assume "main": it silently walks whatever branch of that name
    # happens to exist instead of failing on a missing ref.
    return get_json(f"{API_ROOT}/{owner}/{repo}")["default_branch"]

def latest_asset(owner: str, repo: str, branch: str, predicate) -> str:
    tree = get_json(f"{API_ROOT}/{owner}/{repo}/git/trees/{branch}?recursive=1")
    paths = [item["path"] for item in tree["tree"] if item.get("type") == "blob"]
    matches = sorted(path for path in paths if predicate(path))
    if not matches:
        raise RuntimeError(f"No matching asset found in {owner}/{repo}@{branch}")
    matches.sort(key=lambda path: (0 if "[" in path else 1, 0 if "variablefont" in path.lower() else 1, path.lower()))
    return matches[0]

def save(url: str, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(get_bytes(url))

def fetch(owner: str, repo: str, predicate, destination: Path) -> str:
    branch = default_branch(owner, repo)
    path = latest_asset(owner, repo, branch, predicate)
    save(f"https://raw.githubusercontent.com/{owner}/{repo}/{branch}/{path}", destination)
    return path

def main() -> int:
    root = Path(__file__).resolve().parents[2]
    font_path = fetch(
        "googlefonts",
        "googlesans-flex",
        lambda p: p.lower().endswith(".ttf") and "googlesansflex" in p.lower(),
        root / ".generated/assets/fonts/GoogleSansFlex.ttf",
    )
    metadata = {"google_sans_flex": font_path}
    (root / ".generated/assets/fonts/metadata.json").write_text(
        json.dumps(metadata, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
