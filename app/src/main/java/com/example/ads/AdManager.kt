package com.example.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Finds the host [Activity] from any [Context].
 * Needed to show full-screen ads from inside Compose UI code.
 */
fun Context.findActivity(): Activity? {
    var ctx: Context = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

object AdManager {
    private const val TAG = "AdManager"
    private const val PREFS_NAME = "ad_prefs"
    private const val KEY_LAST_CLOSING_AD_TIME = "last_closing_ad_time"
    private const val KEY_LAST_INTERSTITIAL_TIME = "last_interstitial_time"

    /** App-closing interstitial is shown at most once per hour. */
    const val CLOSING_AD_INTERVAL_MS = 3_600_000L

    /** Regular interstitials are capped to one per 60 seconds. */
    const val INTERSTITIAL_MIN_INTERVAL_MS = 60_000L

    // ---- Production AdMob IDs ----
    const val ADMOB_APP_ID = "ca-app-pub-8212461864193378~9820987959"
    const val PROD_INTERSTITIAL_ID = "ca-app-pub-8212461864193378/8050409587"
    const val PROD_REWARDED_ID = "ca-app-pub-8212461864193378/7680437595"

    // ---- Google sample/test IDs (used automatically in debug builds) ----
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    val interstitialAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_INTERSTITIAL_ID else PROD_INTERSTITIAL_ID

    val rewardedAdUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_REWARDED_ID else PROD_REWARDED_ID

    private var appContext: Context? = null
    private var prefs: SharedPreferences? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private var interstitialAd: InterstitialAd? = null
    private var appClosingInterstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private var isInterstitialLoading = false
    private var isClosingAdLoading = false
    private var isRewardedLoading = false

    /** Counts eligible triggers; an interstitial shows on every 2nd trigger. */
    private var interstitialTriggerCount = 0

    @Volatile
    private var initialized = false

    fun initialize(context: Context) {
        if (initialized) return
        initialized = true
        appContext = context.applicationContext
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Initialize on a background thread (Google's recommendation) so the
        // heavy SDK init never blocks the main thread and causes ANRs.
        Thread {
            try {
                MobileAds.initialize(context.applicationContext) { status ->
                    Log.d(TAG, "AdMob SDK initialized: $status")
                    // Ad loading must happen on the main thread.
                    mainHandler.post {
                        try {
                            loadInterstitialAd()
                            loadAppClosingInterstitialAd()
                            loadRewardedAd()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error preloading ads", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing MobileAds", e)
            }
        }.apply { name = "AdMobInit" }.start()
    }

    fun loadInterstitialAd() {
        val context = appContext ?: return
        if (isInterstitialLoading || interstitialAd != null) return
        isInterstitialLoading = true

        InterstitialAd.load(
            context,
            interstitialAdUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Interstitial ad loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.e(TAG, "Interstitial failed to load: ${error.message}")
                }
            }
        )
    }

    fun loadAppClosingInterstitialAd() {
        val context = appContext ?: return
        if (isClosingAdLoading || appClosingInterstitialAd != null) return
        isClosingAdLoading = true

        InterstitialAd.load(
            context,
            interstitialAdUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    appClosingInterstitialAd = ad
                    isClosingAdLoading = false
                    Log.d(TAG, "App-closing interstitial loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appClosingInterstitialAd = null
                    isClosingAdLoading = false
                    Log.e(TAG, "App-closing interstitial failed to load: ${error.message}")
                }
            }
        )
    }

    fun loadRewardedAd() {
        val context = appContext ?: return
        if (isRewardedLoading || rewardedAd != null) return
        isRewardedLoading = true

        RewardedAd.load(
            context,
            rewardedAdUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "Rewarded ad loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.e(TAG, "Rewarded ad failed to load: ${error.message}")
                }
            }
        )
    }

    /**
     * Shows an interstitial only when eligible:
     *  - every 2nd trigger, and
     *  - at least 60 seconds since the previous interstitial.
     * [onDone] is always invoked (after dismiss, or immediately when not shown).
     */
    fun showInterstitialIfEligible(activity: Activity, onDone: () -> Unit = {}) {
        interstitialTriggerCount++
        val now = System.currentTimeMillis()
        val lastShown = prefs?.getLong(KEY_LAST_INTERSTITIAL_TIME, 0L) ?: 0L
        val everySecondTrigger = interstitialTriggerCount % 2 == 0
        val cooldownPassed = now - lastShown >= INTERSTITIAL_MIN_INTERVAL_MS
        val ad = interstitialAd

        if (everySecondTrigger && cooldownPassed && ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    prefs?.edit()?.putLong(KEY_LAST_INTERSTITIAL_TIME, System.currentTimeMillis())?.apply()
                    loadInterstitialAd()
                    onDone()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    loadInterstitialAd()
                    onDone()
                }
            }
            ad.show(activity)
        } else {
            if (ad == null) loadInterstitialAd()
            Log.d(TAG, "Interstitial skipped (trigger=$interstitialTriggerCount, cooldownPassed=$cooldownPassed)")
            onDone()
        }
    }

    /**
     * Shows a rewarded ad. [onRewardEarned] fires ONLY when the user finishes
     * watching and actually earns the reward.
     */
    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onDismiss: () -> Unit = {}
    ) {
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

    /**
     * App-closing interstitial: shown at most once per hour when the user exits.
     * [onDismiss] always runs so the app can close.
     */
    fun showAppClosingInterstitialIfEligible(activity: Activity, onDismiss: () -> Unit) {
        val now = System.currentTimeMillis()
        val lastTime = prefs?.getLong(KEY_LAST_CLOSING_AD_TIME, 0L) ?: 0L
        val ad = appClosingInterstitialAd

        if (now - lastTime >= CLOSING_AD_INTERVAL_MS && ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appClosingInterstitialAd = null
                    prefs?.edit()?.putLong(KEY_LAST_CLOSING_AD_TIME, System.currentTimeMillis())?.apply()
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appClosingInterstitialAd = null
                    onDismiss()
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "Closing interstitial skipped (elapsed ${(now - lastTime) / 1000}s or ad not loaded)")
            onDismiss()
        }
    }
}
