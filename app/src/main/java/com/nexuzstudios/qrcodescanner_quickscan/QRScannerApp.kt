package com.nexuzstudios.qrcodescanner_quickscan

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.nexuzstudios.qrcodescanner_quickscan.ads.AdManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class QRScannerApp : Application(), Application.ActivityLifecycleCallbacks {

    @Inject
    lateinit var adManager: AdManager

    private var currentActivity: Activity? = null
    private var startedActivities = 0

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        // Initialize AdMob
        adManager.initialize()
    }

    override fun onActivityStarted(activity: Activity) {
        if (startedActivities == 0) {
            // App coming from background to foreground
            // MainActivity's Splash handles the cold start app open ad separately,
            // so we don't necessarily want to duplicate it if this is the very first start.
            // But since adManager handles null properly, it's safe.
            if (activity !is MainActivity || !isColdStart) {
                adManager.showAppOpenAd(activity)
            }
            isColdStart = false
        }
        startedActivities++
    }

    private var isColdStart = true

    override fun onActivityStopped(activity: Activity) {
        startedActivities--
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}
