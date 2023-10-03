package io.github.vincentvibe3.authenticator

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import io.github.vincentvibe3.authenticator.scanner.QrScanner
import io.github.vincenvibe3.qreader.MainActivity
import io.github.vincenvibe3.qreader.SchemeIdentifier
import io.github.vincenvibe3.qreader.ui.theme.QReaderTheme
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraPreview(onQrScanned: (String)->Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(modifier = Modifier.fillMaxSize()){
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
                        cameraProvider.bindToLifecycle(
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