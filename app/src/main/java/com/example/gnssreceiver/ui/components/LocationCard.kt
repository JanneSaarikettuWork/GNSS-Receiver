package com.example.gnssreceiver.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gnssreceiver.model.FixStatus
import com.example.gnssreceiver.model.GnssDataState
import com.example.gnssreceiver.util.CoordinateFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LocationCard(
    state: GnssDataState,
    onCopyCoordinates: () -> Unit,
    onShareLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    var useDmsFormat by remember(state.useDmsFormatByDefault) { mutableStateOf(state.useDmsFormatByDefault) }
    val location = state.currentLocation

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "LOCATION COORDINATES",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }

                if (location != null) {
                    AccuracyBadge(accuracyMeters = if (location.hasAccuracy()) location.accuracy else null)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (location == null) {
                // Fix status message when no fix is available
                val (statusText, statusColor) = when (state.fixStatus) {
                    FixStatus.PERMISSION_REQUIRED -> "Location permission required" to MaterialTheme.colorScheme.error
                    FixStatus.LOCATION_DISABLED -> "GPS / Location services disabled" to MaterialTheme.colorScheme.error
                    FixStatus.SEARCHING -> "Acquiring satellite fix..." to MaterialTheme.colorScheme.primary
                    FixStatus.FIX_ACQUIRED -> "Acquiring coordinates..." to MaterialTheme.colorScheme.secondary
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                // Coordinates display
                val formattedCoordinates = if (useDmsFormat) {
                    CoordinateFormatter.formatDMS(location.latitude, location.longitude)
                } else {
                    CoordinateFormatter.formatDecimalDegrees(location.latitude, location.longitude)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedCoordinates,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = if (useDmsFormat) 18.sp else 22.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = { useDmsFormat = !useDmsFormat }) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Toggle DD / DMS format",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = if (useDmsFormat) "Format: Degrees° Minutes' Seconds\" (DMS)" else "Format: Decimal Degrees (DD)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Location Details Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LocationInfoItem(
                        label = "Altitude",
                        value = CoordinateFormatter.formatAltitude(
                            altitudeMeters = location.altitude,
                            verticalAccuracyMeters = if (location.hasVerticalAccuracy()) location.verticalAccuracyMeters else null
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    LocationInfoItem(
                        label = "Speed",
                        value = CoordinateFormatter.formatSpeed(
                            speedMetersPerSecond = if (location.hasSpeed()) location.speed else null,
                            speedAccuracy = if (location.hasSpeedAccuracy()) location.speedAccuracyMetersPerSecond else null
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LocationInfoItem(
                        label = "Bearing",
                        value = CoordinateFormatter.formatBearing(
                            bearingDegrees = if (location.hasBearing()) location.bearing else null,
                            bearingAccuracy = if (location.hasBearingAccuracy()) location.bearingAccuracyDegrees else null
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    val timeFormatted = remember(location.time) {
                        if (location.time > 0) {
                            SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(location.time))
                        } else "N/A"
                    }

                    LocationInfoItem(
                        label = "Fix Provider / Time",
                        value = "${location.provider?.uppercase() ?: "GNSS"} • $timeFormatted",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onCopyCoordinates,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(text = "Copy")
                    }

                    OutlinedButton(
                        onClick = onShareLocation,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(text = "Share")
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationInfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
