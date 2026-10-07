package com.example.gnssreceiver.model

import android.location.GnssStatus

data class GnssSatelliteInfo(
    val svid: Int,
    val constellationType: Int,
    val constellationName: String,
    val cn0DbHz: Float,
    val elevationDegrees: Float,
    val azimuthDegrees: Float,
    val usedInFix: Boolean,
    val hasCarrierFrequency: Boolean = false,
    val carrierFrequencyHz: Float = 0f,
    val carrierBand: String = "",
    val hasAlmanacData: Boolean = false,
    val hasEphemerisData: Boolean = false
) {
    val id: String
        get() = "$constellationName-$svid"

    companion object {
        fun getConstellationName(type: Int): String {
            return when (type) {
                GnssStatus.CONSTELLATION_GPS -> "GPS"
                GnssStatus.CONSTELLATION_GALILEO -> "Galileo"
                GnssStatus.CONSTELLATION_GLONASS -> "GLONASS"
                GnssStatus.CONSTELLATION_BEIDOU -> "BeiDou"
                GnssStatus.CONSTELLATION_QZSS -> "QZSS"
                GnssStatus.CONSTELLATION_SBAS -> "SBAS"
                GnssStatus.CONSTELLATION_IRNSS -> "NavIC"
                else -> "Unknown"
            }
        }

        fun getCarrierBandName(constellationType: Int, frequencyHz: Float): String {
            if (frequencyHz <= 0f) return ""
            val freqMHz = frequencyHz / 1_000_000f

            return when (constellationType) {
                GnssStatus.CONSTELLATION_GPS -> when {
                    freqMHz in 1560f..1590f -> "L1"
                    freqMHz in 1217f..1237f -> "L2"
                    freqMHz in 1164f..1189f -> "L5"
                    else -> "%.1f MHz".format(freqMHz)
                }
                GnssStatus.CONSTELLATION_GALILEO -> when {
                    freqMHz in 1560f..1590f -> "E1"
                    freqMHz in 1164f..1189f -> "E5a"
                    freqMHz in 1190f..1210f -> "E5b"
                    freqMHz in 1260f..1300f -> "E6"
                    else -> "%.1f MHz".format(freqMHz)
                }
                GnssStatus.CONSTELLATION_GLONASS -> when {
                    freqMHz in 1598f..1606f -> "L1"
                    freqMHz in 1242f..1249f -> "L2"
                    freqMHz in 1200f..1210f -> "L3"
                    else -> "%.1f MHz".format(freqMHz)
                }
                GnssStatus.CONSTELLATION_BEIDOU -> when {
                    freqMHz in 1555f..1580f -> "B1"
                    freqMHz in 1195f..1215f -> "B2"
                    freqMHz in 1260f..1275f -> "B3"
                    freqMHz in 1164f..1189f -> "B1C/B2a"
                    else -> "%.1f MHz".format(freqMHz)
                }
                else -> "%.1f MHz".format(freqMHz)
            }
        }
    }
}
