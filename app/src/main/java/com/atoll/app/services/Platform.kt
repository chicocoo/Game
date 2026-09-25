package com.atoll.app.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.atoll.app.MainActivity
import com.atoll.app.R
import com.atoll.app.data.Store
import com.atoll.core.Daily
import java.time.Duration
import java.time.Instant

/** Effets sonores courts (générés, dans res/raw). */
class Sounds(context: Context) {
    enum class Fx { PICK, PLACE, PEARL, CLEAR, COMBO, WIN, LOSE }

    private val pool = SoundPool.Builder().setMaxStreams(6)
        .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
        .build()
    private val ids = mapOf(
        Fx.PICK to pool.load(context, R.raw.pick, 1),
        Fx.PLACE to pool.load(context, R.raw.place, 1),
        Fx.PEARL to pool.load(context, R.raw.pearl, 1),
        Fx.CLEAR to pool.load(context, R.raw.clear, 1),
        Fx.COMBO to pool.load(context, R.raw.combo, 1),
        Fx.WIN to pool.load(context, R.raw.win, 1),
        Fx.LOSE to pool.load(context, R.raw.lose, 1),
    )
    var enabled = true

    /** [pitch] entre 0.5 et 2 : les combos montent dans les aigus. */
    fun play(fx: Fx, pitch: Float = 1f, volume: Float = 0.8f) {
        if (!enabled) return
        ids[fx]?.let { pool.play(it, volume, volume, 1, 0, pitch.coerceIn(0.5f, 2f)) }
    }
}

class Haptics(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= 31) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION") context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    var enabled = true

    fun tick(ms: Long = 12, amplitude: Int = 80) {
        if (!enabled) return
        runCatching { vibrator?.vibrate(VibrationEffect.createOneShot(ms, amplitude)) }
    }
}

/** Lit une seule fois le paramètre `referrer` du Play Store (défi reçu avant l'installation). */
object Referrer {
    fun read(context: Context, store: Store, onChallenge: (String) -> Unit) {
        if (store.referrerChecked) return
        val client = InstallReferrerClient.newBuilder(context).build()
        runCatching {
            client.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(code: Int) {
                    if (code == InstallReferrerClient.InstallReferrerResponse.OK) {
                        val ref = runCatching { client.installReferrer.installReferrer }.getOrNull()
                        store.referrerChecked = true
                        if (!ref.isNullOrBlank() && ref.contains("s=")) onChallenge(ref)
                    } else if (code != InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE) {
                        store.referrerChecked = true
                    }
                    runCatching { client.endConnection() }
                }
                override fun onInstallReferrerServiceDisconnected() {}
            })
        }
    }
}

/** Rappel local quotidien du Défi des Nations (aucun serveur de notifications). */
object Reminder {
    private const val CHANNEL = "daily"
    private const val WORK = "daily-reminder"

    fun schedule(context: Context, enabled: Boolean) {
        val wm = WorkManager.getInstance(context)
        if (!enabled) { wm.cancelUniqueWork(WORK); return }
        // Première exécution ~2 h après le reset du défi, puis toutes les 24 h.
        val delay = Duration.between(Instant.now(), Daily.nextReset().plus(Duration.ofHours(2))).toMinutes().coerceAtLeast(15)
        val req = PeriodicWorkRequestBuilder<ReminderWorker>(Duration.ofHours(24))
            .setInitialDelay(Duration.ofMinutes(delay))
            .build()
        wm.enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.UPDATE, req)
    }

    fun ensureChannel(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, context.getString(R.string.notif_channel), NotificationManager.IMPORTANCE_DEFAULT))
    }

    @android.annotation.SuppressLint("MissingPermission")
    fun notify(context: Context) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        ensureChannel(context)
        val intent = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java).putExtra("open", "daily"),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val n = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notif_title))
            .setContentText(context.getString(R.string.notif_text))
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(1, n) }
    }
}

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val store = Store(applicationContext)
        if (store.reminders && store.dailyScore(Daily.key()) == null) Reminder.notify(applicationContext)
        return Result.success()
    }
}
