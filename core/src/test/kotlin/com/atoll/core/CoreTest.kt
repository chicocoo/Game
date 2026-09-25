package com.atoll.core

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val N = Game.N
private fun at(r: Int, c: Int) = r * N + c

/** Remplit la grille d'une partie à partir d'un dessin ('#' bloc, 'o' perle, '.' vide). */
private fun Game.draw(vararg rows: String) {
    grid.fill(Game.EMPTY)
    rows.forEachIndexed { r, line -> line.forEachIndexed { c, ch ->
        grid[at(r, c)] = when (ch) { '#' -> Game.BLOCK; 'o' -> Game.PEARL; 'x' -> Game.ROCK; else -> Game.EMPTY }
    } }
}

private fun Game.setHand(vararg family: String) {
    val f = Game::class.java.getDeclaredField("s").apply { isAccessible = true }.get(this)
    val hand = f.javaClass.getDeclaredField("hand").apply { isAccessible = true }.get(f) as Array<Slot?>
    family.forEachIndexed { i, fam -> hand[i] = Slot(Pieces.byFamily(fam), i) }
}

class CoreTest {
    @Test fun `hasard identique au prototype web`() {
        val r = Rng(12345)
        val expected = listOf(0.9797282677609473, 0.3067522644996643, 0.484205421525985, 0.817934412509203, 0.5094283693470061)
        expected.forEach { assertEquals(it, r.next(), 1e-15) }
        assertEquals(3089902447L, hashString("atoll-daily-2026-09-24").toUnsignedLong())
    }

    @Test fun `mêmes pièces que le prototype web`() {
        assertEquals(37, Pieces.ALL.size)
        assertEquals("dot_0,i2_0,i2_1,i3_0,i3_1,i4_0,i4_1,i5_0,i5_1,o2_0,o3_0,r23_0,r23_1,v3_0,v3_1,v3_2,v3_3,l4_0,l4_1,l4_2,l4_3,l4_4,l4_5,l4_6,l4_7,t4_0,t4_1,t4_2,t4_3,s4_0,s4_1,s4_2,s4_3,v5_0,v5_1,v5_2,v5_3",
            Pieces.ALL.joinToString(",") { it.id })
    }

    @Test fun `le défi du jour a la même suite que le prototype web`() {
        val g = Game.daily("2026-09-24")
        val first = g.hand.map { it!!.piece.id } + g.nextHand.map { it.id }
        assertEquals(listOf("i3_1", "i2_0", "o2_0", "v5_3", "t4_1", "v3_1"), first)
        assertEquals(45, g.piecesLeft)
    }

    @Test fun `jour du défi aligné sur le reset UTC-7`() {
        assertEquals("2026-09-24", Daily.key(Instant.parse("2026-09-25T06:59:00Z")))
        assertEquals("2026-09-25", Daily.key(Instant.parse("2026-09-25T07:00:00Z")))
        assertEquals(Instant.parse("2026-09-26T07:00:00Z"), Daily.nextReset(Instant.parse("2026-09-25T12:00:00Z")))
        assertEquals(1L, Daily.number("2026-01-01"))
        assertEquals("2026-09-21", Daily.weekKey(Instant.parse("2026-09-25T12:00:00Z")))
    }

    @Test fun `deux cases encerclées deviennent des perles, une seule reste un trou`() {
        val g = Game(Mode.CLASSIC, 1)
        g.draw(".........", "...####..", "...#..#..")
        g.setHand("i2", "dot", "dot")
        val ev = g.place(0, 3, 4)!!
        assertEquals(1, ev.lagoons.size)
        assertEquals(listOf(at(2, 4), at(2, 5)), ev.lagoons[0].cells)
        assertEquals(Game.lagoonBasePoints(2), ev.lagoonPoints)

        val h = Game(Mode.CLASSIC, 1)
        h.draw(".........", "...###...", "...#.#...")
        h.setHand("dot", "dot", "dot")
        assertEquals(0, h.place(0, 3, 4)!!.lagoons.size)
        assertEquals(Game.EMPTY, h.cell(2, 4))
    }

    @Test fun `tutoriel, lagon puis ligne de perles`() {
        val g = Game(Mode.CLASSIC, 3)
        g.applyTutorial()
        assertEquals(1, g.place(0, 5, 5)!!.lagoons.size)
        val ev = g.place(1, 4, 8)!!
        assertEquals(1, ev.lines.size)
        assertEquals(2, ev.lines[0].pearls)
        assertEquals(270, ev.linePoints)
    }

    @Test fun `la mer ne devient jamais lagon`() {
        val g = Game(Mode.CLASSIC, 1)
        g.draw("#.#......", "###......")
        assertTrue(g.enclosedZones().isEmpty())
    }

    @Test fun `les perles complètent et multiplient la ligne`() {
        val g = Game(Mode.CLASSIC, 1, Rules(minLagoon = 1))
        g.draw(".........", ".........", ".........", "...#.....", "###.####.", "...#.....")
        g.setHand("dot", "dot", "dot")
        val ev = g.place(0, 4, 8)!!
        assertEquals(1, ev.lagoons.size)
        assertEquals(1, ev.lines.size)
        assertEquals(1, ev.lines[0].pearls)
        assertEquals(10 * N * 2, ev.linePoints)
        assertEquals(1, g.pearlsCashed)
        assertEquals(Game.EMPTY, g.cell(4, 3))
    }

    @Test fun `multi-lignes et combo`() {
        val g = Game(Mode.CLASSIC, 1)
        g.draw("########.", "########.")
        g.setHand("i2", "dot", "dot")
        val vertical = Pieces.ALL.first { it.family == "i2" && it.h == 2 }
        val f = Game::class.java.getDeclaredField("s").apply { isAccessible = true }.get(g)
        (f.javaClass.getDeclaredField("hand").apply { isAccessible = true }.get(f) as Array<Slot?>)[0] = Slot(vertical, 0)
        assertEquals((90 + 90) * 2, g.place(0, 0, 8)!!.linePoints)

        val c = Game(Mode.CLASSIC, 1)
        c.draw("########.", ".........", "########.")
        c.setHand("dot", "dot", "dot")
        c.place(0, 0, 8)
        c.place(1, 5, 5)
        val ev = c.place(2, 2, 8)!!
        assertEquals(2, ev.combo)
        assertEquals(135, ev.linePoints)
    }

    @Test fun `fin de partie, continuer une seule fois`() {
        val g = Game(Mode.CLASSIC, 1)
        g.draw(*Array(N) { r -> if (r % 2 == 0) ".#.#.#.#." else "#.#.#.#.#" })
        g.setHand("o3", "o3", "o3")
        assertFalse(g.hasMove())
        val f = Game::class.java.getDeclaredField("s").apply { isAccessible = true }.get(g)
        f.javaClass.getDeclaredField("over").apply { isAccessible = true }.setBoolean(f, true)
        assertTrue(g.revive())
        assertTrue(g.hasMove())
        f.javaClass.getDeclaredField("over").apply { isAccessible = true }.setBoolean(f, true)
        assertFalse(g.revive())
    }

    @Test fun `une partie se reprend en rejouant ses coups`() {
        val a = Game(Mode.CLASSIC, 777)
        var guard = 0
        while (!a.over && guard++ < 40) {
            val (k, r, c) = firstMove(a) ?: break
            a.place(k, r, c)
        }
        val b = Game(Mode.CLASSIC, 777)
        b.replay(a.moves)
        assertEquals(a.score, b.score)
        assertTrue(a.grid.contentEquals(b.grid))
        assertEquals(a.hand, b.hand)
    }

    @Test fun `filet, annuler la dernière pose`() {
        val g = Game(Mode.TOUR, 5, Rules(undos = 1), pieceCount = 20, target = 1_000_000)
        val before = g.grid.copyOf()
        val (k, r, c) = firstMove(g)!!
        g.place(k, r, c)
        assertTrue(g.canUndo)
        assertTrue(g.undo())
        assertTrue(before.contentEquals(g.grid))
        assertEquals(0, g.undosLeft)
        val replayed = Game(Mode.TOUR, 5, Rules(undos = 1), pieceCount = 20, target = 1_000_000)
        replayed.replay(g.moves)
        assertTrue(replayed.grid.contentEquals(g.grid))
    }

    @Test fun `étape du Tour gagnée dès que la cible est atteinte`() {
        val g = Game(Mode.TOUR, 9, Rules(), pieceCount = 30, target = 5)
        val (k, r, c) = firstMove(g)!!
        g.place(k, r, c)
        g.place(firstMove(g)!!.first, firstMove(g)!!.second, firstMove(g)!!.third)
        assertTrue(g.over && g.won)
    }

    @Test fun `run du Tour, souvenirs proposés et miles`() {
        val run = TourRun(42, Tour.unlocked(0))
        assertEquals(6, Tour.unlocked(0).size)
        assertEquals(Souvenir.entries.size, Tour.unlocked(10_000).size)
        val game = run.newStageGame()
        assertEquals(18, game.piecesLeft)
        val forcedWin = Game(Mode.TOUR, 1, pieceCount = 18, target = 1)
        forcedWin.place(firstMove(forcedWin)!!.first, firstMove(forcedWin)!!.second, firstMove(forcedWin)!!.third)
        assertTrue(run.completeStage(forcedWin))
        assertEquals(3, run.offer.size)
        run.choose(run.offer[0])
        assertEquals(1, run.souvenirs.size)
        assertTrue(Tour.stage(17).isStorm)
        assertEquals(Storm.FLAT_CALM, Tour.stage(17).storm)
        assertEquals(0, Tour.rulesFor(Tour.stage(17), emptyList()).comboWindow)
    }

    @Test fun `score tag et tableau des médailles`() {
        val day = "2026-09-24"
        val fr = Nations.tag("FR", 18450, day)
        assertEquals("FR", Nations.countryOf(fr, 18450, day))
        assertNull(Nations.countryOf(fr, 99999, day), "tag recopié sur un autre score")
        assertNull(Nations.countryOf("v1-FR-zzz", 18450, day))
        val entries = listOf(
            Nations.Entry(1, 30000, Nations.tag("BR", 30000, day)),
            Nations.Entry(2, 25000, Nations.tag("FR", 25000, day)),
            Nations.Entry(3, 24000, Nations.tag("BR", 24000, day)),
            Nations.Entry(4, 999_999_999, Nations.tag("US", 999_999_999, day)),
            Nations.Entry(5, 20000, "bidon"),
        )
        val st = Nations.standings(entries, day)
        assertEquals(listOf("BR", "FR"), st.map { it.country })
        assertEquals(100 + 98L, st[0].points)
        assertEquals(1, st[0].gold)
        assertEquals(1, st[0].bronze)
        assertEquals("AF", Nations.continent("ng"))
        assertEquals("🇫🇷", Nations.flag("FR"))
    }

    @Test fun `lien de défi aller-retour`() {
        val ch = Challenge(-123456789, 18450, "Raph & co", "FR")
        val back = Challenge.fromQuery(ch.toQuery())
        assertEquals(ch, back)
        assertNull(Challenge.fromQuery("s=-!&p=1"))
        assertNotNull(Challenge.fromQuery("s=1&p=1&c=??"))
        assertEquals("18 450", ShareText.fmt(18450))
    }

    @Test fun `simulation, la règle du lagon allonge les parties`() {
        val with = (0 until 20).map { botGame(Game(Mode.CLASSIC, 1000 + it)) }.average()
        val without = (0 until 20).map { botGame(Game(Mode.CLASSIC, 1000 + it, Rules(minLagoon = 99))) }.average()
        println("Poses moyennes : avec lagons ${"%.0f".format(with)}, sans ${"%.0f".format(without)}")
        assertTrue(with > without * 1.3)
    }

    @Test fun `simulation, difficulté du Tour du monde`() {
        val reached = (0 until 12).map { seed ->
            val run = TourRun(seed, Tour.unlocked(0))
            while (!run.finished) {
                val g = run.newStageGame()
                botGame(g)
                if (run.completeStage(g)) run.choose(run.offer.firstOrNull())
            }
            run.stagesCleared
        }
        println("Étapes réussies par le bot : $reached")
        assertTrue(reached.average() in 2.0..16.0, "Tour trop facile ou trop dur : $reached")
    }
}

fun firstMove(g: Game): Triple<Int, Int, Int>? {
    g.hand.forEachIndexed { k, slot ->
        if (slot == null) return@forEachIndexed
        for (r in 0..Game.N - slot.piece.h) for (c in 0..Game.N - slot.piece.w) if (g.canPlace(slot.piece, r, c)) return Triple(k, r, c)
    }
    return null
}

/** Bot glouton (même heuristique que prototype/tools/simulate.js). Renvoie le nombre de poses. */
fun botGame(g: Game): Int {
    var moves = 0
    while (!g.over && moves < 3000) {
        var best: Triple<Int, Int, Int>? = null
        var bestV = Double.NEGATIVE_INFINITY
        g.hand.forEachIndexed { k, slot ->
            if (slot == null) return@forEachIndexed
            for (r in 0..Game.N - slot.piece.h) for (c in 0..Game.N - slot.piece.w) {
                if (!g.canPlace(slot.piece, r, c)) continue
                val sim = g.fork()
                val ev = sim.place(k, r, c) ?: continue
                val v = ev.total + boardValue(sim)
                if (v > bestV) { bestV = v; best = Triple(k, r, c) }
            }
        }
        val m = best ?: break
        g.place(m.first, m.second, m.third)
        moves++
    }
    return moves
}

private fun boardValue(g: Game): Double {
    var empties = 0
    var isolated = 0
    for (i in 0 until N * N) {
        if (g.grid[i] != Game.EMPTY) continue
        empties++
        val r = i / N
        val c = i % N
        val free = listOf(r - 1 to c, r + 1 to c, r to c - 1, r to c + 1)
            .count { (y, x) -> y in 0 until N && x in 0 until N && g.grid[y * N + x] == Game.EMPTY }
        if (free == 0) isolated++
    }
    return 4.0 * empties - 8 * isolated + if (g.pieceFits(Pieces.byFamily("o3"))) 30 else 0
}
