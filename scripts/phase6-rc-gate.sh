#!/usr/bin/env bash
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

VERSION="${1:-}"
APK_PATH="${2:-}"
OUT_DIR="${3:-release-candidate}"

fail() { echo "[FAIL] $*" >&2; exit 1; }
pass() { echo "[PASS] $*"; }
info() { echo "[INFO] $*"; }

APP_GRADLE="app/build.gradle.kts"
MANIFEST="app/src/main/AndroidManifest.xml"
RELEASE_WORKFLOW=".github/workflows/release.yml"

[[ -f "$APP_GRADLE" ]] || fail "missing $APP_GRADLE"
[[ -f "$MANIFEST" ]] || fail "missing $MANIFEST"
[[ -f "$RELEASE_WORKFLOW" ]] || fail "missing $RELEASE_WORKFLOW"

version_name="$(sed -nE 's/^[[:space:]]*versionName = "([^"]+)".*/\1/p' "$APP_GRADLE" | head -n1)"
version_code="$(sed -nE 's/^[[:space:]]*versionCode = ([0-9]+).*/\1/p' "$APP_GRADLE" | head -n1)"
compile_sdk="$(sed -nE 's/^[[:space:]]*compileSdk = ([0-9]+).*/\1/p' "$APP_GRADLE" | head -n1)"
target_sdk="$(sed -nE 's/^[[:space:]]*targetSdk = ([0-9]+).*/\1/p' "$APP_GRADLE" | head -n1)"
app_id="$(sed -nE 's/^[[:space:]]*applicationId = "([^"]+)".*/\1/p' "$APP_GRADLE" | head -n1)"

[[ -n "$version_name" ]] || fail "versionName missing"
[[ "$version_code" =~ ^[0-9]+$ ]] || fail "versionCode missing or invalid"
[[ "$compile_sdk" == "37" ]] || fail "compileSdk must remain 37"
[[ "$target_sdk" == "37" ]] || fail "targetSdk must remain 37"
[[ "$app_id" == "com.febricahyaa.clockapp" ]] || fail "unexpected applicationId: $app_id"
pass "source release identity ($version_name / $version_code)"

if [[ -n "$VERSION" ]]; then
  expected="${VERSION#v}"
  [[ "$expected" == "$version_name" ]] || fail "requested release $expected does not match versionName $version_name"
  [[ "$VERSION" =~ ^v[0-9]+\.[0-9]+\.[0-9]+([.-][0-9A-Za-z.-]+)?$ ]] || fail "invalid release tag: $VERSION"
  pass "release tag matches source version"
fi

python3 - "$MANIFEST" <<'PY'
import sys
import xml.etree.ElementTree as ET
path = sys.argv[1]
ns = "{http://schemas.android.com/apk/res/android}"
root = ET.parse(path).getroot()
app = root.find("application")
if app is None:
    raise SystemExit("application node missing")
launchers = []
for activity in app.findall("activity"):
    name = activity.get(ns + "name", "")
    exported = activity.get(ns + "exported")
    launcher = False
    for filt in activity.findall("intent-filter"):
        action = filt.find("action")
        cats = {c.get(ns + "name") for c in filt.findall("category")}
        if action is not None and action.get(ns + "name") == "android.intent.action.MAIN" and "android.intent.category.LAUNCHER" in cats:
            launcher = True
    if launcher:
        launchers.append(name)
    if exported == "true" and not launcher:
        raise SystemExit(f"non-launcher exported activity: {name}")
if launchers != [".MainActivity"]:
    raise SystemExit(f"launcher boundary mismatch: {launchers}")
PY
pass "manifest export boundary"

grep -Fq 'Run Phase 6A static release gate' "$RELEASE_WORKFLOW" || fail "release workflow missing Phase 6A gate"
grep -Fq 'phase6-rc-gate.sh' "$RELEASE_WORKFLOW" || fail "release workflow missing Phase 6C gate"
rc_line="$(grep -n 'phase6-rc-gate.sh' "$RELEASE_WORKFLOW" | head -n1 | cut -d: -f1)"
commit_line="$(grep -n 'git push origin HEAD' "$RELEASE_WORKFLOW" | head -n1 | cut -d: -f1)"
[[ -n "$rc_line" && -n "$commit_line" && "$rc_line" -lt "$commit_line" ]] || fail "release workflow pushes before Phase 6C gate"
pass "release workflow gate order"

for forbidden in '*.jks' '*.keystore' '*.p12' local.properties; do
  if find . -path './.git' -prune -o -type f -name "$forbidden" -print -quit | grep -q .; then
    fail "private/local artifact present: $forbidden"
  fi
done
if find . -path './.git' -prune -o -type f \( -name '*.apk' -o -name '*.aab' \) -print -quit | grep -q .; then
  fail "release binary committed into source tree"
fi
pass "repository release-artifact hygiene"

if [[ -n "$APK_PATH" ]]; then
  [[ -f "$APK_PATH" ]] || fail "APK not found: $APK_PATH"
  command -v sha256sum >/dev/null || fail "sha256sum not available"
  sha_file="${APK_PATH}.sha256"
  [[ -f "$sha_file" ]] || fail "checksum sidecar missing: $sha_file"
  expected_sha="$(cut -d' ' -f1 "$sha_file")"
  actual_sha="$(sha256sum "$APK_PATH" | cut -d' ' -f1)"
  [[ "$expected_sha" == "$actual_sha" ]] || fail "APK SHA-256 mismatch"
  pass "APK SHA-256 verified"

  build_tools="${ANDROID_SDK_ROOT:-}/build-tools/${ANDROID_BUILD_TOOLS:-37.0.0}"
  apksigner="$build_tools/apksigner"
  if [[ -x "$apksigner" ]]; then
    "$apksigner" verify --verbose "$APK_PATH" >/dev/null || fail "apksigner verification failed"
    pass "APK signature verified"
  else
    info "apksigner unavailable locally; CI/device release environment must verify signature"
  fi

  aapt2="${ANDROID_SDK_ROOT:-}/build-tools/${ANDROID_BUILD_TOOLS:-37.0.0}/aapt2"
  if [[ -x "$aapt2" ]]; then
    badging="$($aapt2 dump badging "$APK_PATH")"
    grep -Fq "package: name='com.febricahyaa.clockapp'" <<<"$badging" || fail "APK applicationId mismatch"
    grep -Fq "versionCode='$version_code'" <<<"$badging" || fail "APK versionCode mismatch"
    grep -Fq "versionName='$version_name'" <<<"$badging" || fail "APK versionName mismatch"
    pass "APK identity verified"
  else
    info "aapt2 unavailable locally; CI release environment must verify APK identity"
  fi
fi

mkdir -p "$OUT_DIR"
python3 - "$OUT_DIR/RELEASE-CANDIDATE.json" "$VERSION" "$version_name" "$version_code" "$app_id" "$compile_sdk" "$target_sdk" "$APK_PATH" <<'PY'
import json, os, subprocess, sys
out, tag, vn, vc, app_id, compile_sdk, target_sdk, apk = sys.argv[1:]
commit = ""
try:
    commit = subprocess.check_output(["git", "rev-parse", "HEAD"], text=True, stderr=subprocess.DEVNULL).strip()
except Exception:
    pass
sha = None
if apk and os.path.isfile(apk):
    import hashlib
    h = hashlib.sha256()
    with open(apk, "rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    sha = h.hexdigest()
obj = {
    "product": "Chrona",
    "applicationId": app_id,
    "releaseTag": tag or None,
    "versionName": vn,
    "versionCode": int(vc),
    "compileSdk": int(compile_sdk),
    "targetSdk": int(target_sdk),
    "sourceCommit": commit or None,
    "apkSha256": sha,
    "artifactPath": apk or None,
}
with open(out, "w", encoding="utf-8") as f:
    json.dump(obj, f, indent=2, sort_keys=True)
    f.write("\n")
print(out)
PY

pass "release-candidate manifest generated"
echo "Phase 6C release-candidate gate passed"
echo "  versionName: $version_name"
echo "  versionCode: $version_code"
