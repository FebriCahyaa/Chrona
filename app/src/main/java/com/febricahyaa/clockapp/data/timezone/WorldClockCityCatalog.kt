/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.data.timezone

/**
 * Curated city labels for a Google-Clock-style world-time experience.
 *
 * The timezone ID remains the source of truth for clock math. This catalog
 * only supplies human-friendly city names, country codes, and search aliases.
 */
object WorldClockCityCatalog {

    data class Entry(
        val cityName: String,
        val countryCode: String,
        val zoneId: String,
        val aliases: Set<String> = emptySet(),
    )

    val entries = listOf(
        Entry("Honolulu", "US", "Pacific/Honolulu"),
        Entry("Anchorage", "US", "America/Anchorage"),
        Entry(
            "Los Angeles",
            "US",
            "America/Los_Angeles",
            setOf("LA", "L.A.", "San Francisco", "SF", "Bay Area"),
        ),
        Entry("Phoenix", "US", "America/Phoenix"),
        Entry("Denver", "US", "America/Denver"),
        Entry("Chicago", "US", "America/Chicago"),
        Entry("New York", "US", "America/New_York", setOf("NYC", "New York City")),
        Entry("Toronto", "CA", "America/Toronto"),
        Entry("Vancouver", "CA", "America/Vancouver"),
        Entry("Mexico City", "MX", "America/Mexico_City"),
        Entry("Caracas", "VE", "America/Caracas"),
        Entry("São Paulo", "BR", "America/Sao_Paulo", setOf("Sao Paulo")),
        Entry("Buenos Aires", "AR", "America/Argentina/Buenos_Aires"),

        Entry("Reykjavik", "IS", "Atlantic/Reykjavik"),
        Entry("Ponta Delgada", "PT", "Atlantic/Azores", setOf("Azores")),
        Entry("London", "GB", "Europe/London"),
        Entry("Dublin", "IE", "Europe/Dublin"),
        Entry("Lisbon", "PT", "Europe/Lisbon"),
        Entry("Paris", "FR", "Europe/Paris"),
        Entry("Madrid", "ES", "Europe/Madrid"),
        Entry("Rome", "IT", "Europe/Rome"),
        Entry("Berlin", "DE", "Europe/Berlin"),
        Entry("Amsterdam", "NL", "Europe/Amsterdam"),
        Entry("Brussels", "BE", "Europe/Brussels"),
        Entry("Zurich", "CH", "Europe/Zurich"),
        Entry("Vienna", "AT", "Europe/Vienna"),
        Entry("Prague", "CZ", "Europe/Prague"),
        Entry("Warsaw", "PL", "Europe/Warsaw"),
        Entry("Athens", "GR", "Europe/Athens"),
        Entry("Helsinki", "FI", "Europe/Helsinki"),
        Entry("Stockholm", "SE", "Europe/Stockholm"),
        Entry("Oslo", "NO", "Europe/Oslo"),
        Entry("Copenhagen", "DK", "Europe/Copenhagen"),
        Entry("Istanbul", "TR", "Europe/Istanbul"),
        Entry("Moscow", "RU", "Europe/Moscow"),

        Entry("Casablanca", "MA", "Africa/Casablanca"),
        Entry("Cairo", "EG", "Africa/Cairo"),
        Entry("Lagos", "NG", "Africa/Lagos"),
        Entry("Johannesburg", "ZA", "Africa/Johannesburg"),
        Entry("Nairobi", "KE", "Africa/Nairobi"),

        Entry("Dubai", "AE", "Asia/Dubai"),
        Entry("Riyadh", "SA", "Asia/Riyadh"),
        Entry("Doha", "QA", "Asia/Qatar"),
        Entry("Kuwait City", "KW", "Asia/Kuwait", setOf("Kuwait")),
        Entry("Tehran", "IR", "Asia/Tehran"),
        Entry("Karachi", "PK", "Asia/Karachi"),
        Entry(
            "New Delhi",
            "IN",
            "Asia/Kolkata",
            setOf("Delhi", "Mumbai", "Bombay"),
        ),
        Entry("Dhaka", "BD", "Asia/Dhaka"),
        Entry("Kathmandu", "NP", "Asia/Kathmandu"),
        Entry("Colombo", "LK", "Asia/Colombo"),
        Entry("Yangon", "MM", "Asia/Yangon", setOf("Rangoon")),
        Entry("Bangkok", "TH", "Asia/Bangkok"),
        Entry("Ho Chi Minh City", "VN", "Asia/Ho_Chi_Minh", setOf("Saigon", "Ho Chi Minh")),
        Entry("Jakarta", "ID", "Asia/Jakarta", setOf("Djakarta")),
        Entry("Makassar", "ID", "Asia/Makassar", setOf("Ujung Pandang")),
        Entry("Jayapura", "ID", "Asia/Jayapura"),
        Entry("Kuala Lumpur", "MY", "Asia/Kuala_Lumpur"),
        Entry("Singapore", "SG", "Asia/Singapore"),
        Entry("Manila", "PH", "Asia/Manila"),
        Entry("Hong Kong", "HK", "Asia/Hong_Kong"),
        Entry("Taipei", "TW", "Asia/Taipei"),
        Entry("Beijing", "CN", "Asia/Shanghai", setOf("Peking")),
        Entry("Seoul", "KR", "Asia/Seoul"),
        Entry("Tokyo", "JP", "Asia/Tokyo"),

        Entry("Perth", "AU", "Australia/Perth"),
        Entry("Brisbane", "AU", "Australia/Brisbane"),
        Entry("Sydney", "AU", "Australia/Sydney"),
        Entry("Auckland", "NZ", "Pacific/Auckland"),
        Entry("Fiji", "FJ", "Pacific/Fiji"),
        Entry("Guam", "GU", "Pacific/Guam"),
    )
}
