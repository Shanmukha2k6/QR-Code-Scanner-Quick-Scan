package com.nexuzstudios.qrcodescanner_quickscan.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.nexuzstudios.qrcodescanner_quickscan.domain.model.QRType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object QRGenerator {

    private const val QR_SIZE = 512
    private const val BARCODE_WIDTH = 600
    private const val BARCODE_HEIGHT = 200

    suspend fun generateQRCode(
        content: String,
        qrType: QRType,
        size: Int = QR_SIZE,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Result<Bitmap> = withContext(Dispatchers.Default) {
        runCatching {
            val format = when (qrType) {
                QRType.BARCODE_CODE128 -> BarcodeFormat.CODE_128
                QRType.BARCODE_EAN13 -> BarcodeFormat.EAN_13
                QRType.BARCODE_EAN8 -> BarcodeFormat.EAN_8
                else -> BarcodeFormat.QR_CODE
            }

            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1
            )

            val width = if (format == BarcodeFormat.QR_CODE) size else BARCODE_WIDTH
            val height = if (format == BarcodeFormat.QR_CODE) size else BARCODE_HEIGHT

            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                content, format, width, height, hints
            )

            bitMatrixToBitmap(bitMatrix, foregroundColor, backgroundColor)
        }
    }

    private fun bitMatrixToBitmap(
        bitMatrix: BitMatrix,
        foregroundColor: Int,
        backgroundColor: Int
    ): Bitmap {
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (bitMatrix[x, y]) foregroundColor else backgroundColor
            }
        }

        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }
}
