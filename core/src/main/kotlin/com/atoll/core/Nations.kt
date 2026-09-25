package com.atoll.core

/**
 * Le Défi des Nations sans serveur : chaque score envoyé au classement mondial du jour porte
 * un « score tag » contenant le code pays. Chaque téléphone lit le top 100 et calcule le
 * tableau des médailles lui-même.
 */
object Nations {
    /** Code « pays » neutre (option Citoyen du monde). */
    const val WORLD = "XX"

    private val CONTINENTS: Map<String, String> = buildMap {
        fun add(cont: String, codes: String) { codes.split(' ').forEach { put(it, cont) } }
        add("AF", "DZ AO BJ BW BF BI CV CM CF TD KM CG CD CI DJ EG GQ ER SZ ET GA GM GH GN GW KE LS LR LY MG MW ML MR MU YT MA MZ NA NE NG RE RW SH ST SN SC SL SO ZA SS SD TZ TG TN UG EH ZM ZW")
        add("AS", "AF AM AZ BH BD BT BN KH CN CY GE HK IN ID IR IQ IL JP JO KZ KW KG LA LB MO MY MV MN MM NP KP OM PK PS PH QA SA SG KR LK SY TW TJ TH TL TR TM AE UZ VN YE")
        add("EU", "AL AD AT BY BE BA BG HR CZ DK EE FO FI FR DE GI GR GG HU IS IE IM IT JE XK LV LI LT LU MT MD MC ME NL MK NO PL PT RO RU SM RS SK SI ES SE CH UA GB VA AX")
        add("NA", "AI AG AW BS BB BZ BM BQ VG CA KY CR CU CW DM DO SV GL GD GP GT HT HN JM MQ MX MS NI PA PR BL KN LC MF PM VC SX TT TC US VI")
        add("SA", "AR BO BR CL CO EC FK GF GY PY PE SR UY VE")
        add("OC", "AS AU CK FJ PF GU KI MH FM NR NC NZ NU NF MP PW PG PN WS SB TK TO TV VU WF")
    }

    fun continent(code: String): String? = CONTINENTS[code.uppercase()]

    fun isKnown(code: String) = code == WORLD || CONTINENTS.containsKey(code.uppercase())

    /** Drapeau emoji à partir du code ISO 3166-1 alpha-2. */
    fun flag(code: String): String {
        if (code.length != 2 || code == WORLD) return "🌍"
        val up = code.uppercase()
        return String(Character.toChars(0x1F1E6 + (up[0] - 'A'))) + String(Character.toChars(0x1F1E6 + (up[1] - 'A')))
    }

    // ---------- Score tag ----------
    private val TAG = Regex("^v1-([A-Z]{2})-([0-9a-z]{3})$")

    private fun check(country: String, score: Long, day: String): String {
        val h = hashString("$country|$score|$day|atoll").toUnsignedLong() % 46656L
        return h.toString(36).padStart(3, '0')
    }

    fun tag(country: String, score: Long, day: String): String {
        val c = if (isKnown(country)) country.uppercase() else WORLD
        return "v1-$c-${check(c, score, day)}"
    }

    /** Pays d'un score tag valide pour ce score et ce jour, sinon null. */
    fun countryOf(tag: String?, score: Long, day: String): String? {
        val m = TAG.matchEntire(tag ?: return null) ?: return null
        val c = m.groupValues[1]
        return if (m.groupValues[2] == check(c, score, day)) c else null
    }

    // ---------- Tableau des médailles ----------
    data class Entry(val rank: Long, val score: Long, val tag: String?)

    data class Standing(
        val country: String,
        val points: Long,
        val gold: Int,
        val silver: Int,
        val bronze: Int,
        val inTop: Int,
        val best: Long,
    )

    /**
     * Points des nations = somme de (101 − rang) sur le top 100 du jour ; médailles pour le podium.
     * Les scores au-delà de [maxScore] ou aux tags invalides sont ignorés (anti-triche simple).
     */
    fun standings(entries: List<Entry>, day: String, maxScore: Long = MAX_DAILY_SCORE): List<Standing> {
        val acc = LinkedHashMap<String, LongArray>() // points, gold, silver, bronze, inTop, best
        entries.filter { it.rank in 1..100 && it.score in 0..maxScore }.forEach { e ->
            val c = countryOf(e.tag, e.score, day) ?: return@forEach
            if (c == WORLD) return@forEach
            val a = acc.getOrPut(c) { LongArray(6) }
            a[0] += 101 - e.rank
            when (e.rank) { 1L -> a[1]++; 2L -> a[2]++; 3L -> a[3]++ }
            a[4]++
            a[5] = maxOf(a[5], e.score)
        }
        return acc.map { (c, a) -> Standing(c, a[0], a[1].toInt(), a[2].toInt(), a[3].toInt(), a[4].toInt(), a[5]) }
            .sortedWith(compareByDescending<Standing> { it.points }.thenByDescending { it.best })
    }

    /** Borne haute acceptée pour un score du Défi (45 pièces). */
    const val MAX_DAILY_SCORE = 400_000L
}
