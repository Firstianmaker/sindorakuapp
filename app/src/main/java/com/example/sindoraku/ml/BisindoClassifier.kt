package com.example.sindoraku.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.ArrayDeque

class BisindoClassifier(
    context: Context
) {

    companion object {
        private const val IMAGE_SIZE = 224
        private const val IMAGE_CHANNELS = 3
        private const val IMAGE_FEATURE_SIZE = 256
        private const val LANDMARK_FEATURE_SIZE = 84
        private const val FEATURE_SIZE = 340
        private const val SEQUENCE_LENGTH = 20
        private const val FRAME_SAMPLING = 1
        private const val SEQUENCE_STRIDE = 2
        // #5: notebook memakai argmax murni (tanpa threshold), jadi
        // disamakan ke 0.0f agar setiap prediksi valid diterima.
        private const val CONFIDENCE_THRESHOLD = 0.0f
        private const val FLOAT_BYTES = 4
        private const val VOTE_WINDOW = 5
        // #1: Minimal jumlah prediksi yang harus sepakat di dalam
        // VOTE_WINDOW sebelum sebuah kata ditampilkan. Mencegah hasil
        // "muncul cepat tapi salah" akibat satu prediksi keliru.
        private const val MIN_VOTES = 3
    }

    private val appContext =
        context.applicationContext

    private val featureInterpreter =
        Interpreter(
            loadModelFile(
                "feature_extractor.tflite"
            )
        )

    private val gestureInterpreter =
        Interpreter(
            loadModelFile(
                "bisindo_model.tflite"
            )
        )

    private val labels =
        loadLabels()

    private val sequenceBuffer =
        ArrayDeque<FloatArray>(
            SEQUENCE_LENGTH
        )

    private val recentPredictions =
        ArrayDeque<Int>(VOTE_WINDOW)

    private val imagePixels =
        IntArray(
            IMAGE_SIZE * IMAGE_SIZE
        )

    private val featureInputBuffer =
        ByteBuffer.allocateDirect(
            IMAGE_SIZE *
                    IMAGE_SIZE *
                    IMAGE_CHANNELS *
                    FLOAT_BYTES
        ).order(
            ByteOrder.nativeOrder()
        )

    private val gestureInputBuffer =
        ByteBuffer.allocateDirect(
            SEQUENCE_LENGTH *
                    FEATURE_SIZE *
                    FLOAT_BYTES
        ).order(
            ByteOrder.nativeOrder()
        )

    private val featureOutput =
        Array(1) {
            FloatArray(
                IMAGE_FEATURE_SIZE
            )
        }

    private val gestureOutput =
        Array(1) {
            FloatArray(
                labels.size
            )
        }

    private var frameCounter = 0

    private var processedFrameCounter = 0

    private var isClosed = false

    init {
        validateModels()
    }

    @Synchronized
    fun predict(
        handBitmap: Bitmap,
        landmarkFeatures: FloatArray
    ): String? {
        if (isClosed) {
            return null
        }

        if (
            landmarkFeatures.size !=
            LANDMARK_FEATURE_SIZE
        ) {
            return null
        }

        val shouldProcess =
            frameCounter %
                    FRAME_SAMPLING == 0

        frameCounter++

        if (!shouldProcess) {
            return null
        }

        val imageFeatures =
            extractImageFeatures(
                handBitmap
            )

        val combinedFeatures =
            FloatArray(
                FEATURE_SIZE
            )

        System.arraycopy(
            imageFeatures,
            0,
            combinedFeatures,
            0,
            IMAGE_FEATURE_SIZE
        )

        System.arraycopy(
            landmarkFeatures,
            0,
            combinedFeatures,
            IMAGE_FEATURE_SIZE,
            LANDMARK_FEATURE_SIZE
        )

        sequenceBuffer.addLast(
            combinedFeatures
        )

        if (
            sequenceBuffer.size >
            SEQUENCE_LENGTH
        ) {
            sequenceBuffer.removeFirst()
        }

        processedFrameCounter++

        if (
            sequenceBuffer.size <
            SEQUENCE_LENGTH
        ) {
            return null
        }

        val framesAfterFirstSequence =
            processedFrameCounter -
                    SEQUENCE_LENGTH

        if (
            framesAfterFirstSequence %
            SEQUENCE_STRIDE != 0
        ) {
            return null
        }

        gestureInputBuffer.rewind()

        for (
        frameFeatures in
        sequenceBuffer
        ) {
            for (
            feature in
            frameFeatures
            ) {
                gestureInputBuffer.putFloat(
                    feature
                )
            }
        }

        gestureInputBuffer.rewind()

        gestureOutput[0].fill(0f)

        gestureInterpreter.run(
            gestureInputBuffer,
            gestureOutput
        )

        val probabilities =
            gestureOutput[0]

        val predictionIndex =
            probabilities.indices
                .maxByOrNull {
                    probabilities[it]
                }
                ?: return null

        val confidence =
            probabilities[
                predictionIndex
            ]

        if (
            !confidence.isFinite() ||
            confidence <
            CONFIDENCE_THRESHOLD
        ) {
            return null
        }

        recentPredictions.addLast(
            predictionIndex
        )

        if (
            recentPredictions.size > VOTE_WINDOW
        ) {
            recentPredictions.removeFirst()
        }

        // #1: Hasil hanya ditampilkan setelah minimal MIN_VOTES prediksi
        // terakhir sepakat. Selama konsensus belum tercapai, kembalikan
        // null agar UI tetap menampilkan status "membaca gesture" dan
        // tidak memunculkan kata yang cepat tapi salah.
        val topVote =
            recentPredictions
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }
                ?: return null

        if (topVote.value < MIN_VOTES) {
            return null
        }

        return labels.getOrNull(
            topVote.key
        )
    }

    private fun extractImageFeatures(
        bitmap: Bitmap
    ): FloatArray {
        val resizedBitmap =
            if (
                bitmap.width ==
                IMAGE_SIZE &&
                bitmap.height ==
                IMAGE_SIZE
            ) {
                bitmap
            } else {
                Bitmap.createScaledBitmap(
                    bitmap,
                    IMAGE_SIZE,
                    IMAGE_SIZE,
                    true
                )
            }

        try {
            resizedBitmap.getPixels(
                imagePixels,
                0,
                IMAGE_SIZE,
                0,
                0,
                IMAGE_SIZE,
                IMAGE_SIZE
            )

            featureInputBuffer.rewind()

            for (pixel in imagePixels) {
                featureInputBuffer.putFloat(
                    Color.red(pixel)
                        .toFloat() /
                            127.5f - 1f
                )

                featureInputBuffer.putFloat(
                    Color.green(pixel)
                        .toFloat() /
                            127.5f - 1f
                )

                featureInputBuffer.putFloat(
                    Color.blue(pixel)
                        .toFloat() /
                            127.5f - 1f
                )
            }

            featureInputBuffer.rewind()

            featureOutput[0].fill(0f)

            featureInterpreter.run(
                featureInputBuffer,
                featureOutput
            )

            return featureOutput[0]
                .copyOf()
        } finally {
            if (
                resizedBitmap !== bitmap &&
                !resizedBitmap.isRecycled
            ) {
                resizedBitmap.recycle()
            }
        }
    }

    private fun validateModels() {
        require(
            labels.isNotEmpty()
        )

        require(
            FRAME_SAMPLING > 0
        )

        require(
            SEQUENCE_STRIDE > 0
        )

        val featureInputTensor =
            featureInterpreter
                .getInputTensor(0)

        val featureOutputTensor =
            featureInterpreter
                .getOutputTensor(0)

        val gestureInputTensor =
            gestureInterpreter
                .getInputTensor(0)

        val gestureOutputTensor =
            gestureInterpreter
                .getOutputTensor(0)

        require(
            featureInputTensor.dataType() ==
                    DataType.FLOAT32
        )

        require(
            featureOutputTensor.dataType() ==
                    DataType.FLOAT32
        )

        require(
            gestureInputTensor.dataType() ==
                    DataType.FLOAT32
        )

        require(
            gestureOutputTensor.dataType() ==
                    DataType.FLOAT32
        )

        require(
            featureInputTensor
                .shape()
                .contentEquals(
                    intArrayOf(
                        1,
                        IMAGE_SIZE,
                        IMAGE_SIZE,
                        IMAGE_CHANNELS
                    )
                )
        )

        require(
            featureOutputTensor
                .shape()
                .contentEquals(
                    intArrayOf(
                        1,
                        IMAGE_FEATURE_SIZE
                    )
                )
        )

        require(
            gestureInputTensor
                .shape()
                .contentEquals(
                    intArrayOf(
                        1,
                        SEQUENCE_LENGTH,
                        FEATURE_SIZE
                    )
                )
        )

        require(
            gestureOutputTensor
                .shape()
                .contentEquals(
                    intArrayOf(
                        1,
                        labels.size
                    )
                )
        )
    }

    private fun loadLabels():
            List<String> {
        return appContext.assets
            .open("labels.txt")
            .use { inputStream ->
                BufferedReader(
                    InputStreamReader(
                        inputStream
                    )
                ).useLines { lines ->
                    lines
                        .map {
                            it.trim()
                        }
                        .filter {
                            it.isNotEmpty()
                        }
                        .toList()
                }
            }
    }

    private fun loadModelFile(
        fileName: String
    ): MappedByteBuffer {
        return appContext.assets
            .openFd(fileName)
            .use { fileDescriptor ->
                FileInputStream(
                    fileDescriptor
                        .fileDescriptor
                ).use { inputStream ->
                    inputStream.channel.map(
                        FileChannel
                            .MapMode
                            .READ_ONLY,
                        fileDescriptor
                            .startOffset,
                        fileDescriptor
                            .declaredLength
                    )
                }
            }
    }

    @Synchronized
    fun clearSequence() {
        if (isClosed) {
            return
        }

        sequenceBuffer.clear()
        recentPredictions.clear()
        frameCounter = 0
        processedFrameCounter = 0

        featureInputBuffer.rewind()
        gestureInputBuffer.rewind()
    }

    @Synchronized
    fun close() {
        if (isClosed) {
            return
        }

        isClosed = true

        sequenceBuffer.clear()
        recentPredictions.clear()

        featureInterpreter.close()
        gestureInterpreter.close()
    }
}