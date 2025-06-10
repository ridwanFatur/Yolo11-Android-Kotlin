package com.ridwanfatur.yolodemo.pages

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ridwanfatur.yolodemo.utils.ImageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "TestCameraPage"

@Composable
fun TestCameraPage() {
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val isProcessing = remember { AtomicBoolean(false) }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var screenSize by remember { mutableStateOf<Size?>(null) }

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
                    .aspectRatio(screenSize!!.width / screenSize!!.height)
            ) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        setupCamera(
                            context = ctx,
                            lifecycleOwner = lifecycleOwner,
                            previewView = previewView,
                            coroutineScope = coroutineScope,
                            isProcessing = isProcessing,
                            setBitmap = { newBitmap ->
                                capturedImage = newBitmap
                            },
                            screenSize = screenSize!!
                        )
                        previewView
                    },
                    modifier = Modifier
                        .fillMaxSize()
                )
            }

            if (capturedImage != null) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Image(
                        bitmap = capturedImage!!.asImageBitmap(),
                        contentDescription = "Displayed Image",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )
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
    isProcessing: AtomicBoolean,
    setBitmap: (Bitmap?) -> Unit,
    screenSize: Size,
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        try {
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val imageAnalysis = ImageAnalysis.Builder()
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

            val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { image ->
                if (!isProcessing.getAndSet(true)) {
                    coroutineScope.launch {
                        withContext(Dispatchers.Default) {
                            try {
                                setBitmap(
                                    ImageUtils.preprocessImage(image.toBitmap(), screenSize)
                                )
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

            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
        } catch (e: Exception) {
            Log.e(TAG, "Error binding camera use cases: ${e.message}")
        }
    }, ContextCompat.getMainExecutor(context))
}