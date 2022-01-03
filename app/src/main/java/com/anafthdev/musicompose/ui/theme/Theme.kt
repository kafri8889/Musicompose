package com.anafthdev.musicompose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val LightColorPalette = lightColors(
	primary = primary_light,
	primaryVariant = primary_variant_light,
	onPrimary = on_primary_light,

	secondary = secondary_light,
	secondaryVariant = secondary_variant_light,
	onSecondary = on_secondary_light,

	surface = surface_light,
	onSurface = on_surface_light,

	background = background_light,
	onBackground = on_background_light,

	error = error_light,
	onError = on_error_light
)

private val DarkColorPalette = lightColors(
	primary = primary_dark,
	primaryVariant = primary_variant_dark,
	onPrimary = on_primary_dark,

	secondary = secondary_dark,
	onSecondary = on_secondary_dark,

	surface = surface_dark,
	onSurface = on_surface_dark,

	background = background_dark,
	onBackground = on_background_dark,

	error = error_dark,
	onError = on_error_dark
)

@Composable
fun MusicomposeTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit
) {
	MaterialTheme(
		colors = if (darkTheme) DarkColorPalette else LightColorPalette,
		typography = typographyDmSans(),
		shapes = Shapes,
		content = content
	)
}