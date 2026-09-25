package com.atoll.core

import java.net.URLDecoder
import java.net.URLEncoder

/** Défi entre amis : tout tient dans un lien (graine, score à battre, pseudo, pays). */
data class Challenge(val seed: Int, val score: Long, val name: String, val country: String) {
    fun toQuery(): String =
        "s=" + seed.toUnsignedLong().toString(36) +
            "&p=" + score.toString(36) +
            "&n=" + URLEncoder.encode(name.take(20), "UTF-8") +
            "&c=" + country

    companion object {
        fun fromQuery(query: String?): Challenge? {
            if (query.isNullOrBlank()) return null
            val map = query.removePrefix("?").split('&').mapNotNull {
                val i = it.indexOf('=')
                if (i <= 0) null else it.substring(0, i) to URLDecoder.decode(it.substring(i + 1), "UTF-8")
            }.toMap()
            val seed = map["s"]?.toLongOrNull(36)?.takeIf { it in 0..0xFFFFFFFFL }?.toInt() ?: return null
            val score = map["p"]?.toLongOrNull(36)?.takeIf { it in 0..Nations.MAX_DAILY_SCORE } ?: return null
            val name = (map["n"] ?: "").filter { !it.isISOControl() }.take(20)
            val country = map["c"]?.uppercase()?.takeIf { Nations.isKnown(it) } ?: Nations.WORLD
            return Challenge(seed, score, name, country)
        }
    }
}

object ShareText {
    /** 18450 -> "18 450" (espace fine insécable). */
    fun fmt(n: Long): String {
        val s = n.toString()
        val out = StringBuilder()
        s.forEachIndexed { i, ch ->
            if (i > 0 && (s.length - i) % 3 == 0) out.append('\u202F')
            out.append(ch)
        }
        return out.toString()
    }
}
