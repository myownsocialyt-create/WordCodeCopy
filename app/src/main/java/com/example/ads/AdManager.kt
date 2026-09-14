package com.example.ads

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdManager(private val context: Context) {
    companion object {
        private const val TAG = "AdManager"
        private const val PREFS_NAME = "ad_prefs"
        private const val KEY_LAST_CLOSING_AD_TIME = "last_closing_ad_time"
        const val CLOSING_AD_INTERVAL_MS = 3_600_000L // 1 hour

        // Google standard test ad unit IDs
        const val BANNER_TEST_ID = "ca-app-pub-3940256099942544/6300978111"
        const val INTERSTITIAL_TEST_ID = "ca-app-pub-3940256099942544/1033173712"
        const val REWARDED_TEST_ID = "ca-app-pub-3940256099942544/5224354917"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var interstitialAd: InterstitialAd? = null
    private var appClosingInterstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private var isInterstitialLoading = false
    private var isClosingAdLoading = false
    private var isRewardedLoading = false

    init {
        try {
            MobileAds.initialize(context) { status ->
                Log.d(TAG, "AdMob SDK Initialized: $status")
                loadInterstitialAd()
                loadAppClosingInterstitialAd()
                loadRewardedAd()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MobileAds", e)
        }
    }

    fun loadInterstitialAd() {
        if (isInterstitialLoading || interstitialAd != null) return
        isInterstitialLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_TEST_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Interstitial Ad Loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.e(TAG, "Interstitial Ad Failed to load: ${error.message}")
                }
            }
        )
    }

    fun loadAppClosingInterstitialAd() {
        if (isClosingAdLoading || appClosingInterstitialAd != null) return
        isClosingAdLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_TEST_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    appClosingInterstitialAd = ad
                    isClosingAdLoading = false
                    Log.d(TAG, "App Closing Interstitial Ad Loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appClosingInterstitialAd = null
                    isClosingAdLoading = false
                    Log.e(TAG, "App Closing Interstitial Ad Failed to load: ${error.message}")
                }
            }
        )
    }

    fun loadRewardedAd() {
        if (isRewardedLoading || rewardedAd != null) return
        isRewardedLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_TEST_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "Rewarded Ad Loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.e(TAG, "Rewarded Ad Failed to load: ${error.message}")
                }
            }
        )
    }

    fun showInterstitial(activity: Activity, onDismiss: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd()
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    loadInterstitialAd()
                    onDismiss()
                }
            }
            ad.show(activity)
        } else {
            loadInterstitialAd()
            onDismiss()
        }
    }

    fun showRewarded(activity: Activity, onRewardEarned: () -> Unit, onDismiss: () -> Unit = {}) {
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd()
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    loadRewardedAd()
                    onDismiss()
                }
            }
            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned()
            }
        } else {
            loadRewardedAd()
            onDismiss()
        }
    }

    fun showAppClosingInterstitialIfEligible(activity: Activity, onDismiss: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        val lastClosingAdTime = prefs.getLong(KEY_LAST_CLOSING_AD_TIME, 0L)
        val timeSinceLastAd = currentTime - lastClosingAdTime

        if (timeSinceLastAd >= CLOSING_AD_INTERVAL_MS && appClosingInterstitialAd != null) {
            val ad = appClosingInterstitialAd
            ad?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appClosingInterstitialAd = null
                    prefs.edit().putLong(KEY_LAST_CLOSING_AD_TIME, System.currentTimeMillis()).apply()
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appClosingInterstitialAd = null
                    onDismiss()
                }
            }
            ad?.show(activity)
        } else {
            Log.d(TAG, "Skipping closing interstitial: elapsed ${timeSinceLastAd / 1000}s (< 3600s) or ad not loaded")
            onDismiss()
        }
    }
}
