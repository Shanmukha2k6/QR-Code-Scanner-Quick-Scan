package com.nexuzstudios.qrcodescanner_quickscan.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ScanType {
    QR_CODE, BARCODE, DATA_MATRIX, AZTEC, PDF_417, UNKNOWN
}

enum class ContentType {
    URL, TEXT, EMAIL, PHONE, SMS, WIFI, CONTACT, GEO, CALENDAR, UNKNOWN
}

@Entity(tableName = "scan_history")
data class ScanResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rawValue: String,
    val scanType: ScanType,
    val contentType: ContentType,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val displayFormat: String = ""
)

@Entity(tableName = "generated_qr")
data class GeneratedQR(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val qrType: QRType,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val imagePath: String? = null
)

enum class QRType {
    TEXT, URL, WIFI, CONTACT, EMAIL, PHONE, SMS,
    BARCODE_CODE128, BARCODE_EAN13, BARCODE_EAN8
}

data class WifiCredentials(
    val ssid: String,
    val password: String,
    val securityType: String = "WPA"
) {
    fun toWifiString(): String = "WIFI:T:$securityType;S:$ssid;P:$password;;"
}

data class ContactInfo(
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val organization: String = "",
    val url: String = ""
) {
    fun toVCard(): String = buildString {
        appendLine("BEGIN:VCARD")
        appendLine("VERSION:3.0")
        appendLine("N:$lastName;$firstName;;;")
        appendLine("FN:$firstName $lastName")
        if (organization.isNotBlank()) appendLine("ORG:$organization")
        if (phone.isNotBlank()) appendLine("TEL:$phone")
        if (email.isNotBlank()) appendLine("EMAIL:$email")
        if (url.isNotBlank()) appendLine("URL:$url")
        append("END:VCARD")
    }
}

sealed class ScanState {
    object Idle : ScanState()
    object Scanning : ScanState()
    data class Success(val result: ScanResult) : ScanState()
    data class Error(val message: String) : ScanState()
}
