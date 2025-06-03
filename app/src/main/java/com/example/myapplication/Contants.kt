package com.example.myapplication

object AssetConstants {
    const val MODEL_PATH = "yolo11n_float32.tflite"
    const val EXAMPLE_IMAGE_PATH = "image1.jpg"
}

object RouteConstants {
    const val HOME_PAGE = "home"
    const val TEST_ASSET_PAGE = "test_asset"
    const val TEST_CAMERA_PAGE = "test_camera"
    const val CAPTURE_DETECT_PAGE = "capture_detect"
    const val LIVE_DETECTION_PAGE = "live_detection"
}

object ModelConstants {
    const val INPUT_SIZE = 640
}

object CocoClassNames {
    val VALUES = listOf(
        "person",
        "bicycle",
        "car",
        "motorbike",
        "airplane",
        "bus",
        "train",
        "truck",
        "boat",
        "traffic light",
        "fire hydrant",
        "stop sign",
        "parking meter",
        "bench",
        "bird",
        "cat",
        "dog",
        "horse",
        "sheep",
        "cow",
        "elephant",
        "bear",
        "zebra",
        "giraffe",
        "backpack",
        "umbrella",
        "handbag",
        "tie",
        "suitcase",
        "frisbee",
        "skis",
        "snowboard",
        "sports ball",
        "kite",
        "baseball bat",
        "baseball glove",
        "skateboard",
        "surfboard",
        "tennis racket",
        "bottle",
        "wine glass",
        "cup",
        "fork",
        "knife",
        "spoon",
        "bowl",
        "banana",
        "apple",
        "sandwich",
        "orange",
        "broccoli",
        "carrot",
        "hot dog",
        "pizza",
        "donut",
        "cake",
        "chair",
        "sofa",
        "pottedplant",
        "bed",
        "diningtable",
        "toilet",
        "tvmonitor",
        "laptop",
        "mouse",
        "remote",
        "keyboard",
        "cell phone",
        "microwave",
        "oven",
        "toaster",
        "sink",
        "refrigerator",
        "book",
        "clock",
        "vase",
        "scissors",
        "teddy bear",
        "hair drier",
        "toothbrush"
    )
}