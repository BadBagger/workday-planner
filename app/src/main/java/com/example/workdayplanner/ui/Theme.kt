package com.example.workdayplanner.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.workdayplanner.data.AccentStyle

@Composable
fun WorkdayPlannerTheme(
    darkMode: Boolean = false,
    accentStyle: AccentStyle = AccentStyle.Classic,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkMode) darkColors(accentStyle) else lightColors(accentStyle),
        typography = MaterialTheme.typography,
        content = content
    )
}

private fun lightColors(accentStyle: AccentStyle) = lightColorScheme(
    primary = accentStyle.primaryLight,
    onPrimary = accentStyle.onPrimaryLight,
    primaryContainer = accentStyle.containerLight,
    onPrimaryContainer = accentStyle.onPrimaryContainerLight,
    secondary = accentStyle.secondaryLight,
    tertiary = accentStyle.tertiaryLight,
    background = accentStyle.backgroundLight,
    surface = accentStyle.surfaceLight,
    surfaceVariant = accentStyle.surfaceVariantLight,
    onSurface = accentStyle.onSurfaceLight
)

private fun darkColors(accentStyle: AccentStyle) = darkColorScheme(
    primary = accentStyle.primaryDark,
    onPrimary = accentStyle.onPrimaryDark,
    primaryContainer = accentStyle.containerDark,
    onPrimaryContainer = accentStyle.onPrimaryContainerDark,
    secondary = accentStyle.secondaryDark,
    tertiary = accentStyle.tertiaryDark,
    background = accentStyle.backgroundDark,
    surface = accentStyle.surfaceDark,
    surfaceVariant = accentStyle.surfaceVariantDark,
    onSurface = accentStyle.onSurfaceDark
)

private val AccentStyle.primaryLight: Color
    get() = when (this) {
        AccentStyle.Classic -> Color(0xFF1E5AA8)
        AccentStyle.Emerald -> Color(0xFF00796B)
        AccentStyle.Sunrise -> Color(0xFFB26A00)
        AccentStyle.Logo -> Color(0xFF008C92)
    }

private val AccentStyle.primaryDark: Color
    get() = when (this) {
        AccentStyle.Classic -> Color(0xFF8DBDFF)
        AccentStyle.Emerald -> Color(0xFF80CBC4)
        AccentStyle.Sunrise -> Color(0xFFFFC477)
        AccentStyle.Logo -> Color(0xFF35E0D4)
    }

private val AccentStyle.containerLight: Color
    get() = when (this) {
        AccentStyle.Classic -> Color(0xFFDCEAFF)
        AccentStyle.Emerald -> Color(0xFFD7F2EE)
        AccentStyle.Sunrise -> Color(0xFFFFE4C2)
        AccentStyle.Logo -> Color(0xFFD9F7F3)
    }

private val AccentStyle.containerDark: Color
    get() = when (this) {
        AccentStyle.Classic -> Color(0xFF173457)
        AccentStyle.Emerald -> Color(0xFF113D39)
        AccentStyle.Sunrise -> Color(0xFF4A2B13)
        AccentStyle.Logo -> Color(0xFF063E44)
    }

private val AccentStyle.onPrimaryLight: Color
    get() = when (this) {
        AccentStyle.Logo -> Color(0xFF001F22)
        else -> Color.White
    }

private val AccentStyle.onPrimaryDark: Color
    get() = when (this) {
        AccentStyle.Logo -> Color(0xFF002325)
        else -> Color(0xFF071522)
    }

private val AccentStyle.onPrimaryContainerLight: Color
    get() = when (this) {
        AccentStyle.Logo -> Color(0xFF00373A)
        else -> primaryDark
    }

private val AccentStyle.onPrimaryContainerDark: Color
    get() = when (this) {
        AccentStyle.Logo -> Color(0xFFD9FFFA)
        else -> Color(0xFFEAF2FF)
    }

private val AccentStyle.secondaryLight: Color
    get() = when (this) {
        AccentStyle.Logo -> Color(0xFF6E9500)
        else -> Color(0xFF607D3B)
    }

private val AccentStyle.secondaryDark: Color
    get() = when (this) {
        AccentStyle.Logo -> Color(0xFFB8EA32)
        else -> Color(0xFFBBD68A)
    }

private val AccentStyle.tertiaryLight: Color
    get() = when (this) {
        AccentStyle.Logo -> Color(0xFF007C84)
        else -> Color(0xFFE6B94D)
    }

private val AccentStyle.tertiaryDark: Color
    get() = when (this) {
        AccentStyle.Logo -> Color(0xFF97F5EC)
        else -> Color(0xFFEFD17A)
    }

private val AccentStyle.backgroundLight: Color
    get() = when (this) {
        AccentStyle.Logo -> Color(0xFFF1FAFA)
        else -> Color(0xFFF5F7FB)
    }

private val AccentStyle.backgroundDark: Color
    get() = when (this) {
        AccentStyle.Logo -> Color(0xFF070B16)
        else -> Color(0xFF0E1217)
    }

private val AccentStyle.surfaceLight: Color
    get() = when (this) {
        AccentStyle.Logo -> Color.White
        else -> Color.White
    }

private val AccentStyle.surfaceDark: Color
    get() = when (this) {
        AccentStyle.Logo -> Color(0xFF101827)
        else -> Color(0xFF171C22)
    }

private val AccentStyle.surfaceVariantLight: Color
    get() = when (this) {
        AccentStyle.Logo -> Color(0xFFD7EBED)
        else -> Color(0xFFE7ECF4)
    }

private val AccentStyle.surfaceVariantDark: Color
    get() = when (this) {
        AccentStyle.Logo -> Color(0xFF1A2A34)
        else -> Color(0xFF2A313A)
    }

private val AccentStyle.onSurfaceLight: Color
    get() = when (this) {
        AccentStyle.Logo -> Color(0xFF101923)
        else -> Color(0xFF1B1F24)
    }

private val AccentStyle.onSurfaceDark: Color
    get() = when (this) {
        AccentStyle.Logo -> Color(0xFFEAF7F7)
        else -> Color(0xFFE8EDF3)
    }
