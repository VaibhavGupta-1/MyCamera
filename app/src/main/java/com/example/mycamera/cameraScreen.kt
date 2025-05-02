package com.example.mycamera

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.apply
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@Composable
fun permission() {
    val permission=listOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.RECORD_AUDIO
    )
    val isGranted=remember {
        mutableStateOf(false)
    }
    val context= LocalContext.current
    val launcher= rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
            permission->
            isGranted.value=permission[android.Manifest.permission.CAMERA]==true
        }
    )
    if(isGranted.value){
        cameraScreen()
    }else{
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Button(
                onClick = {
                    launcher.launch(permission.toTypedArray())
                }
            ) {
                Text(text = "Request Permission")
            }
        }
    }
}
@Composable
fun cameraScreen() {
    val context= LocalContext.current
    val lifecycleOwner= LocalLifecycleOwner.current
    val previewView: PreviewView = remember {
        PreviewView(context)
    }
    val cameraSelector= CameraSelector.DEFAULT_BACK_CAMERA
    val preview= Preview.Builder().build()
    val imageCapture= remember {
        ImageCapture.Builder().build()
    }
    val recorder = remember {
        Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
            .build()
    }
    val videoCapture = remember { VideoCapture.withOutput(recorder) }
    var recording: Recording? by remember { mutableStateOf(null) }

    // Mode: photo or video
    var isVideoMode by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        val cameraProvider=context.getCameraProvider()
        cameraProvider.unbindAll()
        if (isVideoMode) {
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                videoCapture
            )
        } else {
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        }
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }
    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ){
        AndroidView(factory = { previewView } , modifier = Modifier.fillMaxSize())
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(16.dp),

            contentAlignment = Alignment.Center
        ){
            IconButton(
                onClick = {
                    captuePhoto(imageCapture,context)
                },
                modifier = Modifier
                    .size(50.dp)
                    .background(color = Color.White, CircleShape)
                    .padding(8.dp)
                    .background(color = Color.Red, CircleShape)
            ) { }
        }
    }
}
private suspend fun Context.getCameraProvider(): ProcessCameraProvider= suspendCoroutine {continuation->
    val cameraProviderFeature= ProcessCameraProvider.getInstance(this)
    cameraProviderFeature.addListener ({
        continuation.resume(cameraProviderFeature.get())
    }, ContextCompat.getMainExecutor(this))
}
private fun captuePhoto(imageCapture: ImageCapture, context: Context) {
    val name="MyCamera_${System.currentTimeMillis()}.jpg"
    val contentValues= ContentValues().apply{
        put(MediaStore.MediaColumns.DISPLAY_NAME,name)
        put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg")
        put(MediaStore.MediaColumns.RELATIVE_PATH,"Pictures/MyCamera-Images")
    }
    val outputOption= ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()
    imageCapture.takePicture(
        outputOption,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback{
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                Toast.makeText(context,"Image Saved to Gallery",Toast.LENGTH_SHORT).show()
            }
            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(context,"Failed to Save Image: ${exception.message}",Toast.LENGTH_SHORT).show()
            }
        }
    )
}