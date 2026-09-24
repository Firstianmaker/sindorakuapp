package com.example.sindoraku.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.example.sindoraku.ui.components.CameraPreview
import com.example.sindoraku.ui.components.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.example.sindoraku.ui.theme.*
import androidx.compose.ui.unit.dp

@Composable
fun DetectionScreen() {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceNight)
    ) {
        if (hasPermission) {
            CameraPreview(context, lifecycleOwner)
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                SindorakuHeroBand(compact = true) {
                    SindorakuEyebrow(text = "Kamera", onDark = true)
                    Spacer(Modifier.height(AppTokens.Space.sm))
                    SindorakuSectionTitle(
                        title = "Izin diperlukan",
                        subtitle = "Aktifkan kamera untuk deteksi BISINDO real-time",
                        onDark = true
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Canvas)
                        .padding(AppTokens.Space.xl),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(AppTokens.Radius.xxl))
                            .background(Surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = AccentVioletDeep,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(Modifier.height(AppTokens.Space.xl))

                    SindorakuSurfaceCard {
                        Text(
                            text = "Sindoraku membutuhkan akses kamera untuk membaca gerakan tangan Anda. Izin dapat diaktifkan di pengaturan perangkat.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Charcoal
                        )
                        Spacer(Modifier.height(AppTokens.Space.xl))
                        SindorakuPrimaryButton(
                            text = "Minta izin kamera",
                            onClick = { launcher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.CameraAlt
                        )
                    }
                }
            }
        }
    }
}
