package com.nexuzstudios.qrcodescanner_quickscan.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // Production AdMob Ad Unit IDs
        const val BANNER_HISTORY_ID = "ca-app-pub-8301491457549237/6503559089"
        const val BANNER_ID = BANNER_HISTORY_ID
        const val APP_OPEN_ID = "ca-app-pub-8301491457549237/6446764472"

        private const val TAG = "AdManager"
        private const val MIN_APP_OPEN_INTERVAL_MS = 15 * 60 * 1000L // 15 minutes interval for optimal revenue & compliance
        private const val PREFS_NAME = "admob_prefs"
        private const val KEY_LAST_APP_OPEN_TIME = "last_app_open_ad_time"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun canShowAppOpenAd(): Boolean {
        if (isProUser) return false
        val lastTime = prefs.getLong(KEY_LAST_APP_OPEN_TIME, 0L)
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastTime) >= MIN_APP_OPEN_INTERVAL_MS
    }

    private var appOpenAd: AppOpenAd? = null
    private var loadTime: Long = 0
    private var isShowingAd = false
    private var isLoadingAppOpenAd = false
    private var showAppOpenAdWhenLoaded = false
    private var pendingActivity: Activity? = null
    private var isProUser = false

    fun initialize() {
        MobileAds.initialize(context) { status ->
            Log.d(TAG, "AdMob initialized: $status")
            loadAppOpenAd()
        }
    }

    fun setProUser(isPro: Boolean) {
        isProUser = isPro
    }

    private fun isAdAvailable(): Boolean {
        // App Open Ads expire after 4 hours
        val dateDifference: Long = System.currentTimeMillis() - loadTime
        val numMillisPerFourHours: Long = 3600000 * 4
        return appOpenAd != null && dateDifference < numMillisPerFourHours
    }

    fun loadAppOpenAd() {
        if (isProUser || isAdAvailable() || isLoadingAppOpenAd) return
        isLoadingAppOpenAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            APP_OPEN_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    loadTime = System.currentTimeMillis()
                    isLoadingAppOpenAd = false
                    Log.d(TAG, "App open ad loaded successfully")

                    if (showAppOpenAdWhenLoaded) {
                        showAppOpenAdWhenLoaded = false
                        pendingActivity?.let { act ->
                            if (!act.isFinishing && !act.isDestroyed) {
                                showAppOpenAdInternal(act)
                            }
                        }
                        pendingActivity = null
                    }
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                    isLoadingAppOpenAd = false
                    showAppOpenAdWhenLoaded = false
                    pendingActivity = null
                    Log.e(TAG, "App open ad failed to load: ${error.message}")
                }
            }
        )
    }

    fun showAppOpenAd(activity: Activity) {
        if (isProUser || isShowingAd) return
        if (!canShowAppOpenAd()) {
            Log.d(TAG, "Skipping App Open Ad: frequency cap (minimum 15 minutes) not reached")
            return
        }

        if (isAdAvailable()) {
            showAppOpenAdInternal(activity)
        } else {
            pendingActivity = activity
            showAppOpenAdWhenLoaded = true
            loadAppOpenAd()
        }
    }

    private fun showAppOpenAdInternal(activity: Activity) {
        if (isShowingAd || appOpenAd == null) return

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                Log.d(TAG, "App open ad dismissed")
                loadAppOpenAd()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                appOpenAd = null
                isShowingAd = false
                Log.e(TAG, "App open ad failed to show: ${error.message}")
                loadAppOpenAd()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
                prefs.edit().putLong(KEY_LAST_APP_OPEN_TIME, System.currentTimeMillis()).apply()
                Log.d(TAG, "App open ad showing")
            }
        }
        isShowingAd = true
        appOpenAd?.show(activity)
    }
}

