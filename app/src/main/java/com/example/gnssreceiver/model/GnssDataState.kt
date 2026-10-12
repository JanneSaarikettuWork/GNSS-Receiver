package com.example.gnssreceiver.model

import android.location.Location

enum class FixStatus {
    SEARCHING,
    FIX_ACQUIRED,
    PERMISSION_REQUIRED,
    LOCATION_DISABLED
}

data class GnssDataState(
    val selectedSource: LocationSource = LocationSource.GOOGLE_FUSED,
    val enabledSources: Set<LocationSource> = LocationSource.entries.toSet(),
    val updateIntervalMs: Long = 1000L,
    val keepScreenOn: Boolean = false,
    val useDmsFormatByDefault: Boolean = false,
    val currentLocation: Location? = null,
    val satellites: List<GnssSatelliteInfo> = emptyList(),
    val dopInfo: NmeaDopInfo = NmeaDopInfo(),
    val isLocationEnabled: Boolean = false,
    val hasPermission: Boolean = false,
    val fixStatus: FixStatus = FixStatus.SEARCHING,
    val lastFixTimestamp: Long = 0L,
    val isDualFrequencyActive: Boolean = false,
    val isHardwareGnssSupported: Boolean = true
) {
    val fixAgeSeconds: Long
        get() {
            if (lastFixTimestamp <= 0L) return 0L
            return ((System.currentTimeMillis() - lastFixTimestamp) / 1000L).coerceAtLeast(0L)
        }

    val isFixStale: Boolean
        get() = currentLocation != null && fixAgeSeconds > 15L

    val filteredSatellites: List<GnssSatelliteInfo>
        get() {
            val constType = selectedSource.constellationType ?: return satellites
            return satellites.filter { it.constellationType == constType }
        }

    val satellitesUsedInFix: Int
        get() = filteredSatellites.count { it.usedInFix }

    val satellitesInView: Int
        get() = filteredSatellites.size

    val totalSatellitesUsedInFix: Int
        get() = satellites.count { it.usedInFix }

    val totalSatellitesInView: Int
        get() = satellites.size

    val averageCn0DbHz: Float
        get() {
            val valid = filteredSatellites.filter { it.cn0DbHz > 0f }
            if (valid.isEmpty()) return 0f
            return valid.map { it.cn0DbHz }.average().toFloat()
        }
}
