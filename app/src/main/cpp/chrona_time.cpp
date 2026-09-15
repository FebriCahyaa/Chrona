#include "chrona_time.h"
#include <algorithm>
#include <chrono>
#include <cmath>
#include <limits>

namespace chrona {
namespace {
constexpr double kPi = 3.14159265358979323846;
constexpr double kRad = kPi / 180.0;
constexpr double kJ2000 = 2451545.0;
constexpr std::int64_t kDayMs = 86'400'000;

double normalize_angle(double value) {
    value = std::fmod(value, 360.0);
    return value < 0.0 ? value + 360.0 : value;
}

double julian_day(std::int64_t epoch_millis) {
    return 2440587.5 + static_cast<double>(epoch_millis) / 86'400'000.0;
}

int day_of_year(int year, int month, int day) {
    static constexpr int days_before_month[] = {0,31,59,90,120,151,181,212,243,273,304,334};
    int n = days_before_month[std::clamp(month, 1, 12) - 1] + day;
    const bool leap = (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0));
    if (leap && month > 2) ++n;
    return n;
}

double deg_sin(double d) { return std::sin(d * kRad); }
double deg_cos(double d) { return std::cos(d * kRad); }
double deg_tan(double d) { return std::tan(d * kRad); }
double rad_to_deg(double r) { return r / kRad; }
double deg_to_rad(double d) { return d * kRad; }
}

ClockAngles clock_angles(std::int64_t epoch_millis, int offset_minutes) {
    const std::int64_t local = epoch_millis + static_cast<std::int64_t>(offset_minutes) * 60'000;
    const std::int64_t day = ((local % kDayMs) + kDayMs) % kDayMs;
    const double total_seconds = static_cast<double>(day) / 1000.0;
    const double hour = std::fmod(total_seconds / 3600.0, 12.0);
    const double minute = std::fmod(total_seconds / 60.0, 60.0);
    const double second = std::fmod(total_seconds, 60.0);
    return {normalize_angle(hour * 30.0), normalize_angle(minute * 6.0), normalize_angle(second * 6.0)};
}

std::int64_t monotonic_millis() {
    const auto now = std::chrono::steady_clock::now().time_since_epoch();
    return std::chrono::duration_cast<std::chrono::milliseconds>(now).count();
}

std::int64_t remaining_millis(std::int64_t end_millis, std::int64_t now_millis) {
    return std::max<std::int64_t>(0, end_millis - now_millis);
}

std::int64_t elapsed_millis(std::int64_t start_millis, std::int64_t now_millis) {
    return std::max<std::int64_t>(0, now_millis - start_millis);
}

SolarTimes solar_times(double latitude, double longitude, int year, int month, int day) {
    if (!std::isfinite(latitude) || !std::isfinite(longitude) || latitude < -90.0 || latitude > 90.0 || longitude < -180.0 || longitude > 180.0) {
        return {0, 0, false};
    }

    const int n = day_of_year(year, month, day);
    const double lng_hour = longitude / 15.0;
    const double approximate_rise = n + ((6.0 - lng_hour) / 24.0);
    const double approximate_set = n + ((18.0 - lng_hour) / 24.0);

    auto calculate = [&](double approx, bool sunrise) -> double {
        const double mean_anomaly = (0.9856 * approx) - 3.289;
        double true_longitude = mean_anomaly + (1.916 * deg_sin(mean_anomaly)) + (0.020 * deg_sin(2.0 * mean_anomaly)) + 282.634;
        true_longitude = normalize_angle(true_longitude);

        double right_ascension = rad_to_deg(std::atan(0.91764 * deg_tan(true_longitude)));
        right_ascension = normalize_angle(right_ascension);
        const double lq = std::floor(true_longitude / 90.0) * 90.0;
        const double raq = std::floor(right_ascension / 90.0) * 90.0;
        right_ascension = right_ascension + (lq - raq);
        right_ascension /= 15.0;

        const double sin_decl = 0.39782 * deg_sin(true_longitude);
        const double cos_decl = std::cos(std::asin(sin_decl));
        const double zenith = 90.833;
        const double cos_local_hour = (deg_cos(zenith) - (sin_decl * deg_sin(latitude))) / (cos_decl * deg_cos(latitude));
        if (cos_local_hour > 1.0 || cos_local_hour < -1.0) return std::numeric_limits<double>::quiet_NaN();

        double local_hour_angle = rad_to_deg(std::acos(cos_local_hour));
        if (sunrise) local_hour_angle = 360.0 - local_hour_angle;
        local_hour_angle /= 15.0;

        const double local_mean_time = local_hour_angle + right_ascension - (0.06571 * approx) - 6.622;
        double utc = local_mean_time - lng_hour;
        utc = std::fmod(utc, 24.0);
        if (utc < 0.0) utc += 24.0;
        return utc;
    };

    const double rise = calculate(approximate_rise, true);
    const double set = calculate(approximate_set, false);
    if (!std::isfinite(rise) || !std::isfinite(set)) return {0, 0, false};

    const auto to_minutes = [](double hours) {
        int minutes = static_cast<int>(std::lround(hours * 60.0));
        minutes %= 1440;
        if (minutes < 0) minutes += 1440;
        return minutes;
    };
    return {to_minutes(rise), to_minutes(set), true};
}

MoonState moon_state(std::int64_t epoch_millis) {
    const double jd = julian_day(epoch_millis);
    const double days_since_new_moon = std::fmod(jd - 2451550.1, 29.530588853);
    const double age = days_since_new_moon < 0.0 ? days_since_new_moon + 29.530588853 : days_since_new_moon;
    const double phase = age / 29.530588853;
    const double illumination = 0.5 * (1.0 - std::cos(2.0 * kPi * phase));
    const int phase_index = static_cast<int>(std::floor((phase * 8.0) + 0.5)) % 8;
    return {illumination, phase_index, age};
}

double cubic_bezier(double t, double p0, double p1, double p2, double p3) {
    t = std::clamp(t, 0.0, 1.0);
    const double u = 1.0 - t;
    return u*u*u*p0 + 3.0*u*u*t*p1 + 3.0*u*t*t*p2 + t*t*t*p3;
}

double spring_progress(double elapsed_ms, double duration_ms, double damping_ratio, double frequency_hz) {
    if (duration_ms <= 0.0) return 1.0;
    const double t = std::clamp(elapsed_ms / duration_ms, 0.0, 1.0);
    const double zeta = std::clamp(damping_ratio, 0.05, 2.0);
    const double omega = std::max(0.01, 2.0 * kPi * frequency_hz);
    const double x = t * duration_ms / 1000.0;
    double response;
    if (zeta < 1.0) {
        const double wd = omega * std::sqrt(1.0 - zeta * zeta);
        response = 1.0 - std::exp(-zeta * omega * x) * (std::cos(wd * x) + (zeta * omega / wd) * std::sin(wd * x));
    } else {
        response = 1.0 - (1.0 + omega * x) * std::exp(-omega * x);
    }
    return std::clamp(response, 0.0, 1.0);
}

} // namespace chrona
