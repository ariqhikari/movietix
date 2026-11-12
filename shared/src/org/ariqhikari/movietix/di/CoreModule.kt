package org.ariqhikari.movietix.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.ariqhikari.movietix.core.data.network.Secrets
import org.ariqhikari.movietix.core.data.network.createHttpClient
import org.ariqhikari.movietix.core.data.network.getHttpEngine
import org.ariqhikari.movietix.core.data.storage.createDataStore
import org.koin.dsl.module

/**
 * Core Koin Module providing:
 * - Singleton HttpClient (configured with base URL, engine, bearer token, and isDebug flag)
 * - Singleton DataStore<Preferences>
 * - IO CoroutineDispatcher
 */
fun createCoreModule(isDebug: Boolean = false) = module {
    single { getHttpEngine() }

    single {
        val engine = get<io.ktor.client.engine.HttpClientEngine>()
        createHttpClient(
            engine = engine,
            bearerToken = Secrets.TMDB_ACCESS_TOKEN,
            isDebug = isDebug
        )
    }

    single { createDataStore() }

    single<CoroutineDispatcher> { Dispatchers.IO }
}

val coreModule = createCoreModule(isDebug = false)
