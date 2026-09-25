package com.atoll.app.ui

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.atoll.app.services.Haptics
import com.atoll.app.services.Sounds
import com.atoll.core.Challenge
import com.atoll.core.Game
import com.atoll.core.PlaceResult
import com.atoll.core.Preview
import com.atoll.core.Stage
import kotlin.math.abs
import kotlin.random.Random

enum class Kind { CLASSIC, DAILY, PRACTICE, CHALLENGE, TOUR }

/** Effets visuels, positionnés en coordonnées de cases (indépendants de la taille d'écran). */
sealed interface Fx { val start: Long; val dur: Long }
data class Pop(val cell: Int, override val start: Long, override val dur: Long = 140) : Fx
data class PearlIn(val cell: Int, override val start: Long, override val dur: Long = 320) : Fx
data class ClearOut(val cell: Int, val kind: Int, val color: Int, override val start: Long, override val dur: Long = 260) : Fx
data class Particle(val x: Float, val y: Float, val vx: Float, val vy: Float, val r: Float, val pearl: Boolean,
                    override val start: Long, override val dur: Long) : Fx
data class Floater(val text: String, val x: Float, val y: Float, val big: Boolean, val gold: Boolean,
                   override val start: Long, override val dur: Long = 1100) : Fx

data class Drag(val slot: Int, val x: Float, val y: Float, val lift: Float, val r0: Int, val c0: Int, val preview: Preview?,
                val keyboard: Boolean = false)

/** Ce que l'écran de fin affiche ; les rangs arrivent plus tard (réseau). */
data class Result(
    val title: String,
    val score: Long,
    val subtitle: String,
    val canRevive: Boolean,
    val won: Boolean = false,
    val rank: String? = null,
    val share: String? = null,
)

/**
 * État d'une partie à l'écran : la logique (core) + les effets + le glisser-déposer.
 */
class GameController(
    val game: Game,
    val kind: Kind,
    val challenge: Challenge? = null,
    val stage: Stage? = null,
    val tutorial: Boolean = false,
    private val sounds: Sounds? = null,
    private val haptics: Haptics? = null,
    private val labels: Labels = Labels(),
) {
    /** Incrémenté à chaque changement de la partie, pour recomposer. */
    var version by mutableIntStateOf(0)
        private set
    var drag by mutableStateOf<Drag?>(null)
    var result by mutableStateOf<Result?>(null)
    var tutorialStep by mutableIntStateOf(if (tutorial) 1 else 0)
    var shakeUntil = 0L
        private set
    val effects = ArrayList<Fx>()
    var sea: BooleanArray = game.seaMask()
        private set

    /** Appelé après chaque pose (succès, sauvegarde). */
    var onPlaced: ((PlaceResult) -> Unit)? = null

    /** Appelé quand la partie se termine (après les animations). */
    var onOver: ((GameController) -> Unit)? = null
    var overAt = 0L
        private set

    fun now() = SystemClock.uptimeMillis()

    fun touch() { sea = game.seaMask(); version++ }

    fun preview(slot: Int, r0: Int, c0: Int): Preview? = game.preview(slot, r0, c0)

    fun place(slot: Int, r0: Int, c0: Int): PlaceResult? {
        val before = game.grid.copyOf()
        val ev = game.place(slot, r0, c0) ?: return null
        effectsFor(ev, before)
        if (tutorialStep == 1 && ev.lagoons.isNotEmpty()) tutorialStep = 2
        else if (tutorialStep == 2 && ev.lines.isNotEmpty()) tutorialStep = 0
        else if (tutorialStep != 0 && game.hand.none { it?.piece?.family == "dot" }) tutorialStep = 0
        if (ev.over) overAt = now() + if (ev.lines.isNotEmpty() || ev.lagoons.isNotEmpty()) 1100 else 500
        touch()
        onPlaced?.invoke(ev)
        return ev
    }

    fun undo(): Boolean = game.undo().also { if (it) { effects.clear(); touch() } }

    fun revive(): Boolean = game.revive().also { if (it) { result = null; overAt = 0; touch() } }

    private fun effectsFor(ev: PlaceResult, before: IntArray) {
        val t = now()
        val n = Game.N
        ev.placed.forEach { effects.add(Pop(it, t)) }
        sounds?.play(Sounds.Fx.PLACE)
        haptics?.tick(10, 60)

        var pearlEnd = t
        ev.lagoons.forEach { lag ->
            lag.cells.forEach { i ->
                val d = ev.placed.minOf { p -> abs(p / n - i / n) + abs(p % n - i % n) }
                val st = t + 80 + d * 45L
                effects.add(PearlIn(i, st))
                pearlEnd = maxOf(pearlEnd, st + 320)
            }
            lag.clearedRing.forEach { i -> effects.add(ClearOut(i, Game.BLOCK, -1, pearlEnd)) }
            val mid = lag.cells[lag.cells.size / 2]
            val big = lag.cells.size >= 6
            effects.add(Floater((if (big) labels.grandLagoon else labels.lagoon) + " +" + fmt(lag.points.toLong()),
                mid % n + 0.5f, mid / n + 0.5f, big, true, t + 120))
        }
        if (ev.lagoons.isNotEmpty()) { sounds?.play(Sounds.Fx.PEARL); haptics?.tick(25, 120) }

        if (ev.lines.isNotEmpty()) {
            val clearStart = if (ev.lagoons.isNotEmpty()) pearlEnd + 60 else t + 90
            val order = HashMap<Int, Int>()
            ev.lines.forEach { l -> l.cells.forEachIndexed { j, i -> order[i] = minOf(order[i] ?: 99, j) } }
            ev.cleared.forEach { cl ->
                val st = clearStart + (order[cl.index] ?: 0) * 22L
                val color = if (cl.index in ev.placed) ev.color else cl.color
                effects.add(ClearOut(cl.index, cl.kind, color, st))
                val wasPearl = cl.kind == Game.PEARL
                repeat(if (wasPearl) 7 else 3) {
                    val a = Random.nextFloat() * 6.283f
                    val sp = 1.5f + Random.nextFloat() * 4f
                    effects.add(Particle(cl.index % n + 0.5f, cl.index / n + 0.5f, kotlin.math.cos(a) * sp, kotlin.math.sin(a) * sp - 3f,
                        0.05f + Random.nextFloat() * 0.06f, wasPearl, st, 520L + Random.nextLong(260)))
                }
            }
            ev.lines.forEachIndexed { idx, l ->
                val c = l.cells[4]
                val label = if (l.pearls > 0) "×${1 + l.pearls}  +${fmt(l.points.toLong())}" else "+${fmt(l.points.toLong())}"
                effects.add(Floater(label, c % n + 0.5f, c / n + 0.5f, l.pearls > 0, l.pearls > 0, clearStart + idx * 90L))
            }
            val parts = mutableListOf<String>()
            if (ev.multi > 1) parts.add(labels.lines.format(ev.multi) + " ×" + ev.multi)
            if (ev.combo > 1) parts.add(labels.combo.format(fmtMult(1 + 0.5 * (ev.combo - 1))))
            if (parts.isNotEmpty()) effects.add(Floater(parts.joinToString(" · "), n / 2f, 1.2f, true, true, clearStart + 200))
            if (ev.multi > 1 || ev.linePoints >= 1000) shakeUntil = clearStart + 260
            val pitch = 1f + 0.12f * (ev.combo - 1).coerceAtMost(8)
            sounds?.play(if (ev.combo > 2) Sounds.Fx.COMBO else Sounds.Fx.CLEAR, pitch)
            haptics?.tick(if (ev.multi > 1) 40 else 22, 160)
        }
        ev.rock?.let { effects.add(Pop(it, t + 400)) }
        if (ev.over) sounds?.play(if (game.won || kind == Kind.CLASSIC) Sounds.Fx.WIN else Sounds.Fx.LOSE)
    }

    fun prune(t: Long) { effects.removeAll { t - it.start > it.dur } }

    companion object {
        fun fmt(n: Long): String = com.atoll.core.ShareText.fmt(n)
        fun fmtMult(x: Double): String = if (x == x.toLong().toDouble()) x.toLong().toString() else String.format("%.1f", x)
    }
}

/** Textes traduits utilisés dans les effets (fournis depuis les ressources). */
data class Labels(
    val lagoon: String = "Lagoon",
    val grandLagoon: String = "Grand lagoon!",
    val lines: String = "%d lines",
    val combo: String = "Combo ×%s",
)
