package com.nexuzstudios.qrcodescanner_quickscan.di

import android.content.Context
import androidx.room.Room
import com.nexuzstudios.qrcodescanner_quickscan.data.db.QRDatabase
import com.nexuzstudios.qrcodescanner_quickscan.data.db.dao.GeneratedQRDao
import com.nexuzstudios.qrcodescanner_quickscan.data.db.dao.ScanHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): QRDatabase {
        return Room.databaseBuilder(
            context,
            QRDatabase::class.java,
            "qr_scanner_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideScanHistoryDao(db: QRDatabase): ScanHistoryDao = db.scanHistoryDao()

    @Provides
    @Singleton
    fun provideGeneratedQRDao(db: QRDatabase): GeneratedQRDao = db.generatedQRDao()
}
