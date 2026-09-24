package com.example.sindoraku.ui.components

import android.content.Context
import android.graphics.RectF
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.sindoraku.ml.HandLandmarkerHelper
import com.example.sindoraku.ui.theme.*
import java.util.concurrent.Executors

private const val REQUIRED_STABLE_PREDICTIONS = 2
private const val RELEASE_FRAME_COUNT = 3

@Composable
fun CameraPreview(
    context: Context,
    lifecycleOwner: LifecycleOwner
) {
    var lensFacing by remember {
        mutableIntStateOf(CameraSelector.LENS_FACING_FRONT)
    }

    var detectedText by remember {
        mutableStateOf("Arahkan tangan ke dalam layar")
    }

    // #10: state bounding box untuk overlay preview
    var handBoxes by remember {
        mutableStateOf<List<RectF>>(emptyList())
    }

    var boxSrcWidth by remember {
        mutableIntStateOf(0)
    }

    var boxSrcHeight by remember {
        mutableIntStateOf(0)
    }

    var sentenceText by remember {
        mutableStateOf("")
    }

    var lastPrediction by remember {
        mutableStateOf("")
    }

    var stableCount by remember {
        mutableIntStateOf(0)
    }

    var noHandFrameCount by remember {
        mutableIntStateOf(0)
    }

    var isCollecting by remember {
        mutableStateOf(false)
    }

    var awaitingGestureRelease by remember {
        mutableStateOf(false)
    }

    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    val mainExecutor = remember(context) {
        ContextCompat.getMainExecutor(context)
    }

    val cameraProviderFuture = remember(context) {
        ProcessCameraProvider.getInstance(context)
    }

    val handLandmarkerHelper = remember(
        context,
        mainExecutor,
        lensFacing
    ) {
        lateinit var createdHelper: HandLandmarkerHelper

        createdHelper = HandLandmarkerHelper(
            context = context.applicationContext,
            onPrediction = { label ->
                mainExecutor.execute {
                    if (!awaitingGestureRelease) {
                        noHandFrameCount = 0
                        isCollecting = true

                        if (label == lastPrediction) {
                            stableCount++
                        } else {
                            lastPrediction = label
                            stableCount = 1
                        }

                        detectedText = "Terdeteksi: $label"

                        if (
                            stableCount >=
                            REQUIRED_STABLE_PREDICTIONS
                        ) {
                            sentenceText =
                                if (sentenceText.isBlank()) {
                                    label
                                } else {
                                    "$sentenceText $label"
                                }

                            lastPrediction = ""
                            stableCount = 0
                            noHandFrameCount = 0
                            isCollecting = false
                            awaitingGestureRelease = true

                            createdHelper.resetSequence()

                            detectedText =
                                "Kata \"$label\" masuk · lepaskan tangan untuk gerakan berikutnya"
                        }
                    }
                }
            },
            onWaitingSequence = {
                mainExecutor.execute {
                    if (!awaitingGestureRelease) {
                        noHandFrameCount = 0
                        isCollecting = true
                        detectedText =
                            "Tangan terdeteksi · sedang membaca gerakan..."
                    }
                }
            },
            onNoHand = {
                mainExecutor.execute {
                    isCollecting = false
                    lastPrediction = ""
                    stableCount = 0

                    if (awaitingGestureRelease) {
                        noHandFrameCount++

                        if (
                            noHandFrameCount >=
                            RELEASE_FRAME_COUNT
                        ) {
                            awaitingGestureRelease = false
                            noHandFrameCount = 0
                            createdHelper.resetSequence()
                            detectedText =
                                "Arahkan tangan ke dalam layar"
                        } else {
                            detectedText =
                                "Lepaskan tangan untuk gerakan berikutnya"
                        }
                    } else {
                        noHandFrameCount = 0
                        detectedText =
                            "Arahkan tangan ke dalam layar"
                    }
                }
            },
            onError = { error ->
                mainExecutor.execute {
                    isCollecting = false
                    lastPrediction = ""
                    stableCount = 0
                    detectedText = "Error: $error"
                }
            },
            onHandBoxes = { boxes, srcW, srcH ->
                mainExecutor.execute {
                    handBoxes = boxes
                    boxSrcWidth = srcW
                    boxSrcHeight = srcH
                }
            }
        )

        createdHelper
    }

    DisposableEffect(handLandmarkerHelper) {
        onDispose {
            handLandmarkerHelper.close()
        }
    }

    DisposableEffect(
        cameraProviderFuture,
        cameraExecutor
    ) {
        onDispose {
            try {
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (_: Exception) {
            }

            cameraExecutor.shutdown()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        key(lensFacing) {
            AndroidView(
                factory = { ctx ->
                    val previewView =
                        PreviewView(ctx).apply {
                            scaleType =
                                PreviewView.ScaleType.FILL_CENTER
                        }

                    cameraProviderFuture.addListener(
                        {
                            try {
                                val cameraProvider =
                                    cameraProviderFuture.get()

                                val preview =
                                    Preview.Builder()
                                        .build()
                                        .also {
                                            it.setSurfaceProvider(
                                                previewView.surfaceProvider
                                            )
                                        }

                                val imageAnalysis =
                                    ImageAnalysis.Builder()
                                        .setBackpressureStrategy(
                                            ImageAnalysis
                                                .STRATEGY_KEEP_ONLY_LATEST
                                        )
                                        .build()
                                        .also { analysis ->
                                            analysis.setAnalyzer(
                                                cameraExecutor
                                            ) { imageProxy ->
                                                handLandmarkerHelper.detect(
                                                    imageProxy = imageProxy,
                                                    isFrontCamera =
                                                    lensFacing ==
                                                            CameraSelector
                                                                .LENS_FACING_FRONT
                                                )
                                            }
                                        }

                                val cameraSelector =
                                    CameraSelector.Builder()
                                        .requireLensFacing(
                                            lensFacing
                                        )
                                        .build()

                                cameraProvider.unbindAll()

                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                detectedText =
                                    "Camera error: ${e.message}"
                            }
                        },
                        ContextCompat.getMainExecutor(ctx)
                    )

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // #10: overlay bounding box di atas preview kamera
        Canvas(modifier = Modifier.fillMaxSize()) {
            val boxes = handBoxes
            val srcW = boxSrcWidth.toFloat()
            val srcH = boxSrcHeight.toFloat()
            if (boxes.isNotEmpty() && srcW > 0f && srcH > 0f) {
                val viewW = size.width
                val viewH = size.height

                // PreviewView = FILL_CENTER: isi layar lalu crop tengah
                val scale = maxOf(viewW / srcW, viewH / srcH)
                val offsetX = (viewW - srcW * scale) / 2f
                val offsetY = (viewH - srcH * scale) / 2f

                // Kamera depan ditampilkan ter-mirror oleh PreviewView,
                // sedangkan box dihitung dari frame non-mirror -> balik X.
                val mirror =
                    lensFacing == CameraSelector.LENS_FACING_FRONT

                boxes.forEach { box ->
                    val sLeft = if (mirror) srcW - box.right else box.left
                    val sRight = if (mirror) srcW - box.left else box.right

                    val left = sLeft * scale + offsetX
                    val top = box.top * scale + offsetY
                    val right = sRight * scale + offsetX
                    val bottom = box.bottom * scale + offsetY

                    drawRect(
                        color = Color(0xFF9BE15D),
                        topLeft = Offset(left, top),
                        size = Size(right - left, bottom - top),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .sindorakuStatusBarPadding()
                .padding(
                    top = AppTokens.Space.md,
                    start = AppTokens.Space.xl,
                    end = AppTokens.Space.xl
                ),
            horizontalArrangement =
            Arrangement.SpaceBetween,
            verticalAlignment =
            Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        end = AppTokens.Space.md
                    )
            ) {
                SindorakuEyebrow(
                    text = "Live detection",
                    onDark = true
                )

                Spacer(
                    modifier = Modifier.height(
                        AppTokens.Space.xxs
                    )
                )

                Text(
                    text = "BISINDO",
                    style =
                    MaterialTheme.typography.titleMedium,
                    color = Canvas
                )

                Spacer(
                    modifier = Modifier.height(
                        AppTokens.Space.xxs
                    )
                )

                Text(
                    text = detectedText,
                    style =
                    MaterialTheme.typography.bodySmall,
                    color =
                    Canvas.copy(alpha = 0.86f),
                    maxLines = 2
                )
            }

            IconButton(
                onClick = {
                    detectedText =
                        "Arahkan tangan ke dalam layar"

                    sentenceText = ""
                    lastPrediction = ""
                    stableCount = 0
                    noHandFrameCount = 0
                    isCollecting = false
                    awaitingGestureRelease = false
                    handBoxes = emptyList()

                    lensFacing =
                        if (
                            lensFacing ==
                            CameraSelector.LENS_FACING_FRONT
                        ) {
                            CameraSelector.LENS_FACING_BACK
                        } else {
                            CameraSelector.LENS_FACING_FRONT
                        }
                },
                modifier = Modifier
                    .size(
                        AppTokens.Component.iconButtonSize
                    )
                    .background(Color.Transparent)
            ) {
                Icon(
                    imageVector =
                    Icons.Default.Cameraswitch,
                    contentDescription =
                    "Ganti kamera",
                    tint = Surface,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-80).dp)
                .fillMaxWidth()
                .padding(AppTokens.Space.xl)
        ) {
            val cardShape =
                RoundedCornerShape(
                    AppTokens.Radius.xl
                )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
                    .clip(cardShape)
                    .background(Canvas)
                    .border(
                        width = 1.dp,
                        color = Hairline,
                        shape = cardShape
                    )
                    .padding(AppTokens.Space.lg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    horizontalArrangement =
                    Arrangement.SpaceBetween,
                    verticalAlignment =
                    Alignment.CenterVertically
                ) {
                    SindorakuEyebrow(
                        text = "Kalimat"
                    )

                    Box(
                        modifier =
                        Modifier.width(82.dp),
                        contentAlignment =
                        Alignment.CenterEnd
                    ) {
                        if (isCollecting) {
                            SindorakuTagBadge(
                                text = "Membaca",
                                backgroundColor =
                                AccentLime,
                                textColor = InkDeep
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(
                        AppTokens.Space.sm
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    horizontalArrangement =
                    Arrangement.SpaceBetween,
                    verticalAlignment =
                    Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            sentenceText.isBlank() &&
                                    isCollecting -> "..."

                            sentenceText.isBlank() ->
                                detectedText

                            isCollecting ->
                                "$sentenceText ..."

                            else -> sentenceText
                        },
                        style =
                        MaterialTheme.typography.bodyLarge,
                        color = Ink,
                        maxLines = 2,
                        modifier =
                        Modifier.weight(1f)
                    )

                    Spacer(
                        modifier = Modifier.width(
                            AppTokens.Space.sm
                        )
                    )

                    SindorakuIconButton(
                        onClick = {
                            detectedText =
                                "Arahkan tangan ke dalam layar"

                            sentenceText = ""
                            lastPrediction = ""
                            stableCount = 0
                            noHandFrameCount = 0
                            isCollecting = false
                            awaitingGestureRelease =
                                false
                            handBoxes = emptyList()

                            handLandmarkerHelper
                                .resetSequence()
                        },
                        icon =
                        Icons.Default.Refresh,
                        contentDescription =
                        "Reset deteksi"
                    )
                }
            }
        }
    }
}