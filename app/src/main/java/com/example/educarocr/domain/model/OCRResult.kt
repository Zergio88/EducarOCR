package com.example.educarocr.domain.model

data class OCRResult(
    val rawText: String,
    val uniqueHardwareId: String? = null,
    val bootTick: String? = null
) {
    fun isValid(): Boolean = !uniqueHardwareId.isNullOrEmpty() || !bootTick.isNullOrEmpty()

    fun getFormattedOutput(): String {
        val uOut = uniqueHardwareId ?: ""
        val bOut = bootTick ?: ""

        return when {
            uOut.isNotEmpty() && bOut.isNotEmpty() -> "$uOut\n$bOut"
            uOut.isNotEmpty() -> uOut
            bOut.isNotEmpty() -> bOut
            else -> "(No se encontraron datos válidos)"
        }
    }
}

