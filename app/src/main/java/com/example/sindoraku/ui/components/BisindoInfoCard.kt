package com.example.sindoraku.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.sindoraku.ui.theme.*

@Composable
fun BisindoInfoCard() {

    var selectedTab by remember { mutableIntStateOf(0) }

    SindorakuSpotlightCard {
        Spacer(Modifier.height(AppTokens.Space.sm))
        SindorakuSegmentedTabs(
            tabs = listOf("BISINDO", "Sindoraku"),
            selectedIndex = selectedTab,
            onTabSelected = { selectedTab = it },
            onDark = true
        )
        Spacer(Modifier.height(AppTokens.Space.lg))
        Text(
            text = when (selectedTab) {
                0 -> "BISINDO adalah bahasa isyarat yang digunakan komunitas tuli di Indonesia untuk berkomunikasi melalui gerakan tangan."
                else -> "Sindoraku adalah aplikasi berbasis AI yang mendeteksi dan menerjemahkan gerakan BISINDO secara real-time melalui kamera."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = OnDark.copy(alpha = 0.92f)
        )
    }
}
