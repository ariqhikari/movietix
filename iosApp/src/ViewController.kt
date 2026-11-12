package org.ariqhikari.movietix

import androidx.compose.ui.window.ComposeUIViewController
import org.ariqhikari.movietix.di.startMovieTixKoin

fun ViewController() = ComposeUIViewController {
    startMovieTixKoin()
    App()
}
