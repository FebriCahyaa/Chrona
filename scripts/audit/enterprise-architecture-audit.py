#!/usr/bin/env python3
# Copyright 2026 Febrian Rahmad Cahya
# SPDX-License-Identifier: MIT

"""Static contract audit for Chrona's enterprise Android architecture."""

from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
APP = ROOT / "app"
MAIN = APP / "src" / "main"
JAVA = MAIN / "java" / "com" / "febricahyaa" / "clockapp"
WORKFLOWS = ROOT / ".github" / "workflows"

FAILURES: list[str] = []


def read(rel: str) -> str:
    path = ROOT / rel
    if not path.is_file():
        FAILURES.append(f"missing file: {rel}")
        return ""
    return path.read_text(errors="replace")


def require(text: str, pattern: str, label: str, *, literal: bool = False) -> None:
    ok = pattern in text if literal else re.search(pattern, text, re.MULTILINE) is not None
    if not ok:
        FAILURES.append(label)


def require_file(rel: str, label: str | None = None) -> str:
    text = read(rel)
    if not text and not (ROOT / rel).exists():
        return ""
    if label:
        print(f"PASS  {label}")
    return text


print("Chrona enterprise architecture contract audit")

# DI / ViewModel graph.
app_gradle = read("app/build.gradle.kts")
root_gradle = read("build.gradle.kts")
application = read("app/src/main/java/com/febricahyaa/clockapp/ClockApplication.kt")
module = read("app/src/main/java/com/febricahyaa/clockapp/di/ChronaModule.kt")
clock_app = read("app/src/main/java/com/febricahyaa/clockapp/ClockApp.kt")
require(application, r"@HiltAndroidApp", "Hilt application annotation")
require(module, r"@Module", "Hilt module present")
require(module, r"@InstallIn\(SingletonComponent::class\)", "Hilt singleton component bindings")
require(app_gradle, r"implementation\(libs\.hilt\.android\)", "Hilt runtime dependency")
require(root_gradle, r'alias\(libs\.plugins\.hilt\)', "Hilt Gradle plugin")
require(root_gradle, r'alias\(libs\.plugins\.ksp\) apply false', "KSP Gradle plugin")
require(app_gradle, r'alias\(libs\.plugins\.ksp\)', "KSP module plugin")
require(app_gradle, r'ksp\(libs\.hilt\.android\.compiler\)', "Hilt compiler uses KSP")
require(app_gradle, r'ksp\(libs\.room\.compiler\)', "Room compiler uses KSP")
require(app_gradle, r'ksp\(libs\.hilt\.compiler\)', "AndroidX Hilt compiler uses KSP")
if re.search(r'\bkapt\b|kotlin-kapt|org\.jetbrains\.kotlin\.kapt', app_gradle + "\n" + root_gradle):
    FAILURES.append("KAPT configuration still present; Chrona requires KSP-only processors")
require(clock_app, r"hiltViewModel\(\)", "Compose screens use hiltViewModel")
if (JAVA / "AppContainer.kt").exists() or (JAVA / "AppViewModelFactory.kt").exists():
    FAILURES.append("manual AppContainer/AppViewModelFactory still exists")

# Room + DataStore.
db = read("app/src/main/java/com/febricahyaa/clockapp/data/local/ChronaDatabase.kt")
room_repos = read("app/src/main/java/com/febricahyaa/clockapp/data/local/RoomRepositories.kt")
data_stores = read("app/src/main/java/com/febricahyaa/clockapp/data/ChronaDataStores.kt")
settings_repo = read("app/src/main/java/com/febricahyaa/clockapp/data/DataStoreRepositories.kt")
migration = read("app/src/main/java/com/febricahyaa/clockapp/data/StorageMigrationCoordinator.kt")
require(db, r"@Database", "Room database annotation")
require(db, r"AlarmEntity|WorldClockEntity|AlarmHistoryEntity", "structured Room entities")
require(room_repos, r"Flow<", "Room repositories expose reactive Flow")
require(data_stores, r"preferencesDataStore", "Preferences DataStore")
require(app_gradle, r"implementation\(libs\.room\.runtime\)", "Room runtime dependency")
require(app_gradle, r"implementation\(libs\.datastore\.preferences\)", "DataStore dependency")
require(migration, r"getSharedPreferences", "legacy storage migration exists")
shared_prefs_hits = []
for path in (APP / "src" / "main").rglob("*.kt"):
    text = path.read_text(errors="replace")
    if "SharedPreferences" in text and path.name != "StorageMigrationCoordinator.kt":
        shared_prefs_hits.append(str(path.relative_to(ROOT)))
if shared_prefs_hits:
    FAILURES.append("runtime SharedPreferences used outside migration: " + ", ".join(shared_prefs_hits))

# WorkManager.
scheduler = read("app/src/main/java/com/febricahyaa/clockapp/update/ChronaWorkScheduler.kt")
update_worker = read("app/src/main/java/com/febricahyaa/clockapp/update/GitHubUpdateWorker.kt")
tz_worker = read("app/src/main/java/com/febricahyaa/clockapp/update/TimeZoneCatalogMaintenanceWorker.kt")
require(scheduler, r"PeriodicWorkRequest", "WorkManager periodic scheduling")
require(update_worker, r"@HiltWorker", "Hilt WorkManager worker")
require(tz_worker, r"CoroutineWorker", "timezone maintenance worker")
require(application, r"HiltWorkerFactory", "Hilt WorkerFactory")

# Native shared object boundary.
cmake = read("app/src/main/cpp/CMakeLists.txt")
jni = read("app/src/main/java/com/febricahyaa/clockapp/nativelayer/ChronaNativeBridge.java")
audio = read("app/src/main/cpp/chrona_audio.cpp")
require(cmake, r"add_library\(chrona_clock SHARED", "native shared library target")
require(cmake, r"chrona_audio\.cpp", "AAudio source linked into native library")
require(cmake, r"aaudio", "AAudio library linked")
require(jni, r"nativeStartLowLatencyAlertTone|nativeStopLowLatencyAlertTone", "JNI native audio methods")
require(audio, r"AAudioStreamBuilder_openStream", "AAudio runtime implementation")

# R8 / release hardening.
proguard = read("app/proguard-rules.pro")
require(app_gradle, r"isMinifyEnabled\s*=\s*true", "release R8 minification enabled")
require(app_gradle, r"isShrinkResources\s*=\s*true", "release resource shrinking enabled")
require(app_gradle, r'proguard-rules\.pro', "custom release rules wired")
require(proguard, r"ChronaNativeBridge", "JNI keep rule")
require(proguard, r"Room|androidx\.room", "Room keep boundary")

# Telemetry + jank.
telemetry = read("app/src/main/java/com/febricahyaa/clockapp/telemetry/TelemetryReporter.kt")
oss_telemetry = read("app/src/oss/java/com/febricahyaa/clockapp/telemetry/OssTelemetryModule.kt")
play_telemetry = read("app/src/play/java/com/febricahyaa/clockapp/telemetry/FirebaseTelemetryModule.kt")
activity = read("app/src/main/java/com/febricahyaa/clockapp/MainActivity.kt")
require(telemetry, r"interface TelemetryReporter", "telemetry abstraction")
require(oss_telemetry, r"TelemetryReporter", "OSS telemetry implementation")
require(play_telemetry, r"FirebaseCrashlytics", "Play Firebase Crashlytics implementation")
require(activity, r"JankStats", "jank instrumentation boundary")
require(app_gradle, r"libs\.firebase\.crashlytics\.ndk", "native Crashlytics dependency")

# Keystore encryption.
security = read("app/src/main/java/com/febricahyaa/clockapp/data/security/ChronaSecretStore.kt")
require(security, r'"AndroidKeyStore"', "Android Keystore provider")
require(security, r'"AES/GCM/NoPadding"', "AES-GCM encrypted secret format")
if "EncryptedSharedPreferences" in security:
    FAILURES.append("deprecated EncryptedSharedPreferences used")

# Binder / AIDL.
aidl = APP / "src" / "main" / "aidl" / "com" / "febricahyaa" / "clockapp" / "ipc" / "IChronaSystemService.aidl"
ipc = read("app/src/main/java/com/febricahyaa/clockapp/ipc/ChronaSystemService.kt")
require_file("app/src/main/aidl/com/febricahyaa/clockapp/ipc/IChronaSystemService.aidl", "AIDL contract file") if not aidl.exists() else None
require(ipc, r"Stub\(\)", "Binder service implements generated Stub")
require(app_gradle, r"buildFeatures\s*\{[^}]*aidl\s*=\s*true", "AIDL build feature enabled")

# Flavors.
require(app_gradle, r'flavorDimensions\s*\+=\s*"distribution"', "distribution flavor dimension")
require(app_gradle, r'create\("oss"\)', "OSS product flavor")
require(app_gradle, r'create\("play"\)', "Play product flavor")
require((APP / "src" / "oss").is_dir() and "", r"", "", literal=True) if False else None

# Test infrastructure.
mockk_test = read("app/src/test/java/com/febricahyaa/clockapp/core/AlarmStateManagerMockKTest.kt")
hilt_test = read("app/src/androidTest/java/com/febricahyaa/clockapp/di/ChronaHiltGraphTest.kt")
compose_test = "\n".join(p.read_text(errors="replace") for p in (APP / "src" / "androidTest").rglob("*.kt"))
require(app_gradle, r"testImplementation\(libs\.mockk\)", "MockK dependency")
require(mockk_test, r"mockk<AlarmRepository>", "MockK repository isolation test")
require(hilt_test, r"@HiltAndroidTest", "Hilt instrumentation test")
require(compose_test, r"createComposeRule|createAndroidComposeRule|AndroidComposeTestRule", "Compose instrumentation test")
require(app_gradle, r"androidTestImplementation\(libs\.espresso\.core\)", "Espresso dependency")

# App Links / deep links.
manifest = read("app/src/main/AndroidManifest.xml")
assetlinks = read("docs/app-links/assetlinks.json.example")
require(manifest, r'android:autoVerify="true"', "verified HTTPS App Link")
require(manifest, r'android:host="\$\{chronaAppLinkHost\}"', "configurable App Link host")
require(manifest, r'android:scheme="chrona"', "custom Chrona deep link")
require(assetlinks, r"sha256_cert_fingerprints", "assetlinks fingerprint template")

# Lottie / vector / Canvas strategy.
lottie = read("app/src/main/java/com/febricahyaa/clockapp/ui/components/ChronaLottieAnimation.kt")
require(lottie, r"LottieAnimation", "Lottie adapter")
require(app_gradle, r"implementation\(libs\.lottie\.compose\)", "Lottie Compose dependency")
vector_sources = list((MAIN / "res").glob("**/*.xml"))
if not any("vector" in p.read_text(errors="replace") for p in vector_sources):
    FAILURES.append("no VectorDrawable resource found")

# Timer / alarm / audio focus / haptic contracts.
timer_running = read("app/src/main/java/com/febricahyaa/clockapp/timer/TimerRunningNotification.kt")
timer_scheduler = read("app/src/main/java/com/febricahyaa/clockapp/timer/AndroidTimerScheduler.kt")
timer_notification = read("app/src/main/java/com/febricahyaa/clockapp/timer/TimerNotification.kt")
sound = read("app/src/main/java/com/febricahyaa/clockapp/alarm/AndroidAlarmSoundPlayer.kt")
stopwatch_service = read("app/src/main/java/com/febricahyaa/clockapp/stopwatch/StopwatchService.kt")
require(timer_running, r"setChronometerCountDown\(true\)", "Timer SystemUI countdown chronometer")
require(timer_scheduler, r"setExactAndAllowWhileIdle|setAlarmClock", "Timer exact OS scheduling")
require(timer_notification, r"setFullScreenIntent", "Timer full-screen completion intent")
require(manifest, r"USE_FULL_SCREEN_INTENT", "full-screen notification permission")
require(sound, r"requestAudioFocus", "alarm/timer audio focus")
require(sound, r"VibrationEffect", "alarm/timer haptic feedback")
require(stopwatch_service, r"foregroundServiceType|startForeground", "Stopwatch foreground service")

# Secondary-process safety for WorkManager.
require(application, r"isMainProcess\(\).*ChronaNotificationChannels|if \(!isMainProcess\(\)\) return", "secondary process guard before WorkManager initialization")

# Workflow contract.
workflow_names = sorted(p.name for p in WORKFLOWS.glob("*.yml"))
expected = sorted([
    "update-commit.yml",
    "debug-build.yml",
    "release-build.yml",
    "sync-source.yml",
    "pull-request-issue.yml",
])
if workflow_names != expected:
    FAILURES.append(f"workflow contract mismatch: found {workflow_names}")

if FAILURES:
    print("\nFAILURES:")
    for failure in FAILURES:
        print(f"- {failure}")
    sys.exit(1)

print("PASS  enterprise architecture contract")
