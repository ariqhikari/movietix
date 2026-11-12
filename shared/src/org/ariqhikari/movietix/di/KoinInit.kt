package org.ariqhikari.movietix.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatformTools

/**
 * Initializes Koin for MovieTix safely (idempotent).
 * Uses KoinPlatformTools.defaultContext() for multiplatform compatibility.
 */
fun startMovieTixKoin(
    additionalModules: List<Module> = emptyList(),
    appDeclaration: KoinAppDeclaration = {}
) {
    if (KoinPlatformTools.defaultContext().getOrNull() == null) {
        startKoin {
            appDeclaration()
            modules(
                listOf(coreModule) + additionalModules
            )
        }
    }
}


