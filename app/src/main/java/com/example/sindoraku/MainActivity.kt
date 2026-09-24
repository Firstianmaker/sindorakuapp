package com.example.sindoraku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.sindoraku.navigation.AppNavigation
import com.example.sindoraku.ui.theme.SindorakuTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            SindorakuTheme {
                AppNavigation()
            }
        }
    }
}