package com.example.myapplication.utils

import com.example.myapplication.CocoClassNames

data class DetectionBox(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
    val label: String,
    val confidence: Double
)

class YoloPostProcessor {
    companion object {
        private fun customNms(
            boxes: List<List<Double>>,
            scores: List<Double>,
            confThreshold: Double,
            iouThreshold: Double
        ): List<Int> {
            // Filter by confidence threshold
            val validIndices = mutableListOf<Int>()
            scores.forEachIndexed { index, score ->
                if (score >= confThreshold) {
                    validIndices.add(index)
                }
            }

            if (validIndices.isEmpty()) {
                return emptyList()
            }

            // Sort by scores in descending order
            val indices = validIndices.sortedByDescending { scores[it] }
            val filteredBoxes = indices.map { boxes[it] }

            val keepIndices = mutableListOf<Int>()

            var currentIndices = indices
            var currentBoxes = filteredBoxes

            while (currentIndices.isNotEmpty()) {
                // Pick the box with the highest score
                val currentIdx = currentIndices[0]
                keepIndices.add(currentIdx)

                if (currentIndices.size == 1) {
                    break
                }

                // Compute IoU between the current box and all others
                val currentBox = currentBoxes[0]
                val otherBoxes = currentBoxes.drop(1)
                val otherIndices = currentIndices.drop(1)

                // Calculate IoU
                val ious = otherBoxes.map { otherBox ->
                    // Intersection coordinates
                    val x1 = maxOf(currentBox[0], otherBox[0])
                    val y1 = maxOf(currentBox[1], otherBox[1])
                    val x2 = minOf(currentBox[2], otherBox[2])
                    val y2 = minOf(currentBox[3], otherBox[3])

                    // Intersection area
                    val w = maxOf(0.0, x2 - x1)
                    val h = maxOf(0.0, y2 - y1)
                    val intersection = w * h

                    // Union area
                    val currentArea = (currentBox[2] - currentBox[0]) * (currentBox[3] - currentBox[1])
                    val otherArea = (otherBox[2] - otherBox[0]) * (otherBox[3] - otherBox[1])
                    val union = currentArea + otherArea - intersection

                    // Compute IoU
                    if (union > 0) intersection / union else 0.0
                }

                // Keep boxes with IoU below threshold
                val newIndices = mutableListOf<Int>()
                val newBoxes = mutableListOf<List<Double>>()
                ious.forEachIndexed { i, iou ->
                    if (iou <= iouThreshold) {
                        newIndices.add(otherIndices[i])
                        newBoxes.add(otherBoxes[i])
                    }
                }

                currentIndices = newIndices
                currentBoxes = newBoxes
            }

            return keepIndices
        }

        private fun postprocessOutput(
            output: Array<Array<FloatArray>>,
            confThreshold: Double = 0.25,
            iouThreshold: Double = 0.45,
            numClasses: Int = 80
        ): Triple<List<List<Double>>, List<Double>, List<Int>> {
            // Transpose output from [1, 84, 8400] to [1, 8400, 84]
            val transposed = Array(1) { Array(output[0][0].size) { FloatArray(output[0].size) } }
            for (i in output[0][0].indices) {
                for (j in output[0].indices) {
                    transposed[0][i][j] = output[0][j][i]
                }
            }

            val boxes = mutableListOf<List<Double>>()
            val scores = mutableListOf<Double>()
            val classes = mutableListOf<Int>()

            // Process each detection
            for (detection in transposed[0]) {
                // Extract class probabilities and find max
                val classProbs = detection.slice(4 until 4 + numClasses)
                var classId = 0
                var maxProb = classProbs[0]
                for (i in 1 until classProbs.size) {
                    if (classProbs[i] > maxProb) {
                        maxProb = classProbs[i]
                        classId = i
                    }
                }
                val confidence = maxProb.toDouble()

                if (confidence > confThreshold) {
                    // Bounding box: center_x, center_y, width, height
                    val centerX = detection[0].toDouble()
                    val centerY = detection[1].toDouble()
                    val width = detection[2].toDouble()
                    val height = detection[3].toDouble()

                    // Convert to pixel coordinates (x_min, y_min, x_max, y_max)
                    val xMin = (centerX - width / 2) * 640
                    val yMin = (centerY - height / 2) * 640
                    val xMax = (centerX + width / 2) * 640
                    val yMax = (centerY + height / 2) * 640

                    boxes.add(listOf(xMin, yMin, xMax, yMax))
                    scores.add(confidence)
                    classes.add(classId)
                }
            }

            // Apply Non-Maximum Suppression
            val indices = customNms(boxes, scores, confThreshold, iouThreshold)

            // Filter results
            val finalBoxes = indices.map { boxes[it] }
            val finalScores = indices.map { scores[it] }
            val finalClasses = indices.map { classes[it] }

            return Triple(finalBoxes, finalScores, finalClasses)
        }

        fun getDetectionBoxes(
            output: Array<Array<FloatArray>>,
            imageWidth: Double,
            imageHeight: Double,
            inputSize: Double,
            confThreshold: Double = 0.25,
            iouThreshold: Double = 0.45,
            numClasses: Int = 80
        ): List<DetectionBox> {
            val boxes = mutableListOf<DetectionBox>()
            val (detectedBoxes, detectedScores, detectedClasses) = postprocessOutput(
                output,
                confThreshold,
                iouThreshold,
                numClasses
            )

            for (i in detectedBoxes.indices) {
                val detectedBox = detectedBoxes[i]
                val detectedScore = detectedScores[i]
                val detectedClass = detectedClasses[i]

                val scaleX = imageWidth / inputSize
                val scaleY = imageHeight / inputSize

                val xMin = detectedBox[0] * scaleX
                val yMin = detectedBox[1] * scaleY
                val xMax = detectedBox[2] * scaleX
                val yMax = detectedBox[3] * scaleY

                val x = xMin
                val y = yMin
                val width = xMax - xMin
                val height = yMax - yMin

                val box = DetectionBox(
                    x = x,
                    y = y,
                    width = width,
                    height = height,
                    label = CocoClassNames.VALUES.getOrElse(detectedClass) { "Unknown" },
                    confidence = detectedScore
                )

                boxes.add(box)
            }
            return boxes
        }
    }
}