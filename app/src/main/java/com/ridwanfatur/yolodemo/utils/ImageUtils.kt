package com.ridwanfatur.yolodemo.utils


import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.ui.geometry.Size
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

class ImageUtils {
    companion object {
        fun convertBitmapToArray(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
            val width = bitmap.width
            val height = bitmap.height

            val output = Array(1) {
                Array(height) {
                    Array(width) {
                        FloatArray(3)
                    }
                }
            }
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val pixel = bitmap.getPixel(x, y)
                    output[0][y][x][0] = pixel.red / 255f
                    output[0][y][x][1] = pixel.green / 255f
                    output[0][y][x][2] = pixel.blue / 255f
                }
            }

            return output
        }

        private fun cropBitmapCenter(source: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
            val xOffset = (source.width - targetWidth) / 2
            val yOffset = (source.height - targetHeight) / 2

            val safeX = xOffset.coerceAtLeast(0)
            val safeY = yOffset.coerceAtLeast(0)
            val safeWidth = targetWidth.coerceAtMost(source.width)
            val safeHeight = targetHeight.coerceAtMost(source.height)

            return Bitmap.createBitmap(source, safeX, safeY, safeWidth, safeHeight)
        }

        fun preprocessImage(originImage: Bitmap, screenSize: Size): Bitmap {
            val targetCropHeight = (screenSize.width * originImage.width) / screenSize.height
            val croppedImage = cropBitmapCenter(
                originImage,
                targetWidth = originImage.width,
                targetHeight = targetCropHeight.toInt(),
            )
            val matrix = Matrix().apply {
                postRotate(90f)
            }
            return Bitmap.createBitmap(
                croppedImage,
                0,
                0,
                croppedImage.width,
                croppedImage.height,
                matrix,
                true
            )
        }
    }
}