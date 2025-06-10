package com.ridwanfatur.yolodemo.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ridwanfatur.yolodemo.RouteConstants

@Composable
fun HomePage(navController: NavHostController) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = { navController.navigate(RouteConstants.TEST_ASSET_PAGE) }) {
                Text("Test Asset Image")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate(RouteConstants.TEST_CAMERA_PAGE) }) {
                Text("Test Camera")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate(RouteConstants.CAPTURE_DETECT_PAGE) }) {
                Text("Capture and Detect")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate(RouteConstants.LIVE_DETECTION_PAGE) }) {
                Text("Live Detection")
            }
        }
    }
}