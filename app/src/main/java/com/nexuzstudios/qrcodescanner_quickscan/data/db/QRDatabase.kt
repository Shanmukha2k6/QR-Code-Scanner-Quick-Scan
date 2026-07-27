package com.nexuzstudios.qrcodescanner_quickscan.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nexuzstudios.qrcodescanner_quickscan.data.db.dao.GeneratedQRDao
import com.nexuzstudios.qrcodescanner_quickscan.data.db.dao.ScanHistoryDao
import com.nexuzstudios.qrcodescanner_quickscan.domain.model.GeneratedQR
import com.nexuzstudios.qrcodescanner_quickscan.domain.model.ScanResult

@Database(
    entities = [ScanResult::class, GeneratedQR::class],
    version = 1,
    exportSchema = false
)
abstract class QRDatabase : RoomDatabase() {
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun generatedQRDao(): GeneratedQRDao
}
