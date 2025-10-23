package com.example.educarocr.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import coil.compose.rememberAsyncImagePainter
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.educarocr.data.repository.OCRRepository
import kotlinx.coroutines.launch

@Composable
fun PreviewScreen(
    imageUri: Uri,
    onBack: () -> Unit,
    onProcessComplete: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { OCRRepository(context) }
    var croppedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Permiso de lectura de imágenes según versión
    val readPermission = if (Build.VERSION.SDK_INT >= 33) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var pendingProcess by remember { mutableStateOf(false) }
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted && pendingProcess) {
                pendingProcess = false
                isProcessing = true
                scope.launch {
                    repository.processImage(croppedImageUri ?: imageUri).fold(
                        onSuccess = { text ->
                            isProcessing = false
                            if (text.isNotEmpty()) {
                                onProcessComplete(text)
                            } else {
                                Toast.makeText(context, "No se detectó texto en la imagen", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onFailure = { error ->
                            isProcessing = false
                            Toast.makeText(context, "Error: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            } else if (!granted) {
                Toast.makeText(context, "Se requiere permiso para leer imágenes", Toast.LENGTH_LONG).show()
            }
        }
    )

    // Lanzador para recorte con CanHub Cropper
    val cropLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract(),
        onResult = { result ->
            if (result.isSuccessful) {
                croppedImageUri = result.uriContent
                Toast.makeText(context, "Imagen recortada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Recorte cancelado", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Text(
                text = "Vista Previa",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color(0xFF1976D2)
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Subtítulo
            Text(
                text = "Recorte de imagen",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Imagen de vista previa
            Image(
                painter = rememberAsyncImagePainter(croppedImageUri ?: imageUri),
                contentDescription = "Preview",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val options = CropImageOptions().apply {
                        guidelines = com.canhub.cropper.CropImageView.Guidelines.ON
                        fixAspectRatio = false
                        showCropOverlay = true
                        activityTitle = "Recortar imagen"
                        activityMenuIconColor = android.graphics.Color.WHITE
                        toolbarColor = "#FF6200EE".toColorInt()
                        toolbarBackButtonColor = android.graphics.Color.WHITE
                        toolbarTintColor = android.graphics.Color.WHITE
                        cropMenuCropButtonTitle = "Confirmar"
                        allowRotation = true
                        allowFlipping = true
                        cropShape = com.canhub.cropper.CropImageView.CropShape.RECTANGLE
                    }
                    cropLauncher.launch(
                        CropImageContractOptions(
                            uri = imageUri,
                            cropImageOptions = options
                        )
                    )
                }) { Text("Crop") }

                Button(
                    onClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            readPermission
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!hasPermission) {
                            pendingProcess = true
                            storagePermissionLauncher.launch(readPermission)
                            return@Button
                        }
                        isProcessing = true
                        scope.launch {
                            repository.processImage(croppedImageUri ?: imageUri).fold(
                                onSuccess = { text ->
                                    isProcessing = false
                                    if (text.isNotEmpty()) {
                                        onProcessComplete(text)
                                    } else {
                                        Toast.makeText(context, "No se detectó texto en la imagen", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onFailure = { error ->
                                    isProcessing = false
                                    Toast.makeText(context, "Error: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF2196F3),
                        disabledContainerColor = androidx.compose.ui.graphics.Color(0xFF90CAF9)
                    )
                ) {
                    Text(
                        text = if (isProcessing) "Procesando..." else "Procesar",
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }

                Button(onClick = { onBack() }) { Text("Volver") }
            }
        }
    }
}

