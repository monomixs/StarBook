package com.starbook.core.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.DynamicMaterialExpressiveTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.starbook.core.data.ThemeColorScheme
import com.starbook.core.data.ThemeMode

val StarBookBlue = Color(0xFF003b7f)

val CrimsonPrimary = Color(0xFFDC2626)
val CrimsonSecondary = Color(0xFFFEE2E2)

val MidnightPrimary = Color(0xFF334155)
val MidnightSecondary = Color(0xFFE2E8F0)

val GoldenPrimary = Color(0xFFF59E0B)
val GoldenSecondary = Color(0xFFFEF3C7)

val CyanPrimary = Color(0xFF06B6D4)
val CyanSecondary = Color(0xFFCFFAFE)

val LimePrimary = Color(0xFF65A30D)
val LimeSecondary = Color(0xFFECFCCB)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StarBookTheme(
  themeMode: ThemeMode = ThemeMode.FollowSystem,
  themeColorScheme: ThemeColorScheme = ThemeColorScheme.StarBookBlue,
  content: @Composable () -> Unit,
) {
  val darkTheme = when (themeMode) {
    ThemeMode.FollowSystem -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
  }
  val themedContent = remember(content) {
    movableContentOf {
      content()
    }
  }

  when {
    themeColorScheme == ThemeColorScheme.Dynamic && Build.VERSION.SDK_INT >= 31 -> {
      MaterialExpressiveTheme(
        colorScheme = systemDynamicColorScheme(darkTheme),
      ) {
        themedContent()
      }
    }
    else -> {
      val (primary, secondary) = when (themeColorScheme) {
        ThemeColorScheme.StarBookBlue -> StarBookBlue to Color(0xFF5E6F95)
        ThemeColorScheme.Crimson -> CrimsonPrimary to CrimsonSecondary
        ThemeColorScheme.Midnight -> MidnightPrimary to MidnightSecondary
        ThemeColorScheme.Golden -> GoldenPrimary to GoldenSecondary
        ThemeColorScheme.Cyan -> CyanPrimary to CyanSecondary
        ThemeColorScheme.Lime -> LimePrimary to LimeSecondary
        ThemeColorScheme.Dynamic -> StarBookBlue to Color(0xFF5E6F95)
      }

      DynamicMaterialExpressiveTheme(
        primary = primary,
        secondary = secondary,
        isDark = darkTheme,
        style = PaletteStyle.Expressive,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
      ) {
        themedContent()
      }
    }
  }
}

@RequiresApi(31)
@Composable
private fun systemDynamicColorScheme(darkTheme: Boolean): ColorScheme {
  return if (darkTheme) {
    dynamicDarkColorScheme(LocalContext.current)
  } else {
    dynamicLightColorScheme(LocalContext.current)
  }
}

