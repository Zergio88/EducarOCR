package com.example.educarocr

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.educarocr.ui.screens.MainScreen
import com.example.educarocr.ui.screens.OCRResultScreen
import com.example.educarocr.ui.screens.PreviewScreen
import com.example.educarocr.ui.theme.EducarOCRTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Instalar splash screen antes de super.onCreate
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Handler global para capturar crashes y loguearlos
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("EducarOCR", "UNCAUGHT EXCEPTION en hilo ${thread.name}", throwable)
        }

        setContent {
            EducarOCRTheme {
                EducarOCRNavigation()
            }
        }
    }
}

@Composable
fun EducarOCRNavigation() {
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var showPreview by remember { mutableStateOf(false) }
    var showOCRResult by remember { mutableStateOf(false) }
    var extractedText by remember { mutableStateOf("") }

    when {
        showOCRResult -> {
            OCRResultScreen(
                extractedText = extractedText,
                onBack = { showOCRResult = false }
            )
        }
        showPreview && selectedImage != null -> {
            PreviewScreen(
                imageUri = selectedImage!!,
                onBack = { showPreview = false },
                onProcessComplete = { text: String ->
                    extractedText = text
                    showOCRResult = true
                }
            )
        }
        else -> {
            MainScreen(
                onImageSelected = { uri ->
                    selectedImage = uri
                    showPreview = true
                }
            )
        }
    }
}

