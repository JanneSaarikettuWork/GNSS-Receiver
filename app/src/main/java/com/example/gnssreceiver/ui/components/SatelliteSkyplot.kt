package com.example.gnssreceiver.ui.components

import android.location.GnssStatus
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gnssreceiver.model.GnssDataState
import com.example.gnssreceiver.model.GnssSatelliteInfo
import com.example.gnssreceiver.model.LocationSource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SatelliteSkyplot(
    state: GnssDataState,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SATELLITE SKYPLOT",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )

                Text(
                    text = "Used: ${state.totalSatellitesUsedInFix} / Visible: ${state.totalSatellitesInView}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Skyplot
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxWidth()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxRadius = size.width / 2f - 24.dp.toPx()

                    // Draw concentric elevation rings (0°, 30°, 60°)
                    val ringRadii = listOf(maxRadius, maxRadius * (2f / 3f), maxRadius * (1f / 3f))
                    ringRadii.forEach { radius ->
                        drawCircle(
                            color = gridColor,
                            radius = radius,
                            center = center,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // Draw azimuth axis lines (N-S, E-W)
                    drawLine(
                        color = gridColor,
                        start = Offset(center.x, center.y - maxRadius),
                        end = Offset(center.x, center.y + maxRadius),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = gridColor,
                        start = Offset(center.x - maxRadius, center.y),
                        end = Offset(center.x + maxRadius, center.y),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Draw cardinal direction labels
                    val labelStyle = TextStyle(
                        color = onSurfaceColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    drawText(textMeasurer, "N", Offset(center.x - 6.dp.toPx(), center.y - maxRadius - 20.dp.toPx()), style = labelStyle)
                    drawText(textMeasurer, "S", Offset(center.x - 5.dp.toPx(), center.y + maxRadius + 4.dp.toPx()), style = labelStyle)
                    drawText(textMeasurer, "E", Offset(center.x + maxRadius + 6.dp.toPx(), center.y - 8.dp.toPx()), style = labelStyle)
                    drawText(textMeasurer, "W", Offset(center.x - maxRadius - 20.dp.toPx(), center.y - 8.dp.toPx()), style = labelStyle)

                    // Draw satellites
                    state.satellites.forEach { sat ->
                        // Convert elevation and azimuth to polar coordinates
                        // Elevation 90 -> center (r = 0), Elevation 0 -> outer ring (r = maxRadius)
                        val r = maxRadius * (1f - (sat.elevationDegrees / 90f))
                        // Convert azimuth (0 = N = straight UP) to radians
                        val angleRad = (sat.azimuthDegrees - 90f) * (PI / 180f)

                        val satX = center.x + r * cos(angleRad).toFloat()
                        val satY = center.y + r * sin(angleRad).toFloat()
                        val satPos = Offset(satX, satY)

                        val satColor = getConstellationColor(sat.constellationType)
                        val isHighlightedSource = state.selectedSource != LocationSource.GOOGLE_FUSED &&
                                state.selectedSource != LocationSource.ALL_GNSS &&
                                state.selectedSource.constellationType == sat.constellationType

                        val satRadius = if (isHighlightedSource) 10.dp.toPx() else 8.dp.toPx()

                        // Outer highlight ring if source matches
                        if (isHighlightedSource) {
                            drawCircle(
                                color = satColor,
                                radius = satRadius + 4.dp.toPx(),
                                center = satPos,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }

                        // Satellite circle (filled if used in fix, outlined if only tracked)
                        if (sat.usedInFix) {
                            drawCircle(
                                color = satColor,
                                radius = satRadius,
                                center = satPos
                            )
                        } else {
                            drawCircle(
                                color = satColor,
                                radius = satRadius,
                                center = satPos,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }

                        // Draw satellite label prefix (G01, E12, R04, B08)
                        val prefix = getConstellationPrefix(sat.constellationType)
                        val satLabel = "$prefix%02d".format(sat.svid)
                        val textStyle = TextStyle(
                            color = if (sat.usedInFix) Color.White else satColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )

                        val measuredText = textMeasurer.measure(satLabel, textStyle)
                        drawText(
                            textMeasurer = textMeasurer,
                            text = satLabel,
                            topLeft = Offset(satPos.x - measuredText.size.width / 2f, satPos.y - measuredText.size.height / 2f),
                            style = textStyle
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Constellation Legend
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LegendItem("GPS", getConstellationColor(GnssStatus.CONSTELLATION_GPS))
                LegendItem("Galileo", getConstellationColor(GnssStatus.CONSTELLATION_GALILEO))
                LegendItem("GLONASS", getConstellationColor(GnssStatus.CONSTELLATION_GLONASS))
                LegendItem("BeiDou", getConstellationColor(GnssStatus.CONSTELLATION_BEIDOU))
                LegendItem("Others", getConstellationColor(GnssStatus.CONSTELLATION_SBAS))
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(10.dp),
            shape = MaterialTheme.shapes.extraSmall,
            color = color
        ) {}
        Text(
            text = " $label",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}

fun getConstellationColor(type: Int): Color {
    return when (type) {
        GnssStatus.CONSTELLATION_GPS -> Color(0xFF1E88E5) // Blue
        GnssStatus.CONSTELLATION_GALILEO -> Color(0xFFFFB300) // Amber / Gold
        GnssStatus.CONSTELLATION_GLONASS -> Color(0xFFE53935) // Red
        GnssStatus.CONSTELLATION_BEIDOU -> Color(0xFF43A047) // Green
        GnssStatus.CONSTELLATION_QZSS -> Color(0xFF8E24AA) // Purple
        GnssStatus.CONSTELLATION_SBAS -> Color(0xFF00ACC1) // Cyan
        GnssStatus.CONSTELLATION_IRNSS -> Color(0xFFD81B60) // Pink
        else -> Color(0xFF757575) // Gray
    }
}

fun getConstellationPrefix(type: Int): String {
    return when (type) {
        GnssStatus.CONSTELLATION_GPS -> "G"
        GnssStatus.CONSTELLATION_GALILEO -> "E"
        GnssStatus.CONSTELLATION_GLONASS -> "R"
        GnssStatus.CONSTELLATION_BEIDOU -> "B"
        GnssStatus.CONSTELLATION_QZSS -> "J"
        GnssStatus.CONSTELLATION_SBAS -> "S"
        GnssStatus.CONSTELLATION_IRNSS -> "I"
        else -> "U"
    }
}
