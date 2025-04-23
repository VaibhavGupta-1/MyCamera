package com.example.mycamera

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

    LaunchedEffect(Unit) {
        val cameraProvider=context.getCameraProvider()

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner=lifecycleOwner,
            cameraSelector=cameraSelector,
            preview,
            imageCapture

        )
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ){
        AndroidView(factory = { previewView } , modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(16.dp),

            contentAlignment = Alignment.Center
        ){

            IconButton(
                onClick = {},
                modifier = Modifier.size(50.dp)
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