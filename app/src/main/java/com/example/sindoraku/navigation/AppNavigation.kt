package com.example.sindoraku.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sindoraku.ui.components.ModernBottomBar
import com.example.sindoraku.ui.screens.DetectionScreen
import com.example.sindoraku.ui.screens.HomeScreen
import com.example.sindoraku.ui.screens.SplashScreen
import com.example.sindoraku.ui.screens.WordDetailScreen
import com.example.sindoraku.ui.screens.WordListScreen
import com.example.sindoraku.ui.theme.Canvas

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val showBottomBar =
        currentRoute != "splash" &&
                currentRoute?.startsWith("word_detail") != true

    val isSplash = currentRoute == "splash"
    val isDetection = currentRoute == "detection"

    Scaffold(
        containerColor = Canvas,
        bottomBar = {
            if (showBottomBar) {
                ModernBottomBar(navController)
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = if (isDetection) {
                        0.dp
                    } else {
                        paddingValues.calculateBottomPadding()
                    }
                )
        ) {
            composable("splash") {
                SplashScreen(navController)
            }

            composable("home") {
                HomeScreen(navController)
            }

            composable("detection") {
                DetectionScreen()
            }

            composable("word_list") {
                WordListScreen(navController)
            }

            composable("word_detail/{word}") { backStackEntry ->
                val word = backStackEntry.arguments?.getString("word")

                WordDetailScreen(
                    word = word,
                    onWordClick = { wordId ->
                        navController.navigate("word_detail/$wordId") {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}