package com.ridwanfatur.yolodemo

import android.Manifest
import android.content.res.AssetFileDescriptor
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ridwanfatur.yolodemo.pages.CaptureDetectPage
import com.ridwanfatur.yolodemo.pages.HomePage
import com.ridwanfatur.yolodemo.pages.LiveDetectionPage
import com.ridwanfatur.yolodemo.pages.TestAssetImagePage
import com.ridwanfatur.yolodemo.pages.TestCameraPage
import com.ridwanfatur.yolodemo.ui.theme.MyApplicationTheme
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : ComponentActivity() {
    private var tflite: Interpreter? = null
    companion object {
        private const val TAG = "LogMainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { _ -> }
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)

        try {
            tflite = Interpreter(loadModelFile())
            Log.d(TAG, "Success loading model")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model", e)
            e.printStackTrace()
        }

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = RouteConstants.HOME_PAGE) {
                    composable(RouteConstants.HOME_PAGE) { HomePage(navController) }
                    composable(RouteConstants.TEST_ASSET_PAGE) { TestAssetImagePage(tflite) }
                    composable(RouteConstants.TEST_CAMERA_PAGE) { TestCameraPage() }
                    composable(RouteConstants.CAPTURE_DETECT_PAGE) { CaptureDetectPage(tflite) }
                    composable(RouteConstants.LIVE_DETECTION_PAGE) { LiveDetectionPage(tflite) }
                }
            }
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = assets.openFd(AssetConstants.MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    override fun onDestroy() {
        super.onDestroy()
        tflite?.close()
    }
}