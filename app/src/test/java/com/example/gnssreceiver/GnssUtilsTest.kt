package com.example.gnssreceiver

import com.example.gnssreceiver.model.NmeaDopInfo
import com.example.gnssreceiver.util.AccuracyLevel
import com.example.gnssreceiver.util.CoordinateFormatter
import com.example.gnssreceiver.util.NmeaParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GnssUtilsTest {

    @Test
    fun testDecimalDegreesFormatting() {
        val result = CoordinateFormatter.formatDecimalDegrees(60.169854, 24.938411)
        assertEquals("60.169854° N, 24.938411° E", result)

        val southWest = CoordinateFormatter.formatDecimalDegrees(-33.868820, -151.209296)
        assertEquals("33.868820° S, 151.209296° W", southWest)
    }

    @Test
    fun testDMSFormatting() {
        val result = CoordinateFormatter.formatDMS(60.169888, 24.938411)
        assertTrue(result.contains("60° 10'"))
        assertTrue(result.contains("N"))
        assertTrue(result.contains("24° 56'"))
        assertTrue(result.contains("E"))
    }

    @Test
    fun testAccuracyLevels() {
        assertEquals(AccuracyLevel.EXCELLENT, CoordinateFormatter.getAccuracyLevel(2.5f))
        assertEquals(AccuracyLevel.GOOD, CoordinateFormatter.getAccuracyLevel(5.0f))
        assertEquals(AccuracyLevel.FAIR, CoordinateFormatter.getAccuracyLevel(12.0f))
        assertEquals(AccuracyLevel.POOR, CoordinateFormatter.getAccuracyLevel(35.0f))
    }

    @Test
    fun testCardinalDirection() {
        assertEquals("N", CoordinateFormatter.getCardinalDirection(0f))
        assertEquals("E", CoordinateFormatter.getCardinalDirection(90f))
        assertEquals("S", CoordinateFormatter.getCardinalDirection(180f))
        assertEquals("W", CoordinateFormatter.getCardinalDirection(270f))
        assertEquals("NE", CoordinateFormatter.getCardinalDirection(45f))
    }

    @Test
    fun testNmeaParserGsa() {
        val gsaSentence = "\$GNGSA,A,3,01,02,03,04,,,,,,,,,1.5,1.2,0.9*1A"
        val parsed = NmeaParser.parseSentence(gsaSentence, NmeaDopInfo())

        assertEquals("3D Fix", parsed.fixType)
        assertEquals(1.5f, parsed.pdop)
        assertEquals(1.2f, parsed.hdop)
        assertEquals(0.9f, parsed.vdop)
    }

    @Test
    fun testNmeaParserGga() {
        val ggaSentence = "\$GPGGA,123519,4807.038,N,01131.000,E,1,08,0.9,545.4,M,46.9,M,,*47"
        val parsed = NmeaParser.parseSentence(ggaSentence, NmeaDopInfo())

        assertEquals("GPS Fix (SPS)", parsed.fixType)
        assertEquals(0.9f, parsed.hdop)
    }
}
