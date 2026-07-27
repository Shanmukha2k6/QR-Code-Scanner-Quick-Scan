package com.nexuzstudios.qrcodescanner_quickscan.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexuzstudios.qrcodescanner_quickscan.data.repository.QRRepository
import com.nexuzstudios.qrcodescanner_quickscan.domain.model.*
import com.nexuzstudios.qrcodescanner_quickscan.utils.ContentDetector
import com.nexuzstudios.qrcodescanner_quickscan.utils.HapticUtil
import com.nexuzstudios.qrcodescanner_quickscan.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val repository: QRRepository,
    private val prefsManager: PreferencesManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val _isFlashOn = MutableStateFlow(false)
    val isFlashOn: StateFlow<Boolean> = _isFlashOn.asStateFlow()

    private val _lastScanResult = MutableStateFlow<ScanResult?>(null)
    val lastScanResult: StateFlow<ScanResult?> = _lastScanResult.asStateFlow()

    val vibrateOnScan = prefsManager.vibrateOnScan.stateIn(
        viewModelScope, SharingStarted.Eagerly, true
    )
    val beepOnScan = prefsManager.beepOnScan.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )
    val autoOpenUrl = prefsManager.autoOpenUrl.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )
    val scanCount = prefsManager.scanCount.stateIn(
        viewModelScope, SharingStarted.Eagerly, 0
    )

    private var lastScannedValue: String = ""
    private var scanCooldown = false

    fun onBarcodeScanned(rawValue: String, formatInt: Int) {
        if (scanCooldown || rawValue == lastScannedValue) return
        scanCooldown = true
        lastScannedValue = rawValue

        val contentType = ContentDetector.detectContentType(rawValue)
        val scanType = ContentDetector.detectScanType(formatInt)
        val displayFormat = ContentDetector.getDisplayFormat(formatInt)

        val result = ScanResult(
            rawValue = rawValue,
            scanType = scanType,
            contentType = contentType,
            displayFormat = displayFormat
        )

        _scanState.value = ScanState.Success(result)
        _lastScanResult.value = result

        if (vibrateOnScan.value) HapticUtil.vibrate(context)
        if (beepOnScan.value) HapticUtil.playBeep()

        viewModelScope.launch {
            repository.insertScan(result)
            prefsManager.incrementScanCount()
            kotlinx.coroutines.delay(2000)
            scanCooldown = false
        }
    }

    fun resetScan() {
        _scanState.value = ScanState.Idle
        lastScannedValue = ""
        scanCooldown = false
    }

    fun toggleFlash() {
        _isFlashOn.value = !_isFlashOn.value
    }

    fun setFlash(enabled: Boolean) {
        _isFlashOn.value = enabled
    }
}
