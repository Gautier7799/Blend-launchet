package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val GoogleDarkColorScheme = darkColorScheme(
    primary = GoogleDarkPrimary,
    onPrimary = GoogleDarkOnPrimary,
    primaryContainer = GoogleDarkPrimaryContainer,
    onPrimaryContainer = GoogleDarkOnPrimaryContainer,
    secondary = GoogleDarkSecondary,
    onSecondary = GoogleDarkOnSecondary,
    secondaryContainer = GoogleDarkSecondaryContainer,
    onSecondaryContainer = GoogleDarkOnSecondaryContainer,
    background = GoogleDarkBackground,
    onBackground = GoogleDarkOnBackground,
    surface = GoogleDarkSurface,
    onSurface = GoogleDarkOnSurface,
    surfaceVariant = GoogleDarkSurfaceVariant,
    onSurfaceVariant = GoogleDarkOnSurfaceVariant,
)

private val GoogleLightColorScheme = lightColorScheme(
    primary = GoogleBluePrimary,
    onPrimary = GoogleBlueOnPrimary,
    primaryContainer = GoogleBluePrimaryContainer,
    onPrimaryContainer = GoogleBlueOnPrimaryContainer,
    secondary = GoogleSecondary,
    onSecondary = GoogleOnSecondary,
    secondaryContainer = GoogleSecondaryContainer,
    onSecondaryContainer = GoogleOnSecondaryContainer,
    background = GoogleBackgroundLight,
    onBackground = GoogleOnBackgroundLight,
    surface = GoogleSurfaceLight,
    onSurface = GoogleOnSurfaceLight,
    surfaceVariant = GoogleSurfaceVariantLight,
    onSurfaceVariant = GoogleOnSurfaceVariantLight,
    outline = GoogleOutlineLight,
    outlineVariant = GoogleOutlineVariantLight,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Material You dynamic color from Android 12+ wallpaper extraction
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> GoogleDarkColorScheme
        else -> GoogleLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
