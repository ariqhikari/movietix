package org.ariqhikari.movietix

import androidx.compose.runtime.Composable
import org.ariqhikari.movietix.core.presentation.theme.MovieTixTheme
import org.ariqhikari.movietix.navigation.AppNavigation
import org.jetbrains.compose.reload.DevelopmentEntryPoint

@Composable
@DevelopmentEntryPoint
fun App() {
    MovieTixTheme {
        AppNavigation()
    }
}
