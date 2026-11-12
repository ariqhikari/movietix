package org.ariqhikari.movietix.core.data.network

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import org.ariqhikari.movietix.core.domain.model.AppError
import org.ariqhikari.movietix.core.domain.model.AppResult

/**
 * Safely executes a network call block and translates Ktor exceptions into typed AppError.
 * Rethrows CancellationException to respect structured concurrency.
 */
suspend fun <T> safeApiCall(block: suspend () -> T): AppResult<T> {
    return try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpRequestTimeoutException) {
        AppResult.Error(AppError.Network.Timeout)
    } catch (e: ClientRequestException) {
        val code = e.response.status.value
        if (code == HttpStatusCode.Unauthorized.value) {
            AppResult.Error(AppError.Network.Unauthorized)
        } else {
            AppResult.Error(AppError.Network.ServerError(code, e.message))
        }
    } catch (e: ServerResponseException) {
        AppResult.Error(AppError.Network.ServerError(e.response.status.value, e.message))
    } catch (e: IOException) {
        AppResult.Error(AppError.Network.NoInternet)
    } catch (e: Throwable) {
        // Fallback for platform-specific network socket / connection errors
        if (e.message?.contains("connect", ignoreCase = true) == true ||
            e.message?.contains("network", ignoreCase = true) == true ||
            e.message?.contains("host", ignoreCase = true) == true ||
            e.message?.contains("unresolved", ignoreCase = true) == true
        ) {
            AppResult.Error(AppError.Network.NoInternet)
        } else {
            AppResult.Error(AppError.Unknown(e))
        }
    }
}
