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
 * - Singleton HttpClient (configured with base URL, engine, and bearer token)
 * - Singleton DataStore<Preferences>
 * - IO CoroutineDispatcher
 */
val coreModule = module {
    single { getHttpEngine() }

    single {
        val engine = get<io.ktor.client.engine.HttpClientEngine>()
        createHttpClient(
            engine = engine,
            bearerToken = Secrets.TMDB_ACCESS_TOKEN,
            isDebug = true
        )
    }

    single { createDataStore() }

    single<CoroutineDispatcher> { Dispatchers.IO }
}
