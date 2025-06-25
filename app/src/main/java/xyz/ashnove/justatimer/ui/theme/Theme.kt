package xyz.ashnove.justatimer.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val RoseColorScheme = lightColorScheme(
    primary = RoseRed,
    onPrimary = White,
    background = RoseRed,
    surface = TransparentWhite,
    onSurface = White,
    primaryContainer = DarkBrown,
    onPrimaryContainer = White,
    errorContainer = RoseRed.copy(alpha = 0.5f),
    onErrorContainer = White
)

private val BlackColorScheme = lightColorScheme(
    primary = PremiumWhiteText,
    onPrimary = PremiumBlack,
    background = PremiumBlack,
    surface = PremiumBlackSurface,
    onSurface = PremiumWhiteText,
    primaryContainer = PremiumGray,
    onPrimaryContainer = PremiumWhiteText,
    errorContainer = PremiumBlackSurface.copy(alpha = 0.5f),
    onErrorContainer = PremiumWhiteText
)

private val WhiteColorScheme = lightColorScheme(
    primary = PremiumDarkText,
    onPrimary = PremiumLightBg,
    background = PremiumLightBg,
    surface = PremiumLightSurface,
    onSurface = PremiumDarkText,
    primaryContainer = PremiumLightGray,
    onPrimaryContainer = PremiumDarkText,
    errorContainer = PremiumLightGray.copy(alpha = 0.5f),
    onErrorContainer = PremiumDarkText
)

@Composable
fun JustATimerTheme(
    theme: String = "Black",
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        "Black" -> BlackColorScheme
        "White" -> WhiteColorScheme
        else -> RoseColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = theme == "White"
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
} 