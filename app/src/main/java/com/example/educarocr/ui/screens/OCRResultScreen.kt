package com.example.educarocr.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.educarocr.domain.usecase.NormalizeOCRUseCase

@Composable
fun OCRResultScreen(
    extractedText: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf(extractedText) }
    val normalizeUseCase = remember { NormalizeOCRUseCase() }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Texto Extraído",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color(0xFF1976D2)
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Campo de texto grande con scroll horizontal y fuente monoespaciada
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                label = { Text("Texto reconocido") },
                placeholder = { Text("El texto aparecerá aquí...") },
                maxLines = Int.MAX_VALUE,
                minLines = 10,
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de acción en dos filas (2x2)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primera fila: Preparar datos y Copiar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón Preparar datos
                    Button(
                        onClick = {
                            val result = normalizeUseCase.execute(text)
                            text = result.getFormattedOutput()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Preparar datos") }

                    // Botón Copiar
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Texto OCR", text)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Texto copiado", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Copiar") }
                }

                // Segunda fila: Compartir y Volver
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón Compartir
                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Compartir texto")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Compartir") }

                    // Botón Volver
                    Button(
                        onClick = { onBack() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) { Text("Volver") }
                }
            }
        }
    }
}

