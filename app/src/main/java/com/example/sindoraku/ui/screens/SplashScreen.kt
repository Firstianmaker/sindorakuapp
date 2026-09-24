package com.example.sindoraku.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sindoraku.ui.components.SindorakuCaption
import com.example.sindoraku.ui.components.SindorakuEyebrow
import com.example.sindoraku.ui.components.sindorakuStatusBarPadding
import com.example.sindoraku.ui.theme.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.sindoraku.R

@Composable
fun SplashScreen(navController: NavController) {

    LaunchedEffect(Unit) {
        delay(2400)
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splashPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .sindorakuStatusBarPadding()
                .padding(horizontal = AppTokens.Space.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(AppTokens.Space.xl))

            SindorakuEyebrow(
                text = "AI · BISINDO · Real-time",
                onDark = false
            )

            Spacer(Modifier.weight(1f))

            Image(
                painter = painterResource(id = R.drawable.logo_sindoraku),
                contentDescription = "Logo Sindoraku",
                modifier = Modifier.size(120.dp)
            )

            Spacer(Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(3.dp)
                        .alpha(pulseAlpha)
                        .background(AccentLime)
                )
                Spacer(Modifier.height(AppTokens.Space.md))
                SindorakuCaption(text = "Memuat aplikasi…")
            }

            Spacer(Modifier.height(AppTokens.Space.sectionLg))
        }
    }
}
