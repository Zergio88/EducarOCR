package com.example.educarocr

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.educarocr.ui.theme.EducarOCRTheme
import java.io.File

class MainActivity : ComponentActivity() {

    private var tempImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EducarOCRTheme {
                var selectedImage by remember { mutableStateOf<Uri?>(null) }
                var showPreview by remember { mutableStateOf(false) }

                if (showPreview && selectedImage != null) {
                    PreviewScreen(
                        imageUri = selectedImage!!,
                        onBack = { showPreview = false }
                    )
                } else {
                    MainScreen(
                        onImageSelected = { uri ->
                            selectedImage = uri
                            showPreview = true
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun MainScreen(onImageSelected: (Uri) -> Unit) {
        val context = LocalContext.current
        var showOptions by remember { mutableStateOf(false) }

        // Lanzador para galería
        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri: Uri? ->
                uri?.let { onImageSelected(it) }
            }
        )

        // Lanzador para cámara
        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture(),
            onResult = { success ->
                if (success && tempImageUri != null) {
                    onImageSelected(tempImageUri!!)
                }
            }
        )

        // Lanzador para permiso cámara
        val cameraPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted ->
                if (granted) {
                    abrirCamara(cameraLauncher)
                } else {
                    Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
                }
            }
        )

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!showOptions) {
                    Button(onClick = { showOptions = true }) {
                        Text("Capturar")
                    }
                } else {
                    Text("Selecciona una opción:")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        // Pedir permiso solo si no está otorgado
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            abrirCamara(cameraLauncher)
                        }
                    }) {
                        Text("Cámara")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { galleryLauncher.launch("image/*") }) {
                        Text("Galería")
                    }
                }
            }
        }
    }

    private fun abrirCamara(cameraLauncher: androidx.activity.result.ActivityResultLauncher<Uri>) {
        val file = File.createTempFile("photo_", ".jpg", cacheDir).apply { deleteOnExit() }
        tempImageUri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        cameraLauncher.launch(tempImageUri)
    }

    @Composable
    fun PreviewScreen(imageUri: Uri, onBack: () -> Unit) {
        val context = LocalContext.current
        var croppedImageUri by remember { mutableStateOf<Uri?>(null) }

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
                // Imagen ocupando siempre la misma porción
                Image(
                    painter = rememberAsyncImagePainter(croppedImageUri ?: imageUri),
                    contentDescription = "Preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(onClick = {
                        // Configuración simple de Cropper
                        val options = CropImageOptions().apply {
                            guidelines = com.canhub.cropper.CropImageView.Guidelines.ON
                            fixAspectRatio = false
                            showCropOverlay = true
                        }
                        cropLauncher.launch(
                            CropImageContractOptions(
                                uri = imageUri,
                                cropImageOptions = options
                            )
                        )
                    }) {
                        Text("Crop")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = { onBack() }) {
                        Text("Volver")
                    }
                }
            }
        }
    }
}
