package com.example.myapplication.pages

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.AssetConstants
import com.example.myapplication.ModelConstants
import com.example.myapplication.components.DetectionBoxUI
import com.example.myapplication.utils.DetectionBox
import com.example.myapplication.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.IOException

private const val TAG = "TestAssetImagePage"

@Composable
fun TestAssetImagePage(tflite: Interpreter?) {
    val context = LocalContext.current
    var detectionBoxes by remember { mutableStateOf<List<DetectionBox>>(emptyList()) }

    val bitmap = remember {
        try {
            context.assets.open(AssetConstants.EXAMPLE_IMAGE_PATH).use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to load bitmap from assets", e)
            null
        }
    }

    LaunchedEffect(bitmap) {
        bitmap?.let {
            detectionBoxes = withContext(Dispatchers.Default) {
                Utils.runInference(tflite, it, TAG)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Image from assets",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Gray)
                    )
                }

                DetectionBoxUI(
                    detectionBoxes = detectionBoxes,
                    boxWidth = ModelConstants.INPUT_SIZE.toFloat(),
                    boxHeight = ModelConstants.INPUT_SIZE.toFloat()
                )
            }
        }
    }
}
