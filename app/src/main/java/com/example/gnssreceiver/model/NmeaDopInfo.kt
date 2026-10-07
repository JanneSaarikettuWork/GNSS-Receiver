package com.example.gnssreceiver.model

data class NmeaDopInfo(
    val hdop: Float? = null,
    val vdop: Float? = null,
    val pdop: Float? = null,
    val fixType: String? = null,
    val talkerId: String? = null,
    val lastNmeaSentence: String? = null,
    val recentSentences: List<String> = emptyList()
) {
    val dopQualityRating: String
        get() {
            val h = hdop ?: return "Unknown"
            return when {
                h <= 1.0f -> "Ideal (<= 1.0)"
                h <= 2.0f -> "Excellent (1 - 2)"
                h <= 5.0f -> "Good (2 - 5)"
                h <= 10.0f -> "Moderate (5 - 10)"
                h <= 20.0f -> "Fair (10 - 20)"
                else -> "Poor (> 20)"
            }
        }
}
