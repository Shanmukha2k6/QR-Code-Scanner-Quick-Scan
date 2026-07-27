package com.nexuzstudios.qrcodescanner_quickscan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexuzstudios.qrcodescanner_quickscan.data.repository.QRRepository
import com.nexuzstudios.qrcodescanner_quickscan.domain.model.GeneratedQR
import com.nexuzstudios.qrcodescanner_quickscan.domain.model.ScanResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HistoryTab { SCANNED, GENERATED }
enum class HistoryFilter { ALL, FAVORITES }

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: QRRepository
) : ViewModel() {

    private val _activeTab = MutableStateFlow(HistoryTab.SCANNED)
    val activeTab: StateFlow<HistoryTab> = _activeTab.asStateFlow()

    private val _filter = MutableStateFlow(HistoryFilter.ALL)
    val filter: StateFlow<HistoryFilter> = _filter.asStateFlow()

    val scanHistory: StateFlow<List<ScanResult>> = repository.getAllScans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteScanHistory: StateFlow<List<ScanResult>> = repository.getFavoriteScans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val generatedHistory: StateFlow<List<GeneratedQR>> = repository.getAllGenerated()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteGeneratedHistory: StateFlow<List<GeneratedQR>> = repository.getFavoriteGenerated()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val displayedScans = combine(scanHistory, favoriteScanHistory, _filter) { all, favs, filter ->
        if (filter == HistoryFilter.FAVORITES) favs else all
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val displayedGenerated = combine(generatedHistory, favoriteGeneratedHistory, _filter) { all, favs, filter ->
        if (filter == HistoryFilter.FAVORITES) favs else all
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(tab: HistoryTab) { _activeTab.value = tab }
    fun setFilter(f: HistoryFilter) { _filter.value = f }

    fun deleteScan(scan: ScanResult) = viewModelScope.launch {
        repository.deleteScan(scan)
    }

    fun deleteGenerated(qr: GeneratedQR) = viewModelScope.launch {
        repository.deleteGenerated(qr)
    }

    fun toggleScanFavorite(scan: ScanResult) = viewModelScope.launch {
        repository.toggleScanFavorite(scan.id, !scan.isFavorite)
    }

    fun toggleGeneratedFavorite(qr: GeneratedQR) = viewModelScope.launch {
        repository.toggleGeneratedFavorite(qr.id, !qr.isFavorite)
    }

    fun clearAllScans() = viewModelScope.launch { repository.deleteAllScans() }
    fun clearAllGenerated() = viewModelScope.launch { repository.deleteAllGenerated() }
}
