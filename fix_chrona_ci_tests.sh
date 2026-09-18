#!/usr/bin/env bash
# Chrona CI/test repair for the compileDebugUnitTestKotlin failure.
set -euo pipefail

ROOT_DIR="$(cd "${1:-.}" && pwd)"
cd "$ROOT_DIR"

python3 <<'PY'
from pathlib import Path

ROOT = Path.cwd()

# AlarmStateManagerTest
p = ROOT / 'app/src/test/java/com/febricahyaa/clockapp/core/AlarmStateManagerTest.kt'
s = p.read_text(encoding='utf-8')
old = 'AlarmItem(1L, LocalTime.of(8, 0))'
new = 'AlarmItem(1L, LocalTime.of(8, 0), label = "", enabled = true, repeatDays = emptySet())'
if old in s:
    s = s.replace(old, new, 1)
old = '''            time = LocalTime.of(8, 0),
            repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),'''
new = '''            time = LocalTime.of(8, 0),
            label = "",
            enabled = true,
            repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),'''
if old in s:
    s = s.replace(old, new, 1)
p.write_text(s, encoding='utf-8')

# AlarmTimeCalculatorTest
p = ROOT / 'app/src/test/java/com/febricahyaa/clockapp/core/AlarmTimeCalculatorTest.kt'
s = p.read_text(encoding='utf-8')
s = s.replace(
    'AlarmItem(1L, LocalTime.of(10, 0))',
    'AlarmItem(1L, LocalTime.of(10, 0), label = "", enabled = true, repeatDays = emptySet())',
)
s = s.replace(
    'AlarmItem(1L, LocalTime.of(8, 0), repeatDays = setOf(DayOfWeek.FRIDAY))',
    'AlarmItem(1L, LocalTime.of(8, 0), label = "", enabled = true, repeatDays = setOf(DayOfWeek.FRIDAY))',
)
p.write_text(s, encoding='utf-8')

# WorldClockScreen: expose a deterministic, Compose-safe hue helper and use it.
p = ROOT / 'app/src/main/java/com/febricahyaa/clockapp/ui/screens/WorldClockScreen.kt'
s = p.read_text(encoding='utf-8')
if 'internal fun cityThumbnailHues(' not in s:
    marker = 'private data class CityVisual(val topHue: Float, val bottomHue: Float, val buildingHeights: List<Float>)\n\n@Composable\nprivate fun CityThumbnail'
    replacement = '''private data class CityVisual(val topHue: Float, val bottomHue: Float, val buildingHeights: List<Float>)

/**
 * Returns deterministic HSV hues that are always valid for Compose Color.hsv.
 * floorMod avoids the Int.MIN_VALUE edge case that makes abs(Int.MIN_VALUE)
 * negative and keeps both hue values inside [0, 360).
 */
internal fun cityThumbnailHues(cityHash: Int): Pair<Float, Float> {
    val seed = Math.floorMod(cityHash, 360)
    val bottom = Math.floorMod(seed + 34, 360)
    return seed.toFloat() to bottom.toFloat()
}

@Composable
private fun CityThumbnail'''
    if marker not in s:
        raise SystemExit('WorldClockScreen: CityThumbnail marker not found')
    s = s.replace(marker, replacement, 1)
old = '''    val visual = remember(city) {
        val seed = Math.floorMod(city.hashCode(), 360)
        CityVisual(
            topHue = seed.toFloat(),
            bottomHue = Math.floorMod(seed + 34, 360).toFloat(),
            buildingHeights = List(6) { index -> 0.22f + ((index + city.length) % 4) * 0.10f },
        )
    }'''
new = '''    val visual = remember(city) {
        val (topHue, bottomHue) = cityThumbnailHues(city.hashCode())
        CityVisual(
            topHue = topHue,
            bottomHue = bottomHue,
            buildingHeights = List(6) { index -> 0.22f + ((index + city.length) % 4) * 0.10f },
        )
    }'''
if old in s:
    s = s.replace(old, new, 1)
p.write_text(s, encoding='utf-8')

# Toolchain pin consistency: 9.7.0 -> 9.7.1 in current CI source-of-truth files.
files = [
    ROOT / 'gradle/wrapper/gradle-wrapper.properties',
    ROOT / '.github/workflows/debug-matrix.yml',
    ROOT / '.github/workflows/package-maintenance.yml',
    ROOT / '.github/workflows/release.yml',
    ROOT / 'scripts/verify-android17.sh',
]
for p in files:
    s = p.read_text(encoding='utf-8')
    s = s.replace('gradle-9.7.0-bin.zip', 'gradle-9.7.1-bin.zip')
    s = s.replace("GRADLE_VERSION: '9.7.0'", "GRADLE_VERSION: '9.7.1'")
    s = s.replace('EXPECTED_GRADLE="9.7.0"', 'EXPECTED_GRADLE="9.7.1"')
    p.write_text(s, encoding='utf-8')
PY

# Static validation.
grep -Fq 'label = "", enabled = true, repeatDays = emptySet()' \
  app/src/test/java/com/febricahyaa/clockapp/core/AlarmTimeCalculatorTest.kt
grep -Fq 'internal fun cityThumbnailHues(cityHash: Int): Pair<Float, Float>' \
  app/src/main/java/com/febricahyaa/clockapp/ui/screens/WorldClockScreen.kt
grep -Fq 'gradle-9.7.1-bin.zip' gradle/wrapper/gradle-wrapper.properties
grep -Fq 'EXPECTED_GRADLE="9.7.1"' scripts/verify-android17.sh
if grep -RFn "GRADLE_VERSION: '9.7.0'" .github/workflows >/dev/null 2>&1; then
  echo 'ERROR: stale Gradle 9.7.0 workflow pin remains' >&2
  exit 1
fi

if command -v git >/dev/null 2>&1 && [[ -d .git ]]; then
  git diff --check
fi

echo
echo '✅ Chrona fixes applied.'
echo
echo 'Expected Gradle CI verification:'
echo '  ./gradlew --no-daemon :app:compileDebugUnitTestKotlin --stacktrace'
echo '  ./gradlew --no-daemon test'
echo
echo 'Commit:'
echo '  git add app/src/test/java/com/febricahyaa/clockapp/core/AlarmStateManagerTest.kt \'
echo '    app/src/test/java/com/febricahyaa/clockapp/core/AlarmTimeCalculatorTest.kt \'
echo '    app/src/main/java/com/febricahyaa/clockapp/ui/screens/WorldClockScreen.kt \'
echo '    gradle/wrapper/gradle-wrapper.properties .github/workflows scripts/verify-android17.sh'
echo '  git commit -m "fix(ci,test): repair unit tests and align Gradle toolchain"'
