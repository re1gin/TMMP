@file:Suppress("DEPRECATION")

package com.teladanprimaagro.tmpp.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val MainColorPalette = lightColorScheme(
    primary = MainBackground,
    onPrimary = Orange,

    secondary = DarkBrown1,
    onSecondary = Orange,

    background = Black,
    onBackground = OliveGreen,

    surface = DarkBrown1,
    onSurface = Black,
    surfaceVariant = Orange,
    onSurfaceVariant = White,

    outline = Orange,
    surfaceContainer = BackgroundDarkGrey,
    surfaceBright = Brown,
)

// Bentuk (Shapes)
val MainShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun MainTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = MainColorPalette.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = MainColorPalette,
        typography = Typography,
        shapes = MainShapes,
        content = content
    )
}