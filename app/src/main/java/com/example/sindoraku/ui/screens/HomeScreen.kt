package com.example.sindoraku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.sindoraku.ui.components.*
import com.example.sindoraku.ui.theme.*
import androidx.compose.ui.res.painterResource
import com.example.sindoraku.R

@Composable
fun HomeScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .verticalScroll(rememberScrollState())
    ) {
        SindorakuHeroBand(compact = true) {
            SindorakuWordmark(onDark = true, showBadge = false)
            Spacer(Modifier.height(AppTokens.Space.md))
            SindorakuEyebrow(text = "Penerjemah isyarat", onDark = true)
            Spacer(Modifier.height(AppTokens.Space.xs))
            SindorakuSectionTitle(
                title = "Deteksi BISINDO",
                subtitle = "Terjemahkan gerakan menjadi makna dengan AI real-time",
                onDark = true,
                highlightWord = "BISINDO"
            )

            Spacer(Modifier.height(AppTokens.Space.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppTokens.Space.sm)
            ) {
                SindorakuStatPill(
                    value = "15",
                    label = "Kosakata",
                    modifier = Modifier.weight(1f)
                )
                SindorakuStatPill(
                    value = "AI",
                    label = "Gesture",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(AppTokens.Space.lg))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTokens.Space.xl)
        ) {
            Spacer(Modifier.height(AppTokens.Space.lg))

            SindorakuSurfaceCard {
                SindorakuEyebrow(text = "Selamat datang")
                Spacer(Modifier.height(AppTokens.Space.xs))
                Text(
                    text = "Siap menerjemahkan gerakan menjadi makna?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Charcoal
                )
            }

            Spacer(Modifier.height(AppTokens.Space.lg))

            TipsCarousel()

            Spacer(Modifier.height(AppTokens.Space.lg))

            SindorakuEyebrow(text = "Aksi cepat")
            Spacer(Modifier.height(AppTokens.Space.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppTokens.Space.sm)
            ) {
                ActionCard(
                    title = "Terjemahkan",
                    caption = "Kamera AI",
                    icon = painterResource(R.drawable.ic_solar_camera),
                    featured = true,
                    compact = true,
                    onClick = { navController.navigate("detection") },
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    title = "Kamus",
                    caption = "15 kosakata",
                    icon = painterResource(R.drawable.ic_solar_book),
                    backgroundColor = Surface,
                    iconTint = AccentVioletDeep,
                    compact = true,
                    onClick = { navController.navigate("word_list") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(AppTokens.Space.lg))

            BisindoInfoCard()

            Spacer(Modifier.height(AppTokens.Space.section))
        }
    }
}
