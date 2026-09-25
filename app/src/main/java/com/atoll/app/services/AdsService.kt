package com.atoll.app.services

import android.app.Activity
import android.content.Context
import android.util.Log
import com.atoll.app.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

/**
 * AdMob + consentement UMP (RGPD). Pubs récompensées au choix du joueur,
 * interstitiels rares en fin de partie. Aucune pub dans le Défi des Nations.
 */
class AdsService(private val context: Context) {
    private val tag = "Ads"
    private val started = AtomicBoolean(false)
    private lateinit var consent: ConsentInformation
    private var rewarded: RewardedAd? = null
    private var interstitial: InterstitialAd? = null
    private var loadingRewarded = false
    private var loadingInterstitial = false

    val ready get() = started.get()
    val rewardedReady get() = rewarded != null
    val privacyOptionsRequired: Boolean
        get() = ::consent.isInitialized &&
            consent.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /** À appeler au démarrage : demande le consentement si nécessaire, puis initialise les pubs. */
    fun gatherConsent(activity: Activity) {
        consent = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder().build()
        consent.requestConsentInfoUpdate(activity, params, {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { err ->
                if (err != null) Log.w(tag, "consent form: ${err.message}")
                if (consent.canRequestAds()) start(activity)
            }
        }, { err -> Log.w(tag, "consent info: ${err.message}") })
        if (consent.canRequestAds()) start(activity)
    }

    fun showPrivacyOptions(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { err -> if (err != null) Log.w(tag, err.message) }
    }

    private fun start(activity: Activity) {
        if (!started.compareAndSet(false, true)) return
        MobileAds.initialize(context) { preload(activity) }
    }

    fun preload(activity: Activity, interstitials: Boolean = true) {
        if (!started.get()) return
        if (rewarded == null && !loadingRewarded) {
            loadingRewarded = true
            RewardedAd.load(activity, BuildConfig.ADMOB_REWARDED, AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) { rewarded = ad; loadingRewarded = false }
                override fun onAdFailedToLoad(error: LoadAdError) { loadingRewarded = false; Log.w(tag, "rewarded: ${error.message}") }
            })
        }
        if (interstitials && interstitial == null && !loadingInterstitial) {
            loadingInterstitial = true
            InterstitialAd.load(activity, BuildConfig.ADMOB_INTERSTITIAL, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { interstitial = ad; loadingInterstitial = false }
                override fun onAdFailedToLoad(error: LoadAdError) { loadingInterstitial = false; Log.w(tag, "interstitial: ${error.message}") }
            })
        }
    }

    /** Montre une pub récompensée ; [onReward] n'est appelé que si la vidéo a été regardée. */
    fun showRewarded(activity: Activity, onClosed: (rewarded: Boolean) -> Unit) {
        val ad = rewarded
        if (ad == null) { onClosed(false); preload(activity); return }
        rewarded = null
        var earned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { onClosed(earned); preload(activity) }
            override fun onAdFailedToShowFullScreenContent(error: AdError) { onClosed(false); preload(activity) }
        }
        ad.show(activity) { earned = true }
    }

    /** Interstitiel si disponible ; renvoie true s'il a été affiché. */
    fun showInterstitial(activity: Activity, onClosed: () -> Unit): Boolean {
        val ad = interstitial ?: run { preload(activity); return false }
        interstitial = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { onClosed(); preload(activity) }
            override fun onAdFailedToShowFullScreenContent(error: AdError) { onClosed(); preload(activity) }
        }
        ad.show(activity)
        return true
    }
}
