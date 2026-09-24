package com.example.sindoraku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sindoraku.ui.theme.*
import androidx.compose.ui.graphics.painter.Painter

@Composable
fun ActionCard(
    modifier: Modifier = Modifier,
    title: String,
    caption: String,
    icon: Painter,
    onClick: () -> Unit,
    backgroundColor: Color = SurfaceCard,
    iconTint: Color = Ink,
    featured: Boolean = false,
    compact: Boolean = false
) {
    val shape = RoundedCornerShape(AppTokens.Radius.xl)
    val bg = if (featured) SurfaceNight else backgroundColor
    val titleColor = if (featured) OnDark else Ink
    val captionColor = if (featured) OnDarkMuted else Slate
    val borderColor = if (featured) HairlineViolet else Hairline
    val iconBg = if (featured) OnDarkFaint else AccentLime.copy(alpha = 0.35f)
    val iconColor = if (featured) AccentLime else iconTint
    val cardHeight = if (compact) {
        AppTokens.Component.actionCardCompactHeight
    } else {
        AppTokens.Component.actionCardHeight
    }

    if (compact) {
        Column(
            modifier = modifier
                .height(cardHeight)
                .clip(shape)
                .background(bg)
                .border(1.dp, borderColor, shape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(AppTokens.Space.md),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(AppTokens.Radius.md))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = caption,
                    style = SindorakuTextStyles.Caption,
                    color = captionColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(cardHeight)
                .clip(shape)
                .background(bg)
                .border(1.dp, borderColor, shape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(AppTokens.Space.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(AppTokens.Radius.md))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(AppTokens.Space.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = titleColor
                )
                Spacer(Modifier.height(AppTokens.Space.xxs))
                Text(
                    text = caption,
                    style = SindorakuTextStyles.Caption,
                    color = captionColor
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (featured) OnDarkMuted else Stone,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
