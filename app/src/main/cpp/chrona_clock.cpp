#include <jni.h>
#include <cmath>
#include <cstdint>

namespace {
struct Angles { float hour; float minute; float second; };
Angles calculate(std::int64_t epochMillis, int offsetMinutes) {
    constexpr std::int64_t kMinuteMs = 60'000;
    constexpr std::int64_t kHourMs = 60 * kMinuteMs;
    constexpr std::int64_t kDayMs = 24 * kHourMs;
    const std::int64_t localMillis = epochMillis + static_cast<std::int64_t>(offsetMinutes) * kMinuteMs;
    const std::int64_t dayMillis = ((localMillis % kDayMs) + kDayMs) % kDayMs;
    const int hour = static_cast<int>(dayMillis / kHourMs);
    const int minute = static_cast<int>((dayMillis % kHourMs) / kMinuteMs);
    const float second = static_cast<float>((dayMillis % kMinuteMs) / 1000.0);
    const float minutePosition = static_cast<float>(minute) + second / 60.0f;
    const float hourPosition = static_cast<float>(hour % 12) + minutePosition / 60.0f;
    return {std::fmod(hourPosition * 30.0f, 360.0f),
            std::fmod(minutePosition * 6.0f, 360.0f),
            std::fmod(second * 6.0f, 360.0f)};
}
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_febricahyaa_clockapp_core_ChronaNativeBridge_nativeAnglesForEpochMillis(
        JNIEnv* env, jclass, jlong epochMillis, jint offsetMinutes) {
    const Angles a = calculate(static_cast<std::int64_t>(epochMillis), static_cast<int>(offsetMinutes));
    const jfloat values[3] = {a.hour, a.minute, a.second};
    auto result = env->NewFloatArray(3);
    if (!result) return nullptr;
    env->SetFloatArrayRegion(result, 0, 3, values);
    return result;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_febricahyaa_clockapp_core_ChronaNativeBridge_nativeRemainingSeconds(
        JNIEnv*, jclass, jlong endMillis, jlong nowMillis) {
    const std::int64_t delta = static_cast<std::int64_t>(endMillis) - static_cast<std::int64_t>(nowMillis);
    return static_cast<jlong>(delta > 0 ? delta / 1000 : 0);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_febricahyaa_clockapp_core_ChronaNativeBridge_nativeElapsedMillis(
        JNIEnv*, jclass, jlong startMillis, jlong nowMillis) {
    const std::int64_t delta = static_cast<std::int64_t>(nowMillis) - static_cast<std::int64_t>(startMillis);
    return static_cast<jlong>(delta > 0 ? delta : 0);
}
