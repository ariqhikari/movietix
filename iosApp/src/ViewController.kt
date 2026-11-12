package org.ariqhikari.movietix

import androidx.compose.ui.window.ComposeUIViewController
import org.ariqhikari.movietix.di.startMovieTixKoin
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

@OptIn(ExperimentalNativeApi::class)
fun ViewController() = ComposeUIViewController {
    startMovieTixKoin(isDebug = Platform.isDebugBinary)
    App()
}
