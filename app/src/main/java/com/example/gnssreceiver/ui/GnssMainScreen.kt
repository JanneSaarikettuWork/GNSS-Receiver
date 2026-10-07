package com.example.gnssreceiver.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gnssreceiver.model.FixStatus
import com.example.gnssreceiver.ui.components.LocationCard
import com.example.gnssreceiver.ui.components.NmeaDopCard
import com.example.gnssreceiver.ui.components.PermissionGate
import com.example.gnssreceiver.ui.components.SatelliteListCard
import com.example.gnssreceiver.ui.components.SatelliteSkyplot
import com.example.gnssreceiver.ui.components.SourceSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GnssMainScreen(
    viewModel: GnssViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showSettings by remember { mutableStateOf(false) }

    // Keep screen awake setting handler
    DisposableEffect(state.keepScreenOn) {
        val window = (context as? Activity)?.window
        if (state.keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    if (showSettings) {
        SettingsScreen(
            viewModel = viewModel,
            onBackClick = { showSettings = false }
        )
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "GNSS Receiver",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val statusColor = when (state.fixStatus) {
                                    FixStatus.FIX_ACQUIRED -> Color(0xFF43A047)
                                    FixStatus.SEARCHING -> Color(0xFFFB8C00)
                                    else -> Color(0xFFE53935)
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .background(statusColor, shape = CircleShape)
                                        .padding(4.dp)
                                )
                                Text(
                                    text = when (state.fixStatus) {
                                        FixStatus.FIX_ACQUIRED -> "3D GNSS Fix"
                                        FixStatus.SEARCHING -> "Searching GNSS Satellites..."
                                        FixStatus.PERMISSION_REQUIRED -> "No Permission"
                                        FixStatus.LOCATION_DISABLED -> "GPS Disabled"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        if (state.isDualFrequencyActive) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = CircleShape,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = "Dual Frequency (L1+L5)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.checkGpsStatus() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Location Status"
                            )
                        }

                        IconButton(onClick = { showSettings = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            }
        ) { innerPadding ->
            PermissionGate(
                hasPermission = state.hasPermission,
                isLocationEnabled = state.isLocationEnabled,
                onPermissionResult = { viewModel.updatePermissionStatus(it) },
                onCheckLocationServices = { viewModel.checkGpsStatus() },
                modifier = Modifier.padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        SourceSelector(
                            selectedSource = state.selectedSource,
                            enabledSources = state.enabledSources,
                            onSourceSelected = { viewModel.selectSource(it) }
                        )
                    }

                    item {
                        LocationCard(
                            state = state,
                            onCopyCoordinates = { viewModel.copyCoordinatesToClipboard(context) },
                            onShareLocation = { viewModel.shareLocation(context) }
                        )
                    }

                    item {
                        SatelliteSkyplot(state = state)
                    }

                    item {
                        SatelliteListCard(state = state)
                    }

                    item {
                        NmeaDopCard(dopInfo = state.dopInfo)
                    }
                }
            }
        }
    }
}
