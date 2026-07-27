package com.nexuzstudios.qrcodescanner_quickscan.data.repository

import com.nexuzstudios.qrcodescanner_quickscan.data.db.dao.GeneratedQRDao
import com.nexuzstudios.qrcodescanner_quickscan.data.db.dao.ScanHistoryDao
import com.nexuzstudios.qrcodescanner_quickscan.domain.model.GeneratedQR
import com.nexuzstudios.qrcodescanner_quickscan.domain.model.ScanResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QRRepository @Inject constructor(
    private val scanHistoryDao: ScanHistoryDao,
    private val generatedQRDao: GeneratedQRDao
) {
    // Scan History
    fun getAllScans(): Flow<List<ScanResult>> = scanHistoryDao.getAllScans()
    fun getFavoriteScans(): Flow<List<ScanResult>> = scanHistoryDao.getFavoriteScans()
    suspend fun insertScan(scan: ScanResult): Long = scanHistoryDao.insert(scan)
    suspend fun deleteScan(scan: ScanResult) = scanHistoryDao.delete(scan)
    suspend fun deleteScanById(id: Long) = scanHistoryDao.deleteById(id)
    suspend fun deleteAllScans() = scanHistoryDao.deleteAll()
    suspend fun toggleScanFavorite(id: Long, isFavorite: Boolean) =
        scanHistoryDao.updateFavorite(id, isFavorite)
    suspend fun getScanCount(): Int = scanHistoryDao.getCount()

    // Generated QR
    fun getAllGenerated(): Flow<List<GeneratedQR>> = generatedQRDao.getAllGenerated()
    fun getFavoriteGenerated(): Flow<List<GeneratedQR>> = generatedQRDao.getFavoriteGenerated()
    suspend fun insertGenerated(qr: GeneratedQR): Long = generatedQRDao.insert(qr)
    suspend fun deleteGenerated(qr: GeneratedQR) = generatedQRDao.delete(qr)
    suspend fun deleteGeneratedById(id: Long) = generatedQRDao.deleteById(id)
    suspend fun deleteAllGenerated() = generatedQRDao.deleteAll()
    suspend fun toggleGeneratedFavorite(id: Long, isFavorite: Boolean) =
        generatedQRDao.updateFavorite(id, isFavorite)
}
