package org.ariqhikari.movietix.core.data.network

import java.io.File
import java.io.FileInputStream
import java.util.Properties

actual fun getTmdbAccessToken(): String {
    // 1. Coba baca dari system environment (mis. CI/CD)
    System.getenv("TMDB_ACCESS_TOKEN")?.takeIf { it.isNotBlank() }?.let { return it }

    // 2. Coba baca dari local.properties di root project
    try {
        val candidates = listOf(
            File("local.properties"),
            File("../local.properties"),
            File("../../local.properties")
        )
        for (file in candidates) {
            if (file.exists() && file.isFile) {
                val props = Properties()
                FileInputStream(file).use { props.load(it) }
                val token = props.getProperty("tmdb.access.token")
                    ?: props.getProperty("TMDB_ACCESS_TOKEN")
                if (!token.isNullOrBlank() && !token.startsWith("YOUR_")) {
                    return token.trim()
                }
            }
        }
    } catch (_: Throwable) {
        // Fallback gracefully jika file tidak bisa diakses di runtime Android sandbox
    }

    return ""
}
