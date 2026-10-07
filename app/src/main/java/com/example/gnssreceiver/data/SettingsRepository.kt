package com.example.gnssreceiver.data

import android.content.Context
import android.content.SharedPreferences
import com.example.gnssreceiver.model.LocationSource

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("gnss_receiver_prefs", Context.MODE_PRIVATE)

    fun getEnabledSources(): Set<LocationSource> {
        val saved = prefs.getStringSet(KEY_ENABLED_SOURCES, null) ?: return LocationSource.entries.toSet()
        val sources = saved.mapNotNull { name ->
            runCatching { LocationSource.valueOf(name) }.getOrNull()
        }.toSet()
        return if (sources.isEmpty()) LocationSource.entries.toSet() else sources
    }

    fun setEnabledSources(sources: Set<LocationSource>) {
        val names = sources.map { it.name }.toSet()
        prefs.edit().putStringSet(KEY_ENABLED_SOURCES, names).apply()
    }

    fun getDefaultSource(): LocationSource {
        val name = prefs.getString(KEY_DEFAULT_SOURCE, LocationSource.GOOGLE_FUSED.name)
        return runCatching { LocationSource.valueOf(name!!) }.getOrDefault(LocationSource.GOOGLE_FUSED)
    }

    fun setDefaultSource(source: LocationSource) {
        prefs.edit().putString(KEY_DEFAULT_SOURCE, source.name).apply()
    }

    fun getUpdateIntervalMs(): Long {
        return prefs.getLong(KEY_UPDATE_INTERVAL, 1000L)
    }

    fun setUpdateIntervalMs(intervalMs: Long) {
        prefs.edit().putLong(KEY_UPDATE_INTERVAL, intervalMs).apply()
    }

    fun getKeepScreenOn(): Boolean {
        return prefs.getBoolean(KEY_KEEP_SCREEN_ON, false)
    }

    fun setKeepScreenOn(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, enabled).apply()
    }

    fun getUseDmsFormat(): Boolean {
        return prefs.getBoolean(KEY_USE_DMS, false)
    }

    fun setUseDmsFormat(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_USE_DMS, enabled).apply()
    }

    companion object {
        private const val KEY_ENABLED_SOURCES = "enabled_sources"
        private const val KEY_DEFAULT_SOURCE = "default_source"
        private const val KEY_UPDATE_INTERVAL = "update_interval_ms"
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val KEY_USE_DMS = "use_dms_format"
    }
}
