#!/usr/bin/env bash
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

fail() {
  echo "[FAIL] $*" >&2
  exit 1
}
pass() { echo "[PASS] $*"; }

APP_GRADLE="app/build.gradle.kts"
ROOT_GRADLE="build.gradle.kts"
WRAPPER="gradle/wrapper/gradle-wrapper.properties"
MANIFEST="app/src/main/AndroidManifest.xml"
RELEASE_WORKFLOW=".github/workflows/release.yml"

for path in "$APP_GRADLE" "$ROOT_GRADLE" "$WRAPPER" "$MANIFEST" "$RELEASE_WORKFLOW"; do
  [[ -f "$path" ]] || fail "required file missing: $path"
done

version_name="$(sed -nE 's/^[[:space:]]*versionName = "([^"]+)".*/\1/p' "$APP_GRADLE" | head -n1)"
version_code="$(sed -nE 's/^[[:space:]]*versionCode = ([0-9]+).*/\1/p' "$APP_GRADLE" | head -n1)"
[[ "$version_name" =~ ^[0-9]+\.[0-9]+\.[0-9]+([.-][0-9A-Za-z.-]+)?$ ]] || fail "invalid versionName: $version_name"
[[ "$version_code" =~ ^[1-9][0-9]*$ ]] || fail "invalid versionCode: $version_code"

requested_version="${1:-}"
if [[ -n "$requested_version" ]]; then
  [[ "$requested_version" =~ ^v[0-9]+\.[0-9]+\.[0-9]+([.-][0-9A-Za-z.-]+)?$ ]] || fail "invalid release tag: $requested_version"
  requested_semver="${requested_version#v}"
  [[ "$requested_semver" == "$version_name" ]] || fail "release tag $requested_version does not match versionName $version_name"
fi
pass "version metadata: $version_name (code $version_code)"

grep -Eq 'compileSdk[[:space:]]*=[[:space:]]*37' "$APP_GRADLE" || fail "compileSdk is not 37"
grep -Eq 'compileSdkMinor[[:space:]]*=[[:space:]]*1' "$APP_GRADLE" || fail "compileSdkMinor is not 1"
grep -Eq 'targetSdk[[:space:]]*=[[:space:]]*37' "$APP_GRADLE" || fail "targetSdk is not 37"
grep -Eq 'minSdk[[:space:]]*=[[:space:]]*26' "$APP_GRADLE" || fail "minSdk is not 26"
grep -Fq 'buildToolsVersion = "37.0.0"' "$APP_GRADLE" || fail "Build Tools pin is not 37.0.0"
grep -Fq 'id("com.android.application") version "9.4.0" apply false' "$ROOT_GRADLE" || fail "AGP pin is not 9.4.0"
grep -Fq 'id("org.jetbrains.kotlin.plugin.compose") version "2.4.20" apply false' "$ROOT_GRADLE" || fail "Kotlin pin is not 2.4.20"
grep -Fq 'gradle-9.7.1-bin.zip' "$WRAPPER" || fail "Gradle wrapper pin is not 9.7.1"
pass "Android 17 toolchain pins"

# Release signing must be CI-injected; no literal credential values are accepted.
grep -Fq 'CHRONA_KEYSTORE_PATH' "$APP_GRADLE" || fail "release signing path environment variable missing"
grep -Fq 'CHRONA_KEYSTORE_PASSWORD' "$APP_GRADLE" || fail "release keystore password environment variable missing"
grep -Fq 'CHRONA_KEY_ALIAS' "$APP_GRADLE" || fail "release alias environment variable missing"
grep -Fq 'CHRONA_KEY_PASSWORD' "$APP_GRADLE" || fail "release key password environment variable missing"
if grep -nE 'storePassword[[:space:]]*=[[:space:]]*"[^$][^"]+"|keyPassword[[:space:]]*=[[:space:]]*"[^$][^"]+"' "$APP_GRADLE" >/dev/null; then
  fail "hard-coded release credential detected in app/build.gradle.kts"
fi
pass "release signing is environment-backed"

# Documentation referenced by docs/README.md and README.md must exist.
for path in \
  docs/README.md \
  docs/build/BUILD_ENVIRONMENT.md \
  docs/release/RELEASE_SIGNING.md \
  docs/release/RELEASE_CHANGELOG_AUTOMATION.md; do
  [[ -f "$path" ]] || fail "documentation link target missing: $path"
done
pass "release/build documentation links"

# Required release workflow gates.
grep -Fq 'environment: release' "$RELEASE_WORKFLOW" || fail "release GitHub Environment missing"
grep -Fq 'Verify release signing inputs' "$RELEASE_WORKFLOW" || fail "release signing input verification missing"
grep -Fq 'assembleRelease' "$RELEASE_WORKFLOW" || fail "signed release build step missing"
grep -Fq 'sha256sum' "$RELEASE_WORKFLOW" || fail "SHA-256 generation missing"
grep -Fq 'APKSIGNER="$ANDROID_SDK_ROOT/build-tools/${ANDROID_BUILD_TOOLS}/apksigner"' "$RELEASE_WORKFLOW" || fail "apksigner tool path missing"
grep -Fq '"$APKSIGNER" verify' "$RELEASE_WORKFLOW" || fail "APK signature verification invocation missing"
grep -Fq 'sha256sum' "$RELEASE_WORKFLOW" || fail "SHA-256 checksum step missing"
grep -Fq 'softprops/action-gh-release@v2' "$RELEASE_WORKFLOW" || fail "GitHub Release publication step missing"
pass "release workflow gates"

# Manifest export boundary: only the launcher activity may be exported.
python3 - "$MANIFEST" <<'PYMANIFEST'
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
path = Path(sys.argv[1])
ns = "{http://schemas.android.com/apk/res/android}"
root = ET.parse(path).getroot()
activities = root.find("application").findall("activity")
launchers = []
for activity in activities:
    name = activity.get(ns + "name", "")
    exported = activity.get(ns + "exported")
    filters = activity.findall("intent-filter")
    is_launcher = any(
        f.find("action").get(ns + "name") == "android.intent.action.MAIN"
        and any(c.get(ns + "name") == "android.intent.category.LAUNCHER" for c in f.findall("category"))
        for f in filters
        if f.find("action") is not None
    )
    if is_launcher:
        launchers.append(name)
    if exported == "true" and not is_launcher:
        raise SystemExit(f"non-launcher exported activity: {name}")
if launchers != [".MainActivity"]:
    raise SystemExit(f"launcher activity contract mismatch: {launchers}")
PYMANIFEST
pass "manifest export boundary"

# Repository must not contain private signing material or local release output.
for forbidden in '*.jks' '*.keystore' '*.p12' local.properties; do
  if find . -path './.git' -prune -o -type f -name "$forbidden" -print -quit | grep -q .; then
    fail "private/local artifact present: $forbidden"
  fi
done
if find . -path './.git' -prune -o -type f \( -name '*.apk' -o -name '*.aab' \) -print -quit | grep -q .; then
  fail "release binary committed into source tree"
fi
if grep -RInE --exclude-dir=.git --exclude='*.patch' --exclude='phase6-release-gate.sh' '(BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY|ghp_[A-Za-z0-9]{20,}|github_pat_|AIza[0-9A-Za-z_-]{20,}|AKIA[0-9A-Z]{16})' . >/dev/null 2>&1; then
  fail "credential-like secret material detected"
fi
pass "repository secret/output hygiene"

echo "Phase 6A release gate passed"
echo "  versionName: $version_name"
echo "  versionCode: $version_code"
echo "  targetSdk:   37"
echo "  compileSdk:  37.1"
