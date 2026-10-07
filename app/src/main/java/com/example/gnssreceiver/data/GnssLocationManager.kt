package com.example.gnssreceiver.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.os.Looper
import com.example.gnssreceiver.model.FixStatus
import com.example.gnssreceiver.model.GnssDataState
import com.example.gnssreceiver.model.GnssSatelliteInfo
import com.example.gnssreceiver.model.LocationSource
import com.example.gnssreceiver.util.NmeaParser
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GnssLocationManager(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val settingsRepository = SettingsRepository(context)

    private val _state = MutableStateFlow(
        GnssDataState(
            selectedSource = settingsRepository.getDefaultSource(),
            enabledSources = settingsRepository.getEnabledSources(),
            updateIntervalMs = settingsRepository.getUpdateIntervalMs(),
            keepScreenOn = settingsRepository.getKeepScreenOn(),
            useDmsFormatByDefault = settingsRepository.getUseDmsFormat()
        )
    )
    val state: StateFlow<GnssDataState> = _state.asStateFlow()

    private var isTracking = false

    private val locationListener = LocationListener { location ->
        handleNewLocation(location)
    }

    private val fusedLocationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                handleNewLocation(location)
            }
        }
    }

    private val gnssStatusCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            val count = status.satelliteCount
            val satellites = ArrayList<GnssSatelliteInfo>(count)
            var hasDualFreq = false

            for (i in 0 until count) {
                val constellationType = status.getConstellationType(i)
                val constellationName = GnssSatelliteInfo.getConstellationName(constellationType)
                val freqHz = if (status.hasCarrierFrequencyHz(i)) {
                    status.getCarrierFrequencyHz(i)
                } else 0f

                val carrierBand = GnssSatelliteInfo.getCarrierBandName(constellationType, freqHz)
                if (carrierBand.contains("L5") || carrierBand.contains("E5a") || carrierBand.contains("B2a") || carrierBand.contains("L2") || carrierBand.contains("E6")) {
                    hasDualFreq = true
                }

                satellites.add(
                    GnssSatelliteInfo(
                        svid = status.getSvid(i),
                        constellationType = constellationType,
                        constellationName = constellationName,
                        cn0DbHz = status.getCn0DbHz(i),
                        elevationDegrees = status.getElevationDegrees(i),
                        azimuthDegrees = status.getAzimuthDegrees(i),
                        usedInFix = status.usedInFix(i),
                        hasCarrierFrequency = status.hasCarrierFrequencyHz(i),
                        carrierFrequencyHz = freqHz,
                        carrierBand = carrierBand,
                        hasAlmanacData = status.hasAlmanacData(i),
                        hasEphemerisData = status.hasEphemerisData(i)
                    )
                )
            }

            _state.update { currentState ->
                currentState.copy(
                    satellites = satellites,
                    isDualFrequencyActive = hasDualFreq
                )
            }
        }
    }

    private val nmeaListener = OnNmeaMessageListener { message, _ ->
        _state.update { currentState ->
            val updatedDop = NmeaParser.parseSentence(message, currentState.dopInfo)
            currentState.copy(dopInfo = updatedDop)
        }
    }

    fun setLocationSource(source: LocationSource) {
        if (_state.value.selectedSource == source && isTracking) return

        settingsRepository.setDefaultSource(source)
        _state.update { it.copy(selectedSource = source) }
        if (isTracking) {
            restartLocationUpdates()
        }
    }

    fun updateEnabledSources(sources: Set<LocationSource>) {
        val safeSources = if (sources.isEmpty()) LocationSource.entries.toSet() else sources
        settingsRepository.setEnabledSources(safeSources)

        val currentSelected = _state.value.selectedSource
        val newSelected = if (currentSelected in safeSources) currentSelected else safeSources.first()

        settingsRepository.setDefaultSource(newSelected)
        _state.update {
            it.copy(
                enabledSources = safeSources,
                selectedSource = newSelected
            )
        }

        if (isTracking) {
            restartLocationUpdates()
        }
    }

    fun updateUpdateInterval(intervalMs: Long) {
        settingsRepository.setUpdateIntervalMs(intervalMs)
        _state.update { it.copy(updateIntervalMs = intervalMs) }
        if (isTracking) {
            restartLocationUpdates()
        }
    }

    fun updateKeepScreenOn(enabled: Boolean) {
        settingsRepository.setKeepScreenOn(enabled)
        _state.update { it.copy(keepScreenOn = enabled) }
    }

    fun updateUseDmsFormat(enabled: Boolean) {
        settingsRepository.setUseDmsFormat(enabled)
        _state.update { it.copy(useDmsFormatByDefault = enabled) }
    }

    @SuppressLint("MissingPermission")
    fun startTracking(hasPermission: Boolean) {
        val isGpsEnabled = isLocationEnabled()

        _state.update {
            it.copy(
                hasPermission = hasPermission,
                isLocationEnabled = isGpsEnabled,
                fixStatus = when {
                    !hasPermission -> FixStatus.PERMISSION_REQUIRED
                    !isGpsEnabled -> FixStatus.LOCATION_DISABLED
                    it.currentLocation != null -> FixStatus.FIX_ACQUIRED
                    else -> FixStatus.SEARCHING
                }
            )
        }

        if (!hasPermission || !isGpsEnabled) return

        stopTracking()
        isTracking = true

        try {
            locationManager.registerGnssStatusCallback(context.mainExecutor, gnssStatusCallback)
            locationManager.addNmeaListener(context.mainExecutor, nmeaListener)

            val lastGpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastGpsLoc != null && _state.value.currentLocation == null) {
                handleNewLocation(lastGpsLoc)
            }

            requestSourceUpdates(_state.value.selectedSource)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestSourceUpdates(source: LocationSource) {
        val intervalMs = _state.value.updateIntervalMs
        val minFastInterval = (intervalMs / 2).coerceAtLeast(200L)

        when (source) {
            LocationSource.GOOGLE_FUSED -> {
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, intervalMs
                ).setMinUpdateIntervalMillis(minFastInterval)
                    .build()

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    fusedLocationCallback,
                    Looper.getMainLooper()
                )
            }
            else -> {
                val provider = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    LocationManager.GPS_PROVIDER
                } else {
                    LocationManager.PASSIVE_PROVIDER
                }

                locationManager.requestLocationUpdates(
                    provider,
                    intervalMs,
                    0f,
                    locationListener,
                    Looper.getMainLooper()
                )
            }
        }
    }

    fun stopTracking() {
        isTracking = false
        try {
            locationManager.removeUpdates(locationListener)
            fusedLocationClient.removeLocationUpdates(fusedLocationCallback)
            locationManager.unregisterGnssStatusCallback(gnssStatusCallback)
            locationManager.removeNmeaListener(nmeaListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun restartLocationUpdates() {
        stopTracking()
        startTracking(_state.value.hasPermission)
    }

    private fun handleNewLocation(location: Location) {
        _state.update { currentState ->
            currentState.copy(
                currentLocation = location,
                lastFixTimestamp = System.currentTimeMillis(),
                fixStatus = FixStatus.FIX_ACQUIRED
            )
        }
    }

    fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}
