package com.example.gnssreceiver.util

import com.example.gnssreceiver.model.NmeaDopInfo

object NmeaParser {

    fun parseSentence(
        sentence: String,
        currentDopInfo: NmeaDopInfo
    ): NmeaDopInfo {
        val trimmed = sentence.trim()
        if (!trimmed.startsWith("$")) return currentDopInfo

        val cleanSentence = if (trimmed.contains("*")) {
            trimmed.substringBefore("*")
        } else {
            trimmed
        }

        val tokens = cleanSentence.split(",")
        if (tokens.isEmpty()) return currentDopInfo

        val header = tokens[0]
        val talkerId = if (header.length >= 3) header.substring(1, 3) else null
        val sentenceType = if (header.length >= 6) header.substring(3, 6) else ""

        var updatedHdop = currentDopInfo.hdop
        var updatedVdop = currentDopInfo.vdop
        var updatedPdop = currentDopInfo.pdop
        var updatedFixType = currentDopInfo.fixType

        when (sentenceType) {
            "GSA" -> {
                // Tokens: $--GSA, Mode1, Mode2, Sv1..Sv12, PDOP, HDOP, VDOP
                if (tokens.size >= 3) {
                    val mode2 = tokens[2]
                    updatedFixType = when (mode2) {
                        "1" -> "No Fix"
                        "2" -> "2D Fix"
                        "3" -> "3D Fix"
                        else -> updatedFixType
                    }
                }
                if (tokens.size >= 16) {
                    tokens.getOrNull(15)?.toFloatOrNull()?.let { updatedPdop = it }
                }
                if (tokens.size >= 17) {
                    tokens.getOrNull(16)?.toFloatOrNull()?.let { updatedHdop = it }
                }
                if (tokens.size >= 18) {
                    tokens.getOrNull(17)?.toFloatOrNull()?.let { updatedVdop = it }
                }
            }
            "GGA" -> {
                // Tokens: $--GGA, Time, Lat, N/S, Lon, E/W, Quality, NumSV, HDOP, Alt, M, Sep, M...
                if (tokens.size >= 7) {
                    val quality = tokens[6]
                    updatedFixType = when (quality) {
                        "0" -> "Invalid"
                        "1" -> "GPS Fix (SPS)"
                        "2" -> "DGPS Fix"
                        "3" -> "PPS Fix"
                        "4" -> "RTK Fixed"
                        "5" -> "RTK Float"
                        "6" -> "Estimated / Dead Reckoning"
                        else -> updatedFixType
                    }
                }
                if (tokens.size >= 9) {
                    tokens.getOrNull(8)?.toFloatOrNull()?.let { updatedHdop = it }
                }
            }
        }

        val updatedSentences = (listOf(trimmed) + currentDopInfo.recentSentences).take(10)

        return NmeaDopInfo(
            hdop = updatedHdop,
            vdop = updatedVdop,
            pdop = updatedPdop,
            fixType = updatedFixType,
            talkerId = talkerId ?: currentDopInfo.talkerId,
            lastNmeaSentence = trimmed,
            recentSentences = updatedSentences
        )
    }
}
