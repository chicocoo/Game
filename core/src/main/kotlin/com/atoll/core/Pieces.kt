package com.atoll.core

class Piece(
    val id: String,
    val family: String,
    val weight: Double,
    /** Cases occupées, en (ligne, colonne), normalisées et triées. */
    val cells: List<Pair<Int, Int>>,
) {
    val h: Int = cells.maxOf { it.first } + 1
    val w: Int = cells.maxOf { it.second } + 1
    val size: Int get() = cells.size
    override fun toString() = id
}

object Pieces {
    private class Base(val name: String, val weight: Double, val cells: List<Pair<Int, Int>>, val rotate: Boolean, val mirror: Boolean)

    private fun sq(n: Int) = (0 until n).flatMap { r -> (0 until n).map { c -> r to c } }

    private val BASE = listOf(
        Base("dot", 2.0, listOf(0 to 0), false, false),
        Base("i2", 4.0, listOf(0 to 0, 0 to 1), true, false),
        Base("i3", 6.0, listOf(0 to 0, 0 to 1, 0 to 2), true, false),
        Base("i4", 4.0, listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3), true, false),
        Base("i5", 2.0, listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3, 0 to 4), true, false),
        Base("o2", 3.0, sq(2), false, false),
        Base("o3", 1.0, sq(3), false, false),
        Base("r23", 2.0, listOf(0 to 0, 0 to 1, 0 to 2, 1 to 0, 1 to 1, 1 to 2), true, false),
        Base("v3", 6.0, listOf(0 to 0, 1 to 0, 1 to 1), true, false),
        Base("l4", 6.0, listOf(0 to 0, 1 to 0, 2 to 0, 2 to 1), true, true),
        Base("t4", 3.0, listOf(0 to 0, 0 to 1, 0 to 2, 1 to 1), true, false),
        Base("s4", 3.0, listOf(0 to 1, 0 to 2, 1 to 0, 1 to 1), true, true),
        Base("v5", 3.0, listOf(0 to 0, 1 to 0, 2 to 0, 2 to 1, 2 to 2), true, false),
    )

    private fun normalize(cells: List<Pair<Int, Int>>): List<Pair<Int, Int>> {
        val minR = cells.minOf { it.first }
        val minC = cells.minOf { it.second }
        return cells.map { (it.first - minR) to (it.second - minC) }
            .sortedWith(compareBy({ it.first }, { it.second }))
    }

    private fun rotate(cells: List<Pair<Int, Int>>) = normalize(cells.map { it.second to -it.first })
    private fun mirror(cells: List<Pair<Int, Int>>) = normalize(cells.map { it.first to -it.second })
    private fun key(cells: List<Pair<Int, Int>>) = cells.joinToString(";") { "${it.first},${it.second}" }

    /** Même ordre et mêmes poids que le prototype web (parité du Défi du jour). */
    val ALL: List<Piece> = buildList {
        for (b in BASE) {
            var cur = normalize(b.cells)
            var forms = mutableListOf(cur)
            if (b.rotate) repeat(3) { cur = rotate(cur); forms.add(cur) }
            if (b.mirror) forms = (forms + forms.map { mirror(it) }).toMutableList()
            val variants = LinkedHashMap<String, List<Pair<Int, Int>>>()
            for (f in forms) variants[key(f)] = f
            variants.values.forEachIndexed { i, cells ->
                add(Piece("${b.name}_$i", b.name, b.weight / variants.size, cells))
            }
        }
    }

    val BY_ID: Map<String, Piece> = ALL.associateBy { it.id }

    fun byFamily(family: String): Piece = ALL.first { it.family == family }

    private val TOTAL = ALL.fold(0.0) { s, p -> s + p.weight }

    /** Tirage pondéré ; `heavy` double le poids des pièces de 5 cases et plus (tempête « Grosse mer »). */
    fun draw(rng: Rng, heavy: Boolean = false): Piece {
        if (!heavy) {
            var x = rng.next() * TOTAL
            for (p in ALL) { x -= p.weight; if (x < 0) return p }
            return ALL.last()
        }
        val weights = ALL.map { if (it.size >= 5) it.weight * 2 else it.weight }
        var x = rng.next() * weights.sum()
        ALL.forEachIndexed { i, p -> x -= weights[i]; if (x < 0) return p }
        return ALL.last()
    }
}
