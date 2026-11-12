package org.ariqhikari.movietix.domain.ticket.util

import org.ariqhikari.movietix.domain.booking.util.QrPayloadFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QrPayloadParserTest {

    @Test
    fun roundTrip_withQrPayloadFactory_parsesCorrectly() {
        val bookingCode = "MTX-948201"
        val scheduleId = "sch_550_cgv-gi_std-reg_20261112_1030"
        val seats = listOf("C6", "C5") // unordered

        val payloadString = QrPayloadFactory.create(bookingCode, scheduleId, seats)
        // Format should be sorted: C5,C6
        assertEquals("MOVIETIX:v1:MTX-948201:sch_550_cgv-gi_std-reg_20261112_1030:C5,C6", payloadString)

        val parsed = QrPayloadParser.parse(payloadString)
        assertNotNull(parsed)
        assertEquals("v1", parsed.version)
        assertEquals(bookingCode, parsed.bookingCode)
        assertEquals(scheduleId, parsed.scheduleId)
        assertEquals(listOf("C5", "C6"), parsed.seats)
        assertTrue(QrPayloadParser.isValid(payloadString))
    }

    @Test
    fun parse_unknownVersion_returnsNull() {
        val payloadString = "MOVIETIX:v2:MTX-948201:sch_1:C5"
        assertNull(QrPayloadParser.parse(payloadString))
        assertFalse(QrPayloadParser.isValid(payloadString))
    }

    @Test
    fun parse_invalidPrefix_returnsNull() {
        val payloadString = "CINEMATIX:v1:MTX-948201:sch_1:C5"
        assertNull(QrPayloadParser.parse(payloadString))
    }

    @Test
    fun parse_malformedColons_returnsNull() {
        assertNull(QrPayloadParser.parse("MOVIETIX:v1:MTX-948201"))
        assertNull(QrPayloadParser.parse("MOVIETIX:v1:MTX-948201:sch_1:C5:EXTRA"))
        assertNull(QrPayloadParser.parse(""))
    }

    @Test
    fun parse_blankFields_returnsNull() {
        assertNull(QrPayloadParser.parse("MOVIETIX:v1: :sch_1:C5"))
        assertNull(QrPayloadParser.parse("MOVIETIX:v1:MTX-1::C5"))
    }
}
