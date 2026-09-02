package com.ravanbarvar.patientmanager.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ravanbarvar.patientmanager.R

val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_light, FontWeight.Light),
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

private fun style(size: Int, weight: FontWeight, lineHeight: Int, letterSpacing: Double = 0.0) = TextStyle(
    fontFamily = Vazirmatn,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp
)

val RavanbarvarTypography = Typography(
    displayLarge = style(52, FontWeight.Bold, 64),
    displayMedium = style(40, FontWeight.Bold, 50),
    displaySmall = style(34, FontWeight.SemiBold, 44),
    headlineLarge = style(30, FontWeight.SemiBold, 38),
    headlineMedium = style(26, FontWeight.SemiBold, 34),
    headlineSmall = style(22, FontWeight.SemiBold, 30),
    titleLarge = style(20, FontWeight.SemiBold, 28),
    titleMedium = style(17, FontWeight.Medium, 24),
    titleSmall = style(15, FontWeight.Medium, 22),
    bodyLarge = style(16, FontWeight.Normal, 26),
    bodyMedium = style(14, FontWeight.Normal, 22),
    bodySmall = style(12, FontWeight.Normal, 18),
    labelLarge = style(14, FontWeight.Medium, 20),
    labelMedium = style(12, FontWeight.Medium, 18),
    labelSmall = style(11, FontWeight.Medium, 16)
)
