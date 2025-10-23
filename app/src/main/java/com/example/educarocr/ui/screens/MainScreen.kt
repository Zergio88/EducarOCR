package com.example.educarocr.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.educarocr.R
import java.io.File

@Composable
fun MainScreen(onImageSelected: (Uri) -> Unit) {
    val context = LocalContext.current
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

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
                tempImageUri = createTempImageUri(context)
                tempImageUri?.let { cameraLauncher.launch(it) }
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
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono de la app (más grande y solo un poco más arriba del centro)
            Image(
                painter = painterResource(id = R.drawable.educarocr),
                contentDescription = "Logo EducarOCR",
                modifier = Modifier
                    .size(260.dp)
                    .offset(y = (-56).dp)
                    .padding(bottom = 24.dp)
            )

            // Título representativo
            Text(
                text = "Elegí cómo obtener la imagen",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Subtítulo
            Text(
                text = "Selecciona una opción:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Botones lado a lado: Cámara y Galería
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            tempImageUri = createTempImageUri(context)
                            tempImageUri?.let { cameraLauncher.launch(it) }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cámara")
                }

                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Galería")
                }
            }
        }
    }
}

private fun createTempImageUri(context: Context): Uri {
    val file = File.createTempFile("photo_", ".jpg", context.cacheDir).apply { deleteOnExit() }
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}
