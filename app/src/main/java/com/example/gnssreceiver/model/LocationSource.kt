package com.example.gnssreceiver.model

import android.location.GnssStatus

enum class LocationSource(
    val displayName: String,
    val shortName: String,
    val constellationType: Int? = null,
    val description: String
) {
    GOOGLE_FUSED(
        displayName = "Google Location Service",
        shortName = "Fused",
        constellationType = null,
        description = "Combines GPS, Wi-Fi, cellular networks, and device sensors for high accuracy and power efficiency."
    ),
    GPS(
        displayName = "GPS (USA)",
        shortName = "GPS",
        constellationType = GnssStatus.CONSTELLATION_GPS,
        description = "United States Global Positioning System satellites."
    ),
    GALILEO(
        displayName = "Galileo (EU)",
        shortName = "Galileo",
        constellationType = GnssStatus.CONSTELLATION_GALILEO,
        description = "European Union Global Navigation Satellite System."
    ),
    GLONASS(
        displayName = "GLONASS (Russia)",
        shortName = "GLONASS",
        constellationType = GnssStatus.CONSTELLATION_GLONASS,
        description = "Russian Global Navigation Satellite System."
    ),
    BEIDOU(
        displayName = "BeiDou (China)",
        shortName = "BeiDou",
        constellationType = GnssStatus.CONSTELLATION_BEIDOU,
        description = "Chinese Navigation Satellite System."
    ),
    ALL_GNSS(
        displayName = "All GNSS Systems",
        shortName = "All GNSS",
        constellationType = null,
        description = "Raw GNSS hardware receiver combining all visible satellite constellations."
    )
}
