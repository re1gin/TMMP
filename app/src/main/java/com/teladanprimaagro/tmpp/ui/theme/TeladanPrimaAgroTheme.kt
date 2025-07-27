@file:Suppress("DEPRECATION")

package com.teladanprimaagro.tmpp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// Skema Warna
private val DarkColorPalette = darkColorScheme(
    primary = PrimaryOrange,
    onPrimary = TextWhite,
    primaryContainer = PrimaryDarkOrange,
    onPrimaryContainer = TextWhite,

    secondary = SecondaryGray,
    onSecondary = TextWhite,

    background = BackgroundBlack,
    onBackground = TextWhite,

    surface = BackgroundDarkGray,
    onSurface = TextWhite,

    error = Color.Red,
    onError = TextWhite,

    outline = PrimaryOrange
)

private val LightColorPalette = lightColorScheme(
    primary = PrimaryOrange,
    onPrimary = TextWhite,
    primaryContainer = PrimaryDarkOrange,
    onPrimaryContainer = TextWhite,

    secondary = SecondaryGray,
    onSecondary = TextWhite,

    background = Color.White,
    onBackground = Color.Black,

    surface = Color.LightGray,
    onSurface = Color.Black,

    error = Color.Red,
    onError = TextWhite,

    outline = PrimaryOrange
)

// Tipografi (Gaya Teks)
val TeladanPrimaAgroTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Bentuk (Shapes)
val TeladanPrimaAgroShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun TeladanPrimaAgroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorPalette
        else -> LightColorPalette
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TeladanPrimaAgroTypography,
        shapes = TeladanPrimaAgroShapes,
        content = content
    )
}