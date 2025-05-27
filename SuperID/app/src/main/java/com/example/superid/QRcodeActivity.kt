package com.example.superid

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.isGranted

class QRCodeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QRScannerUI { finish() }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QRScannerUI(onBack: () -> Unit) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Escaneie o QR Code") })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (permissionState.status.isGranted) {
                CameraPreview(onQRCodeScanned = { token ->
                    confirmarLogin(token, context)
                })
            } else {
                Text("Permissão de câmera negada.")
            }
        }
    }
}

@Composable
fun CameraPreview(onQRCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = androidx.camera.view.PreviewView(ctx)
            val cameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            val barcodeScanner = BarcodeScanning.getClient()
            val analyzer = ImageAnalysis.Builder().build().apply {
                setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                    processImageProxy(barcodeScanner, imageProxy, onQRCodeScanned)
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                ctx as ComponentActivity,
                cameraSelector,
                preview,
                analyzer
            )

            previewView
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
    )
}

fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onQRCodeScanned: (String) -> Unit
) {
    val mediaImage = imageProxy.image ?: return imageProxy.close()

    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            for (barcode in barcodes) {
                barcode.rawValue?.let {
                    Log.d("QRScanner", "QR Code detectado: $it")
                    onQRCodeScanned(it)
                }
            }
        }
        .addOnFailureListener { Log.e("QRScanner", "Erro: ${it.message}") }
        .addOnCompleteListener { imageProxy.close() }
}

fun confirmarLogin(token: String, context: android.content.Context) {
    val userId = Firebase.auth.currentUser?.uid
    if (userId == null) {
        Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
        return
    }

    val data = hashMapOf(
        "loginToken" to token,
        "userId" to userId
    )

    Firebase.functions
        .getHttpsCallable("confirmLogin")
        .call(data)
        .addOnSuccessListener {
            Toast.makeText(context, "Login confirmado!", Toast.LENGTH_LONG).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Erro: ${it.message}", Toast.LENGTH_LONG).show()
        }
}
