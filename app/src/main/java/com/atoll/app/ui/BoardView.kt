package com.atoll.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.atoll.core.Game
import com.atoll.core.Piece
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private const val N = Game.N

/** Géométrie du plateau et de la main, en pixels. */
private class Geo(val w: Float, bigHand: Boolean) {
    val pad = w * 0.035f
    val size = w - pad * 2
    val cell = size / N
    val bx = pad
    val by = pad
    val handTop = pad + size + pad * 0.9f
    val slotW = w / 3
    val hcell = min(cell * (if (bigHand) 0.74f else 0.66f), slotW / 5.4f)
    val handH = hcell * 3 + pad * 2.2f
    val h = handTop + handH + pad * 0.6f
}

@Composable
fun BoardView(ctrl: GameController, bigHand: Boolean, contentDescription: String, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val measurer = rememberTextMeasurer()
    var time by remember { mutableLongStateOf(ctrl.now()) }
    LaunchedEffect(ctrl) {
        while (true) {
            withFrameMillis { }
            time = ctrl.now()
            ctrl.prune(time)
            val over = ctrl.overAt
            if (over > 0 && time >= over && ctrl.game.over && ctrl.result == null) ctrl.onOver?.invoke(ctrl)
        }
    }

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val wPx = with(LocalDensity.current) { maxWidth.toPx() }
        val geo = remember(wPx, bigHand) { Geo(wPx, bigHand) }
        val hDp: Dp = with(LocalDensity.current) { geo.h.toDp() }

        Canvas(
            Modifier
                .fillMaxWidth()
                .height(hDp)
                .semantics { this.contentDescription = contentDescription }
                .focusable()
                .pointerInput(ctrl, geo) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val game = ctrl.game
                        if (game.over || ctrl.result != null) return@awaitEachGesture
                        if (down.position.y < geo.handTop - geo.cell * 0.3f) return@awaitEachGesture
                        val slot = (down.position.x / geo.slotW).toInt().coerceIn(0, 2)
                        val piece = game.hand[slot]?.piece ?: return@awaitEachGesture
                        val lift = if (down.type == PointerType.Touch) geo.cell * 1.7f else geo.cell * 0.4f
                        fun target(x: Float, y: Float): Pair<Int, Int> {
                            val c0 = ((x - geo.bx) / geo.cell - piece.w / 2f).roundToInt()
                            val r0 = ((y - lift - geo.by) / geo.cell - piece.h / 2f).roundToInt()
                            return r0 to c0
                        }
                        var (r0, c0) = target(down.position.x, down.position.y)
                        ctrl.drag = Drag(slot, down.position.x, down.position.y, lift, r0, c0, ctrl.preview(slot, r0, c0))
                        ctrl.let { } // démarrage du glisser
                        down.consume()
                        while (true) {
                            val event = awaitPointerEvent()
                            val ch = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!ch.pressed) {
                                val d = ctrl.drag
                                ctrl.drag = null
                                if (d?.preview != null) ctrl.place(d.slot, d.r0, d.c0)
                                break
                            }
                            val (nr, nc) = target(ch.position.x, ch.position.y)
                            val prev = ctrl.drag
                            ctrl.drag = if (prev != null && nr == prev.r0 && nc == prev.c0) prev.copy(x = ch.position.x, y = ch.position.y)
                            else Drag(slot, ch.position.x, ch.position.y, lift, nr, nc, ctrl.preview(slot, nr, nc))
                            r0 = nr; c0 = nc
                            ch.consume()
                        }
                    }
                },
        ) {
            @Suppress("UNUSED_VARIABLE") val v = ctrl.version
            drawGame(ctrl, geo, p, time, measurer)
        }
    }
}

private fun DrawScope.drawGame(ctrl: GameController, g: Geo, p: Palette, t: Long, measurer: androidx.compose.ui.text.TextMeasurer) {
    val game = ctrl.game
    val s = g.cell
    val shake = if (t < ctrl.shakeUntil) ((ctrl.shakeUntil - t) / 260f) * 3f else 0f
    val sx = if (shake > 0) (Math.random().toFloat() - 0.5f) * 2 * shake else 0f
    val sy = if (shake > 0) (Math.random().toFloat() - 0.5f) * 2 * shake else 0f

    translate(sx, sy) {
        drawRoundRect(p.board, Offset(g.bx - g.pad * 0.55f, g.by - g.pad * 0.55f), Size(g.size + g.pad * 1.1f, g.size + g.pad * 1.1f), CornerRadius(g.pad * 1.2f))

        // États spéciaux des cases animées
        val pearlScale = HashMap<Int, Float>()
        val hidden = HashSet<Int>()
        val ghosts = HashMap<Int, ClearOut>()
        val pops = HashMap<Int, Float>()
        for (fx in ctrl.effects) {
            val k = (t - fx.start).toFloat() / fx.dur
            when (fx) {
                is PearlIn -> if (k < 0) hidden.add(fx.cell) else if (k < 1) pearlScale[fx.cell] = if (k < 0.7f) k / 0.7f * 1.15f else 1.15f - (k - 0.7f) / 0.3f * 0.15f
                is ClearOut -> if (k < 1) ghosts[fx.cell] = fx
                is Pop -> if (k in 0f..1f) pops[fx.cell] = 0.82f + 0.18f * sin(k * Math.PI.toFloat() / 2)
                else -> {}
            }
        }
        val pv = ctrl.drag?.preview
        val pearlSet = pv?.pearls?.toHashSet() ?: emptySet()
        val lineSet = pv?.lines?.flatMap { it.cells }?.toHashSet() ?: emptySet()

        for (i in 0 until N * N) {
            val x = g.bx + (i % N) * s
            val y = g.by + (i / N) * s
            val ghost = ghosts[i]
            if (ghost != null) {
                drawSea(x, y, s, t, i, p)
                val k = ((t - ghost.start).toFloat() / ghost.dur).coerceAtLeast(0f)
                val sc = 1f - k
                if (sc > 0.02f && i !in hidden) scale(sc, Offset(x + s / 2, y + s / 2)) {
                    if (ghost.kind == Game.PEARL) drawPearl(x, y, s, t, pearlScale[i] ?: 1f, p)
                    else drawBlock(x, y, s, blockColor(p, ghost.color), 1f)
                }
                continue
            }
            when (game.grid[i]) {
                Game.EMPTY -> if (ctrl.sea[i]) drawSea(x, y, s, t, i, p) else drawHole(x, y, s, p)
                Game.PEARL -> if (i in hidden) drawSea(x, y, s, t, i, p) else drawPearl(x, y, s, t, pearlScale[i] ?: 1f, p)
                Game.ROCK -> drawRock(x, y, s, p)
                else -> {
                    val c = blockColor(p, game.colors[i])
                    val pop = pops[i]
                    if (pop != null) scale(pop, Offset(x + s / 2, y + s / 2)) { drawBlock(x, y, s, c, 1f) } else drawBlock(x, y, s, c, 1f)
                }
            }
            if (i in lineSet) drawRoundRect(Color.White.copy(alpha = 0.28f), Offset(x + s * 0.07f, y + s * 0.07f), Size(s * 0.86f, s * 0.86f), CornerRadius(s * 0.2f))
        }

        // Aperçu : pièce fantôme + futures perles
        val d = ctrl.drag
        if (d != null) {
            val slot = game.hand[d.slot]
            if (slot != null) {
                val valid = pv != null
                if (valid || d.keyboard) slot.piece.cells.forEach { (dr, dc) ->
                    val x = g.bx + (d.c0 + dc) * s
                    val y = g.by + (d.r0 + dr) * s
                    if (d.r0 + dr in 0 until N && d.c0 + dc in 0 until N) drawBlock(x, y, s, blockColor(p, slot.color), if (valid) 0.42f else 0.18f)
                }
                val pulse = 0.6f + 0.4f * sin(t / 160f)
                pearlSet.forEach { i ->
                    drawRoundRect(p.gold.copy(alpha = pulse), Offset(g.bx + (i % N) * s + s * 0.1f, g.by + (i / N) * s + s * 0.1f),
                        Size(s * 0.8f, s * 0.8f), CornerRadius(s * 0.2f),
                        style = Stroke(width = s * 0.08f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(s * 0.12f, s * 0.08f))))
                }
            }
        }

        // Tutoriel : case cible qui clignote
        if (ctrl.tutorialStep != 0 && d == null) {
            val (tr, tc) = if (ctrl.tutorialStep == 1) 5 to 5 else 4 to 8
            val a = 0.45f + 0.45f * sin(t / 220f)
            drawRoundRect(p.gold.copy(alpha = a), Offset(g.bx + tc * s + s * 0.08f, g.by + tr * s + s * 0.08f), Size(s * 0.84f, s * 0.84f),
                CornerRadius(s * 0.2f), style = Stroke(width = s * 0.09f))
        }
    }

    // Main
    for (k in 0 until 3) {
        val slot = game.hand[k] ?: continue
        if (ctrl.drag?.slot == k && ctrl.drag?.keyboard != true) continue
        val hs = g.hcell
        val px = k * g.slotW + (g.slotW - slot.piece.w * hs) / 2
        val py = g.handTop + (g.handH - slot.piece.h * hs) / 2
        val fits = game.pieceFits(slot.piece)
        drawPiece(slot.piece, blockColor(p, slot.color), px, py, hs, if (fits) 1f else 0.28f)
    }

    // Pièce en cours de glisser
    val d = ctrl.drag
    if (d != null && !d.keyboard) {
        val slot = game.hand[d.slot]
        if (slot != null) {
            val dx = d.x - slot.piece.w * s / 2
            val dy = d.y - d.lift - slot.piece.h * s / 2
            drawPiece(slot.piece, blockColor(p, slot.color), dx, dy, s, 0.96f)
        }
    }

    // Particules et textes
    for (fx in ctrl.effects) {
        val k = (t - fx.start).toFloat() / fx.dur
        if (k < 0 || k > 1) continue
        when (fx) {
            is Particle -> {
                val dt = (t - fx.start) / 1000f
                val x = g.bx + (fx.x + fx.vx * dt) * s
                val y = g.by + (fx.y + fx.vy * dt + 9f * dt * dt) * s
                drawCircle(if (fx.pearl) p.nacre2 else p.nacre1, fx.r * s, Offset(x, y), alpha = 1f - k)
            }
            is Floater -> {
                val size = (if (fx.big) 0.62f else 0.5f) * s
                val style = TextStyle(fontSize = (size / density).sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center,
                    color = if (fx.gold) p.nacre1 else Color.White)
                val layout = measurer.measure(fx.text, style)
                val x = g.bx + fx.x * s - layout.size.width / 2f
                val y = g.by + fx.y * s - layout.size.height / 2f - k * s * 0.9f
                val alpha = if (k < 0.75f) 1f else 1f - (k - 0.75f) / 0.25f
                drawText(layout, color = Color(0xCC04262D), topLeft = Offset(x + 2, y + 2), alpha = alpha)
                drawText(layout, topLeft = Offset(x, y), alpha = alpha)
            }
            else -> {}
        }
    }
}

private fun blockColor(p: Palette, idx: Int) = p.blocks[if (idx in 0..4) idx else 0]

private fun shade(c: Color, amt: Float): Color =
    if (amt >= 0) Color(c.red + (1 - c.red) * amt, c.green + (1 - c.green) * amt, c.blue + (1 - c.blue) * amt, c.alpha)
    else Color(c.red * (1 + amt), c.green * (1 + amt), c.blue * (1 + amt), c.alpha)

private fun DrawScope.drawBlock(x: Float, y: Float, s: Float, c: Color, alpha: Float) {
    val gap = s * 0.07f
    val r = CornerRadius(s * 0.2f)
    val o = Offset(x + gap, y + gap)
    val sz = Size(s - gap * 2, s - gap * 2)
    drawRoundRect(c, o, sz, r, alpha = alpha)
    drawRoundRect(shade(c, 0.28f), o, Size(sz.width, sz.height * 0.46f), r, alpha = alpha * 0.55f)
    drawRoundRect(shade(c, -0.25f), o, sz, r, alpha = alpha, style = Stroke(width = maxOf(1f, s * 0.05f)))
}

private fun DrawScope.drawPiece(piece: Piece, c: Color, x: Float, y: Float, s: Float, alpha: Float) {
    piece.cells.forEach { (r, col) -> drawBlock(x + col * s, y + r * s, s, c, alpha) }
}

private fun DrawScope.drawSea(x: Float, y: Float, s: Float, t: Long, i: Int, p: Palette) {
    val gap = s * 0.07f
    drawRoundRect(p.sea, Offset(x + gap, y + gap), Size(s - gap * 2, s - gap * 2), CornerRadius(s * 0.2f))
    val wave = sin(t / 1100f + (i % N) * 0.8f + (i / N) * 0.5f) * 0.5f + 0.5f
    val yy = y + s * (0.5f + (wave - 0.5f) * 0.12f)
    val a = s * 0.09f
    val path = Path().apply {
        moveTo(x + s * 0.24f, yy)
        cubicTo(x + s * 0.36f, yy - a, x + s * 0.44f, yy - a, x + s * 0.5f, yy)
        cubicTo(x + s * 0.56f, yy + a, x + s * 0.64f, yy + a, x + s * 0.76f, yy)
    }
    drawPath(path, p.sea2, alpha = 0.1f + wave * 0.2f, style = Stroke(width = maxOf(1f, s * 0.045f), cap = StrokeCap.Round))
}

private fun DrawScope.drawHole(x: Float, y: Float, s: Float, p: Palette) {
    val gap = s * 0.07f
    drawRoundRect(p.hole, Offset(x + gap, y + gap), Size(s - gap * 2, s - gap * 2), CornerRadius(s * 0.2f))
    val dot = shade(p.hole, -0.18f)
    listOf(0.32f to 0.36f, 0.62f to 0.3f, 0.48f to 0.64f, 0.72f to 0.68f).forEach { (a, b) ->
        drawCircle(dot, s * 0.035f, Offset(x + s * a, y + s * b))
    }
}

private fun DrawScope.drawRock(x: Float, y: Float, s: Float, p: Palette) {
    val gap = s * 0.1f
    drawRoundRect(p.rock, Offset(x + gap, y + gap), Size(s - gap * 2, s - gap * 2), CornerRadius(s * 0.35f))
    drawRoundRect(shade(p.rock, 0.25f), Offset(x + gap * 2, y + gap * 1.6f), Size(s * 0.35f, s * 0.18f), CornerRadius(s * 0.1f))
}

private fun DrawScope.drawPearl(x: Float, y: Float, s: Float, t: Long, scale: Float, p: Palette) {
    val gap = s * 0.07f
    drawRoundRect(shade(p.sea, -0.35f), Offset(x + gap, y + gap), Size(s - gap * 2, s - gap * 2), CornerRadius(s * 0.2f), alpha = 0.35f)
    val rad = s * 0.36f * scale
    if (rad < 0.5f) return
    val c = Offset(x + s / 2, y + s / 2)
    val wob = sin(t / 700f + x * 0.05f + y * 0.03f) * 0.5f + 0.5f
    drawCircle(
        Brush.radialGradient(
            0f to p.nacre1, (0.55f + wob * 0.15f) to p.nacre2, 1f to p.nacre3,
            center = Offset(c.x - rad * 0.35f, c.y - rad * 0.4f), radius = rad * 1.4f,
        ),
        rad, c,
    )
    drawCircle(Color.White.copy(alpha = 0.85f), rad * 0.22f, Offset(c.x - rad * 0.35f, c.y - rad * 0.38f))
}
