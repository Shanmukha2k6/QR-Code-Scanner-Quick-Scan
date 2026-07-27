package com.nexuzstudios.qrcodescanner_quickscan.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.nexuzstudios.qrcodescanner_quickscan.domain.model.ContentType
import com.nexuzstudios.qrcodescanner_quickscan.domain.model.QRType
import com.nexuzstudios.qrcodescanner_quickscan.domain.model.ScanType

object ContentDetector {

    fun detectContentType(rawValue: String): ContentType {
        return when {
            rawValue.startsWith("http://") || rawValue.startsWith("https://") -> ContentType.URL
            rawValue.startsWith("mailto:") -> ContentType.EMAIL
            rawValue.startsWith("tel:") -> ContentType.PHONE
            rawValue.startsWith("smsto:") || rawValue.startsWith("sms:") -> ContentType.SMS
            rawValue.startsWith("WIFI:") -> ContentType.WIFI
            rawValue.startsWith("BEGIN:VCARD") -> ContentType.CONTACT
            rawValue.startsWith("geo:") -> ContentType.GEO
            rawValue.startsWith("BEGIN:VEVENT") -> ContentType.CALENDAR
            rawValue.contains("@") && rawValue.contains(".") -> ContentType.EMAIL
            else -> ContentType.TEXT
        }
    }

    fun detectScanType(formatInt: Int): ScanType {
        return when (formatInt) {
            256 -> ScanType.QR_CODE       // FORMAT_QR_CODE
            32 -> ScanType.DATA_MATRIX    // FORMAT_DATA_MATRIX
            4 -> ScanType.AZTEC           // FORMAT_AZTEC
            2048 -> ScanType.PDF_417      // FORMAT_PDF417
            else -> ScanType.BARCODE
        }
    }

    fun getDisplayFormat(formatInt: Int): String {
        return when (formatInt) {
            1 -> "Code 128"
            2 -> "Code 39"
            4 -> "Aztec"
            8 -> "Codabar"
            16 -> "Data Matrix"
            32 -> "EAN-13"
            64 -> "EAN-8"
            128 -> "ITF"
            256 -> "QR Code"
            512 -> "UPC-A"
            1024 -> "UPC-E"
            2048 -> "PDF-417"
            else -> "Unknown"
        }
    }
}

object ClipboardUtil {
    fun copyToClipboard(context: Context, text: String, label: String = "QR Content") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }
}

object ShareUtil {
    fun shareText(context: Context, text: String, title: String = "Share QR Content") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle invalid URL
        }
    }
}

object HapticUtil {
    fun vibrate(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val attributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .build()

                val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                } else {
                    VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                @Suppress("DEPRECATION")
                vibrator.vibrate(effect, attributes)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(150)
            }
        } catch (e: Exception) {
            // Vibration not available or missing permission
        }
    }

    fun playBeep() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
        } catch (e: Exception) {
            // Sound not available
        }
    }
}

fun String.formatForQRType(qrType: QRType): String {
    return when (qrType) {
        QRType.EMAIL -> if (startsWith("mailto:")) this else "mailto:$this"
        QRType.PHONE -> if (startsWith("tel:")) this else "tel:$this"
        QRType.SMS -> if (startsWith("sms:") || startsWith("smsto:")) this else "smsto:$this"
        else -> this
    }
}
