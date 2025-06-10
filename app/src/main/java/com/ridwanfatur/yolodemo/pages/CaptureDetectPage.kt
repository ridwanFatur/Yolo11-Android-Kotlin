package com.ridwanfatur.yolodemo.pages

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ridwanfatur.yolodemo.ModelConstants
import com.ridwanfatur.yolodemo.components.DetectionBoxUI
import com.ridwanfatur.yolodemo.utils.DetectionBox
import com.ridwanfatur.yolodemo.utils.ImageUtils
import com.ridwanfatur.yolodemo.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import java.util.concurrent.Executors

private const val TAG = "CaptureDetectPage"

@Composable
fun CaptureDetectPage(tflite: Interpreter?) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val inferenceExecutor = remember { Executors.newSingleThreadExecutor() }

    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var detectionBoxes by remember { mutableStateOf<List<DetectionBox>>(emptyList()) }

    val imageCapture = remember { ImageCapture.Builder().build() }
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
                            coroutineScope = coroutineScope,
                            imageCapture = imageCapture
                        )
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }


            if (capturedImage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Image(
                        bitmap = capturedImage!!.asImageBitmap(),
                        contentDescription = "Captured Image",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )

                    DetectionBoxUI(
                        detectionBoxes = detectionBoxes,
                        boxWidth = ModelConstants.INPUT_SIZE.toFloat(),
                        boxHeight = ModelConstants.INPUT_SIZE.toFloat(),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Button(
                    onClick = {
                        if (capturedImage == null) {
                            imageCapture.takePicture(
                                inferenceExecutor,
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        val processedImage = ImageUtils.preprocessImage(
                                            originImage = image.toBitmap(),
                                            screenSize = screenSize!!,
                                        )

                                        capturedImage = processedImage

                                        coroutineScope.launch {
                                            detectionBoxes =
                                                Utils.runInference(tflite, processedImage, TAG)
                                        }
                                        image.close()
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        Log.e(TAG, "Image capture failed", exception)
                                    }
                                }
                            )
                        } else {
                            capturedImage = null
                            detectionBoxes = emptyList()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(text = if (capturedImage == null) "Take Picture" else "Back")
                }
            }
        }


    }
}

private fun setupCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    coroutineScope: CoroutineScope,
    imageCapture: ImageCapture
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    coroutineScope.launch {
        try {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            Log.e(TAG, "Camera binding failed", e)
        }
    }
}
