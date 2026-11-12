package org.ariqhikari.movietix

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
