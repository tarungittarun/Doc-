package app.onedoc.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.onedoc.R

/**
 * OneDoc design system theme (§6: tokens/themes).
 * All colors, typography, and shapes come from tokens — no hardcoded literals in UI.
 */
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    primaryContainer = Color(0xFF1E4E8C),
    secondary = Color(0xFF81C784),
    surface = Color(0xFF121212),
    surfaceContainer = Color(0xFF1E1E1E),
    background = Color(0xFF090909),
    error = Color(0xFFEF5350),
    onPrimary = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    onBackground = Color(0xFFFFFFFF),
    outline = Color(0xFF333333),
)

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    primaryContainer = Color(0xFFE3F2FD),
    secondary = Color(0xFF4CAF50),
    surface = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFF5F5F5),
    background = Color(0xFFFAFAFA),
    error = Color(0xFFD32F2F),
    onPrimary = Color(0xFFFFFFFF),
    onSurface = Color(0xFF212121),
    onBackground = Color(0xFF212121),
    outline = Color(0xFFE0E0E0),
)

@Composable
fun OneDocTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

val Typography = Typography(
    displayLarge = TextStyle(fontSize = 56.sp),
    displayMedium = TextStyle(fontSize = 44.sp),
    displaySmall = TextStyle(fontSize = 36.sp),
    headlineLarge = TextStyle(fontSize = 32.sp),
    headlineMedium = TextStyle(fontSize = 28.sp),
    headlineSmall = TextStyle(fontSize = 24.sp),
    titleLarge = TextStyle(fontSize = 22.sp),
    titleMedium = TextStyle(fontSize = 18.sp),
    titleSmall = TextStyle(fontSize = 14.sp),
    bodyLarge = TextStyle(fontSize = 18.sp),
    bodyMedium = TextStyle(fontSize = 16.sp),
    bodySmall = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontSize = 16.sp),
    labelMedium = TextStyle(fontSize = 14.sp),
    labelSmall = TextStyle(fontSize = 12.sp),
)


