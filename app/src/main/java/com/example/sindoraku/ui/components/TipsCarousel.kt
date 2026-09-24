package com.example.sindoraku.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.sindoraku.ui.theme.*
import kotlinx.coroutines.delay
import androidx.compose.animation.core.tween

private data class Tip(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TipsCarousel() {

    val tips = remember {
        listOf(
            Tip(
                "Gunakan pencahayaan yang baik",
                "Deteksi lebih akurat di lingkungan terang",
                Icons.Filled.LightMode
            ),
            Tip(
                "Pastikan tangan terlihat",
                "Jaga tangan tetap di dalam frame kamera",
                Icons.Filled.PanTool
            ),
            Tip(
                "Ulangi Gerakan",
                "Lakukan gerakan berulang agar lebih akurat",
                Icons.Filled.Replay
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { tips.size })

    LaunchedEffect(Unit) {
        while (true) {
            delay(7000)
            if (!pagerState.isScrollInProgress) {
                val nextPage = (pagerState.currentPage + 1) % tips.size
                pagerState.animateScrollToPage(
                    page = nextPage,
                    animationSpec = tween(durationMillis = 1000)
                )
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        SindorakuEyebrow(text = "Tips penggunaan")
        Spacer(Modifier.height(AppTokens.Space.md))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = AppTokens.Space.md
        ) { page ->
            val tip = tips[page]
            val shape = RoundedCornerShape(AppTokens.Radius.xxl)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(shape)
                    .background(InkDeep)
                    .border(1.dp, HairlineViolet, shape)
                    .padding(AppTokens.Space.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(AppTokens.Radius.md))
                        .background(OnDarkFaint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tip.icon,
                        contentDescription = tip.title,
                        tint = Surface,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(AppTokens.Space.md))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tip.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = OnDark,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(AppTokens.Space.xxs))
                    Text(
                        text = tip.description,
                        style = SindorakuTextStyles.Caption,
                        color = OnDarkMuted,
                        maxLines = 2
                    )
                }
            }
        }

        Spacer(Modifier.height(AppTokens.Space.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(tips.size) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(4.dp)
                        .width(if (selected) 20.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (selected) AccentLime else Hairline)
                )
            }
        }
    }
}
