package org.ariqhikari.movietix.core.data.network

import platform.Foundation.NSBundle

actual fun getTmdbAccessToken(): String {
    val token = NSBundle.mainBundle.objectForInfoDictionaryKey("TMDB_ACCESS_TOKEN") as? String
    return token?.takeIf { it.isNotBlank() && !it.startsWith("YOUR_") } ?: ""
}
