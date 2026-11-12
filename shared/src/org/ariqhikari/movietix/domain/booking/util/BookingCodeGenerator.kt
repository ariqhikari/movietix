package org.ariqhikari.movietix.domain.booking.util

import kotlin.random.Random

/**
 * Generates human-readable booking codes with format MTX-XXXXXX (6 digits).
 * Checks against existing codes to avoid collisions.
 */
class BookingCodeGenerator(
    private val random: Random = Random.Default
) {
    fun generate(existingCodes: Set<String> = emptySet()): String {
        var attempts = 0
        while (attempts < 100) {
            val num = random.nextInt(100000, 1000000)
            val code = "MTX-$num"
            if (code !in existingCodes) {
                return code
            }
            attempts++
        }
        return "MTX-${random.nextInt(100000, 1000000)}"
    }
}
