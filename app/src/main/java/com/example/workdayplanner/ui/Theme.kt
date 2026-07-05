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
    onPrimary = Color.White,
    primaryContainer = accentStyle.containerLight,
    onPrimaryContainer = accentStyle.primaryDark,
    secondary = Color(0xFF607D3B),
    tertiary = Color(0xFFE6B94D),
    background = Color(0xFFF5F7FB),
    surface = Color.White,
    surfaceVariant = Color(0xFFE7ECF4),
    onSurface = Color(0xFF1B1F24)
)

private fun darkColors(accentStyle: AccentStyle) = darkColorScheme(
    primary = accentStyle.primaryDark,
    onPrimary = Color(0xFF071522),
    primaryContainer = accentStyle.containerDark,
    onPrimaryContainer = Color(0xFFEAF2FF),
    secondary = Color(0xFFBBD68A),
    tertiary = Color(0xFFEFD17A),
    background = Color(0xFF0E1217),
    surface = Color(0xFF171C22),
    surfaceVariant = Color(0xFF2A313A),
    onSurface = Color(0xFFE8EDF3)
)

private val AccentStyle.primaryLight: Color
    get() = when (this) {
        AccentStyle.Classic -> Color(0xFF1E5AA8)
        AccentStyle.Emerald -> Color(0xFF00796B)
        AccentStyle.Sunrise -> Color(0xFFB26A00)
    }

private val AccentStyle.primaryDark: Color
    get() = when (this) {
        AccentStyle.Classic -> Color(0xFF8DBDFF)
        AccentStyle.Emerald -> Color(0xFF80CBC4)
        AccentStyle.Sunrise -> Color(0xFFFFC477)
    }

private val AccentStyle.containerLight: Color
    get() = when (this) {
        AccentStyle.Classic -> Color(0xFFDCEAFF)
        AccentStyle.Emerald -> Color(0xFFD7F2EE)
        AccentStyle.Sunrise -> Color(0xFFFFE4C2)
    }

private val AccentStyle.containerDark: Color
    get() = when (this) {
        AccentStyle.Classic -> Color(0xFF173457)
        AccentStyle.Emerald -> Color(0xFF113D39)
        AccentStyle.Sunrise -> Color(0xFF4A2B13)
    }
