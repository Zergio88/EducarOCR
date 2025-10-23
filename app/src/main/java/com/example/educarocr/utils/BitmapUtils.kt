package com.example.educarocr.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build

object BitmapUtils {

    fun decodeBitmapCompat(context: Context, uri: Uri, maxDimension: Int = 1024): Bitmap? {
        return try {
            val bmp: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    val (w, h) = info.size.let { it.width to it.height }
                    val scale = (maxOf(w, h).toFloat() / maxDimension).coerceAtLeast(1f)
                    val targetW = (w / scale).toInt().coerceAtLeast(1)
                    val targetH = (h / scale).toInt().coerceAtLeast(1)
                    decoder.setTargetSize(targetW, targetH)
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } else {
                val optsBounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, optsBounds)
                }
                val (w, h) = optsBounds.outWidth to optsBounds.outHeight
                if (w <= 0 || h <= 0) return null
                val inSample = computeInSampleSize(w, h, maxDimension)
                val opts = BitmapFactory.Options().apply {
                    inJustDecodeBounds = false
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    inSampleSize = inSample
                }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, opts)
                } ?: return null
            }
            // Asegurar ARGB_8888 y no hardware bitmap
            if (bmp.config != Bitmap.Config.ARGB_8888 || bmp.isMutable) {
                bmp.copy(Bitmap.Config.ARGB_8888, false)
            } else bmp
        } catch (t: Throwable) {
            android.util.Log.e("EducarOCR", "Error decodificando bitmap", t)
            null
        }
    }

    private fun computeInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
        var inSampleSize = 1
        val maxOrig = maxOf(width, height)
        while ((maxOrig / inSampleSize) > maxDimension) {
            inSampleSize *= 2
        }
        return inSampleSize.coerceAtLeast(1)
    }

    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.lowercase().contains("emulator")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86"))
    }

    fun toGrayscale(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(out)
        val paint = android.graphics.Paint()
        val cm = android.graphics.ColorMatrix()
        cm.setSaturation(0f)
        paint.colorFilter = android.graphics.ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return out
    }
}

