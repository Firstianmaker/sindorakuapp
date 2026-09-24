package com.example.sindoraku.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import com.example.sindoraku.ui.theme.*

/** Top inset for edge-to-edge layouts (status bar / notch). */
fun Modifier.sindorakuStatusBarPadding(): Modifier = statusBarsPadding()

@Composable
fun SindorakuWordmark(
    modifier: Modifier = Modifier,
    text: String = "Sindoraku",
    onDark: Boolean = false,
    showBadge: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTokens.Space.sm)
    ) {
        if (showBadge) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(AppTokens.Radius.sm))
                    .background(if (onDark) AccentLime else Primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "S",
                    style = SindorakuTextStyles.ButtonCap,
                    color = if (onDark) InkDeep else OnPrimary
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = if (onDark) OnDark else Ink
        )
    }
}

@Composable
fun SindorakuEyebrow(
    text: String,
    modifier: Modifier = Modifier,
    onDark: Boolean = false
) {
    Text(
        text = text.uppercase(),
        style = SindorakuTextStyles.Eyebrow,
        color = if (onDark) AccentLime else AccentVioletMid,
        modifier = modifier
    )
}

@Composable
fun SindorakuCaption(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Steel,
    onDark: Boolean = false
) {
    Text(
        text = text,
        style = SindorakuTextStyles.Caption,
        color = if (onDark) OnDarkMuted else color,
        modifier = modifier,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun SindorakuSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onDark: Boolean = false,
    highlightWord: String? = null
) {
    Column(modifier = modifier) {
        if (highlightWord != null && title.contains(highlightWord, ignoreCase = true)) {
            val parts = title.split(highlightWord, ignoreCase = true, limit = 2)
            val annotated = buildAnnotatedString {
                append(parts[0])
                withStyle(SpanStyle(color = Surface)) {
                    append(highlightWord)
                }
                if (parts.size > 1) append(parts[1])
            }
            Text(
                text = annotated,
                style = SindorakuTextStyles.DisplayHero,
                color = if (onDark) OnDark else Ink
            )
        } else {
            Text(
                text = title,
                style = SindorakuTextStyles.DisplayHero,
                color = if (onDark) OnDark else Ink
            )
        }
        if (subtitle != null) {
            Spacer(Modifier.height(AppTokens.Space.sm))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (onDark) OnDarkMuted else Slate
            )
        }
    }
}

@Composable
fun SindorakuHairlineDivider(
    modifier: Modifier = Modifier,
    onDark: Boolean = false
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = if (onDark) HairlineViolet else Hairline
    )
}

@Composable
fun SindorakuSurfaceCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = SurfaceCard,
    onClick: (() -> Unit)? = null,
    onDark: Boolean = false,
    elevated: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(AppTokens.Radius.xl)
    val borderColor = if (onDark) HairlineViolet else Hairline
    val baseModifier = modifier
        .fillMaxWidth()
        .clip(shape)
        .background(backgroundColor)
        .border(1.dp, borderColor, shape)
        .then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        )

    Column(
        modifier = baseModifier.padding(AppTokens.Space.xl),
        content = content
    )
}

@Composable
fun SindorakuPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val bg = when {
        !enabled -> Hairline
        pressed -> PrimaryPressed
        else -> Primary
    }
    val textColor = when {
        !enabled -> Muted
        pressed -> InkPress
        else -> OnPrimary
    }
    val shape = RoundedCornerShape(AppTokens.Radius.md)

    Row(
        modifier = modifier
            .height(AppTokens.Component.buttonHeight)
            .clip(shape)
            .background(bg)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = AppTokens.Space.lg),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(AppTokens.Space.sm))
        }
        Text(
            text = text.uppercase(),
            style = SindorakuTextStyles.ButtonCap,
            color = textColor
        )
    }
}

@Composable
fun SindorakuInvertedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val bg = when {
        !enabled -> OnDarkFaint
        pressed -> SurfacePressLight
        else -> OnPrimary
    }
    val textColor = when {
        !enabled -> OnDarkMuted
        pressed -> InkPress
        else -> InkDeep
    }
    val shape = RoundedCornerShape(AppTokens.Radius.md)

    Row(
        modifier = modifier
            .height(AppTokens.Component.buttonHeight)
            .clip(shape)
            .background(bg)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = AppTokens.Space.lg),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(AppTokens.Space.sm))
        }
        Text(
            text = text.uppercase(),
            style = SindorakuTextStyles.ButtonCap,
            color = textColor
        )
    }
}

@Composable
fun SindorakuOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onDark: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(AppTokens.Radius.md)
    val borderColor = when {
        !enabled -> Hairline
        onDark && pressed -> OnDark
        onDark -> OnDarkMuted.copy(alpha = 0.5f)
        pressed -> Ink
        else -> HairlineCool
    }
    val textColor = when {
        !enabled -> Muted
        onDark -> OnDark
        else -> Ink
    }

    Box(
        modifier = modifier
            .height(AppTokens.Component.buttonHeight)
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = AppTokens.Space.lg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = SindorakuTextStyles.ButtonCap,
            color = textColor
        )
    }
}

@Composable
fun SindorakuIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    onDark: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(AppTokens.Radius.xl)
    val bg = when {
        onDark && pressed -> OnDarkFaint.copy(alpha = 0.8f)
        onDark -> OnDarkFaint
        pressed -> SurfacePressLight
        else -> SurfaceCard
    }
    val iconTint = if (onDark) OnDark else Ink

    Box(
        modifier = modifier
            .size(AppTokens.Component.iconButtonSize)
            .clip(shape)
            .background(bg)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun SindorakuIconOutlineButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    onDark: Boolean = false
) = SindorakuIconButton(onClick, icon, contentDescription, modifier, onDark)

@Composable
fun SindorakuSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(AppTokens.Radius.sm)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(AppTokens.Component.inputHeight)
            .clip(shape)
            .background(Canvas)
            .border(1.dp, HairlineCool, shape)
            .padding(horizontal = AppTokens.Space.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Steel,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(AppTokens.Space.sm))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink),
            cursorBrush = SolidColor(AccentViolet),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Steel
                        )
                    }
                    inner()
                }
            }
        )
    }
}

@Composable
fun SindorakuFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (selected) Primary else Color.Transparent,
        animationSpec = tween(150),
        label = "chipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) Primary else Hairline,
        animationSpec = tween(150),
        label = "chipBorder"
    )
    val textColor = if (selected) OnPrimary else Slate
    val shape = RoundedCornerShape(AppTokens.Radius.xs)

    Box(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .border(1.dp, borderColor, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = AppTokens.Space.md, vertical = AppTokens.Space.sm),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

@Composable
fun SindorakuStatPill(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(AppTokens.Radius.md)
    Column(
        modifier = modifier
            .clip(shape)
            .background(SurfaceNight)
            .border(1.dp, HairlineViolet, shape)
            .padding(horizontal = AppTokens.Space.md, vertical = AppTokens.Space.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = OnDark
        )
        Text(
            text = label,
            style = SindorakuTextStyles.MicroCap,
            color = OnDarkMuted
        )
    }
}

@Composable
fun SindorakuSpecCell(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    tint: Color = CardTintLavender
) = SindorakuStatPill(value = value, label = label, modifier = modifier)

@Composable
fun SindorakuListRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeTint: Color = CardTintLavender
) {
    val shape = RoundedCornerShape(AppTokens.Radius.lg)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick)
            .padding(vertical = AppTokens.Space.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(AppTokens.Radius.md))
                    .background(badgeTint),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title.take(1).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = AccentVioletDeep
                )
            }
            Spacer(Modifier.width(AppTokens.Space.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                SindorakuCaption(text = subtitle)
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Stone,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun SindorakuSegmentedTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onDark: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTokens.Radius.md))
            .background(if (onDark) SurfaceNight else Surface)
            .padding(AppTokens.Space.xxs),
        horizontalArrangement = Arrangement.spacedBy(AppTokens.Space.xxs)
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            val tabShape = RoundedCornerShape(AppTokens.Radius.sm)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(tabShape)
                    .background(
                        if (selected) {
                            if (onDark) OnDark else Primary
                        } else Color.Transparent
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(index) }
                    .padding(vertical = AppTokens.Space.sm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab,
                    style = MaterialTheme.typography.labelMedium,
                    color = when {
                        selected && onDark -> InkDeep
                        selected -> OnPrimary
                        onDark -> OnDarkMuted
                        else -> Slate
                    }
                )
            }
        }
    }
}

@Composable
fun SindorakuTagBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = SurfaceNight,
    textColor: Color = OnDark
) {
    Text(
        text = text.uppercase(),
        style = SindorakuTextStyles.MicroCap,
        color = textColor,
        modifier = modifier
            .clip(RoundedCornerShape(AppTokens.Radius.xs))
            .background(backgroundColor)
            .padding(horizontal = AppTokens.Space.sm, vertical = AppTokens.Space.xs)
    )
}

@Composable
fun SindorakuHeroBand(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val verticalPad = if (compact) AppTokens.Space.lg else AppTokens.Space.xxl
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CanvasDark, SurfaceNight)
                )
            )
    ) {
        HeroStarfieldTexture(modifier = Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .sindorakuStatusBarPadding()
                .padding(
                    start = AppTokens.Space.xl,
                    end = AppTokens.Space.xl,
                    top = verticalPad,
                    bottom = verticalPad
                ),
            content = content
        )
    }
}

@Composable
fun SindorakuFeaturedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    SindorakuSurfaceCard(
        modifier = modifier,
        backgroundColor = SurfaceNight,
        onDark = true,
        onClick = onClick,
        content = content
    )
}

@Composable
fun SindorakuSpotlightCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(AppTokens.Radius.xxl)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AccentVioletDeep)
            .padding(AppTokens.Space.xl),
        content = content
    )
}

@Composable
private fun HeroStarfieldTexture(modifier: Modifier = Modifier) {
    val stars = listOf(
        Triple(0.08f, 0.12f, 0.35f),
        Triple(0.22f, 0.28f, 0.25f),
        Triple(0.45f, 0.08f, 0.4f),
        Triple(0.68f, 0.18f, 0.3f),
        Triple(0.85f, 0.32f, 0.45f),
        Triple(0.15f, 0.55f, 0.28f),
        Triple(0.52f, 0.48f, 0.38f),
        Triple(0.78f, 0.62f, 0.32f),
        Triple(0.35f, 0.72f, 0.42f),
        Triple(0.92f, 0.78f, 0.25f)
    )
    BoxWithConstraints(modifier = modifier) {
        stars.forEach { (x, y, alpha) ->
            Box(
                modifier = Modifier
                    .offset(x = maxWidth * x, y = maxHeight * y)
                    .size(2.dp)
                    .clip(CircleShape)
                    .background(OnDark.copy(alpha = alpha))
            )
        }
    }
}
