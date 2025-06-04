# Android Kotlin YOLO11 TFLite Demo

This project is an Android application built with **Kotlin** and **Jetpack Compose**, designed to perform on-device inference using the `yolo11n_float32.tflite` model from **Ultralytics**.

Demo: https://youtube.com/shorts/UR3cWbvP0pY
## 📦 Model
- Model used: `yolo11n_float32.tflite`
- Source: Converted from YOLO11 by [Ultralytics](https://github.com/ultralytics/ultralytics)

## Features

The app contains **4 main pages**, each serving a specific purpose:

### 1. Test Asset Image
- **Purpose:** Validate the YOLO model by running inference on a predefined image (`image1`) and comparing the results with expected outputs.
- **Reference:** Uses inference results generated from a Jupyter Notebook (`YOLO11` repo) as baseline.

### 2. Test Camera Stream
- **Purpose:** Verify camera stream configuration.
- **Details:** Check camera orientation, rotation, and flip to ensure proper alignment before applying the model.

### 3. Capture and Detect (Single Inference)
- **Purpose:** Capture a single frame from the camera and run YOLO inference on it.
- **Use Case:** Useful for testing detection on demand (non-continuous).

### 4. Live Detection (Real-time)
- **Purpose:** Run real-time object detection using the YOLO11n TFLite model on a live camera feed.
- **Use Case:** Demonstrates continuous frame capture and detection for practical deployment scenarios.

---

## Getting Started

### Requirements
- Android Studio
- Android device or emulator with camera support
- TFLite model: `yolo11n_float32.tflite` from Ultralytics (already included in assets)
