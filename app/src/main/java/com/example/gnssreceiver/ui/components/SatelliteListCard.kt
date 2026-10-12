package com.example.gnssreceiver.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gnssreceiver.model.GnssDataState
import com.example.gnssreceiver.model.GnssSatelliteInfo

@Composable
fun SatelliteListCard(
    state: GnssDataState,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    val isSingleSource = state.enabledSources.size == 1
    val satellites = state.filteredSatellites.sortedByDescending { it.cn0DbHz }

    val headerTitle = if (isSingleSource) {
        "SATELLITE SIGNALS (${state.selectedSource.shortName})"
    } else {
        "SATELLITE SIGNALS"
    }

    val emptyMessage = if (isSingleSource) {
        "No satellites visible for ${state.selectedSource.displayName}"
    } else {
        "No satellites visible"
    }

    val usedCount = if (isSingleSource) state.satellitesUsedInFix else state.totalSatellitesUsedInFix
    val visibleCount = if (isSingleSource) state.satellitesInView else state.totalSatellitesInView

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
                Column {
                    Text(
                        text = headerTitle,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Avg C/N0: %.1f dB-Hz • Used: %d / %d".format(
                            state.averageCn0DbHz,
                            usedCount,
                            visibleCount
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand/Collapse Satellites"
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    if (satellites.isEmpty()) {
                        Text(
                            text = emptyMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        // Table header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SV / SYS", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(70.dp))
                            Text("SIGNAL (C/N0)", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                            Text("BAND", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(50.dp))
                            Text("ELEV / AZ", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(70.dp))
                        }

                        // Satellites list
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                        ) {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(satellites, key = { "${it.constellationType}-${it.svid}" }) { sat ->
                                    SatelliteRowItem(sat)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SatelliteRowItem(sat: GnssSatelliteInfo) {
    val constColor = getConstellationColor(sat.constellationType)
    val prefix = getConstellationPrefix(sat.constellationType)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Satellite identifier badge
            Row(
                modifier = Modifier.width(70.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (sat.usedInFix) constColor else Color.Gray, shape = CircleShape)
                )
                Text(
                    text = " $prefix%02d".format(sat.svid),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Signal strength bar (0..50 dB-Hz)
            Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "%.1f dB-Hz".format(sat.cn0DbHz),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.SemiBold
                    )
                    if (sat.usedInFix) {
                        Text(
                            text = "FIX",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Bold,
                            color = constColor
                        )
                    }
                }

                val fraction = (sat.cn0DbHz / 50f).coerceIn(0f, 1f)
                val barColor = when {
                    sat.cn0DbHz >= 35f -> Color(0xFF43A047)
                    sat.cn0DbHz >= 25f -> Color(0xFFFB8C00)
                    else -> Color(0xFFE53935)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .background(barColor)
                    )
                }
            }

            // Band
            Text(
                text = sat.carrierBand.ifEmpty { "-" },
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(50.dp)
            )

            // Elevation / Azimuth
            Text(
                text = "%.0f°/%.0f°".format(sat.elevationDegrees, sat.azimuthDegrees),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(70.dp)
            )
        }
    }
}
