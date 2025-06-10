package com.ridwanfatur.yolodemo.pages

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ridwanfatur.yolodemo.ModelConstants
import com.ridwanfatur.yolodemo.components.DetectionBoxUI
import com.ridwanfatur.yolodemo.utils.DetectionBox
import com.ridwanfatur.yolodemo.utils.ImageUtils
import com.ridwanfatur.yolodemo.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "LiveDetectionPage"

@Composable
fun LiveDetectionPage(tflite: Interpreter?) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val inferenceExecutor = remember { Executors.newSingleThreadExecutor() }

    var detectionBoxes by remember { mutableStateOf<List<DetectionBox>>(emptyList()) }
    val isProcessing = remember { AtomicBoolean(false) }

    var screenSize by remember { mutableStateOf<Size?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            inferenceExecutor.shutdown()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                if (screenSize == null) {
                    screenSize = Size(size.width.toFloat(), size.height.toFloat())
                    Log.d(
                        TAG,
                        "Screen size initialized: ${screenSize?.width} | ${screenSize?.height}"
                    )
                }
            }
    ) {
        if (screenSize != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        setupCamera(
                            context = ctx,
                            lifecycleOwner = lifecycleOwner,
                            previewView = previewView,
                            tflite = tflite,
                            coroutineScope = coroutineScope,
                            isProcessing = isProcessing,
                            onDetectionBoxesUpdated = { boxes ->
                                detectionBoxes = boxes
                            },
                            screenSize = screenSize!!
                        )
                        previewView
                    },
                    modifier = Modifier
                        .fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                DetectionBoxUI(
                    detectionBoxes = detectionBoxes,
                    boxWidth = ModelConstants.INPUT_SIZE.toFloat(),
                    boxHeight = ModelConstants.INPUT_SIZE.toFloat(),
                )
            }
        }

    }
}

private fun setupCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    tflite: Interpreter?,
    coroutineScope: CoroutineScope,
    isProcessing: AtomicBoolean,
    onDetectionBoxesUpdated: (List<DetectionBox>) -> Unit,
    screenSize: Size,
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        try {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val imageAnalysis = ImageAnalysis.Builder()
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { image ->
                if (!isProcessing.getAndSet(true)) {
                    coroutineScope.launch {
                        withContext(Dispatchers.Default) {
                            try {
                                val processedImage = ImageUtils.preprocessImage(
                                    originImage = image.toBitmap(),
                                    screenSize = screenSize,
                                )
                                val results = Utils.runInference(
                                    tflite = tflite,
                                    bitmap = processedImage,
                                    logName = TAG,
                                )
                                onDetectionBoxesUpdated(results)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error during inference: ${e.message}")
                            } finally {
                                isProcessing.set(false)
                                image.close()
                            }
                        }
                    }
                } else {
                    image.close()
                }
            }

            val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
        } catch (e: Exception) {
            Log.e(TAG, "Error binding camera use cases: ${e.message}")
        }
    }, ContextCompat.getMainExecutor(context))
}
