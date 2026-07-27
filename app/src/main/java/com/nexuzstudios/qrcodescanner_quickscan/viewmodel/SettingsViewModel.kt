package com.nexuzstudios.qrcodescanner_quickscan.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexuzstudios.qrcodescanner_quickscan.utils.HapticUtil
import com.nexuzstudios.qrcodescanner_quickscan.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsManager: PreferencesManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val vibrateOnScan = prefsManager.vibrateOnScan.stateIn(
        viewModelScope, SharingStarted.Eagerly, true
    )
    val beepOnScan = prefsManager.beepOnScan.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )
    val autoOpenUrl = prefsManager.autoOpenUrl.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )
    val copyOnScan = prefsManager.copyOnScan.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )

    fun setVibrateOnScan(v: Boolean) = viewModelScope.launch {
        prefsManager.setVibrateOnScan(v)
        if (v) HapticUtil.vibrate(context)
    }
    fun setBeepOnScan(v: Boolean)    = viewModelScope.launch { prefsManager.setBeepOnScan(v) }
    fun setAutoOpenUrl(v: Boolean)   = viewModelScope.launch { prefsManager.setAutoOpenUrl(v) }
    fun setCopyOnScan(v: Boolean)    = viewModelScope.launch { prefsManager.setCopyOnScan(v) }

    fun rateApp() {
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=com.nexuzstudios.qrcodescanner_quickscan")
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.nexuzstudios.qrcodescanner_quickscan")
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(intent)
        }
    }

    fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "📷 Try QR Code Scanner & Barcode Scanner - Fast QR Reader!\n" +
                "https://play.google.com/store/apps/details?id=com.nexuzstudios.qrcodescanner_quickscan"
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share App").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun openPrivacyPolicy() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://nexuzstudios.com/privacy-policy")
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(intent)
    }
}
