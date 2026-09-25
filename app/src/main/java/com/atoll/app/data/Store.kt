package com.atoll.app.data

import android.content.Context
import android.content.SharedPreferences
import android.telephony.TelephonyManager
import com.atoll.core.Nations
import org.json.JSONObject
import java.util.Locale

/** Tout ce que le jeu mémorise, sur le téléphone (et dans la sauvegarde cloud Play Games). */
class Store(context: Context) {
    private val p: SharedPreferences = context.getSharedPreferences("atoll", Context.MODE_PRIVATE)

    // ---------- Réglages ----------
    var country: String
        get() = p.getString("country", null) ?: guessCountry
        set(v) = p.edit().putString("country", v).apply()
    val countryChosen get() = p.contains("country")
    private val guessCountry: String = run {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val sim = tm?.simCountryIso?.uppercase(Locale.ROOT).orEmpty()
        val loc = Locale.getDefault().country.uppercase(Locale.ROOT)
        listOf(sim, loc).firstOrNull { it.length == 2 && Nations.isKnown(it) } ?: Nations.WORLD
    }
    var sound by bool("sound", true)
    var haptics by bool("haptics", true)
    var bigCells by bool("bigCells", false)
    var reminders by bool("reminders", true)
    var tutorialDone by bool("tutorialDone", false)
    var adsRemoved by bool("adsRemoved", false)
    var referrerChecked by bool("referrerChecked", false)
    var reviewAsked by bool("reviewAsked", false)
    var notificationAsked by bool("notificationAsked", false)
    var playerName: String
        get() = p.getString("playerName", "") ?: ""
        set(v) = p.edit().putString("playerName", v).apply()

    // ---------- Progression ----------
    var bestClassic by long("bestClassic")
    var gamesPlayed by long("gamesPlayed")
    var totalPearls by long("totalPearls")
    var totalLagoons by long("totalLagoons")
    var miles by long("miles")
    var bestStage by long("bestStage")
    var streak by long("streak")
    var bestStreak by long("bestStreak")
    var lastDailyDay: String
        get() = p.getString("lastDailyDay", "") ?: ""
        set(v) = p.edit().putString("lastDailyDay", v).apply()
    var dailiesPlayed by long("dailiesPlayed")
    var lastInterstitialAt by long("lastInterstitialAt")

    /** Score officiel du Défi d'un jour donné (premier essai), ou null. */
    fun dailyScore(day: String): Long? = if (p.contains("daily.$day")) p.getLong("daily.$day", 0) else null

    fun recordDaily(day: String, score: Long, previousDay: String) {
        if (dailyScore(day) != null) return
        p.edit().putLong("daily.$day", score).apply()
        streak = if (lastDailyDay == previousDay) streak + 1 else 1
        bestStreak = maxOf(bestStreak, streak)
        lastDailyDay = day
        dailiesPlayed += 1
    }

    // ---------- Partie en cours (reprise) ----------
    var savedGame: String?
        get() = p.getString("savedGame", null)
        set(v) = p.edit().putString("savedGame", v).apply()

    // ---------- Sauvegarde cloud ----------
    fun exportJson(): String = JSONObject().apply {
        put("v", 1)
        put("bestClassic", bestClassic); put("gamesPlayed", gamesPlayed); put("totalPearls", totalPearls)
        put("totalLagoons", totalLagoons); put("miles", miles); put("bestStage", bestStage)
        put("streak", streak); put("bestStreak", bestStreak); put("lastDailyDay", lastDailyDay)
        put("dailiesPlayed", dailiesPlayed); put("tutorialDone", tutorialDone)
        if (countryChosen) put("country", country)
        val days = JSONObject()
        p.all.keys.filter { it.startsWith("daily.") }.forEach { days.put(it.removePrefix("daily."), p.getLong(it, 0)) }
        put("dailies", days)
    }.toString()

    /** Fusionne une sauvegarde cloud : on garde toujours le meilleur des deux. */
    fun mergeJson(json: String) {
        val o = runCatching { JSONObject(json) }.getOrNull() ?: return
        bestClassic = maxOf(bestClassic, o.optLong("bestClassic"))
        gamesPlayed = maxOf(gamesPlayed, o.optLong("gamesPlayed"))
        totalPearls = maxOf(totalPearls, o.optLong("totalPearls"))
        totalLagoons = maxOf(totalLagoons, o.optLong("totalLagoons"))
        miles = maxOf(miles, o.optLong("miles"))
        bestStage = maxOf(bestStage, o.optLong("bestStage"))
        bestStreak = maxOf(bestStreak, o.optLong("bestStreak"))
        dailiesPlayed = maxOf(dailiesPlayed, o.optLong("dailiesPlayed"))
        if (o.optString("lastDailyDay") > lastDailyDay) {
            lastDailyDay = o.optString("lastDailyDay"); streak = o.optLong("streak")
        }
        if (o.optBoolean("tutorialDone")) tutorialDone = true
        if (!countryChosen && o.has("country")) country = o.getString("country")
        o.optJSONObject("dailies")?.let { d ->
            val e = p.edit()
            d.keys().forEach { day -> if (!p.contains("daily.$day")) e.putLong("daily.$day", d.getLong(day)) }
            e.apply()
        }
    }

    // ---------- Délégués ----------
    private fun bool(key: String, def: Boolean) = object : kotlin.properties.ReadWriteProperty<Any?, Boolean> {
        override fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>) = p.getBoolean(key, def)
        override fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, value: Boolean) { p.edit().putBoolean(key, value).apply() }
    }

    private fun long(key: String) = object : kotlin.properties.ReadWriteProperty<Any?, Long> {
        override fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>) = p.getLong(key, 0L)
        override fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, value: Long) { p.edit().putLong(key, value).apply() }
    }
}
