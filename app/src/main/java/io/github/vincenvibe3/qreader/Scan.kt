package io.github.vincenvibe3.qreader

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import io.github.vincentvibe3.authenticator.scanner.QrScanner

fun setupCamera(
    context: Context,
    lifecycleOwner:LifecycleOwner,
    preview: Preview,
    onScan:(String, String)->Unit,
    onCameraSet: (Camera)->Unit
){
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        // Used to bind the lifecycle of cameras to the lifecycle owner
        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

        val imageAnalysis = ImageAnalysis.Builder()
            .build()
            .apply {
                setAnalyzer(MainActivity.imageProcessingExecutor, QrScanner(onScan))
            }


        // Select back camera as a default
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()

            // Bind use cases to camera
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, imageAnalysis)
            onCameraSet(camera)
        } catch(exc: Exception) {
            Log.e(MainActivity.TAG, "Use case binding failed", exc)
        }

    }, ContextCompat.getMainExecutor(context))
}

@Composable
fun CameraPreview(onQrScanned: (String, String)->Unit, onCameraReady: (Camera)->Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var camera:Camera? by remember {
        mutableStateOf(null)
    }
    var scale by remember { mutableStateOf(
        camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
    ) }
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
        .transformable(previewTransform)
    ){
        AndroidView(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            factory = { context ->
                val previewView = PreviewView(context)

                // Preview
                val preview = Preview.Builder()
                    .build()
                    .apply {
                        setSurfaceProvider(previewView.surfaceProvider)
                    }
                setupCamera(context, lifecycleOwner, preview, onQrScanned){
                    camera = it
                    onCameraReady(it)
                }
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
//                    .offset()
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



}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Scanner() {
    val context = LocalContext.current
    val initialCameraPermission = ContextCompat.checkSelfPermission(
        context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    var permissionsOk by remember {
        mutableStateOf(initialCameraPermission)
    }
    var camera:Camera? by remember {
        mutableStateOf(null)
    }
    var currentData:String? by remember {
        mutableStateOf(null)
    }
    var format:String? by remember {
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
                    var offset by remember {
                        mutableStateOf(0f)
                    }
                    val cornerRadius by animateDpAsState(targetValue = if (currentData==null){10.dp} else {0.dp}, label="cornerAnimation")
                    val transitionState by remember {
                        mutableStateOf(
                            MutableTransitionState(false)
                        )
                    }
                    // When dismissed reset offset and data
                    LaunchedEffect(key1 = transitionState.isIdle){
                        if (!transitionState.currentState&&transitionState.isIdle){
                            offset = 0f
                            currentData = null
                        }
                    }
                    // Starts animation to display info when not data is not null
                    LaunchedEffect(key1 = currentData!=null){
                        if (currentData!=null){
                            transitionState.targetState = true
                        }
                    }
                    Column(verticalArrangement = Arrangement.Bottom) {
                        AnimatedVisibility(visibleState = transitionState, enter = slideInVertically{
                            it/2
                        }+ fadeIn(), exit = slideOutVertically { it/2 }+ fadeOut())  {
                            Card(
                                Modifier
                                    .fillMaxWidth()
                                    .offset(y = offset.dp)
                                    .animateContentSize()
                                    .pointerInput(Unit) {
                                        this.detectVerticalDragGestures({

                                        }, {
                                            if (offset > 50) {
                                                transitionState.targetState = false
                                            } else {
                                                offset = 0f
                                            }
                                        }) { change, dragAmount ->
                                            val next = offset + dragAmount
                                            if (next >= 0f) {
                                                offset += dragAmount
                                            }
                                        }
                                    },
                                shape = RoundedCornerShape(10.dp, 10.dp),
                                colors = CardDefaults.elevatedCardColors()
                            ) {
                                Column(Modifier.padding(20.dp, 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.Center){
                                        Spacer(modifier = Modifier
                                            .clip(
                                                RoundedCornerShape(5.dp)
                                            )
                                            .background(Color.Gray)
                                            .width(50.dp)
                                            .height(5.dp))
                                    }
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = format?:"",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
//                                        IconButton(onClick = { currentData = null }) {
//                                            Icon(
//                                                Icons.Default.Close,
//                                                contentDescription = null
//                                            )
//                                        }
                                    }
                                    val data = currentData ?: ""
                                    when (SchemeIdentifier.identify(data)) {
                                        SchemeIdentifier.PayloadTypes.WIFI -> {
                                            val wifiInfo = WifiParser.parse(data)
                                            Text(text = "SSID: ${wifiInfo.ssid}")
                                            var showPassword by remember {
                                                mutableStateOf(false)
                                            }
                                            val passwordText by remember {
                                                derivedStateOf {
                                                    if (showPassword) {
                                                        wifiInfo.password
                                                    } else {
                                                        (1..(wifiInfo.password?.length ?: 0)).joinToString("") {
                                                            "•"
                                                        }
                                                    }
                                                }
                                            }
                                            Row {
                                                Text(text = "Password: $passwordText")
                                                Button(onClick = {
                                                    showPassword = !showPassword
                                                }) {
                                                    Icon(Icons.Default.Lock, null)
                                                }
                                            }
                                            Text(text = "Authentication Type: ${wifiInfo.auth}")
                                            Text(text = "Hidden: ${wifiInfo.hidden}")
                                        }

                                        SchemeIdentifier.PayloadTypes.LINK -> {
                                            Text(text = data)
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(
                                                    10.dp
                                                )
                                            ) {
                                                Button(onClick = {
                                                    val browserIntent = Intent(
                                                        Intent.ACTION_VIEW,
                                                        Uri.parse(data)
                                                    )
                                                    context.startActivity(browserIntent)
                                                }) {
                                                    Text(text = "Open Link")
                                                }
                                                TextButton(onClick = {
                                                    val clipboardManager: ClipboardManager =
                                                        context.getSystemService(
                                                            CLIPBOARD_SERVICE
                                                        ) as ClipboardManager
                                                    val clipData =
                                                        ClipData.newPlainText("Link", data)
                                                    clipboardManager.setPrimaryClip(clipData)
                                                }) {
                                                    Text(text = "Copy to Clipboard")
                                                }
                                            }
                                        }

                                        else -> {
                                            Text(text = data)
                                            Button(onClick = {
                                                val clipboardManager: ClipboardManager =
                                                    context.getSystemService(
                                                        CLIPBOARD_SERVICE
                                                    ) as ClipboardManager
                                                val clipData =
                                                    ClipData.newPlainText("Link", data)
                                                clipboardManager.setPrimaryClip(clipData)
                                            }) {
                                                Text(text = "Copy to Clipboard")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Card(
                            Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(cornerRadius, cornerRadius)
                        ) {
                            Column(
                                modifier = Modifier
                                    .animateContentSize(),
//                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    Modifier
                                        .navigationBarsPadding()
                                        .padding(10.dp)
                                        .fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    IconButton(onClick = {
                                        val currentTorch = camera?.cameraInfo?.torchState?.value ?: 0
                                        camera?.cameraControl?.enableTorch(currentTorch == 0)
                                    }) {
                                        Icon(painterResource(id = R.drawable.flashlight), contentDescription = null, tint=MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
//                    }
                }
            ) { _ ->
                CameraPreview({ data, codeFormat ->
                    currentData = data
                    format = codeFormat
                }){
                    camera = it
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