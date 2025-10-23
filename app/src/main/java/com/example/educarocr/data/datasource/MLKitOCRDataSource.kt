package com.example.educarocr.data.datasource

import android.content.Context
import android.net.Uri
import com.example.educarocr.utils.BitmapUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class MLKitOCRDataSource(private val context: Context) {

    suspend fun extractTextFromImage(imageUri: Uri): Result<String> = suspendCancellableCoroutine { continuation ->
        try {
            val maxDim = if (BitmapUtils.isEmulator()) 640 else 1280
            val bitmap = BitmapUtils.decodeBitmapCompat(context, imageUri, maxDimension = maxDim)

            if (bitmap == null) {
                continuation.resume(Result.failure(Exception("No se pudo cargar la imagen")))
                return@suspendCancellableCoroutine
            }

            val prepped = if (BitmapUtils.isEmulator()) BitmapUtils.toGrayscale(bitmap) else bitmap
            val image = InputImage.fromBitmap(prepped, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    try {
                        // Construir texto línea por línea para mantener orden visual
                        val allLines = mutableListOf<Pair<android.graphics.Rect, String>>()
                        for (block in visionText.textBlocks) {
                            for (line in block.lines) {
                                val rect = line.boundingBox ?: android.graphics.Rect(0, 0, 0, 0)
                                allLines.add(rect to line.text)
                            }
                        }

                        // Ordenar por fila (top) y luego por columna (left)
                        val sorted = allLines.sortedWith(compareBy({ it.first.top }, { it.first.left }))
                        val lineByLine = buildString {
                            sorted.forEach { append(it.second).append('\n') }
                        }.trimEnd()

                        android.util.Log.d("EducarOCR", "OCR OK (lines=${sorted.size})")

                        val result = if (lineByLine.isNotEmpty()) lineByLine else "(No se detectó texto en la imagen)"
                        continuation.resume(Result.success(result))
                    } catch (e: Exception) {
                        android.util.Log.e("EducarOCR", "OCR processing error", e)
                        continuation.resume(Result.failure(e))
                    }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("EducarOCR", "OCR FAIL", e)
                    continuation.resume(Result.failure(e))
                }
        } catch (e: Exception) {
            android.util.Log.e("EducarOCR", "OCR setup error", e)
            continuation.resume(Result.failure(e))
        }
    }

    fun getEmulatorFallbackText(): String {
        return "[MODO EMULADOR]\n" +
                "El OCR real está deshabilitado por estabilidad.\n" +
                "Prueba en un dispositivo físico o un emulador ARM64 Google Play.\n\n" +
                "(Coloca aquí tu texto de prueba)"
    }
}

