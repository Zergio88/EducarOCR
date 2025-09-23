package com.example.educarocr

import android.Manifest
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
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
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

        // Permiso cámara
        val cameraPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted ->
                if (granted) {
                    val file = File.createTempFile("photo_", ".jpg", cacheDir).apply {
                        deleteOnExit()
                    }
                    tempImageUri = FileProvider.getUriForFile(
                        this@MainActivity,
                        "${packageName}.provider",
                        file
                    )
                    cameraLauncher.launch(tempImageUri)
                } else {
                    Toast.makeText(this@MainActivity, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
                }
            }
        )

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Button(onClick = { showOptions = !showOptions }) {
                    Text(text = "Capture")
                }

                if (showOptions) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
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

    @Composable
    fun PreviewScreen(imageUri: Uri, onBack: () -> Unit) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onBack() }) {
                    Text("Volver")
                }
            }
        }
    }
}
