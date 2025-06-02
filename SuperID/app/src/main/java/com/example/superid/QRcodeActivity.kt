package com.example.superid

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.superid.ui.theme.SuperIDTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.firebase.auth.ktx.auth
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class QRCodeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SuperIDTheme {
                QRCodeScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun QRCodeScreen(onBack: () -> Unit = {}) {
    val functions = Firebase.functions
    val auth = Firebase.auth

    var uiState by remember { mutableStateOf<QRCodeScreenState>(QRCodeScreenState.Idle) }
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var allowScanning by remember { mutableStateOf(true) }

    LaunchedEffect(uiState) {
        if (uiState == QRCodeScreenState.Idle) {
            allowScanning = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("SuperID")
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secure",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val state = uiState) {
                QRCodeScreenState.Idle -> {
                    if (cameraPermissionState.status.isGranted) {
                        Text(
                            "Aponte para o QR Code.",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                        Box(modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .aspectRatio(1f)
                        ) {
                            if (allowScanning) {
                                CameraPreview(
                                    onQrCodeScanned = { qrCodeValue ->
                                        if (allowScanning) {
                                            allowScanning = false
                                            uiState = QRCodeScreenState.Processing("Verificando QR Code...")


                                            Log.d("QRCodeDebug", "Valor escaneado do QR Code (loginToken): '$qrCodeValue'")

                                            val currentUser = auth.currentUser
                                            if (currentUser == null) {
                                                Log.e("QRCodeDebug", "Usuário não autenticado ao tentar confirmar login.")
                                                uiState = QRCodeScreenState.Error("Usuário não autenticado. Faça login primeiro.")
                                                return@CameraPreview
                                            }

                                            Log.d("QRCodeDebug", "UID do usuário atual (userId): '${currentUser.uid}'")

                                            val data = hashMapOf(
                                                "loginToken" to qrCodeValue,
                                                "userId" to currentUser.uid
                                            )

                                            Log.d("QRCodeDebug", "Dados enviados para a função: $data")


                                            functions.getHttpsCallable("confirmLogin")
                                                .call(data)
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        val resultData = task.result?.data as? Map<String, Any>
                                                        if (resultData?.get("success") == true) {
                                                            uiState = QRCodeScreenState.Success("Login confirmado com sucesso!")
                                                        } else {
                                                            val errorMessage = resultData?.get("error")?.toString() ?: "Resposta inesperada da função."
                                                            uiState = QRCodeScreenState.Error("Falha ao confirmar: $errorMessage")
                                                        }
                                                    } else {
                                                        val e = task.exception
                                                        var errorMessage = "Erro ao chamar função: ${e?.message}"
                                                        if (e is FirebaseFunctionsException) {
                                                            errorMessage += " (Código: ${e.code}, Detalhes: ${e.details})"
                                                        }
                                                        uiState = QRCodeScreenState.Error(errorMessage)
                                                        Log.e("QRCodeScreen", "Falha na chamada da função", e)
                                                    }
                                                }
                                        }
                                    }
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Aguardando para escanear...")
                                }
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
                                "A permissão da câmera é importante para ler QR Codes. Por favor, conceda a permissão."
                            } else {
                                "Precisamos da permissão da câmera para ler o QR Code."
                            }
                            Text(textToShow, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                                Text("Conceder Permissão")
                            }
                        }
                    }
                }
                is QRCodeScreenState.Processing -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(state.message)
                }
                is QRCodeScreenState.Success -> {
                    Text(state.message, color = MaterialTheme.colorScheme.primary, fontSize = 20.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { uiState = QRCodeScreenState.Idle }) {
                        Text("Escanear Outro")
                    }
                }
                is QRCodeScreenState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error, fontSize = 16.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { uiState = QRCodeScreenState.Idle }) {
                        Text("Tentar Novamente")
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var hasFiredScanEventThisSession by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        hasFiredScanEventThisSession = false
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(executor, BarcodeAnalyzer(
                            onBarcodeDetected = { barcodeValue ->
                                if (!hasFiredScanEventThisSession) {
                                    hasFiredScanEventThisSession = true
                                    Log.d("CameraPreview", "QR Code detectado internamente: $barcodeValue. Chamando onQrCodeScanned.")
                                    onQrCodeScanned(barcodeValue)
                                }
                            },
                            onError = { exception ->
                                Log.e("CameraPreview", "Erro na análise do barcode", exception)
                            }
                        ))
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Falha ao vincular casos de uso da câmera", e)
                }
            }, executor)
            previewView
        },
        modifier = modifier.fillMaxSize(),
        update = {
        }
    )
}

private class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit,
    private val onError: (Exception) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    private val scanner = BarcodeScanning.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        barcodes.firstOrNull()?.rawValue?.let { barcodeValue ->
                            onBarcodeDetected(barcodeValue)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    onError(exception)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}