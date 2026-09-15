#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"
JH="$(dirname "$(dirname "$(readlink -f "$(command -v javac)")")")/include"
clang++ -std=c++20 -Wall -Wextra -Werror=return-type -fsyntax-only \
  -I"$JH" -I"$JH/linux" \
  app/src/main/cpp/chrona_time.cpp app/src/main/cpp/chrona_clock.cpp
echo "C++ syntax: PASS"
# Android resource references
python3 - <<'PY'
from pathlib import Path
import re
root=Path('app/src/main')
refs=[]
for p in root.rglob('*'):
    if p.suffix in {'.kt','.java'}:
        refs += [(a,b,str(p)) for a,b in re.findall(r'R\.(string|drawable|layout|xml|mipmap|color|style|id)\.([A-Za-z0-9_]+)', p.read_text(errors='ignore'))]
res={k:set() for k in ['string','drawable','layout','xml','mipmap','color','style','id']}
for p in (root/'res').rglob('*'):
    if not p.is_file(): continue
    parent=p.parent.name
    if parent.startswith('values') and p.suffix=='.xml':
        s=p.read_text(errors='ignore')
        for k in res: res[k].update(re.findall(fr'<{k}\s+[^>]*name="([^"]+)"',s))
    elif parent.split('-')[0] in res:
        res[parent.split('-')[0]].add(p.stem)
missing=sorted(set((t,n,p) for t,n,p in refs if n not in res[t]))
if missing: raise SystemExit(f'Missing resources: {missing}')
print('Resource references: PASS')
PY
printf '%s\n' 'Android Gradle compile must be run on a networked host because this package intentionally retains the existing Gradle wrapper/toolchain.'
