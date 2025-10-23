package com.example.educarocr.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class OCRManager {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Procesa una imagen y devuelve el texto reconocido mediante callbacks.
     */
    fun recognizeText(
        bitmap: Bitmap,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { result: Text ->
                // Devuelve todo el texto reconocido
                onSuccess(result.text)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    /**
     * Libera recursos del recognizer.
     */
    fun close() {
        recognizer.close()
    }
}
