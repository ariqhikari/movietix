package org.ariqhikari.movietix.di

import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

/**
 * Initializes Koin for MovieTix safely (idempotent).
 * Called once at application startup from Android MainActivity and iOS ViewController.
 */
fun startMovieTixKoin(
    additionalModules: List<Module> = emptyList(),
    appDeclaration: KoinAppDeclaration = {}
) {
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            appDeclaration()
            modules(
                listOf(coreModule) + additionalModules
            )
        }
    }
}

