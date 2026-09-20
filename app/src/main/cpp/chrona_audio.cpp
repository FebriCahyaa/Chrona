/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

#include "chrona_audio.h"

#include <aaudio/AAudio.h>
#include <atomic>
#include <cmath>
#include <cstdint>
#include <mutex>

namespace chrona {
namespace {

std::mutex g_stream_mutex;
AAudioStream* g_stream = nullptr;
std::atomic<double> g_phase{0.0};

constexpr double kTwoPi = 6.28318530717958647692;
constexpr double kToneHz = 880.0;
constexpr float kAmplitude = 0.16f;

AAudioStream_dataCallbackResult_t data_callback(
        AAudioStream* stream,
        void*,
        void* audio_data,
        int32_t num_frames) {
    auto* output = static_cast<float*>(audio_data);
    if (output == nullptr || num_frames <= 0) {
        return AAUDIO_CALLBACK_RESULT_CONTINUE;
    }

    const int32_t channels = AAudioStream_getChannelCount(stream);
    const int32_t sample_rate = AAudioStream_getSampleRate(stream);
    if (channels <= 0 || sample_rate <= 0) {
        return AAUDIO_CALLBACK_RESULT_CONTINUE;
    }

    const double increment = kTwoPi * kToneHz / static_cast<double>(sample_rate);
    double phase = g_phase.load(std::memory_order_relaxed);

    for (int32_t frame = 0; frame < num_frames; ++frame) {
        const float sample = static_cast<float>(std::sin(phase)) * kAmplitude;
        for (int32_t channel = 0; channel < channels; ++channel) {
            output[frame * channels + channel] = sample;
        }
        phase += increment;
        if (phase >= kTwoPi) {
            phase -= kTwoPi;
        }
    }

    g_phase.store(phase, std::memory_order_relaxed);
    return AAUDIO_CALLBACK_RESULT_CONTINUE;
}

} // namespace

bool start_low_latency_alert_tone() {
    std::lock_guard<std::mutex> lock(g_stream_mutex);
    if (g_stream != nullptr) {
        return true;
    }

    AAudioStreamBuilder* builder = nullptr;
    if (AAudio_createStreamBuilder(&builder) != AAUDIO_OK || builder == nullptr) {
        return false;
    }

    AAudioStreamBuilder_setDirection(builder, AAUDIO_DIRECTION_OUTPUT);
    AAudioStreamBuilder_setPerformanceMode(builder, AAUDIO_PERFORMANCE_MODE_LOW_LATENCY);
    AAudioStreamBuilder_setSharingMode(builder, AAUDIO_SHARING_MODE_SHARED);
    AAudioStreamBuilder_setFormat(builder, AAUDIO_FORMAT_PCM_FLOAT);
    AAudioStreamBuilder_setChannelCount(builder, 2);
    AAudioStreamBuilder_setDataCallback(builder, data_callback, nullptr);

    AAudioStream* stream = nullptr;
    const aaudio_result_t result = AAudioStreamBuilder_openStream(builder, &stream);
    AAudioStreamBuilder_delete(builder);
    if (result != AAUDIO_OK || stream == nullptr) {
        return false;
    }

    const aaudio_result_t start_result = AAudioStream_requestStart(stream);
    if (start_result != AAUDIO_OK) {
        AAudioStream_close(stream);
        return false;
    }

    g_phase.store(0.0, std::memory_order_relaxed);
    g_stream = stream;
    return true;
}

void stop_low_latency_alert_tone() {
    std::lock_guard<std::mutex> lock(g_stream_mutex);
    if (g_stream == nullptr) {
        return;
    }

    AAudioStream_requestStop(g_stream);
    AAudioStream_close(g_stream);
    g_stream = nullptr;
    g_phase.store(0.0, std::memory_order_relaxed);
}

} // namespace chrona
