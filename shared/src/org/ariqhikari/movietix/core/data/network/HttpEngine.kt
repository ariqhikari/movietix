package org.ariqhikari.movietix.core.data.network

import io.ktor.client.engine.HttpClientEngine

/**
 * Platform-specific HTTP engine factory.
 * OkHttp on Android, Darwin on iOS.
 */
expect fun getHttpEngine(): HttpClientEngine
