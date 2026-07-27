package com.nexuzstudios.qrcodescanner_quickscan

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.nexuzstudios.qrcodescanner_quickscan.ads.AdManager
import com.nexuzstudios.qrcodescanner_quickscan.ui.MainNavigation
import com.nexuzstudios.qrcodescanner_quickscan.ui.screens.splash.SplashScreen
import com.nexuzstudios.qrcodescanner_quickscan.ui.theme.QRCodeScannerTheme
import com.nexuzstudios.qrcodescanner_quickscan.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var adManager: AdManager

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            QRCodeScannerTheme(darkTheme = true) {
                var showSplash by remember { mutableStateOf(true) }

                AnimatedContent(
                    targetState = showSplash,
                    transitionSpec = {
                        fadeIn(tween(400)) togetherWith fadeOut(tween(300))
                    },
                    label = "splash_transition"
                ) { isSplash ->
                    if (isSplash) {
                        SplashScreen(onSplashFinished = { 
                            showSplash = false 
                            // Show initial app open ad here after splash, when ad is likely loaded
                            adManager.showAppOpenAd(this@MainActivity)
                        })
                    } else {
                        MainNavigation()
                    }
                }
            }
        }
    }
}

