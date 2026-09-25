package com.atoll.core

/**
 * Hasard déterministe, identique bit à bit au prototype web (mulberry32),
 * pour que le Défi du jour donne la même suite de pièces sur tous les appareils.
 */
class Rng(seed: Int) {
    private var a: Int = seed

    fun next(): Double {
        a += 0x6D2B79F5
        var t = a
        t = (t xor (t ushr 15)) * (t or 1)
        t = t xor (t + (t xor (t ushr 7)) * (t or 61))
        return ((t xor (t ushr 14)).toLong() and 0xFFFFFFFFL) / 4294967296.0
    }

    fun nextInt(bound: Int): Int = (next() * bound).toInt().coerceIn(0, bound - 1)

    /** État interne, pour sauvegarder / reprendre une partie. */
    var state: Int
        get() = a
        set(value) { a = value }
}

/** FNV-1a 32 bits sur les unités UTF-16 (comme `charCodeAt` en JavaScript). */
fun hashString(s: String): Int {
    var h = 0x811c9dc5.toInt()
    for (ch in s) {
        h = h xor ch.code
        h *= 0x01000193
    }
    return h
}

fun Int.toUnsignedLong(): Long = toLong() and 0xFFFFFFFFL
