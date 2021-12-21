package com.anafthdev.musicompose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalTextApi::class)
@Composable
fun dMSansFontFamily() = FontFamily(
	Font(LocalContext.current.assets, "dm_sans_regular.ttf")
)

@OptIn(ExperimentalTextApi::class)
@Composable
fun sKModernistFontFamily() = FontFamily(
	Font(LocalContext.current.assets, "sk_modernist_regular.otf")
)

@Composable
fun typographyDmSans() = Typography(
	body1 = TextStyle(
		fontFamily = dMSansFontFamily(),
		fontWeight = FontWeight.Normal,
		fontSize = 16.sp,
		color = if (isSystemInDarkTheme()) white else black,
	)
)

@Composable
fun typographySkModernist() = Typography(
	body1 = TextStyle(
		fontFamily = sKModernistFontFamily(),
		fontWeight = FontWeight.Normal,
		fontSize = 16.sp,
		color = if (isSystemInDarkTheme()) white else black,
	)
)
