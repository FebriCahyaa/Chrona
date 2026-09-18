#!/usr/bin/env bash
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

# Native syntax check.
if [[ -n "${JAVA_HOME:-}" && -d "${JAVA_HOME}/include" ]]; then
  JH="${JAVA_HOME}/include"
else
  JAVAC_PATH="$(command -v javac || true)"
  if [[ -z "$JAVAC_PATH" ]]; then
    echo "javac is required for the JNI header syntax check." >&2
    exit 1
  fi
  JAVAC_REAL="$(readlink -f "$JAVAC_PATH")"
  JAVA_HOME_DETECTED="$(cd "$(dirname "$JAVAC_REAL")/.." && pwd)"
  JH="$JAVA_HOME_DETECTED/include"
fi

test -d "$JH"

a=$(printf '%s' app/src/main/cpp/chrona_time.cpp)
b=$(printf '%s' app/src/main/cpp/chrona_clock.cpp)
clang++ -std=c++20 -Wall -Wextra -Werror=return-type -fsyntax-only \
  -I"$JH" -I"$JH/linux" \
  "$a" "$b"
echo "C++ syntax: PASS"

# Android resource reference check.
python3 - <<'PY'
from pathlib import Path
import re

root = Path('app/src/main')
refs = []
for p in root.rglob('*'):
    if p.suffix in {'.kt', '.java'}:
        text = p.read_text(errors='ignore')
        refs += [(*m, str(p)) for m in re.findall(
            r'R\.(string|drawable|layout|xml|mipmap|color|style|id)\.([A-Za-z0-9_]+)',
            text,
        )]

res = {k: set() for k in ['string', 'drawable', 'layout', 'xml', 'mipmap', 'color', 'style', 'id']}
for p in (root / 'res').rglob('*'):
    if not p.is_file():
        continue
    parent = p.parent.name
    if parent.startswith('values') and p.suffix == '.xml':
        text = p.read_text(errors='ignore')
        for kind in res:
            res[kind].update(re.findall(fr'<{kind}\s+[^>]*name="([^"]+)"', text))
    elif parent.split('-')[0] in res:
        res[parent.split('-')[0]].add(p.stem)

missing = sorted({(kind, name, path) for kind, name, path in refs if name not in res[kind]})
if missing:
    raise SystemExit(f'Missing resources: {missing}')
print('Resource references: PASS')
PY

# Copyright coverage check for project-authored source/configuration files.
# Missing notices are repaired automatically before the final verification.
python3 - <<'PY'
from pathlib import Path

notice = 'Copyright (c) 2026 Febrian Rahmad Cahya'
tracked_extensions = {'.kt', '.java', '.cpp', '.h', '.xml', '.kts', '.pro', '.properties', '.txt', '.yml', '.yaml', '.sh', '.md'}
excluded_extensions = {'.png', '.jpg', '.jpeg', '.webp', '.gif', '.jar', '.bat'}
excluded_names = {'gradlew', 'gradlew.bat', 'gradle-wrapper.properties'}


def header_for(path: Path) -> str:
    ext = path.suffix.lower()
    if ext in {'.kt', '.java', '.cpp', '.h', '.kts', '.pro'}:
        return f'// {notice}\n'
    if ext in {'.xml', '.md'}:
        return f'<!-- {notice} -->\n'
    return f'# {notice}\n'


def add_notice(path: Path, text: str) -> str:
    header = header_for(path)
    lines = text.splitlines(keepends=True)
    newline = '\r\n' if lines and lines[0].endswith('\r\n') else '\n'
    header = header.replace('\n', newline)

    if path.suffix.lower() == '.sh' and lines and lines[0].startswith('#!'):
        return lines[0] + header + ''.join(lines[1:])

    if path.suffix.lower() == '.xml' and lines and lines[0].lstrip().startswith('<?xml'):
        return lines[0] + header + ''.join(lines[1:])

    return header + text


def is_audited(path: Path) -> bool:
    if not path.is_file() or '.git' in path.parts:
        return False
    if path.name in excluded_names or path.suffix.lower() in excluded_extensions:
        return False
    return path.suffix.lower() in tracked_extensions or path.name in {'.gitignore', '.editorconfig', 'CODEOWNERS'}

missing = []
added = []
checked = 0

for path in Path('.').rglob('*'):
    if not is_audited(path):
        continue
    checked += 1
    try:
        text = path.read_text(encoding='utf-8')
    except UnicodeDecodeError:
        continue
    if notice in text:
        continue
    path.write_text(add_notice(path, text), encoding='utf-8', newline='')
    added.append(str(path))

for path in Path('.').rglob('*'):
    if not is_audited(path):
        continue
    try:
        text = path.read_text(encoding='utf-8')
    except UnicodeDecodeError:
        continue
    if notice not in text:
        missing.append(str(path))

if added:
    print('Copyright headers added automatically:')
    print('\n'.join(f'  {p}' for p in added))
if missing:
    print('Missing copyright headers after automatic repair:')
    print('\n'.join(f'  {p}' for p in missing))
    raise SystemExit(1)

print(f'Copyright coverage: PASS ({checked} project-authored text/config files)')
PY

echo 'Audit completed.'
