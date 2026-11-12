package org.ariqhikari.movietix.core.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = MovieTixColors.CinemaPrimary,
    onPrimary = MovieTixColors.CinemaOnPrimary,
    primaryContainer = MovieTixColors.CinemaPrimaryVariant,
    onPrimaryContainer = MovieTixColors.CinemaTextPrimary,
    secondary = MovieTixColors.CinemaSecondary,
    onSecondary = MovieTixColors.CinemaOnPrimary,
    background = MovieTixColors.CinemaBackground,
    onBackground = MovieTixColors.CinemaTextPrimary,
    surface = MovieTixColors.CinemaSurfaceCard,
    onSurface = MovieTixColors.CinemaTextPrimary,
    surfaceVariant = MovieTixColors.CinemaSurfaceSubtle,
    onSurfaceVariant = MovieTixColors.CinemaTextSecondary,
    outline = MovieTixColors.CinemaSurfaceBorder,
    error = MovieTixColors.StateErrorDark,
    onError = MovieTixColors.CinemaBackground,
)

private val LightColorScheme = lightColorScheme(
    primary = MovieTixColors.LightPrimary,
    onPrimary = MovieTixColors.LightOnPrimary,
    primaryContainer = MovieTixColors.LightSurfaceSubtle,
    onPrimaryContainer = MovieTixColors.LightTextPrimary,
    secondary = MovieTixColors.CinemaSecondary,
    onSecondary = MovieTixColors.LightOnPrimary,
    background = MovieTixColors.LightBackground,
    onBackground = MovieTixColors.LightTextPrimary,
    surface = MovieTixColors.LightSurface,
    onSurface = MovieTixColors.LightTextPrimary,
    surfaceVariant = MovieTixColors.LightSurfaceSubtle,
    onSurfaceVariant = MovieTixColors.LightTextSecondary,
    outline = MovieTixColors.LightBorder,
    error = MovieTixColors.StateErrorLight,
    onError = MovieTixColors.LightSurface,
)

@Composable
fun MovieTixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = movieTixTypography()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
