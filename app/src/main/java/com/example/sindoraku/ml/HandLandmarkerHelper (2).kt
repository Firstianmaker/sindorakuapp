package com.example.sindoraku.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlin.math.max

class HandLandmarkerHelper(
    private val context: Context,
    private val onPrediction: (String) -> Unit,
    private val onWaitingSequence: (Int) -> Unit,
    private val onNoHand: () -> Unit,
    private val onError: (String) -> Unit,
    // #10: kirim bounding box (ruang piksel frame hasil rotasi) ke UI
    private val onHandBoxes: (boxes: List<RectF>, srcWidth: Int, srcHeight: Int) -> Unit = { _, _, _ -> }
) {

    companion object {
        private const val MAX_HANDS = 2
        private const val DETECTION_CONFIDENCE = 0.30f
        private const val PRESENCE_CONFIDENCE = 0.30f
        private const val TRACKING_CONFIDENCE = 0.30f
        private const val ROI_PADDING_PX = 20
        private const val SAMPLE_INTERVAL_MS = 200L
        // #3 fix: jumlah frame tanpa tangan berturut-turut sebelum buffer
        // classifier di-reset (gesture dianggap selesai). ~200 ms/frame
        // -> 3 frame ≈ 0,6 detik.
        private const val NO_HAND_RESET_FRAMES = 3
        // #2: Mirror kamera depan dinonaktifkan (hasil deteksi lebih buruk
        // saat frame di-mirror). Frame depan kini diproses apa adanya.
        private const val MIRROR_FRONT_CAMERA = false

        // === DEBUG SNAPSHOT START (hapus blok ini setelah debug) ===
        // Dinonaktifkan untuk final build: tidak lagi menyimpan frame
        // ke storage saat deteksi berjalan.
        private const val DEBUG_SAVE_FRAMES = false
        private const val DEBUG_MAX_FRAMES = 5
        // === DEBUG SNAPSHOT END ===

        private val PALM_INDICES = intArrayOf(
            0, 1, 2, 5, 9, 13, 17
        )

        private val HAND_CONNECTIONS = arrayOf(
            intArrayOf(0, 1),
            intArrayOf(1, 2),
            intArrayOf(2, 3),
            intArrayOf(3, 4),
            intArrayOf(0, 5),
            intArrayOf(5, 6),
            intArrayOf(6, 7),
            intArrayOf(7, 8),
            intArrayOf(5, 9),
            intArrayOf(9, 10),
            intArrayOf(10, 11),
            intArrayOf(11, 12),
            intArrayOf(9, 13),
            intArrayOf(13, 14),
            intArrayOf(14, 15),
            intArrayOf(15, 16),
            intArrayOf(13, 17),
            intArrayOf(17, 18),
            intArrayOf(18, 19),
            intArrayOf(19, 20),
            intArrayOf(0, 17)
        )
    }

    private data class PixelPoint(
        val x: Int,
        val y: Int
    )

    private data class HandGeometry(
        val points: List<PixelPoint>,
        val lineThickness: Int,
        val pointRadius: Int,
        val handScale: Int
    )

    private var handLandmarker: HandLandmarker? = null

    private val classifier = run {
        val perfLoadStart = android.os.SystemClock.elapsedRealtime()
        val created = BisindoClassifier(context.applicationContext)
        PerformanceLogger.logModelLoad(
            android.os.SystemClock.elapsedRealtime() - perfLoadStart
        )
        created
    }

    private var lastTimestampMs = -1L

    private var lastSampledTimestampMs = -1L

    // === DEBUG SNAPSHOT START (hapus baris ini setelah debug) ===
    private var debugSavedCount = 0
    // === DEBUG SNAPSHOT END ===

    private var emptyFrameBitmap: Bitmap? = null

    // #3 fix: penghitung frame beruntun tanpa tangan, dipakai untuk
    // memutuskan kapan buffer classifier di-reset.
    private var consecutiveNoHandFrames = 0

    private var isClosed = false

    init {
        setupHandLandmarker()
    }

    private fun setupHandLandmarker() {
        try {
            context.assets.open(
                "hand_landmarker.task"
            ).use { }

            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(
                    "hand_landmarker.task"
                )
                .build()

            val options =
                HandLandmarker.HandLandmarkerOptions
                    .builder()
                    .setBaseOptions(baseOptions)
                    .setRunningMode(
                        RunningMode.VIDEO
                    )
                    .setNumHands(MAX_HANDS)
                    .setMinHandDetectionConfidence(
                        DETECTION_CONFIDENCE
                    )
                    .setMinHandPresenceConfidence(
                        PRESENCE_CONFIDENCE
                    )
                    .setMinTrackingConfidence(
                        TRACKING_CONFIDENCE
                    )
                    .build()

            handLandmarker =
                HandLandmarker.createFromOptions(
                    context,
                    options
                )
        } catch (e: Exception) {
            onError(
                "Gagal init MediaPipe: " +
                        (e.message ?: "Unknown error")
            )
        }
    }

    @Synchronized
    fun detect(
        imageProxy: ImageProxy,
        isFrontCamera: Boolean
    ) {
        if (isClosed) {
            imageProxy.close()
            return
        }

        // Sampling berbasis waktu (~200 ms / frame ke-6 @30fps)
        val sourceTimestampMs =
            imageProxy.imageInfo.timestamp / 1_000_000L

        if (
            lastSampledTimestampMs >= 0 &&
            sourceTimestampMs - lastSampledTimestampMs <
            SAMPLE_INTERVAL_MS
        ) {
            imageProxy.close()
            return
        }
        lastSampledTimestampMs = sourceTimestampMs

        val perfE2eStart = android.os.SystemClock.elapsedRealtime()

        var rawBitmap: Bitmap? = null
        var rotatedBitmap: Bitmap? = null

        try {
            rawBitmap =
                imageProxyToBitmap(imageProxy)

            rotatedBitmap = transformBitmap(
                bitmap = rawBitmap,
                rotationDegrees =
                imageProxy.imageInfo
                    .rotationDegrees,
                mirror =
                isFrontCamera && MIRROR_FRONT_CAMERA
            )

            // === DEBUG SNAPSHOT START (hapus baris ini setelah debug) ===
            saveDebugFrame(rotatedBitmap)
            // === DEBUG SNAPSHOT END ===

            val mpImage =
                BitmapImageBuilder(
                    rotatedBitmap
                ).build()

            val timestampMs =
                if (
                    sourceTimestampMs >
                    lastTimestampMs
                ) {
                    sourceTimestampMs
                } else {
                    lastTimestampMs + 1L
                }

            lastTimestampMs = timestampMs

            val perfLmStart = android.os.SystemClock.elapsedRealtime()
            val result =
                handLandmarker?.detectForVideo(
                    mpImage,
                    timestampMs
                )
            PerformanceLogger.recordLandmark(
                android.os.SystemClock.elapsedRealtime() - perfLmStart
            )

            if (
                result == null ||
                result.landmarks().isEmpty()
            ) {
                handleNoHandFrame(
                    rotatedBitmap
                )
            } else {
                handleResult(
                    result = result,
                    bitmap = rotatedBitmap
                )
            }
        } catch (e: Exception) {
            if (!isClosed) {
                onError(
                    e.message
                        ?: "Gagal membaca frame kamera"
                )
            }
        } finally {
            PerformanceLogger.endFrame(
                android.os.SystemClock.elapsedRealtime() - perfE2eStart
            )
            if (
                rotatedBitmap != null &&
                rotatedBitmap !== rawBitmap &&
                !rotatedBitmap.isRecycled
            ) {
                rotatedBitmap.recycle()
            }

            if (
                rawBitmap != null &&
                !rawBitmap.isRecycled
            ) {
                rawBitmap.recycle()
            }

            imageProxy.close()
        }
    }

    private fun handleNoHandFrame(
        bitmap: Bitmap
    ) {
        // #10: kosongkan overlay bounding box saat tak ada tangan.
        onHandBoxes(emptyList(), bitmap.width, bitmap.height)

        // #3 fix: JANGAN umpankan frame kosong ke classifier. Sebelumnya
        // frame hitam + landmark nol ikut mengisi sequence & vote buffer,
        // sehingga prediksi lama/acak menumpuk dan kata bisa "muncul cepat
        // tapi salah". Sekarang: bila tangan hilang beberapa frame
        // berturut-turut, gesture dianggap selesai lalu buffer di-reset
        // agar gesture berikutnya mulai dari nol.
        consecutiveNoHandFrames++
        if (consecutiveNoHandFrames >= NO_HAND_RESET_FRAMES) {
            classifier.clearSequence()
        }

        onNoHand()
    }

    private fun handleResult(
        result: HandLandmarkerResult,
        bitmap: Bitmap
    ) {
        val width = bitmap.width
        val height = bitmap.height

        val landmarkFeatures =
            FloatArray(84)

        val handPointsList =
            mutableListOf<List<PixelPoint>>()

        var featureIndex = 0

        for (
        handLandmarks in
        result.landmarks().take(MAX_HANDS)
        ) {
            val points =
                mutableListOf<PixelPoint>()

            for (
            landmark in
            handLandmarks.take(21)
            ) {
                val x = (
                        landmark.x() * width
                        ).toInt().coerceIn(
                        0,
                        width - 1
                    )

                val y = (
                        landmark.y() * height
                        ).toInt().coerceIn(
                        0,
                        height - 1
                    )

                points.add(
                    PixelPoint(
                        x = x,
                        y = y
                    )
                )

                if (
                    featureIndex + 1 <
                    landmarkFeatures.size
                ) {
                    landmarkFeatures[
                        featureIndex++
                    ] = x.toFloat() /
                            width.toFloat()

                    landmarkFeatures[
                        featureIndex++
                    ] = y.toFloat() /
                            height.toFloat()
                }
            }

            if (points.size == 21) {
                handPointsList.add(points)
            }
        }

        if (handPointsList.isEmpty()) {
            handleNoHandFrame(bitmap)
            return
        }

        // #3 fix: tangan terdeteksi lagi -> nolkan penghitung frame kosong
        // agar reset hanya terjadi saat tangan benar-benar hilang.
        consecutiveNoHandFrames = 0

        // #10: bounding box per tangan dari landmark + padding ROI (20 px),
        // koordinat dalam ruang piksel `bitmap` (width x height) hasil rotasi.
        val handBoxes = handPointsList.map { points ->
            val minX = points.minOf { it.x }
            val minY = points.minOf { it.y }
            val maxX = points.maxOf { it.x }
            val maxY = points.maxOf { it.y }
            RectF(
                (minX - ROI_PADDING_PX).coerceAtLeast(0).toFloat(),
                (minY - ROI_PADDING_PX).coerceAtLeast(0).toFloat(),
                (maxX + ROI_PADDING_PX).coerceAtMost(width - 1).toFloat(),
                (maxY + ROI_PADDING_PX).coerceAtMost(height - 1).toFloat()
            )
        }
        onHandBoxes(handBoxes, width, height)

        val handBitmap =
            createMaskedHandRoi(
                bitmap = bitmap,
                handPointsList =
                handPointsList
            )

        try {
            val perfInfStart = android.os.SystemClock.elapsedRealtime()
            val prediction =
                classifier.predict(
                    handBitmap = handBitmap,
                    landmarkFeatures =
                    landmarkFeatures
                )
            PerformanceLogger.recordInference(
                android.os.SystemClock.elapsedRealtime() - perfInfStart
            )

            if (prediction == null) {
                onWaitingSequence(
                    featureIndex
                )
            } else {
                onPrediction(prediction)
            }
        } finally {
            if (!handBitmap.isRecycled) {
                handBitmap.recycle()
            }
        }
    }

    private fun createMaskedHandRoi(
        bitmap: Bitmap,
        handPointsList:
        List<List<PixelPoint>>
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val geometries =
            handPointsList.mapNotNull {
                    points ->

                if (points.size < 21) {
                    null
                } else {
                    val handWidth =
                        points.maxOf { it.x } -
                                points.minOf { it.x }

                    val handHeight =
                        points.maxOf { it.y } -
                                points.minOf { it.y }

                    val handScale = max(
                        max(
                            handWidth,
                            handHeight
                        ),
                        1
                    )

                    val lineThickness =
                        max(
                            10,
                            (
                                    handScale *
                                            0.10f
                                    ).toInt()
                        )

                    val pointRadius =
                        max(
                            6,
                            lineThickness / 2
                        )

                    HandGeometry(
                        points = points,
                        lineThickness =
                        lineThickness,
                        pointRadius =
                        pointRadius,
                        handScale =
                        handScale
                    )
                }
            }

        if (geometries.isEmpty()) {
            return Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
            ).apply {
                eraseColor(Color.BLACK)
            }
        }

        var kernelSize = max(
            3,
            (
                    geometries.maxOf {
                        it.handScale
                    } * 0.03f
                    ).toInt()
        )

        if (kernelSize % 2 == 0) {
            kernelSize += 1
        }

        val dilationRadius =
            kernelSize / 2

        val maskBitmap =
            Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
            )

        val maskCanvas =
            Canvas(maskBitmap)

        maskCanvas.drawColor(
            Color.TRANSPARENT,
            PorterDuff.Mode.CLEAR
        )

        for (geometry in geometries) {
            val expandedLineThickness =
                geometry.lineThickness +
                        dilationRadius * 2

            val expandedPointRadius =
                geometry.pointRadius +
                        dilationRadius

            drawPalmMask(
                canvas = maskCanvas,
                points = geometry.points,
                dilationRadius =
                dilationRadius
            )

            drawConnectionMask(
                canvas = maskCanvas,
                points = geometry.points,
                lineThickness =
                expandedLineThickness
            )

            drawLandmarkPointMask(
                canvas = maskCanvas,
                points = geometry.points,
                pointRadius =
                expandedPointRadius
            )
        }

        // #2: bounding box dihitung dari piksel mask yang sudah
        // digambar, setara dengan get_bbox_from_mask + cv2.boundingRect
        // di notebook (bukan dari titik landmark + radius).
        val maskBounds =
            computeMaskBounds(maskBitmap)

        val left: Int
        val top: Int
        val right: Int
        val bottom: Int

        if (maskBounds == null) {
            left = 0
            top = 0
            right = width
            bottom = height
        } else {
            left = maskBounds[0]
            top = maskBounds[1]
            right = maskBounds[2] + 1
            bottom = maskBounds[3] + 1
        }

        val representationBitmap =
            Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
            )

        val representationCanvas =
            Canvas(representationBitmap)

        representationCanvas.drawColor(
            Color.BLACK
        )

        val layer =
            representationCanvas.saveLayer(
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                null
            )

        representationCanvas.drawBitmap(
            bitmap,
            0f,
            0f,
            null
        )

        val maskPaint =
            Paint().apply {
                isAntiAlias = false
                xfermode =
                    PorterDuffXfermode(
                        PorterDuff.Mode.DST_IN
                    )
            }

        representationCanvas.drawBitmap(
            maskBitmap,
            0f,
            0f,
            maskPaint
        )

        maskPaint.xfermode = null

        representationCanvas
            .restoreToCount(layer)

        val cropLeft =
            (
                    left -
                            ROI_PADDING_PX
                    ).coerceIn(
                    0,
                    width - 1
                )

        val cropTop =
            (
                    top -
                            ROI_PADDING_PX
                    ).coerceIn(
                    0,
                    height - 1
                )

        val cropRight =
            (
                    right +
                            ROI_PADDING_PX
                    ).coerceIn(
                    cropLeft + 1,
                    width
                )

        val cropBottom =
            (
                    bottom +
                            ROI_PADDING_PX
                    ).coerceIn(
                    cropTop + 1,
                    height
                )

        val croppedBitmap =
            Bitmap.createBitmap(
                representationBitmap,
                cropLeft,
                cropTop,
                cropRight - cropLeft,
                cropBottom - cropTop
            )

        maskBitmap.recycle()
        representationBitmap.recycle()

        return croppedBitmap
    }

    // #2: cari kotak pembatas dari piksel mask yang tidak transparan
    // (alpha != 0), meniru cv2.boundingRect pada mask di notebook.
    private fun computeMaskBounds(
        mask: Bitmap
    ): IntArray? {
        val w = mask.width
        val h = mask.height

        val pixels = IntArray(w * h)
        mask.getPixels(
            pixels,
            0,
            w,
            0,
            0,
            w,
            h
        )

        var minX = w
        var minY = h
        var maxX = -1
        var maxY = -1

        var index = 0
        for (y in 0 until h) {
            for (x in 0 until w) {
                val alpha =
                    pixels[index] ushr 24

                if (alpha != 0) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }

                index++
            }
        }

        if (maxX < 0 || maxY < 0) {
            return null
        }

        return intArrayOf(
            minX,
            minY,
            maxX,
            maxY
        )
    }

    private fun drawPalmMask(
        canvas: Canvas,
        points: List<PixelPoint>,
        dilationRadius: Int
    ) {
        val palmPoints =
            mutableListOf<PixelPoint>()

        for (index in PALM_INDICES) {
            val point =
                points.getOrNull(index)

            if (point != null) {
                palmPoints.add(point)
            }
        }

        val hull =
            calculateConvexHull(
                palmPoints
            )

        if (hull.size < 3) {
            return
        }

        val path =
            Path().apply {
                moveTo(
                    hull.first().x.toFloat(),
                    hull.first().y.toFloat()
                )

                for (
                index in
                1 until hull.size
                ) {
                    lineTo(
                        hull[index].x.toFloat(),
                        hull[index].y.toFloat()
                    )
                }

                close()
            }

        val paint =
            Paint().apply {
                color = Color.WHITE
                isAntiAlias = false
                style =
                    Paint.Style.FILL_AND_STROKE
                strokeWidth =
                    max(
                        1f,
                        dilationRadius * 2f
                    )
                strokeJoin =
                    Paint.Join.ROUND
                strokeCap =
                    Paint.Cap.ROUND
            }

        canvas.drawPath(
            path,
            paint
        )
    }

    private fun drawConnectionMask(
        canvas: Canvas,
        points: List<PixelPoint>,
        lineThickness: Int
    ) {
        val paint =
            Paint().apply {
                color = Color.WHITE
                isAntiAlias = false
                style =
                    Paint.Style.STROKE
                strokeWidth =
                    lineThickness.toFloat()
                strokeCap =
                    Paint.Cap.ROUND
                strokeJoin =
                    Paint.Join.ROUND
            }

        for (
        connection in
        HAND_CONNECTIONS
        ) {
            val start =
                points.getOrNull(
                    connection[0]
                ) ?: continue

            val end =
                points.getOrNull(
                    connection[1]
                ) ?: continue

            canvas.drawLine(
                start.x.toFloat(),
                start.y.toFloat(),
                end.x.toFloat(),
                end.y.toFloat(),
                paint
            )
        }
    }

    private fun drawLandmarkPointMask(
        canvas: Canvas,
        points: List<PixelPoint>,
        pointRadius: Int
    ) {
        val paint =
            Paint().apply {
                color = Color.WHITE
                isAntiAlias = false
                style =
                    Paint.Style.FILL
            }

        for (point in points) {
            canvas.drawCircle(
                point.x.toFloat(),
                point.y.toFloat(),
                pointRadius.toFloat(),
                paint
            )
        }
    }

    private fun calculateConvexHull(
        inputPoints: List<PixelPoint>
    ): List<PixelPoint> {
        val points =
            inputPoints
                .distinct()
                .sortedWith(
                    compareBy<PixelPoint> {
                        it.x
                    }.thenBy {
                        it.y
                    }
                )

        if (points.size <= 2) {
            return points
        }

        val lower =
            mutableListOf<PixelPoint>()

        for (point in points) {
            while (
                lower.size >= 2 &&
                cross(
                    origin =
                    lower[
                        lower.size - 2
                    ],
                    pointA =
                    lower[
                        lower.size - 1
                    ],
                    pointB = point
                ) <= 0L
            ) {
                lower.removeAt(
                    lower.lastIndex
                )
            }

            lower.add(point)
        }

        val upper =
            mutableListOf<PixelPoint>()

        for (
        point in
        points.asReversed()
        ) {
            while (
                upper.size >= 2 &&
                cross(
                    origin =
                    upper[
                        upper.size - 2
                    ],
                    pointA =
                    upper[
                        upper.size - 1
                    ],
                    pointB = point
                ) <= 0L
            ) {
                upper.removeAt(
                    upper.lastIndex
                )
            }

            upper.add(point)
        }

        lower.removeAt(
            lower.lastIndex
        )

        upper.removeAt(
            upper.lastIndex
        )

        return lower + upper
    }

    private fun cross(
        origin: PixelPoint,
        pointA: PixelPoint,
        pointB: PixelPoint
    ): Long {
        return (
                pointA.x -
                        origin.x
                ).toLong() * (
                pointB.y -
                        origin.y
                ).toLong() - (
                pointA.y -
                        origin.y
                ).toLong() * (
                pointB.x -
                        origin.x
                ).toLong()
    }

    private fun getEmptyFrameBitmap(
        width: Int,
        height: Int
    ): Bitmap {
        val current =
            emptyFrameBitmap

        if (
            current != null &&
            !current.isRecycled &&
            current.width == width &&
            current.height == height
        ) {
            return current
        }

        current?.recycle()

        return Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        ).apply {
            eraseColor(Color.BLACK)
            emptyFrameBitmap = this
        }
    }

    private fun imageProxyToBitmap(
        imageProxy: ImageProxy
    ): Bitmap {
        val width = imageProxy.width
        val height = imageProxy.height

        // #4: konversi YUV -> RGB langsung (BT.601) tanpa kompresi
        // JPEG, sehingga tidak ada artefak JPEG seperti pipeline lama.
        val nv21 =
            yuv420888ToNv21(imageProxy)

        val argb =
            IntArray(width * height)

        val frameSize = width * height

        for (row in 0 until height) {
            var uvIndex =
                frameSize +
                        (row shr 1) * width

            var u = 0
            var v = 0

            for (col in 0 until width) {
                var y =
                    (
                            nv21[row * width + col]
                                .toInt() and 0xff
                            ) - 16

                if (y < 0) {
                    y = 0
                }

                if (col and 1 == 0) {
                    v =
                        (
                                nv21[uvIndex++]
                                    .toInt() and 0xff
                                ) - 128

                    u =
                        (
                                nv21[uvIndex++]
                                    .toInt() and 0xff
                                ) - 128
                }

                val y1192 = 1192 * y

                var r = y1192 + 1634 * v
                var g = y1192 - 833 * v - 400 * u
                var b = y1192 + 2066 * u

                r = r.coerceIn(0, 262143)
                g = g.coerceIn(0, 262143)
                b = b.coerceIn(0, 262143)

                argb[row * width + col] =
                    (0xff shl 24) or
                            ((r shl 6) and 0xff0000) or
                            ((g shr 2) and 0xff00) or
                            ((b shr 10) and 0xff)
            }
        }

        return Bitmap.createBitmap(
            argb,
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
    }

    private fun yuv420888ToNv21(
        imageProxy: ImageProxy
    ): ByteArray {
        val width =
            imageProxy.width

        val height =
            imageProxy.height

        val output =
            ByteArray(
                width * height * 3 / 2
            )

        val yPlane =
            imageProxy.planes[0]

        val uPlane =
            imageProxy.planes[1]

        val vPlane =
            imageProxy.planes[2]

        val yBuffer =
            yPlane.buffer.duplicate()

        val uBuffer =
            uPlane.buffer.duplicate()

        val vBuffer =
            vPlane.buffer.duplicate()

        var outputIndex = 0

        for (row in 0 until height) {
            val rowStart =
                row * yPlane.rowStride

            for (
            column in
            0 until width
            ) {
                val sourceIndex =
                    rowStart +
                            column *
                            yPlane.pixelStride

                output[outputIndex++] =
                    if (
                        sourceIndex <
                        yBuffer.limit()
                    ) {
                        yBuffer.get(
                            sourceIndex
                        )
                    } else {
                        0
                    }
            }
        }

        for (
        row in
        0 until height / 2
        ) {
            val uRowStart =
                row * uPlane.rowStride

            val vRowStart =
                row * vPlane.rowStride

            for (
            column in
            0 until width / 2
            ) {
                val uIndex =
                    uRowStart +
                            column *
                            uPlane.pixelStride

                val vIndex =
                    vRowStart +
                            column *
                            vPlane.pixelStride

                output[outputIndex++] =
                    if (
                        vIndex <
                        vBuffer.limit()
                    ) {
                        vBuffer.get(
                            vIndex
                        )
                    } else {
                        0
                    }

                output[outputIndex++] =
                    if (
                        uIndex <
                        uBuffer.limit()
                    ) {
                        uBuffer.get(
                            uIndex
                        )
                    } else {
                        0
                    }
            }
        }

        return output
    }


    private fun saveDebugFrame(bitmap: Bitmap?) {
        if (
            !DEBUG_SAVE_FRAMES ||
            bitmap == null ||
            debugSavedCount >= DEBUG_MAX_FRAMES
        ) {
            return
        }

        try {
            val directory =
                context.getExternalFilesDir(
                    android.os.Environment.DIRECTORY_PICTURES
                )

            val file =
                java.io.File(
                    directory,
                    "debug_frame_$debugSavedCount.jpg"
                )

            java.io.FileOutputStream(file).use { out ->
                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    95,
                    out
                )
            }

            android.util.Log.d(
                "HandLandmarkerDebug",
                "Frame debug tersimpan: ${file.absolutePath}"
            )

            debugSavedCount++
        } catch (e: Exception) {
            android.util.Log.e(
                "HandLandmarkerDebug",
                "Gagal simpan frame debug: ${e.message}"
            )
        }
    }
    // === DEBUG SNAPSHOT END ===

    private fun transformBitmap(
        bitmap: Bitmap,
        rotationDegrees: Int,
        mirror: Boolean
    ): Bitmap {
        if (rotationDegrees == 0 && !mirror) {
            return bitmap
        }

        val matrix =
            Matrix().apply {
                if (mirror) {
                    preScale(-1f, 1f)
                }
                postRotate(
                    rotationDegrees
                        .toFloat()
                )
            }

        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    @Synchronized
    fun resetSequence() {
        if (!isClosed) {
            lastSampledTimestampMs = -1L
            lastTimestampMs = -1L
            classifier.clearSequence()
        }
    }

    @Synchronized
    fun close() {
        if (isClosed) {
            return
        }

        isClosed = true

        handLandmarker?.close()
        handLandmarker = null

        classifier.close()

        emptyFrameBitmap?.recycle()
        emptyFrameBitmap = null
    }
}


// ===== PERFORMANCE LOGGING START (hapus setelah pengujian) =====
private object PerformanceLogger {
    private const val TAG = "Performa"
    private var frameCount = 0
    private var windowStartMs = 0L
    private val e2e = ArrayList<Long>()
    private val landmark = ArrayList<Long>()
    private val inference = ArrayList<Long>()
    private var peakMemMb = 0L
    private var memSum = 0L
    private var memCount = 0

    fun logModelLoad(ms: Long) {
        android.util.Log.d(TAG, "Waktu pemuatan model: $ms ms")
    }

    fun recordLandmark(ms: Long) {
        landmark.add(ms)
    }

    fun recordInference(ms: Long) {
        if (ms >= 0) inference.add(ms)
    }

    fun endFrame(e2eMs: Long) {
        e2e.add(e2eMs)

        val memInfo = android.os.Debug.MemoryInfo()
        android.os.Debug.getMemoryInfo(memInfo)
        val usedMb = memInfo.totalPss / 1024L
        if (usedMb > peakMemMb) peakMemMb = usedMb
        memSum += usedMb
        memCount++

        frameCount++
        val now = android.os.SystemClock.elapsedRealtime()
        if (windowStartMs == 0L) windowStartMs = now
        val elapsed = now - windowStartMs

        if (elapsed >= 1000L) {
            val fps = frameCount * 1000f / elapsed
            android.util.Log.d(
                TAG,
                "FPS: ${"%.1f".format(fps)}" +
                    " | End-to-end: ${avg(e2e)} ms" +
                    " | Landmark: ${avg(landmark)} ms" +
                    " | Inferensi model: ${avg(inference)} ms" +
                    " | RAM rata2: ${if (memCount == 0) 0 else memSum / memCount} MB" +
                    " | RAM puncak: $peakMemMb MB"
            )
            frameCount = 0
            windowStartMs = now
            e2e.clear()
            landmark.clear()
            inference.clear()
        }
    }

    private fun avg(list: List<Long>): Long =
        if (list.isEmpty()) 0 else list.sum() / list.size
}
// ===== PERFORMANCE LOGGING END =====
