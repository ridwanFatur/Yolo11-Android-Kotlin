package com.example.myapplication.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.example.myapplication.utils.DetectionBox
import java.text.DecimalFormat

@Composable
fun DetectionBoxUI(
    detectionBoxes: List<DetectionBox>, boxWidth: Float,
    boxHeight: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        detectionBoxes.forEach { box ->
            val x = (box.x / boxWidth).toFloat() * size.width
            val y = (box.y / boxHeight).toFloat() * size.height
            val width = (box.width / boxWidth).toFloat() * size.width
            val height = (box.height / boxHeight).toFloat() * size.height

            drawRect(
                color = Color.Red.copy(alpha = 0.5f),
                topLeft = Offset(x, y),
                size = Size(width, height),
                style = Stroke(width = 10f)
            )

            drawIntoCanvas { canvas ->
                val text = "${DecimalFormat("#.##").format(box.confidence)} ${box.label}"

                val paint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 12f * density
                    textAlign = Paint.Align.LEFT
                    isAntiAlias = true
                }

                val backgroundPaint = Paint().apply {
                    color = android.graphics.Color.argb(
                        (0.5f * 255).toInt(),
                        255,
                        0,
                        0
                    )
                    style = Paint.Style.FILL
                }

                val bounds = android.graphics.Rect()
                paint.getTextBounds(text, 0, text.length, bounds)

                canvas.nativeCanvas.drawRect(
                    x + 4f,
                    y + 6f,
                    x + 4f + bounds.width() + 8f,
                    y + 6f + bounds.height() + 8f,
                    backgroundPaint
                )

                canvas.nativeCanvas.drawText(
                    text,
                    x + 8f,
                    y + 20f,
                    paint
                )
            }
        }
    }
}