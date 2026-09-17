/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

#pragma once
#include <cstdint>

namespace chrona {

struct ClockAngles { double hour; double minute; double second; };
struct SolarTimes { int sunrise_minutes; int sunset_minutes; bool valid; };
struct MoonState { double illumination; int phase_index; double age_days; };

ClockAngles clock_angles(std::int64_t epoch_millis, int offset_minutes);
std::int64_t monotonic_millis();
std::int64_t remaining_millis(std::int64_t end_millis, std::int64_t now_millis);
std::int64_t elapsed_millis(std::int64_t start_millis, std::int64_t now_millis);
SolarTimes solar_times(double latitude, double longitude, int year, int month, int day);
MoonState moon_state(std::int64_t epoch_millis);
double cubic_bezier(double t, double p0, double p1, double p2, double p3);
double spring_progress(double elapsed_ms, double duration_ms, double damping_ratio, double frequency_hz);

} // namespace chrona
