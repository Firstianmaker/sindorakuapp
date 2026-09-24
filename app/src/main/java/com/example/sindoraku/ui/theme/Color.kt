package com.example.sindoraku.ui.theme

import androidx.compose.ui.graphics.Color

// —— DESIGN.md brand palette ——

/** Midnight violet — primary CTA on light surfaces */
val Primary = Color(0xFF150F23)
val PrimaryPressed = Color(0xFFEFEFEF)
val OnPrimary = Color(0xFFFFFFFF)

/** Ink violet — hero canvas & body text on light */
val InkDeep = Color(0xFF1F1633)
val Ink = Color(0xFF1F1633)
val InkPress = Color(0xFF1A1A1A)

val AccentLime = Color(0xFFC2EF4E)
val AccentPink = Color(0xFFFA7FAA)
val AccentViolet = Color(0xFF6A5FC1)
val AccentVioletDeep = Color(0xFF422082)
val AccentVioletMid = Color(0xFF79628C)

// Surfaces — two-polarity canvas system
val CanvasDark = Color(0xFF1F1633)
val SurfaceNight = Color(0xFF150F23)
val Canvas = Color(0xFFFFFFFF)
val Surface = Color(0xFFF6F5F8)
val SurfaceSoft = Color(0xFFFAFAFC)
val SurfaceCard = Color(0xFFFFFFFF)
val SurfacePressLight = Color(0xFFF0F0F0)
val SurfacePressStronger = Color(0xFFEFEFEF)

val HairlineViolet = Color(0xFF362D59)
val HairlineCool = Color(0xFFCFCFDB)
val Hairline = Color(0xFFE5E7EB)
val HairlineSoft = Color(0xFFEDE9F0)

// Text
val Charcoal = Color(0xFF2A2240)
val Slate = Color(0xFF5C5470)
val Steel = Color(0xFF78708A)
val Stone = Color(0xFF9B94A8)
val Muted = Color(0xFFBDB8C0)

val OnDark = Color(0xFFFFFFFF)
val OnDarkMuted = Color(0xFFBDB8C0)
val OnDarkFaint = Color(0xFF3F3849)

val RingFocus = Color(0x809DC1F5)

// Semantic
val SemanticSuccess = Color(0xFF4ADE80)
val SemanticWarning = Color(0xFFFBBF24)
val SemanticError = Color(0xFFF87171)

// Card tints — harmonized with violet family (WordList / WordDetail compatibility)
val CardTintPeach = Color(0xFFF3EEF8)
val CardTintRose = Color(0xFFF5E8F0)
val CardTintMint = Color(0xFFE8F5EE)
val CardTintLavender = Color(0xFFE8E4F5)
val CardTintSky = Color(0xFFE4ECFA)
val CardTintYellow = Color(0xFFF5F9E4)
val CardTintYellowBold = Color(0xFFE8F4C8)
val CardTintCream = Color(0xFFF8F6FC)
val CardTintGray = Color(0xFFF0EEF4)

// Legacy aliases used across the app
val BrandNavy = CanvasDark
val BrandNavyDeep = SurfaceNight
val BrandNavyMid = InkDeep
val BrandPurple = AccentViolet
val BrandPurpleDark = AccentVioletDeep
val BrandPink = AccentPink
val BrandOrange = AccentVioletMid
val BrandTeal = AccentViolet
val BrandGreen = SemanticSuccess
val LinkBlue = AccentViolet
val LinkBluePressed = AccentVioletDeep

val Body = Charcoal
val BodyStrong = Ink
val MutedSoft = Stone
val Link = AccentViolet
val Warning = SemanticWarning
val Success = SemanticSuccess
val SurfaceElevated = SurfaceCard
val Background = Canvas
val Secondary = Slate
val Tertiary = Steel
val Outline = Hairline
val OnBackground = Ink
val PrimaryContainer = CardTintLavender
val OnPrimaryLegacy = OnPrimary
val HairlineStrong = HairlineCool
