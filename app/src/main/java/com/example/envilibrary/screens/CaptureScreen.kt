package com.example.envilibrary.screens

import android.Manifest
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.envilibrary.viewmodel.CaptureViewModel
import com.example.envilibrary.data.repository.Result
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CaptureScreen(viewModel: CaptureViewModel = hiltViewModel(), navController: NavController, onTabChange: () -> Unit) {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(false) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasCameraPermission = isGranted
        }
    )

    LaunchedEffect(Unit) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (hasCameraPermission) {
        CameraPreview(viewModel, navController, onTabChange)
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission is required to use this feature.")
        }
    }
}

@Composable
fun CameraPreview(viewModel: CaptureViewModel, navController: NavController, onTabChange: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture by remember { mutableStateOf(ImageCapture.Builder().build()) }
    val previewView = remember { PreviewView(context) }
    val cameraExecutor = ContextCompat.getMainExecutor(context)
    var recognizedText by remember { mutableStateOf<String?>(null) }
    var capturedImageFile by remember { mutableStateOf<File?>(null) }
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(cameraProviderFuture) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraPreview", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Scaffold(
        scaffoldState = scaffoldState,
        bottomBar = {
            if (viewModel.textSaved.value) {
                BottomAppBar {
                    Text("Text saved to library!", modifier = Modifier.weight(1f))
                    Button(onClick = {
                        onTabChange()
                        navController.navigate("library")
                    }) {
                        Text("Go to Library")
                    }
                }
            }
        },
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                if (capturedImageFile == null && recognizedText == null) {
                    AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

                    IconButton(
                        onClick = {
                            val photoFile = File(context.externalMediaDirs.firstOrNull(), "${System.currentTimeMillis()}.jpg")
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                            imageCapture.takePicture(
                                outputOptions,
                                cameraExecutor,
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onError(exception: ImageCaptureException) {
                                        Log.e("CameraPreview", "Photo capture failed: ${exception.message}", exception)
                                    }

                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        Log.d("CameraPreview", "Photo captured successfully")
                                        Log.d("CameraPreview", "Photo file: $photoFile")
                                        capturedImageFile = photoFile
                                        viewModel.uploadImage(photoFile)
                                    }
                                }
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Capture Photo",
                            tint = Color.White,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                } else if (recognizedText != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = recognizedText ?: "")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                viewModel.saveToLibrary(context, recognizedText ?: "")
                                capturedImageFile = null
                                recognizedText = null
                                coroutineScope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar("Text saved to library!")
                                }
                            }) {
                                Text("Save text to library")
                            }
                        }
                    }
                } else {
                    Image(
                        bitmap = BitmapFactory.decodeFile(capturedImageFile!!.absolutePath).asImageBitmap(),
                        contentDescription = "Captured image",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                val result = viewModel.ocrResponse.value

                if (result is Result.Loading && capturedImageFile != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            bitmap = BitmapFactory.decodeFile(capturedImageFile!!.absolutePath).asImageBitmap(),
                            contentDescription = "Captured image",
                            modifier = Modifier.fillMaxSize()
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .background(Color(0x80000000))
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Text("OCR in progress...", color = Color.White)
                        }
                    }
                }

                when (result) {
                    is Result.Success<*> -> {
                        recognizedText = (result as Result.Success<String>).data
                        Log.d("CameraPreview", "OCR succeeded: $recognizedText")
                    }
                    is Result.NoTextFound -> {
                        Log.d("CameraPreview", "OCR result: No text found")
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No text found in the image", color = Color.Red)
                        }
                    }
                    is Result.Error -> {
                        Log.e("CameraPreview", "OCR failed: ${(result as Result.Error).message}")
                        Text("Error: ${(result as Result.Error).message}", color = Color.Red, modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    )
}
