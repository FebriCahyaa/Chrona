#include <jni.h>
#include <cstdint>
#include "chrona_time.h"

namespace {
const char* kNativeErrorClass = "java/lang/IllegalArgumentException";

jdoubleArray make_double_array(JNIEnv* env, const double* values, jsize size) {
    auto result = env->NewDoubleArray(size);
    if (!result) return nullptr;
    env->SetDoubleArrayRegion(result, 0, size, values);
    return result;
}
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_febricahyaa_clockapp_core_ChronaNativeBridge_nativeAnglesForEpochMillis(
        JNIEnv* env, jclass, jlong epochMillis, jint offsetMinutes) {
    const auto a = chrona::clock_angles(static_cast<std::int64_t>(epochMillis), static_cast<int>(offsetMinutes));
    const jfloat values[3] = {static_cast<float>(a.hour), static_cast<float>(a.minute), static_cast<float>(a.second)};
    auto result = env->NewFloatArray(3);
    if (!result) return nullptr;
    env->SetFloatArrayRegion(result, 0, 3, values);
    return result;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_febricahyaa_clockapp_core_ChronaNativeBridge_nativeRemainingMillis(
        JNIEnv*, jclass, jlong endMillis, jlong nowMillis) {
    return static_cast<jlong>(chrona::remaining_millis(endMillis, nowMillis));
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_febricahyaa_clockapp_core_ChronaNativeBridge_nativeElapsedMillis(
        JNIEnv*, jclass, jlong startMillis, jlong nowMillis) {
    return static_cast<jlong>(chrona::elapsed_millis(startMillis, nowMillis));
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_febricahyaa_clockapp_core_ChronaNativeBridge_nativeMonotonicMillis(
        JNIEnv*, jclass) {
    return static_cast<jlong>(chrona::monotonic_millis());
}

extern "C" JNIEXPORT jdoubleArray JNICALL
Java_com_febricahyaa_clockapp_core_ChronaNativeBridge_nativeSolarTimes(
        JNIEnv* env, jclass, jdouble latitude, jdouble longitude, jint year, jint month, jint day) {
    const auto s = chrona::solar_times(latitude, longitude, year, month, day);
    const jdouble values[3] = {static_cast<double>(s.sunrise_minutes), static_cast<double>(s.sunset_minutes), s.valid ? 1.0 : 0.0};
    return make_double_array(env, values, 3);
}

extern "C" JNIEXPORT jdoubleArray JNICALL
Java_com_febricahyaa_clockapp_core_ChronaNativeBridge_nativeMoonState(
        JNIEnv* env, jclass, jlong epochMillis) {
    const auto m = chrona::moon_state(static_cast<std::int64_t>(epochMillis));
    const jdouble values[3] = {m.illumination, static_cast<double>(m.phase_index), m.age_days};
    return make_double_array(env, values, 3);
}

extern "C" JNIEXPORT jdouble JNICALL
Java_com_febricahyaa_clockapp_core_ChronaNativeBridge_nativeSpringProgress(
        JNIEnv*, jclass, jdouble elapsedMs, jdouble durationMs, jdouble dampingRatio, jdouble frequencyHz) {
    return chrona::spring_progress(elapsedMs, durationMs, dampingRatio, frequencyHz);
}

extern "C" JNIEXPORT jdouble JNICALL
Java_com_febricahyaa_clockapp_core_ChronaNativeBridge_nativeCubicBezier(
        JNIEnv*, jclass, jdouble t, jdouble p0, jdouble p1, jdouble p2, jdouble p3) {
    return chrona::cubic_bezier(t, p0, p1, p2, p3);
}
