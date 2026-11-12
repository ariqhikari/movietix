package org.ariqhikari.movietix.domain.booking.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QrPayloadFactoryTest {

    @Test
    fun create_generatesStandardV1FormatWithDeterministicSeatSorting() {
        val payload = QrPayloadFactory.create(
            bookingCode = "MTX-948201",
            scheduleId = "sch_123",
            seats = listOf("C6", "C5", "A1")
        )

        assertEquals("MOVIETIX:v1:MTX-948201:sch_123:A1,C5,C6", payload)
        assertTrue(payload.startsWith("MOVIETIX:v1:"))
    }

    @Test
    fun create_handlesEmptySeatsGracefully() {
        val payload = QrPayloadFactory.create(
            bookingCode = "MTX-112233",
            scheduleId = "sch_empty",
            seats = emptyList()
        )

        assertEquals("MOVIETIX:v1:MTX-112233:sch_empty:", payload)
    }
}
