package com.example.myapplication.utils

import android.graphics.Bitmap
import android.util.Log
import com.example.myapplication.ModelConstants
import org.tensorflow.lite.Interpreter
import java.io.IOException

class Utils {
    companion object {
        private fun createOutputPlaceholder(): Array<Array<FloatArray>> {
            return Array(1) {
                Array(84) {
                    FloatArray(8400) { 0f }
                }
            }
        }

        fun runInference(
            tflite: Interpreter?,
            bitmap: Bitmap,
            logName: String,
        ): List<DetectionBox> {
            val detectionBoxes = mutableListOf<DetectionBox>()

            try {
                val resizedBitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    ModelConstants.INPUT_SIZE,
                    ModelConstants.INPUT_SIZE,
                    false
                )
                val input = ImageUtils.convertBitmapToArray(resizedBitmap)

                val output = createOutputPlaceholder()
                tflite?.let { interpreter ->
                    interpreter.run(input, output)
                    detectionBoxes.addAll(
                        YoloPostProcessor.getDetectionBoxes(
                            output = output,
                            imageWidth = ModelConstants.INPUT_SIZE.toDouble(),
                            imageHeight = ModelConstants.INPUT_SIZE.toDouble(),
                            inputSize = ModelConstants.INPUT_SIZE.toDouble()
                        )
                    )
                    detectionBoxes.forEach { box ->
                        Log.d(
                            logName,
                            "Detected: ${box.label}, confidence: ${box.confidence}, box: [${box.x}, ${box.y}, ${box.width}, ${box.height}]"
                        )
                    }
                }
            } catch (e: IOException) {
                Log.e(logName, "Failed to load image: ${e.message}")
            }

            return detectionBoxes
        }
    }
}