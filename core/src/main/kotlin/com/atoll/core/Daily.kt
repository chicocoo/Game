package com.atoll.core

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

/**
 * Le jour du défi suit le reset quotidien des classements Play Games (UTC-7, toute l'année).
 */
object Daily {
    private val RESET_ZONE: ZoneOffset = ZoneOffset.ofHours(-7)
    private val EPOCH: LocalDate = LocalDate.of(2026, 1, 1)
    const val PIECES = 45

    fun date(now: Instant = Instant.now()): LocalDate = now.atOffset(RESET_ZONE).toLocalDate()

    fun key(now: Instant = Instant.now()): String = date(now).toString()

    fun seed(key: String): Int = hashString("atoll-daily-$key")

    /** Numéro du défi (#1 = 1er janvier 2026), affiché sur la carte de partage. */
    fun number(key: String): Long = ChronoUnit.DAYS.between(EPOCH, LocalDate.parse(key)) + 1

    /** Instant du prochain reset (pour le compte à rebours et la notification). */
    fun nextReset(now: Instant = Instant.now()): Instant =
        date(now).plusDays(1).atStartOfDay().toInstant(RESET_ZONE)

    /** Graine de la semaine (lundi du jour du défi) pour le Grand Tour hebdo. */
    fun weekKey(now: Instant = Instant.now()): String {
        val d = date(now)
        return d.minusDays((d.dayOfWeek.value - 1).toLong()).toString()
    }
}
