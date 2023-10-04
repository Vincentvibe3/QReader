package io.github.vincenvibe3.qreader

import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import io.github.vincentvibe3.authenticator.scanner.QrScanner
import kotlin.math.abs

@Composable
fun CameraPreview(onQrScanned: (String)->Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var camera:Camera? by remember {
        mutableStateOf(null)
    }
    var scale by remember { mutableStateOf(
        camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
    ) }
    val zoomRatioMin = camera?.cameraInfo?.zoomState?.value?.minZoomRatio ?: 0f
    val zoomRatioMax = camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 0f
    val zoomRatioInterval = zoomRatioMax - zoomRatioMin
    val previewTransform = rememberTransformableState{ zoomChange, offsetChange, rotationChange ->
        scale = camera?.cameraInfo?.zoomState?.value?.zoomRatio?.let {
            it * zoomChange
        } ?: 0f
    }
    LaunchedEffect(key1 = scale){
        camera?.cameraControl?.setZoomRatio(scale)
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .transformable(previewTransform)){
        AndroidView(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            factory = { context ->
                val previewView = PreviewView(context)

                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

                cameraProviderFuture.addListener({
                    // Used to bind the lifecycle of cameras to the lifecycle owner
                    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                    // Preview
                    val preview = Preview.Builder()
                        .build()
                        .apply {
                            setSurfaceProvider(previewView.surfaceProvider)
                        }


                    val imageAnalysis = ImageAnalysis.Builder()
                        .build()
                        .apply {
                            setAnalyzer(MainActivity.imageProcessingExecutor, QrScanner(onQrScanned))
                        }


                    // Select back camera as a default
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        // Unbind use cases before rebinding
                        cameraProvider.unbindAll()

                        // Bind use cases to camera
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview, imageAnalysis)

                    } catch(exc: Exception) {
                        Log.e(MainActivity.TAG, "Use case binding failed", exc)
                    }

                }, ContextCompat.getMainExecutor(context))
                previewView
            })

        Canvas(modifier = Modifier
            .fillMaxSize(), onDraw = {
            drawRect(
                color=Color.Black,
                alpha = 0.5f
            )
            drawRoundRect(
                topLeft= Offset(size.width/4, (size.height-size.width/2)/2),
                color = Color.White,
                size = Size(size.width/2, size.width/2),
                style = Fill,
                cornerRadius = CornerRadius(10f),
                blendMode = BlendMode.Clear
            )
            drawRoundRect(
                topLeft= Offset(size.width/4, (size.height-size.width/2)/2),
                color = Color.White,
                size = Size(size.width/2, size.width/2),
                style = Stroke(3f),
                cornerRadius = CornerRadius(10f),
            )

            //alignment squares
            drawRoundRect(
                topLeft= Offset(size.width/4+size.width/20, (size.height-size.width/2)/2+size.width/20),
                color = Color.White,
                size = Size(size.width/15, size.width/15),
                style = Stroke(3f),
                cornerRadius = CornerRadius(10f),
                alpha = 0.5f
            )
            drawRoundRect(
                topLeft= Offset(size.width/4*3-size.width/15-size.width/20, (size.height-size.width/2)/2+size.width/20),
                color = Color.White,
                size = Size(size.width/15, size.width/15),
                style = Stroke(3f),
                cornerRadius = CornerRadius(10f),
                alpha = 0.5f
            )
            drawRoundRect(
                topLeft= Offset(size.width/4+size.width/20, (size.height-size.width/2)/2+size.width/2-size.width/15-size.width/20),
                color = Color.White,
                size = Size(size.width/15, size.width/15),
                style = Stroke(3f),
                cornerRadius = CornerRadius(10f),
                alpha = 0.5f
            )
        })
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset()
                    .fillMaxHeight(0.5f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    "Scan the QR code \nby placing it in the square",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
            Text(text = "$scale", color = Color.White)
            LinearProgressIndicator((scale-zoomRatioMin)/zoomRatioInterval)
            Button(onClick = {
                val currentTorch = camera?.cameraInfo?.torchState?.value ?: 0
                camera?.cameraControl?.enableTorch(currentTorch == 0)
            }) {
                Text(text = "Flashlight")
            }
        }
    }



}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Scanner() {
    val context = LocalContext.current
//    val systemUiController = rememberSystemUiController()
//    DisposableEffect(systemUiController) {
//        // Update all of the system bar colors to be transparent, and use
//        // dark icons if we're in light theme
//        systemUiController.setSystemBarsColor(
//            color = Color.Transparent,
//            darkIcons = false
//        )
//
//        systemUiController.setNavigationBarColor(
//            color = Color.Transparent,
//            darkIcons = false
//        )
//
//        // setStatusBarColor() and setNavigationBarColor() also exist
//
//        onDispose {}
//    }
    val initialCameraPermission = ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    var permissionsOk by remember {
        mutableStateOf(initialCameraPermission)
    }

    var currentData:String? by remember {
        mutableStateOf(null)
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            if (!it){
                Toast.makeText(context, "Permissions Refused", Toast.LENGTH_LONG).show()
            }
            permissionsOk=it
        })
    if (permissionsOk) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = {
                    AnimatedVisibility(visible = currentData!=null, enter = slideInVertically{
                        it/2
                    }+ fadeIn(), exit = slideOutVertically { it/2 }+ fadeOut()) {
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "QR Code", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    IconButton(onClick = { currentData=null }) {
                                        Icon(Icons.Default.Close, contentDescription = null)
                                    }
                                }
                                val data = currentData ?: ""
                                when(SchemeIdentifier.identify(data)){
                                    SchemeIdentifier.PayloadTypes.WIFI -> {
                                        val ssid = data.split(";")[0].split(":")[2]
                                        val password = data.split(";")[2].split(":")[1]
                                        Text(text = "SSID: $ssid")
                                        var showPassword by remember {
                                            mutableStateOf(false)
                                        }
                                        val passwordText by remember {
                                            derivedStateOf {
                                                if (showPassword){
                                                    password
                                                } else {
                                                    (1..password.length).joinToString("") {
                                                        "•"
                                                    }
                                                }
                                            }
                                        }
                                        Row {
                                            Text(text = "Password: $passwordText")
                                            Button(onClick = { showPassword=!showPassword }) {
                                                Text("Show password")
                                            }
                                        }
                                    }
                                    SchemeIdentifier.PayloadTypes.LINK -> {
                                        Text(text = data)
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Button(onClick = { /*TODO*/ }) {
                                                Text(text = "Open Link")
                                            }
                                            TextButton(onClick = { /*TODO*/ }) {
                                                Text(text = "Copy to Clipboard")
                                            }
                                        }
                                    }
                                    else -> {
                                        Text(text = data)
                                        TextButton(onClick = { /*TODO*/ }) {
                                            Text(text = "Copy to Clipboard")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            ) { _ ->
                CameraPreview{
                    currentData = it
                }
            }

        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Text(text = "You must give permission to read QR codes.")
            Button(onClick = { launcher.launch(android.Manifest.permission.CAMERA) }) {
                Text("Allow")
            }
        }

    }
}