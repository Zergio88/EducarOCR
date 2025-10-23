package com.example.educarocr.data.repository

import android.content.Context
import android.net.Uri
import com.example.educarocr.data.datasource.MLKitOCRDataSource
import com.example.educarocr.utils.BitmapUtils

class OCRRepository(context: Context) {

    private val dataSource = MLKitOCRDataSource(context)

    suspend fun processImage(imageUri: Uri): Result<String> {
        return if (BitmapUtils.isEmulator()) {
            Result.success(dataSource.getEmulatorFallbackText())
        } else {
            dataSource.extractTextFromImage(imageUri)
        }
    }
}

