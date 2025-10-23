package com.example.educarocr.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.educarocr.data.repository.OCRRepository
import com.example.educarocr.domain.model.OCRResult
import com.example.educarocr.domain.usecase.NormalizeOCRUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OCRViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = OCRRepository(application)
    private val normalizeUseCase = NormalizeOCRUseCase()

    private val _uiState = MutableStateFlow<OCRUiState>(OCRUiState.Idle)
    val uiState: StateFlow<OCRUiState> = _uiState.asStateFlow()

    fun processImage(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = OCRUiState.Processing

            repository.processImage(imageUri).fold(
                onSuccess = { text ->
                    _uiState.value = OCRUiState.Success(text)
                },
                onFailure = { error ->
                    _uiState.value = OCRUiState.Error(error.localizedMessage ?: "Error desconocido")
                }
            )
        }
    }

    fun normalizeText(rawText: String): String {
        val result = normalizeUseCase.execute(rawText)
        return result.getFormattedOutput()
    }

    fun resetState() {
        _uiState.value = OCRUiState.Idle
    }
}

sealed class OCRUiState {
    object Idle : OCRUiState()
    object Processing : OCRUiState()
    data class Success(val text: String) : OCRUiState()
    data class Error(val message: String) : OCRUiState()
}

