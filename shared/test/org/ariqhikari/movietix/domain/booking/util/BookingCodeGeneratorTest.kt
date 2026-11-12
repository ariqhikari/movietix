package org.ariqhikari.movietix.domain.booking.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BookingCodeGeneratorTest {

    private val generator = BookingCodeGenerator()

    @Test
    fun generate_returnsFormatWithMtxPrefixAndSixChars() {
        val code = generator.generate()
        val regex = Regex("^MTX-[A-Z0-9]{6}$")
        assertTrue(regex.matches(code), "Generated code '$code' must match ^MTX-[A-Z0-9]{6}$")
    }

    @Test
    fun generate_multipleCallsProduceUniqueCodes() {
        val count = 100
        val codes = (1..count).map { generator.generate() }.toSet()
        assertEquals(count, codes.size, "All generated booking codes must be unique")
    }
}
