package com.example.educarocr.domain.usecase

import com.example.educarocr.domain.model.OCRResult

class NormalizeOCRUseCase {

    fun execute(rawText: String): OCRResult {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        var unique: String? = null
        var bootTick: String? = null

        // 1. Buscar Unique Hardware ID por etiqueta (incluir variaciones en español y errores de tipeo)
        val uniqueRegex = Regex("(?i)(unique\\s*hardware\\s*id|hardward\\s*id|hardware\\s*id|id\\s*[úu]nico\\s*de\\s*hardware)\\s*[:=]?\\s*(.*)")
        for (i in lines.indices) {
            val ln = lines[i]
            uniqueRegex.find(ln)?.groupValues?.getOrNull(2)?.let { candidate ->
                // Intentar extraer de la misma línea
                var extracted = extractUnique(candidate)

                // Si no hay suficientes datos en la misma línea, mirar la siguiente
                if (extracted.length < 12 && i + 1 < lines.size) {
                    val nextLine = lines[i + 1]
                    extracted = extractUnique(nextLine)
                }

                if (extracted.length >= 12) {
                    unique = extracted.substring(0, 12)
                }
            }
            if (unique != null) break
        }

        // 2. Si no se encontró por etiqueta, buscar patrón de 6 pares hex en líneas individuales
        if (unique == null) {
            for (ln in lines) {
                val matches = Regex("(?i)([0-9A-Fa-f]{2}[:\\s-]?){6,}").findAll(ln)
                for (match in matches) {
                    val extracted = extractUnique(match.value)
                    if (extracted.length >= 12) {
                        unique = extracted.substring(0, 12)
                        break
                    }
                }
                if (unique != null) break
            }
        }

        // 3. Buscar Boot Tick por etiqueta (incluir variaciones en español: "Marca de arranque")
        val bootRegex = Regex("(?i)(boot\\s*tick|marca\\s*de\\s*arranque)\\s*[:=]?\\s*(.*)")
        for (i in lines.indices) {
            val ln = lines[i]
            bootRegex.find(ln)?.let { match ->
                // 3.1 Intentar con pares ANTES de la etiqueta en la misma línea (por si el valor quedó a la izquierda)
                val preLabel = ln.substring(0, match.range.first)
                var extracted = extractBootTickFromValue(preLabel)

                // 3.2 Si no, probar DESPUÉS de la etiqueta en la misma línea
                if (extracted.length != 2) {
                    val candidate = match.groupValues.getOrNull(2) ?: ""
                    val cleaned = candidate.replace("*", "").trim()
                    extracted = extractBootTickFromValue(cleaned)
                }

                // 3.3 Si aún no, probar la LÍNEA SIGUIENTE
                if (extracted.length != 2 && i + 1 < lines.size) {
                    val nextLine = lines[i + 1].replace("*", "").trim()
                    extracted = extractBootTickFromValue(nextLine)
                }

                // 3.4 Como refuerzo, probar la LÍNEA ANTERIOR
                if (extracted.length != 2 && i - 1 >= 0) {
                    val prevLine = lines[i - 1].replace("*", "").trim()
                    extracted = extractBootTickFromValue(prevLine)
                }

                if (extracted.length == 2) {
                    bootTick = extracted
                }
            }
            if (bootTick != null) break
        }

        // 4. Fallback: si no se encontró, buscar en líneas que contengan "boot" o "arranque"
        if (bootTick == null) {
            val bootLineIdx = lines.indexOfFirst { it.contains(Regex("(?i)(boot|arranque)")) }
            if (bootLineIdx >= 0) {
                val ln = lines[bootLineIdx]
                // Preferir par ANTES de la etiqueta
                val afterColon = ln.substringAfter(":", "").replace("*", "").trim()
                val beforeColon = ln.substringBefore(":", "")
                var extracted = extractBootTickFromValue(beforeColon)
                if (extracted.length != 2) extracted = extractBootTickFromValue(afterColon)
                if (extracted.length != 2 && bootLineIdx + 1 < lines.size) {
                    extracted = extractBootTickFromValue(lines[bootLineIdx + 1])
                }
                if (extracted.length != 2 && bootLineIdx - 1 >= 0) {
                    extracted = extractBootTickFromValue(lines[bootLineIdx - 1])
                }
                if (extracted.length == 2) bootTick = extracted
            }
        }

        return OCRResult(
            rawText = rawText,
            uniqueHardwareId = unique,
            bootTick = bootTick
        )
    }

    private fun extractUnique(value: String): String {
        val cleaned = value
            .replace(Regex("(?<![0-9A-Fa-f])[oO](?![a-zA-Z])"), "0")
            .replace(Regex("(?<![A-Za-z])[Il|](?![A-Za-z])"), "1")
            .replace(Regex("[–—−]"), "-")

        val hexOnly = cleaned
            .replace(Regex("[^0-9A-Fa-f]"), "")
            .uppercase()

        return hexOnly
    }

    private fun extractBootTickFromValue(value: String): String {
        val cleaned = value
            .replace(Regex("(?<![0-9A-Fa-f])[oO](?![a-zA-Z])"), "0")
            .replace(Regex("(?<![A-Za-z])[Il|](?![A-Za-z])"), "1")
            .replace(Regex("[–—−]"), "-")
            .replace("*", "")
            .trim()
            .uppercase()

        // Buscar SOLO pares hex rodeados por separadores (espacio/guion o inicio/fin)
        val validPairs = Regex("(?:^|\\s|-)([0-9A-F]{2})(?:\\s|-|$)")
            .findAll(cleaned)
            .map { it.groupValues[1] }
            .toList()

        return validPairs.lastOrNull() ?: ""
    }
}
