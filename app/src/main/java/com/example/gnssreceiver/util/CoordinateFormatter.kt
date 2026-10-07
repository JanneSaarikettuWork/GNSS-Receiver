package com.example.gnssreceiver.util

import androidx.compose.ui.graphics.Color
import java.util.Locale
import kotlin.math.abs

enum class AccuracyLevel(
    val label: String,
    val color: Color
) {
    EXCELLENT("Excellent (< 3 m)", Color(0xFF2E7D32)),
    GOOD("Good (3 - 8 m)", Color(0xFF43A047)),
    FAIR("Fair (8 - 20 m)", Color(0xFFFB8C00)),
    POOR("Poor (> 20 m)", Color(0xFFE53935)),
    UNKNOWN("Unknown", Color(0xFF757575))
}

object CoordinateFormatter {

    fun formatDecimalDegrees(latitude: Double, longitude: Double): String {
        val latDirection = if (latitude >= 0) "N" else "S"
        val lonDirection = if (longitude >= 0) "E" else "W"

        return String.format(
            Locale.US,
            "%.6f° %s, %.6f° %s",
            abs(latitude),
            latDirection,
            abs(longitude),
            lonDirection
        )
    }

    fun formatDMS(latitude: Double, longitude: Double): String {
        return "${toDMS(latitude, isLatitude = true)}, ${toDMS(longitude, isLatitude = false)}"
    }

    private fun toDMS(coordinate: Double, isLatitude: Boolean): String {
        val direction = if (isLatitude) {
            if (coordinate >= 0) "N" else "S"
        } else {
            if (coordinate >= 0) "E" else "W"
        }

        val absVal = abs(coordinate)
        val degrees = absVal.toInt()
        val minutesDouble = (absVal - degrees) * 60.0
        val minutes = minutesDouble.toInt()
        val seconds = (minutesDouble - minutes) * 60.0

        return String.format(
            Locale.US,
            "%d° %02d' %04.1f\" %s",
            degrees,
            minutes,
            seconds,
            direction
        )
    }

    fun formatAccuracy(accuracyMeters: Float?): String {
        if (accuracyMeters == null || accuracyMeters <= 0f) return "N/A"
        return String.format(Locale.US, "± %.1f m", accuracyMeters)
    }

    fun getAccuracyLevel(accuracyMeters: Float?): AccuracyLevel {
        if (accuracyMeters == null || accuracyMeters <= 0f) return AccuracyLevel.UNKNOWN
        return when {
            accuracyMeters <= 3.0f -> AccuracyLevel.EXCELLENT
            accuracyMeters <= 8.0f -> AccuracyLevel.GOOD
            accuracyMeters <= 20.0f -> AccuracyLevel.FAIR
            else -> AccuracyLevel.POOR
        }
    }

    fun formatAltitude(altitudeMeters: Double?, verticalAccuracyMeters: Float?): String {
        if (altitudeMeters == null) return "N/A"
        val altStr = String.format(Locale.US, "%.1f m", altitudeMeters)
        return if (verticalAccuracyMeters != null && verticalAccuracyMeters > 0f) {
            "$altStr (± %.1f m)".format(Locale.US, verticalAccuracyMeters)
        } else {
            altStr
        }
    }

    fun formatSpeed(speedMetersPerSecond: Float?, speedAccuracy: Float?): String {
        if (speedMetersPerSecond == null || speedMetersPerSecond < 0f) return "N/A"
        val kmh = speedMetersPerSecond * 3.6f
        val speedStr = String.format(Locale.US, "%.1f km/h (%.1f m/s)", kmh, speedMetersPerSecond)
        return if (speedAccuracy != null && speedAccuracy > 0f) {
            "$speedStr ± %.1f m/s".format(Locale.US, speedAccuracy)
        } else {
            speedStr
        }
    }

    fun formatBearing(bearingDegrees: Float?, bearingAccuracy: Float?): String {
        if (bearingDegrees == null || bearingDegrees < 0f) return "N/A"
        val cardinal = getCardinalDirection(bearingDegrees)
        val bearingStr = String.format(Locale.US, "%.1f° (%s)", bearingDegrees, cardinal)
        return if (bearingAccuracy != null && bearingAccuracy > 0f) {
            "$bearingStr ± %.1f°".format(Locale.US, bearingAccuracy)
        } else {
            bearingStr
        }
    }

    fun getCardinalDirection(degrees: Float): String {
        val normalized = (degrees % 360 + 360) % 360
        return when {
            normalized in 337.5..360.0 || normalized in 0.0..<22.5 -> "N"
            normalized in 22.5..<67.5 -> "NE"
            normalized in 67.5..<112.5 -> "E"
            normalized in 112.5..<157.5 -> "SE"
            normalized in 157.5..<202.5 -> "S"
            normalized in 202.5..<247.5 -> "SW"
            normalized in 247.5..<292.5 -> "W"
            normalized in 292.5..<337.5 -> "NW"
            else -> "N"
        }
    }
}
