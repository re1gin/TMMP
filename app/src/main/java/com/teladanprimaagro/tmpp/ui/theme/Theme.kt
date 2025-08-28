@file:Suppress("DEPRECATION")

package com.teladanprimaagro.tmpp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val BlueNeonColorPalette = lightColorScheme(

    primary = Black, //Warna Utama (Background)
    onPrimary = TealPrimary, //Warna Utama (Komponen)


    // --- Secondary & Tertiary Colors ---
    secondary = DeepTeal, // Background for date and menu cards
    onSecondary = TealPrimary, // Text and label on cards,

    // --- Background & Surface Colors ---
    background = Black, // Main screen background
    onBackground = DarkBlueTeal, // Main background card

    surface = DeepTeal, // Background for menu buttons
    onSurface = BlackTeal, // Icon Background
    surfaceVariant = TealPrimary, // For Icon
    onSurfaceVariant = White, // Text dan label

    // --- Special Colors ---
    outline = TealPrimary, // Main border color
    surfaceContainer = BackgroundDarkGrey, // Background for input fields adn Main text
    surfaceBright = TextGrey, // Background for input fields adn Main text
)

private val YellowNeonColorPalette = lightColorScheme(
    primary = Black,
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
val TeladanPrimaAgroShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun TeladanPrimaAgroTheme(
    isYellowNeonTheme: Boolean = false, // Tambahkan parameter untuk memilih tema
    content: @Composable () -> Unit
) {
    val colorScheme = if (isYellowNeonTheme) YellowNeonColorPalette else BlueNeonColorPalette
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false // Setel ke false agar ikon status bar terang
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = TeladanPrimaAgroShapes,
        content = content
    )
}