package com.nexuzstudios.qrcodescanner_quickscan.data.db.dao

import androidx.room.*
import com.nexuzstudios.qrcodescanner_quickscan.domain.model.GeneratedQR
import com.nexuzstudios.qrcodescanner_quickscan.domain.model.ScanResult
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {

    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScanResult>>

    @Query("SELECT * FROM scan_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteScans(): Flow<List<ScanResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scan: ScanResult): Long

    @Update
    suspend fun update(scan: ScanResult)

    @Delete
    suspend fun delete(scan: ScanResult)

    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM scan_history")
    suspend fun deleteAll()

    @Query("UPDATE scan_history SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM scan_history")
    suspend fun getCount(): Int
}

@Dao
interface GeneratedQRDao {

    @Query("SELECT * FROM generated_qr ORDER BY timestamp DESC")
    fun getAllGenerated(): Flow<List<GeneratedQR>>

    @Query("SELECT * FROM generated_qr WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteGenerated(): Flow<List<GeneratedQR>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(qr: GeneratedQR): Long

    @Update
    suspend fun update(qr: GeneratedQR)

    @Delete
    suspend fun delete(qr: GeneratedQR)

    @Query("DELETE FROM generated_qr WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM generated_qr")
    suspend fun deleteAll()

    @Query("UPDATE generated_qr SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)
}
