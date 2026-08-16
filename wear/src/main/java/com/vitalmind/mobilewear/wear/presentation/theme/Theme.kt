package com.vitalmind.mobilewear.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

private val VitalMindWearColorScheme =
    ColorScheme(
        primary = VitalMindBlueLight,
        primaryDim = VitalMindBlue,
        primaryContainer = VitalMindBlue,

        onPrimary = VitalMindBackground,
        onPrimaryContainer = VitalMindText,

        secondary = VitalMindLavender,
        secondaryDim = VitalMindLavenderDark,
        secondaryContainer = VitalMindLavenderDark,

        onSecondary = VitalMindSurface,
        onSecondaryContainer = VitalMindText,

        tertiary = VitalMindBlueLight,
        tertiaryDim = VitalMindBlue,
        tertiaryContainer = VitalMindSurface,

        onTertiary = VitalMindBackground,
        onTertiaryContainer = VitalMindText,

        surfaceContainerLow = VitalMindBackground,
        surfaceContainer = VitalMindSurface,
        surfaceContainerHigh = VitalMindLavenderDark,

        onSurface = VitalMindText,
        onSurfaceVariant = VitalMindLavender,

        outline = VitalMindBlueLight,
        outlineVariant = VitalMindLavenderDark,

        background = VitalMindBackground,
        onBackground = VitalMindText,

        error = androidx.compose.ui.graphics.Color(0xFFFFB4AB),
        onError = androidx.compose.ui.graphics.Color(0xFF690005),
        errorContainer = androidx.compose.ui.graphics.Color(0xFF93000A),
        onErrorContainer = androidx.compose.ui.graphics.Color(0xFFFFDAD6)
    )

@Composable
fun VitalMindWearTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VitalMindWearColorScheme,
        content = content
    )
}