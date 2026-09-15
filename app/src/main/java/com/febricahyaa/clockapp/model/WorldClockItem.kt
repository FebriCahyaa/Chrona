package com.febricahyaa.clockapp.model

/** A saved city added to the World Clock screen. */
data class WorldClockItem(
    val id: Long,
    val city: String,
    val zoneId: String
)

/** Small curated catalog of cities to pick from when adding a world clock. */
object TimeZoneCatalog {
    data class Entry(val city: String, val country: String, val zoneId: String)

    val entries: List<Entry> = listOf(
        Entry("Jakarta", "Indonesia", "Asia/Jakarta"),
        Entry("Makassar", "Indonesia", "Asia/Makassar"),
        Entry("Jayapura", "Indonesia", "Asia/Jayapura"),
        Entry("Singapore", "Singapore", "Asia/Singapore"),
        Entry("Kuala Lumpur", "Malaysia", "Asia/Kuala_Lumpur"),
        Entry("Bangkok", "Thailand", "Asia/Bangkok"),
        Entry("Tokyo", "Japan", "Asia/Tokyo"),
        Entry("Seoul", "South Korea", "Asia/Seoul"),
        Entry("Beijing", "China", "Asia/Shanghai"),
        Entry("Hong Kong", "China", "Asia/Hong_Kong"),
        Entry("New Delhi", "India", "Asia/Kolkata"),
        Entry("Dubai", "UAE", "Asia/Dubai"),
        Entry("London", "United Kingdom", "Europe/London"),
        Entry("Paris", "France", "Europe/Paris"),
        Entry("Berlin", "Germany", "Europe/Berlin"),
        Entry("Moscow", "Russia", "Europe/Moscow"),
        Entry("New York", "United States", "America/New_York"),
        Entry("Los Angeles", "United States", "America/Los_Angeles"),
        Entry("Chicago", "United States", "America/Chicago"),
        Entry("Toronto", "Canada", "America/Toronto"),
        Entry("Sao Paulo", "Brazil", "America/Sao_Paulo"),
        Entry("Sydney", "Australia", "Australia/Sydney"),
        Entry("Auckland", "New Zealand", "Pacific/Auckland"),
        Entry("Cairo", "Egypt", "Africa/Cairo"),
        Entry("Johannesburg", "South Africa", "Africa/Johannesburg"),
    )
}
