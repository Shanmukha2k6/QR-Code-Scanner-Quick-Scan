package com.nexuzstudios.qrcodescanner_quickscan.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexuzstudios.qrcodescanner_quickscan.data.repository.QRRepository
import com.nexuzstudios.qrcodescanner_quickscan.domain.model.GeneratedQR
import com.nexuzstudios.qrcodescanner_quickscan.domain.model.QRType
import com.nexuzstudios.qrcodescanner_quickscan.utils.QRGenerator
import com.nexuzstudios.qrcodescanner_quickscan.utils.formatForQRType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class GenerateState {
    object Idle : GenerateState()
    object Loading : GenerateState()
    data class Success(val bitmap: Bitmap, val content: String) : GenerateState()
    data class Error(val message: String) : GenerateState()
}

@HiltViewModel
class CreateViewModel @Inject constructor(
    private val repository: QRRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _generateState = MutableStateFlow<GenerateState>(GenerateState.Idle)
    val generateState: StateFlow<GenerateState> = _generateState.asStateFlow()

    private val _selectedQRType = MutableStateFlow(QRType.TEXT)
    val selectedQRType: StateFlow<QRType> = _selectedQRType.asStateFlow()

    private val _savedMessage = MutableStateFlow<String?>(null)
    val savedMessage: StateFlow<String?> = _savedMessage.asStateFlow()

    fun setQRType(type: QRType) {
        _selectedQRType.value = type
        _generateState.value = GenerateState.Idle
    }

    fun generateQR(content: String, title: String = "") {
        if (content.isBlank()) {
            _generateState.value = GenerateState.Error("Please enter content")
            return
        }
        val qrType = _selectedQRType.value
        val formattedContent = content.formatForQRType(qrType)

        _generateState.value = GenerateState.Loading
        viewModelScope.launch {
            QRGenerator.generateQRCode(formattedContent, qrType)
                .onSuccess { bitmap ->
                    _generateState.value = GenerateState.Success(bitmap, formattedContent)
                }
                .onFailure { e ->
                    _generateState.value = GenerateState.Error(e.message ?: "Generation failed")
                }
        }
    }

    fun saveToHistory(content: String, title: String) {
        val qrType = _selectedQRType.value
        viewModelScope.launch {
            repository.insertGenerated(
                GeneratedQR(
                    title = title.ifBlank { qrType.name },
                    content = content,
                    qrType = qrType
                )
            )
            _savedMessage.value = "Saved to history"
            kotlinx.coroutines.delay(2000)
            _savedMessage.value = null
        }
    }

    fun clearState() {
        _generateState.value = GenerateState.Idle
    }
}
