/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

#include <jni.h>
#include <cstdint>
#include <cstring>
#include "chrona_time.h"

namespace {
constexpr char kBridgeClass[] = "com/febricahyaa/clockapp/core/ChronaNativeBridge";

jfloatArray make_float_array(JNIEnv* env, const float* values, jsize size) {
    const jfloatArray result = env->NewFloatArray(size);
    if (result == nullptr) return nullptr;
    env->SetFloatArrayRegion(result, 0, size, values);
    return result;
}

jdoubleArray make_double_array(JNIEnv* env, const double* values, jsize size) {
    const jdoubleArray result = env->NewDoubleArray(size);
    if (result == nullptr) return nullptr;
    env->SetDoubleArrayRegion(result, 0, size, values);
    return result;
}

jfloatArray native_angles_for_epoch_millis(
        JNIEnv* env,
        jclass,
        jlong epoch_millis,
        jint offset_minutes) {
    const auto angles = chrona::clock_angles(
            static_cast<std::int64_t>(epoch_millis),
            static_cast<int>(offset_minutes));
    const float values[] = {
            static_cast<float>(angles.hour),
            static_cast<float>(angles.minute),
            static_cast<float>(angles.second),
    };
    return make_float_array(env, values, 3);
}

jlong native_remaining_millis(JNIEnv*, jclass, jlong end_millis, jlong now_millis) {
    return static_cast<jlong>(chrona::remaining_millis(end_millis, now_millis));
}

jlong native_elapsed_millis(JNIEnv*, jclass, jlong start_millis, jlong now_millis) {
    return static_cast<jlong>(chrona::elapsed_millis(start_millis, now_millis));
}

jlong native_monotonic_millis(JNIEnv*, jclass) {
    return static_cast<jlong>(chrona::monotonic_millis());
}

jdoubleArray native_solar_times(
        JNIEnv* env,
        jclass,
        jdouble latitude,
        jdouble longitude,
        jint year,
        jint month,
        jint day) {
    const auto solar = chrona::solar_times(
            latitude,
            longitude,
            static_cast<int>(year),
            static_cast<int>(month),
            static_cast<int>(day));
    const double values[] = {
            static_cast<double>(solar.sunrise_minutes),
            static_cast<double>(solar.sunset_minutes),
            solar.valid ? 1.0 : 0.0,
    };
    return make_double_array(env, values, 3);
}

jdoubleArray native_moon_state(JNIEnv* env, jclass, jlong epoch_millis) {
    const auto moon = chrona::moon_state(static_cast<std::int64_t>(epoch_millis));
    const double values[] = {
            moon.illumination,
            static_cast<double>(moon.phase_index),
            moon.age_days,
    };
    return make_double_array(env, values, 3);
}

jdouble native_spring_progress(
        JNIEnv*, jclass,
        jdouble elapsed_ms,
        jdouble duration_ms,
        jdouble damping_ratio,
        jdouble frequency_hz) {
    return chrona::spring_progress(elapsed_ms, duration_ms, damping_ratio, frequency_hz);
}

jdouble native_cubic_bezier(
        JNIEnv*, jclass,
        jdouble t,
        jdouble p0,
        jdouble p1,
        jdouble p2,
        jdouble p3) {
    return chrona::cubic_bezier(t, p0, p1, p2, p3);
}

// JNI class/method names and descriptors below are ABI metadata required by RegisterNatives;
// they intentionally remain in native source and are not Android UI/localization resources.
JNINativeMethod kMethods[] = {
        {const_cast<char*>("nativeAnglesForEpochMillis"), const_cast<char*>("(JI)[F"), reinterpret_cast<void*>(native_angles_for_epoch_millis)},
        {const_cast<char*>("nativeRemainingMillis"), const_cast<char*>("(JJ)J"), reinterpret_cast<void*>(native_remaining_millis)},
        {const_cast<char*>("nativeElapsedMillis"), const_cast<char*>("(JJ)J"), reinterpret_cast<void*>(native_elapsed_millis)},
        {const_cast<char*>("nativeMonotonicMillis"), const_cast<char*>("()J"), reinterpret_cast<void*>(native_monotonic_millis)},
        {const_cast<char*>("nativeSolarTimes"), const_cast<char*>("(DDIII)[D"), reinterpret_cast<void*>(native_solar_times)},
        {const_cast<char*>("nativeMoonState"), const_cast<char*>("(J)[D"), reinterpret_cast<void*>(native_moon_state)},
        {const_cast<char*>("nativeSpringProgress"), const_cast<char*>("(DDDD)D"), reinterpret_cast<void*>(native_spring_progress)},
        {const_cast<char*>("nativeCubicBezier"), const_cast<char*>("(DDDDD)D"), reinterpret_cast<void*>(native_cubic_bezier)},
};
}

extern "C" jint JNI_OnLoad(JavaVM* vm, void*) {
    JNIEnv* env = nullptr;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK || env == nullptr) {
        return JNI_ERR;
    }

    const jclass bridge_class = env->FindClass(kBridgeClass);
    if (bridge_class == nullptr) return JNI_ERR;

    const jint clock_result = env->RegisterNatives(
            bridge_class,
            kMethods,
            static_cast<jint>(sizeof(kMethods) / sizeof(kMethods[0])));
    env->DeleteLocalRef(bridge_class);
    if (clock_result != JNI_OK) return JNI_ERR;

    return JNI_VERSION_1_6;
}
