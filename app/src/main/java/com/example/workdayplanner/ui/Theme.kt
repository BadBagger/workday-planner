package com.example.workdayplanner.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.workdayplanner.data.AccentStyle
import com.example.workdayplanner.data.AppThemeStyle

@Composable
fun WorkdayPlannerTheme(
    darkMode: Boolean = false,
    accentStyle: AccentStyle = AccentStyle.Default,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = workdayColorScheme(accentStyle, darkMode),
        typography = MaterialTheme.typography,
        content = content
    )
}

fun workdayColorScheme(style: AppThemeStyle, darkMode: Boolean): ColorScheme {
    val p = if (darkMode) style.darkPalette else style.lightPalette
    return if (darkMode) {
        darkColorScheme(
            background = p.background,
            surface = p.surface,
            surfaceVariant = p.surfaceVariant,
            primary = p.primary,
            primaryContainer = p.primaryContainer,
            onPrimary = p.onPrimary,
            onPrimaryContainer = p.onPrimaryContainer,
            secondary = p.secondary,
            secondaryContainer = p.secondaryContainer,
            onSecondary = p.onSecondary,
            onSecondaryContainer = p.onSecondaryContainer,
            tertiary = p.tertiary,
            error = p.error,
            onError = p.onError,
            outline = p.outline,
            onBackground = p.onBackground,
            onSurface = p.onSurface,
            onSurfaceVariant = p.onSurfaceVariant
        )
    } else {
        lightColorScheme(
            background = p.background,
            surface = p.surface,
            surfaceVariant = p.surfaceVariant,
            primary = p.primary,
            primaryContainer = p.primaryContainer,
            onPrimary = p.onPrimary,
            onPrimaryContainer = p.onPrimaryContainer,
            secondary = p.secondary,
            secondaryContainer = p.secondaryContainer,
            onSecondary = p.onSecondary,
            onSecondaryContainer = p.onSecondaryContainer,
            tertiary = p.tertiary,
            error = p.error,
            onError = p.onError,
            outline = p.outline,
            onBackground = p.onBackground,
            onSurface = p.onSurface,
            onSurfaceVariant = p.onSurfaceVariant
        )
    }
}

private data class WorkdayPalette(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val tertiary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val error: Color = Color.Unspecified,
    val onError: Color = Color.Unspecified,
    val onPrimary: Color = Color.Unspecified,
    val onPrimaryContainer: Color = Color.Unspecified,
    val onSecondary: Color = Color.Unspecified,
    val onSecondaryContainer: Color = Color.Unspecified
)

private val AppThemeStyle.lightPalette: WorkdayPalette
    get() = when (this) {
        AppThemeStyle.Default, AppThemeStyle.GraphitePro -> WorkdayPalette(
            background = Color(0xFFF7F4EF),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFEEE7DD),
            primary = Color(0xFFC05621),
            primaryContainer = Color(0xFFFFE2C2),
            secondary = Color(0xFF3F4E46),
            secondaryContainer = Color(0xFFDDE8DF),
            tertiary = Color(0xFF6B5A45),
            onBackground = Color(0xFF1F1F1F),
            onSurface = Color(0xFF1F1F1F),
            onSurfaceVariant = Color(0xFF5F5A54),
            outline = Color(0xFFD8D0C5)
        )
        AppThemeStyle.NightShift -> WorkdayPalette(
            background = Color(0xFFF5F7FA), surface = Color.White, surfaceVariant = Color(0xFFE5E7EB),
            primary = Color(0xFF334155), primaryContainer = Color(0xFFE2E8F0), secondary = Color(0xFF64748B),
            secondaryContainer = Color(0xFFE5E7EB), tertiary = Color(0xFFF59E0B), onBackground = Color(0xFF111827),
            onSurface = Color(0xFF111827), onSurfaceVariant = Color(0xFF4B5563), outline = Color(0xFFCBD5E1)
        )
        AppThemeStyle.PayrollGreen -> WorkdayPalette(
            background = Color(0xFFF4F7F2), surface = Color.White, surfaceVariant = Color(0xFFE5EDE2),
            primary = Color(0xFF2F6F4E), primaryContainer = Color(0xFFD7F2DF), secondary = Color(0xFF596B4E),
            secondaryContainer = Color(0xFFE1EADA), tertiary = Color(0xFFD9A441), onBackground = Color(0xFF1E241F),
            onSurface = Color(0xFF1E241F), onSurfaceVariant = Color(0xFF59635A), outline = Color(0xFFCBD8C8)
        )
        AppThemeStyle.SteelBlueCollar -> WorkdayPalette(
            background = Color(0xFFF3F4F1), surface = Color.White, surfaceVariant = Color(0xFFE4E8E7),
            primary = Color(0xFF465A64), primaryContainer = Color(0xFFD7E4EA), secondary = Color(0xFF7A5C3E),
            secondaryContainer = Color(0xFFE7DACB), tertiary = Color(0xFFD17A22), onBackground = Color(0xFF202426),
            onSurface = Color(0xFF202426), onSurfaceVariant = Color(0xFF5A6266), outline = Color(0xFFC8D0D2)
        )
        AppThemeStyle.MinimalInk -> WorkdayPalette(
            background = Color(0xFFFAFAFA), surface = Color.White, surfaceVariant = Color(0xFFF0F0F0),
            primary = Color(0xFF111827), primaryContainer = Color(0xFFE5E7EB), secondary = Color(0xFF6B7280),
            secondaryContainer = Color(0xFFE5E7EB), tertiary = Color(0xFFC05621), onBackground = Color(0xFF111827),
            onSurface = Color(0xFF111827), onSurfaceVariant = Color(0xFF525252), outline = Color(0xFFD4D4D4)
        )
        AppThemeStyle.SunriseShift -> WorkdayPalette(
            background = Color(0xFFFFF8ED), surface = Color.White, surfaceVariant = Color(0xFFF5E6D0),
            primary = Color(0xFFD97706), primaryContainer = Color(0xFFFFE4B5), secondary = Color(0xFF7C5C2E),
            secondaryContainer = Color(0xFFEBDCC2), tertiary = Color(0xFFFBBF24), onBackground = Color(0xFF2B2118),
            onSurface = Color(0xFF2B2118), onSurfaceVariant = Color(0xFF6B5842), outline = Color(0xFFE0CBAE)
        )
        AppThemeStyle.DeliBoard -> WorkdayPalette(
            background = Color(0xFFFAF4E8), surface = Color.White, surfaceVariant = Color(0xFFF0E1C8),
            primary = Color(0xFFA34716), primaryContainer = Color(0xFFFFD9B8), secondary = Color(0xFF4F5D3A),
            secondaryContainer = Color(0xFFE1E9D3), tertiary = Color(0xFFD6A536), onBackground = Color(0xFF2B2118),
            onSurface = Color(0xFF2B2118), onSurfaceVariant = Color(0xFF665644), outline = Color(0xFFDDC9AA)
        )
        AppThemeStyle.FocusPlum -> WorkdayPalette(
            background = Color(0xFFF8F5FA), surface = Color.White, surfaceVariant = Color(0xFFECE4F0),
            primary = Color(0xFF6D3A7A), primaryContainer = Color(0xFFF0D7F7), secondary = Color(0xFF5A5360),
            secondaryContainer = Color(0xFFE5DDE8), tertiary = Color(0xFFD6863A), onBackground = Color(0xFF231F25),
            onSurface = Color(0xFF231F25), onSurfaceVariant = Color(0xFF625A68), outline = Color(0xFFD7CBDD)
        )
    }.withContentColors(dark = false)

private val AppThemeStyle.darkPalette: WorkdayPalette
    get() = when (this) {
        AppThemeStyle.Default, AppThemeStyle.GraphitePro -> WorkdayPalette(
            background = Color(0xFF121212), surface = Color(0xFF1E1E1E), surfaceVariant = Color(0xFF2A2A2A),
            primary = Color(0xFFF59E5B), primaryContainer = Color(0xFF3A2A20), secondary = Color(0xFF9FCFB1),
            secondaryContainer = Color(0xFF223328), tertiary = Color(0xFFD6C1A3), onBackground = Color(0xFFF5F1EA),
            onSurface = Color(0xFFF5F1EA), onSurfaceVariant = Color(0xFFC8BFB5), outline = Color(0xFF3A3530)
        )
        AppThemeStyle.NightShift -> WorkdayPalette(
            background = Color(0xFF0E1117), surface = Color(0xFF171B24), surfaceVariant = Color(0xFF222938),
            primary = Color(0xFFFFB86B), primaryContainer = Color(0xFF3A2718), secondary = Color(0xFF8AB4F8),
            secondaryContainer = Color(0xFF1D3355), tertiary = Color(0xFFB8F2E6), onBackground = Color(0xFFF4F6FA),
            onSurface = Color(0xFFF4F6FA), onSurfaceVariant = Color(0xFFC8D0DC), outline = Color(0xFF354052)
        )
        AppThemeStyle.PayrollGreen -> WorkdayPalette(
            background = Color(0xFF101510), surface = Color(0xFF1A211A), surfaceVariant = Color(0xFF243024),
            primary = Color(0xFF82D69D), primaryContainer = Color(0xFF193B26), secondary = Color(0xFFA8C49A),
            secondaryContainer = Color(0xFF293827), tertiary = Color(0xFFF0C45C), onBackground = Color(0xFFF1F5EF),
            onSurface = Color(0xFFF1F5EF), onSurfaceVariant = Color(0xFFC8D4C3), outline = Color(0xFF354235)
        )
        AppThemeStyle.SteelBlueCollar -> WorkdayPalette(
            background = Color(0xFF101314), surface = Color(0xFF1B2022), surfaceVariant = Color(0xFF263033),
            primary = Color(0xFF9CB4C0), primaryContainer = Color(0xFF263942), secondary = Color(0xFFC49A6C),
            secondaryContainer = Color(0xFF3A2C20), tertiary = Color(0xFFF29A3F), onBackground = Color(0xFFF2F4F4),
            onSurface = Color(0xFFF2F4F4), onSurfaceVariant = Color(0xFFC6CED1), outline = Color(0xFF3A464A)
        )
        AppThemeStyle.MinimalInk -> WorkdayPalette(
            background = Color(0xFF090909), surface = Color(0xFF181818), surfaceVariant = Color(0xFF262626),
            primary = Color(0xFFF5F5F5), primaryContainer = Color(0xFF303030), secondary = Color(0xFFA3A3A3),
            secondaryContainer = Color(0xFF2C2C2C), tertiary = Color(0xFFF59E5B), onBackground = Color(0xFFF5F5F5),
            onSurface = Color(0xFFF5F5F5), onSurfaceVariant = Color(0xFFC4C4C4), outline = Color(0xFF3A3A3A)
        )
        AppThemeStyle.SunriseShift -> WorkdayPalette(
            background = Color(0xFF18110A), surface = Color(0xFF241A10), surfaceVariant = Color(0xFF332415),
            primary = Color(0xFFFDBA74), primaryContainer = Color(0xFF4A2D12), secondary = Color(0xFFD6B47A),
            secondaryContainer = Color(0xFF3D2B15), tertiary = Color(0xFFFACC15), onBackground = Color(0xFFFFF7ED),
            onSurface = Color(0xFFFFF7ED), onSurfaceVariant = Color(0xFFE8D5BD), outline = Color(0xFF4A3A27)
        )
        AppThemeStyle.DeliBoard -> WorkdayPalette(
            background = Color(0xFF17120E), surface = Color(0xFF241B14), surfaceVariant = Color(0xFF332619),
            primary = Color(0xFFFF9B52), primaryContainer = Color(0xFF4A2612), secondary = Color(0xFFA8BE78),
            secondaryContainer = Color(0xFF2E3A1F), tertiary = Color(0xFFFFD166), onBackground = Color(0xFFFFF1E2),
            onSurface = Color(0xFFFFF1E2), onSurfaceVariant = Color(0xFFE7D0B8), outline = Color(0xFF4A3928)
        )
        AppThemeStyle.FocusPlum -> WorkdayPalette(
            background = Color(0xFF121015), surface = Color(0xFF1E1924), surfaceVariant = Color(0xFF2B2233),
            primary = Color(0xFFD9A7E8), primaryContainer = Color(0xFF3D2448), secondary = Color(0xFFC6BBCD),
            secondaryContainer = Color(0xFF332B3A), tertiary = Color(0xFFF0A45D), onBackground = Color(0xFFF7F1FA),
            onSurface = Color(0xFFF7F1FA), onSurfaceVariant = Color(0xFFD6C9DE), outline = Color(0xFF46394F)
        )
    }.withContentColors(dark = true)

private fun WorkdayPalette.withContentColors(dark: Boolean): WorkdayPalette = copy(
    onPrimary = if (dark) Color(0xFF1B120B) else Color.White,
    onPrimaryContainer = if (dark) Color(0xFFFFE2C2) else Color(0xFF351506),
    onSecondary = if (dark) Color(0xFF102018) else Color.White,
    onSecondaryContainer = if (dark) Color(0xFFDDE8DF) else Color(0xFF102018),
    error = if (dark) Color(0xFFFF7A70) else Color(0xFFC2413A),
    onError = if (dark) Color(0xFF2B0706) else Color.White
)

private val ColorScheme.isDark: Boolean
    get() = background.luminance() < 0.35f

val ColorScheme.warning: Color
    @Composable get() = if (isDark) Color(0xFFFFC46B) else Color(0xFF9A5B00)

val ColorScheme.warningContainer: Color
    @Composable get() = if (isDark) Color(0xFF3A2E18) else Color(0xFFFFE8B5)

val ColorScheme.onWarningContainer: Color
    @Composable get() = if (isDark) Color(0xFFFFE8B5) else Color(0xFF2C1A00)

val ColorScheme.success: Color
    @Composable get() = if (isDark) Color(0xFF7ED99A) else Color(0xFF2E7D47)

val ColorScheme.successContainer: Color
    @Composable get() = if (isDark) Color(0xFF1F3326) else Color(0xFFDDF3E3)

val ColorScheme.onSuccessContainer: Color
    @Composable get() = if (isDark) Color(0xFFDDF3E3) else Color(0xFF0D2614)

val ColorScheme.dangerContainer: Color
    @Composable get() = if (isDark) Color(0xFF3A1D1B) else Color(0xFFFFDAD6)

val ColorScheme.onDangerContainer: Color
    @Composable get() = if (isDark) Color(0xFFFFDAD6) else Color(0xFF410002)

val ColorScheme.infoContainer: Color
    @Composable get() = if (isDark) Color(0xFF1F2F3D) else Color(0xFFDDEAF4)

val ColorScheme.onInfoContainer: Color
    @Composable get() = if (isDark) Color(0xFFDDEAF4) else Color(0xFF112534)
