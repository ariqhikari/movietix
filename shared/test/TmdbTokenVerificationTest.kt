package org.ariqhikari.movietix

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.ariqhikari.movietix.core.data.network.Secrets
import org.ariqhikari.movietix.core.data.network.getHttpEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TmdbTokenVerificationTest {

    @Test
    fun verifyTmdbAccessTokenIsConfiguredAndReturns200() = runTest {
        val token = Secrets.TMDB_ACCESS_TOKEN
        assertFalse(
            token.isBlank() || token.startsWith("YOUR_"),
            "TMDB Access Token must be configured in Secrets.kt"
        )

        val client = HttpClient(getHttpEngine()) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            defaultRequest {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "api.themoviedb.org"
                }
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        // Panggil endpoint autentikasi resmi TMDB v3 /authentication
        val response = client.get("3/authentication")
        val status = response.status
        println("[TMDB Verification] HTTP Status: $status")

        assertEquals(
            HttpStatusCode.OK,
            status,
            "Expected HTTP 200 OK from TMDB authentication endpoint with bearer token"
        )

        val body = response.bodyAsText()
        assertTrue(body.contains("\"success\":true"), "Response must contain success:true")
    }
}
