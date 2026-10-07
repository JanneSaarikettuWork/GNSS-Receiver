package com.example.gnssreceiver.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gnssreceiver.data.GnssLocationManager
import com.example.gnssreceiver.model.GnssDataState
import com.example.gnssreceiver.model.LocationSource
import com.example.gnssreceiver.util.CoordinateFormatter
import kotlinx.coroutines.flow.StateFlow

class GnssViewModel(
    private val gnssLocationManager: GnssLocationManager
) : ViewModel() {

    val state: StateFlow<GnssDataState> = gnssLocationManager.state

    fun selectSource(source: LocationSource) {
        gnssLocationManager.setLocationSource(source)
    }

    fun toggleSourceEnabled(source: LocationSource, enabled: Boolean) {
        val currentEnabled = state.value.enabledSources.toMutableSet()
        if (enabled) {
            currentEnabled.add(source)
        } else {
            // Ensure at least one source stays enabled
            if (currentEnabled.size > 1) {
                currentEnabled.remove(source)
            }
        }
        gnssLocationManager.updateEnabledSources(currentEnabled)
    }

    fun setUpdateInterval(intervalMs: Long) {
        gnssLocationManager.updateUpdateInterval(intervalMs)
    }

    fun setKeepScreenOn(enabled: Boolean) {
        gnssLocationManager.updateKeepScreenOn(enabled)
    }

    fun setUseDmsFormat(enabled: Boolean) {
        gnssLocationManager.updateUseDmsFormat(enabled)
    }

    fun updatePermissionStatus(hasPermission: Boolean) {
        gnssLocationManager.startTracking(hasPermission = hasPermission)
    }

    fun checkGpsStatus() {
        val hasPermission = state.value.hasPermission
        gnssLocationManager.startTracking(hasPermission = hasPermission)
    }

    fun stopTracking() {
        gnssLocationManager.stopTracking()
    }

    fun copyCoordinatesToClipboard(context: Context) {
        val location = state.value.currentLocation ?: run {
            Toast.makeText(context, "No location fix available", Toast.LENGTH_SHORT).show()
            return
        }

        val text = CoordinateFormatter.formatDecimalDegrees(location.latitude, location.longitude)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Coordinates", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Coordinates copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun shareLocation(context: Context) {
        val location = state.value.currentLocation ?: run {
            Toast.makeText(context, "No location fix available", Toast.LENGTH_SHORT).show()
            return
        }

        val text = "Location Fix:\n" +
                "Lat/Lon: ${CoordinateFormatter.formatDecimalDegrees(location.latitude, location.longitude)}\n" +
                "Accuracy: ${CoordinateFormatter.formatAccuracy(location.accuracy)}\n" +
                "Altitude: ${CoordinateFormatter.formatAltitude(location.altitude, if (location.hasVerticalAccuracy()) location.verticalAccuracyMeters else null)}\n" +
                "Source: ${state.value.selectedSource.displayName}\n" +
                "https://maps.google.com/?q=${location.latitude},${location.longitude}"

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share Location")
        context.startActivity(shareIntent)
    }

    override fun onCleared() {
        super.onCleared()
        gnssLocationManager.stopTracking()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val locationManager = GnssLocationManager(context.applicationContext)
            return GnssViewModel(locationManager) as T
        }
    }
}
