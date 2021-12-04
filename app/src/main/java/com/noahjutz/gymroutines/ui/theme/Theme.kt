package com.noahjutz.gymroutines.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.noahjutz.gymroutines.data.ColorTheme

private val WhiteColorPalette = lightColors(
    primary = Primary,
    primaryVariant = PrimaryDark,
    secondary = Secondary,
    secondaryVariant = SecondaryDark,
    onPrimary = Color.White,
    onSecondary = Color.Black,
)

private val BlackColorPalette = darkColors(
    primary = PrimaryDesaturated,
    primaryVariant = PrimaryDark,
    secondary = Secondary,
    secondaryVariant = Secondary,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
)

@Composable
fun GymRoutinesTheme(
    colors: ColorTheme,
    content: @Composable () -> Unit,
) {
    val colorTheme = when (colors) {
        ColorTheme.FollowSystem -> if (isSystemInDarkTheme()) BlackColorPalette else WhiteColorPalette
        ColorTheme.White -> WhiteColorPalette
        ColorTheme.Black -> BlackColorPalette
    }
    MaterialTheme(
        colors = colorTheme,
        content = content
    )
}
