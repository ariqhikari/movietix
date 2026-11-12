package org.ariqhikari.movietix.core.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Creates and configures the singleton HttpClient.
 * - Base URL: https://api.themoviedb.org/3/
 * - Bearer Token: Authorization header
 * - Logging: sanitized Authorization header, disabled in production
 */
fun createHttpClient(
    engine: HttpClientEngine,
    bearerToken: String,
    isDebug: Boolean = true
): HttpClient {
    return HttpClient(engine) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = false
                    coerceInputValues = true
                }
            )
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println("[MovieTix-Ktor] $message")
                }
            }
            level = if (isDebug) LogLevel.HEADERS else LogLevel.NONE
            // Sanitasi header Authorization agar token tidak pernah tercetak di logcat/terminal
            sanitizeHeader { headerName ->
                headerName.equals(HttpHeaders.Authorization, ignoreCase = true)
            }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 30_000
        }

        defaultRequest {
            url {
                protocol = URLProtocol.HTTPS
                host = "api.themoviedb.org"
                path("3/")
            }
            if (bearerToken.isNotBlank()) {
                header(HttpHeaders.Authorization, "Bearer $bearerToken")
            }
        }
    }
}
