package com.atoll.core

enum class Mode { CLASSIC, DAILY, CHALLENGE, TOUR }

/** Une pièce de la main, avec sa couleur d'affichage (0..4). */
data class Slot(val piece: Piece, val color: Int)

data class Lagoon(val cells: List<Int>, val points: Int, val clearedRing: List<Int> = emptyList())

data class Line(val horizontal: Boolean, val index: Int, val cells: List<Int>, val pearls: Int, val points: Int)

data class Cleared(val index: Int, val kind: Int, val color: Int)

data class Preview(val pearls: List<Int>, val lines: List<Line>)

data class PlaceResult(
    val placed: List<Int>,
    val color: Int,
    val lagoons: List<Lagoon>,
    val lines: List<Line>,
    val cleared: List<Cleared>,
    val placePoints: Int,
    val lagoonPoints: Int,
    val linePoints: Int,
    val multi: Int,
    val combo: Int,
    val total: Int,
    val rock: Int?,
    val newHand: Boolean,
    val over: Boolean,
)

/**
 * Une partie d'ATOLL. Toute la logique est déterministe à partir de la graine et des coups joués,
 * ce qui permet de reprendre une partie en rejouant simplement ses coups.
 */
class Game(
    val mode: Mode,
    val seed: Int,
    val rules: Rules = Rules(),
    /** Nombre de pièces de la partie (Défi, défi entre amis, étape du Tour). null = infini. */
    val pieceCount: Int? = null,
    /** Score à atteindre pour gagner (étape du Tour). */
    val target: Int? = null,
) {
    companion object {
        const val N = 9
        const val EMPTY = 0
        const val BLOCK = 1
        const val PEARL = 2
        const val ROCK = 3

        fun lagoonBasePoints(area: Int) = 20 * area + if (area >= 6) 100 else 0

        fun daily(key: String) = Game(Mode.DAILY, Daily.seed(key), pieceCount = Daily.PIECES)
        fun challenge(seed: Int) = Game(Mode.CHALLENGE, seed, pieceCount = Daily.PIECES)
    }

    private class State(
        val grid: IntArray,
        val colors: IntArray,
        val hand: Array<Slot?>,
        var rngState: Int,
        var cursor: Int,
        var colorCursor: Int,
        var score: Int = 0,
        var combo: Int = 0,
        var sinceClear: Int = 99,
        var bestCombo: Int = 0,
        var piecesPlaced: Int = 0,
        var linesCleared: Int = 0,
        var lagoons: Int = 0,
        var pearlsCreated: Int = 0,
        var pearlsCashed: Int = 0,
        var biggestLagoon: Int = 0,
        var bigLagoons: Int = 0,
        var revivesLeft: Int = 0,
        var undosLeft: Int = 0,
        var over: Boolean = false,
        var won: Boolean = false,
    ) {
        fun copy() = State(
            grid.copyOf(), colors.copyOf(), hand.copyOf(), rngState, cursor, colorCursor, score, combo, sinceClear,
            bestCombo, piecesPlaced, linesCleared, lagoons, pearlsCreated, pearlsCashed, biggestLagoon, bigLagoons,
            revivesLeft, undosLeft, over, won,
        )
    }

    private val rng = Rng(seed)
    /** Suite de pièces fixée à l'avance (modes à nombre de pièces limité) : identique pour tout le monde. */
    private val sequence: List<Piece>? = pieceCount?.let { n ->
        List(n + if (mode == Mode.TOUR) rules.extraPieces else 0) { Pieces.draw(rng, rules.heavySea) }
    }
    private var s = State(IntArray(N * N), IntArray(N * N) { -1 }, arrayOfNulls(3), 0, 0, 0)
    private var undoState: State? = null

    /** Journal des actions, pour sauvegarder la partie et la reprendre. */
    val moves = mutableListOf<String>()

    init {
        s.revivesLeft = if (mode == Mode.CLASSIC) 1 else 0
        s.undosLeft = rules.undos
        placeStartingRocks()
        s.rngState = rng.state
        deal()
    }

    // ---------- Lecture de l'état ----------
    val grid: IntArray get() = s.grid
    val colors: IntArray get() = s.colors
    val hand: List<Slot?> get() = s.hand.toList()
    val score get() = s.score
    val combo get() = s.combo
    val bestCombo get() = s.bestCombo
    val piecesPlaced get() = s.piecesPlaced
    val linesCleared get() = s.linesCleared
    val lagoons get() = s.lagoons
    val pearlsCreated get() = s.pearlsCreated
    val pearlsCashed get() = s.pearlsCashed
    val biggestLagoon get() = s.biggestLagoon
    val revivesLeft get() = s.revivesLeft
    val undosLeft get() = s.undosLeft
    val canUndo get() = s.undosLeft > 0 && undoState != null && !s.over
    val over get() = s.over
    val won get() = s.won
    val limited get() = sequence != null

    /** Pièces restantes à jouer (main comprise), ou null si infini. */
    val piecesLeft: Int?
        get() = sequence?.let { it.size - s.cursor + s.hand.count { h -> h != null } }

    /** Main suivante (souvenir Boussole), si la partie a une suite fixée. */
    val nextHand: List<Piece>
        get() = sequence?.drop(s.cursor)?.take(3) ?: emptyList()

    fun cell(r: Int, c: Int) = s.grid[r * N + c]

    // ---------- Distribution ----------
    private fun nextColor(): Int { s.colorCursor = (s.colorCursor + 1) % 5; return s.colorCursor }

    private fun deal() {
        rng.state = s.rngState
        val seq = sequence
        if (seq != null) {
            for (k in 0 until 3) {
                s.hand[k] = if (s.cursor < seq.size) Slot(seq[s.cursor++], nextColor()) else null
            }
        } else {
            var hand: List<Piece> = emptyList()
            for (attempt in 0 until 8) {
                hand = List(3) { Pieces.draw(rng, rules.heavySea) }
                if (hand.any { pieceFits(it) }) break
            }
            if (rules.dotInEveryHand && hand.none { it.size == 1 }) hand = hand.take(2) + Pieces.byFamily("dot")
            for (k in 0 until 3) s.hand[k] = Slot(hand[k], nextColor())
        }
        if (rules.dotInEveryHand && seq != null && s.hand.filterNotNull().none { it.piece.size == 1 } && s.hand[2] != null) {
            s.hand[2] = Slot(Pieces.byFamily("dot"), s.hand[2]!!.color)
        }
        s.rngState = rng.state
    }

    private fun placeStartingRocks() {
        var placed = 0
        var guard = 0
        while (placed < rules.startingRocks && guard++ < 500) {
            val r = 1 + rng.nextInt(N - 2)
            val c = 1 + rng.nextInt(N - 2)
            val i = r * N + c
            if (s.grid[i] == EMPTY) { s.grid[i] = ROCK; placed++ }
        }
    }

    // ---------- Géométrie ----------
    fun canPlace(piece: Piece, r0: Int, c0: Int, grid: IntArray = s.grid): Boolean {
        for ((dr, dc) in piece.cells) {
            val r = r0 + dr
            val c = c0 + dc
            if (r < 0 || c < 0 || r >= N || c >= N) return false
            if (grid[r * N + c] != EMPTY) return false
        }
        return true
    }

    fun pieceFits(piece: Piece, grid: IntArray = s.grid): Boolean {
        for (r in 0..N - piece.h) for (c in 0..N - piece.w) if (canPlace(piece, r, c, grid)) return true
        return false
    }

    fun hasMove(): Boolean = s.hand.any { it != null && pieceFits(it.piece) }

    /** Cases vides reliées au bord (la mer). */
    fun seaMask(grid: IntArray = s.grid): BooleanArray {
        val sea = BooleanArray(N * N)
        val stack = ArrayDeque<Int>()
        fun seed(r: Int, c: Int) {
            val i = r * N + c
            if (grid[i] == EMPTY && !sea[i]) { sea[i] = true; stack.addLast(i) }
        }
        for (k in 0 until N) { seed(0, k); seed(N - 1, k); seed(k, 0); seed(k, N - 1) }
        while (stack.isNotEmpty()) {
            val i = stack.removeLast()
            val r = i / N
            val c = i % N
            if (r > 0) seed(r - 1, c)
            if (r < N - 1) seed(r + 1, c)
            if (c > 0) seed(r, c - 1)
            if (c < N - 1) seed(r, c + 1)
        }
        return sea
    }

    /** Zones vides encerclées (toutes tailles), triées par index. */
    fun enclosedZones(grid: IntArray = s.grid): List<List<Int>> {
        val sea = seaMask(grid)
        val seen = BooleanArray(N * N)
        val zones = mutableListOf<List<Int>>()
        for (j in 0 until N * N) {
            if (grid[j] != EMPTY || sea[j] || seen[j]) continue
            val comp = mutableListOf<Int>()
            val q = ArrayDeque<Int>().apply { addLast(j) }
            seen[j] = true
            while (q.isNotEmpty()) {
                val x = q.removeLast()
                comp.add(x)
                val r = x / N
                val c = x % N
                for (nb in neighbours(r, c)) if (grid[nb] == EMPTY && !seen[nb]) { seen[nb] = true; q.addLast(nb) }
            }
            zones.add(comp.sorted())
        }
        return zones
    }

    private fun neighbours(r: Int, c: Int): List<Int> = buildList(4) {
        if (r > 0) add((r - 1) * N + c)
        if (r < N - 1) add((r + 1) * N + c)
        if (c > 0) add(r * N + c - 1)
        if (c < N - 1) add(r * N + c + 1)
    }

    private fun fullLines(grid: IntArray): List<Pair<Boolean, Int>> = buildList {
        for (r in 0 until N) if ((0 until N).all { grid[r * N + it] != EMPTY }) add(true to r)
        for (c in 0 until N) if ((0 until N).all { grid[it * N + c] != EMPTY }) add(false to c)
    }

    private fun lineCells(horizontal: Boolean, index: Int) =
        List(N) { if (horizontal) index * N + it else it * N + index }

    fun preview(slotIndex: Int, r0: Int, c0: Int): Preview? {
        val slot = s.hand.getOrNull(slotIndex) ?: return null
        if (!canPlace(slot.piece, r0, c0)) return null
        if (rules.fog) return Preview(emptyList(), emptyList())
        val g = s.grid.copyOf()
        for ((dr, dc) in slot.piece.cells) g[(r0 + dr) * N + c0 + dc] = BLOCK
        val pearls = mutableListOf<Int>()
        for (zone in enclosedZones(g)) if (zone.size >= rules.minLagoon) zone.forEach { g[it] = PEARL; pearls.add(it) }
        val lines = fullLines(g).map { (h, i) ->
            val cells = lineCells(h, i)
            Line(h, i, cells, cells.count { g[it] == PEARL }, 0)
        }
        return Preview(pearls, lines)
    }

    // ---------- Jouer ----------
    fun place(slotIndex: Int, r0: Int, c0: Int): PlaceResult? {
        val slot = s.hand.getOrNull(slotIndex) ?: return null
        if (s.over || !canPlace(slot.piece, r0, c0)) return null
        undoState = if (s.undosLeft > 0) s.copy() else null
        moves.add("p$slotIndex.$r0.$c0")

        val placed = slot.piece.cells.map { (dr, dc) -> (r0 + dr) * N + c0 + dc }
        placed.forEach { s.grid[it] = BLOCK; s.colors[it] = slot.color }
        val placePoints = placed.size

        // Lagons
        val lagoons = mutableListOf<Lagoon>()
        var lagoonPoints = 0
        for (zone in enclosedZones(s.grid)) {
            if (zone.size < rules.minLagoon) continue
            zone.forEach { s.grid[it] = PEARL; s.colors[it] = -1 }
            var pts = lagoonBasePoints(zone.size) * rules.lagoonPointsFactor
            if (rules.firstLagoonTimesFive && s.lagoons == 0) pts *= 5
            val ring = mutableListOf<Int>()
            if (zone.size >= 6) {
                s.bigLagoons++
                if (rules.bigLagoonClearsRing) {
                    val inZone = zone.toHashSet()
                    for (i in zone) for (nb in neighbours(i / N, i % N)) {
                        if (nb !in inZone && (s.grid[nb] == BLOCK || s.grid[nb] == ROCK) && nb !in ring) ring.add(nb)
                    }
                    ring.forEach { s.grid[it] = EMPTY; s.colors[it] = -1 }
                    pts += ring.size * 10
                }
            }
            lagoons.add(Lagoon(zone, pts.toInt(), ring))
            lagoonPoints += pts.toInt()
            s.lagoons++
            s.pearlsCreated += zone.size
            s.biggestLagoon = maxOf(s.biggestLagoon, zone.size)
        }

        // Lignes
        val full = fullLines(s.grid)
        val lines = mutableListOf<Line>()
        val cleared = mutableListOf<Cleared>()
        var linePoints = 0
        if (full.isNotEmpty()) {
            s.combo = if (rules.comboWindow > 0 && s.combo > 0 && s.sinceClear < rules.comboWindow) s.combo + 1 else 1
            s.sinceClear = 0
            s.bestCombo = maxOf(s.bestCombo, s.combo)
            val toClear = LinkedHashSet<Int>()
            var base = 0
            for ((h, idx) in full) {
                val cells = lineCells(h, idx)
                val pearls = cells.count { s.grid[it] == PEARL }
                var pts = 10 * N * (1 + pearls * rules.pearlBonus)
                if (rules.fullPearlLineTriple && pearls == N) pts *= 3
                base += pts
                lines.add(Line(h, idx, cells, pearls, pts))
                toClear.addAll(cells)
            }
            val comboMult = if (rules.comboWindow == 0) 1.0 else 1 + 0.5 * (s.combo - 1)
            val stageMult = if (rules.bigLagoonAddsMultiplier) 1 + s.bigLagoons else 1
            linePoints = Math.round(base.toDouble() * full.size * comboMult * stageMult).toInt()
            for (i in toClear) {
                if (s.grid[i] == PEARL) s.pearlsCashed++
                cleared.add(Cleared(i, s.grid[i], s.colors[i]))
                s.grid[i] = EMPTY
                s.colors[i] = -1
            }
            s.linesCleared += full.size
        } else {
            s.sinceClear++
            if (s.sinceClear >= rules.comboWindow) s.combo = 0
        }

        val total = placePoints + lagoonPoints + linePoints
        s.score += total
        s.hand[slotIndex] = null
        s.piecesPlaced++

        // Houle : un rocher apparaît sur le bord
        var rock: Int? = null
        if (rules.swellEvery > 0 && s.piecesPlaced % rules.swellEvery == 0) {
            rng.state = s.rngState
            val sea = seaMask()
            val edge = (0 until N * N).filter { sea[it] && (it / N == 0 || it / N == N - 1 || it % N == 0 || it % N == N - 1) }
            if (edge.isNotEmpty()) {
                rock = edge[rng.nextInt(edge.size)]
                s.grid[rock] = ROCK
                s.colors[rock] = -1
            }
            s.rngState = rng.state
        }

        var newHand = false
        if (s.hand.all { it == null }) { deal(); newHand = s.hand.any { it != null } }

        if (target != null && s.score >= target) {
            s.over = true
            s.won = true
        } else {
            s.over = s.hand.all { it == null } || !hasMove()
        }

        return PlaceResult(placed, slot.color, lagoons, lines, cleared, placePoints, lagoonPoints, linePoints,
            full.size, if (full.isEmpty()) 0 else s.combo, total, rock, newHand, s.over)
    }

    /** « Continuer » après une pub récompensée (mode Classique) : nouvelle main garantie jouable. */
    fun revive(): Boolean {
        if (!s.over || s.revivesLeft <= 0 || mode != Mode.CLASSIC) return false
        val fitting = Pieces.ALL.filter { pieceFits(it) }.sortedBy { it.size }
        if (fitting.isEmpty()) return false
        rng.state = s.rngState
        val pool = fitting.take(maxOf(3, (fitting.size + 1) / 2))
        for (k in 0 until 3) s.hand[k] = Slot(pool[rng.nextInt(pool.size)], nextColor())
        s.rngState = rng.state
        s.revivesLeft--
        s.over = false
        moves.add("r")
        return true
    }

    /** Annule la dernière pose (souvenir Filet). */
    fun undo(): Boolean {
        val u = undoState ?: return false
        if (s.undosLeft <= 0 || s.over) return false
        val left = s.undosLeft - 1
        s = u
        s.undosLeft = left
        undoState = null
        moves.add("u")
        return true
    }

    /**
     * Première partie : un anneau presque fermé autour de deux cases et une ligne à compléter.
     * À appeler juste après la création (aussi avant un rejeu de coups).
     */
    fun applyTutorial() {
        val blocks = listOf(3 to 3, 3 to 4, 3 to 5, 3 to 6, 4 to 3, 4 to 6, 5 to 3, 5 to 4, 5 to 6, 4 to 0, 4 to 1, 4 to 2, 4 to 7)
        s.grid.fill(EMPTY); s.colors.fill(-1)
        blocks.forEach { (r, c) -> s.grid[r * N + c] = BLOCK; s.colors[r * N + c] = (r + c) % 5 }
        val dot = Pieces.byFamily("dot")
        s.hand[0] = Slot(dot, 1); s.hand[1] = Slot(dot, 3); s.hand[2] = Slot(Pieces.ALL.first { it.family == "i3" && it.w == 3 }, 2)
    }

    /** Copie indépendante de la partie (simulations, bots). */
    fun fork(): Game {
        val g = Game(mode, seed, rules, pieceCount, target)
        g.s = s.copy()
        g.undoState = undoState?.copy()
        g.moves.addAll(moves)
        return g
    }

    /** Rejoue un journal d'actions (reprise de partie). */
    fun replay(log: List<String>) {
        for (m in log) {
            when {
                m == "r" -> revive()
                m == "u" -> undo()
                m.startsWith("p") -> {
                    val parts = m.substring(1).split('.').map { it.toInt() }
                    place(parts[0], parts[1], parts[2])
                }
            }
        }
    }
}
