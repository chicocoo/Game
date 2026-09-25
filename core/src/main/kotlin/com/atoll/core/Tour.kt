package com.atoll.core

/** Souvenirs : reliques du Tour du monde qui modifient les règles. */
enum class Souvenir(val unlockMiles: Int, val apply: (Rules) -> Rules) {
    DIVER(0, { it.copy(minLagoon = 1) }),                         // Plongeur
    NACRE(0, { it.copy(pearlBonus = it.pearlBonus + 1) }),        // Nacre
    LOW_TIDE(0, { it.copy(dotInEveryHand = true) }),              // Marée basse
    TAILWIND(0, { it.copy(extraPieces = it.extraPieces + 3) }),   // Vent arrière
    SHELL(0, { it.copy(lagoonPointsFactor = it.lagoonPointsFactor * 1.5) }), // Coquillage
    LIGHTHOUSE(0, { it.copy(comboWindow = maxOf(it.comboWindow, 5)) }),     // Phare
    ANCHOR(30, { it.copy(fullPearlLineTriple = true) }),          // Ancre
    NET(80, { it.copy(undos = it.undos + 1) }),                   // Filet
    COMPASS(150, { it.copy(showNextHand = true) }),               // Boussole
    TREASURE_MAP(250, { it.copy(firstLagoonTimesFive = true) }),  // Carte au trésor
    CORAL(400, { it.copy(bigLagoonClearsRing = true) }),          // Corail
    PALM(600, { it.copy(bigLagoonAddsMultiplier = true) });       // Palmier
}

/** Tempêtes : contraintes des étapes finales de chaque mer. */
enum class Storm(val apply: (Rules) -> Rules) {
    FOG({ it.copy(fog = true) }),
    SWELL({ it.copy(swellEvery = 5) }),
    REEF({ it.copy(startingRocks = 6) }),
    HEAVY_SEA({ it.copy(heavySea = true) }),
    COUNTERCURRENT({ it.copy(minLagoon = maxOf(it.minLagoon, 4)) }),
    FLAT_CALM({ it.copy(comboWindow = 0) }),
}

data class Stage(val index: Int, val sea: Int, val step: Int, val target: Int, val pieces: Int, val storm: Storm?) {
    val isStorm get() = storm != null
}

object Tour {
    const val SEAS = 6
    const val STEPS = 3
    const val STAGES = SEAS * STEPS

    private val STORM_ORDER = listOf(Storm.FOG, Storm.SWELL, Storm.REEF, Storm.HEAVY_SEA, Storm.COUNTERCURRENT, Storm.FLAT_CALM)

    fun stage(index: Int): Stage {
        val sea = index / STEPS
        val step = index % STEPS
        val target = (600 * Math.pow(1.27, index.toDouble()) / 50).toInt() * 50
        val pieces = 18 + index
        return Stage(index, sea, step, target, pieces, if (step == STEPS - 1) STORM_ORDER[sea] else null)
    }

    fun rulesFor(stage: Stage, souvenirs: List<Souvenir>): Rules {
        var r = souvenirs.fold(Rules()) { acc, s -> s.apply(acc) }
        stage.storm?.let { r = it.apply(r) }
        return r
    }

    fun unlocked(totalMiles: Int): List<Souvenir> = Souvenir.entries.filter { it.unlockMiles <= totalMiles }

    /** Miles gagnés en fin de run : 10 par étape réussie + bonus de pièces restantes. */
    fun miles(stagesCleared: Int, spareTotal: Int) = stagesCleared * 10 + spareTotal
}

/**
 * Une run du Tour du monde : enchaîne les étapes, propose des souvenirs, compte les Miles.
 * Déterministe à partir de la graine (le Grand Tour hebdo utilise la graine de la semaine).
 */
class TourRun(val seed: Int, private val unlocked: List<Souvenir>) {
    private val rng = Rng(seed)
    var stageIndex = 0
        private set
    val souvenirs = mutableListOf<Souvenir>()
    var totalScore = 0L
        private set
    var spare = 0
        private set
    var finished = false
        private set
    var offer: List<Souvenir> = emptyList()
        private set

    val stage: Stage get() = Tour.stage(stageIndex)
    val stagesCleared get() = if (finished && stageIndex >= Tour.STAGES) Tour.STAGES else stageIndex

    fun newStageGame(): Game {
        val st = stage
        return Game(Mode.TOUR, seed xor ((st.index + 1) * 0x9E3779B1.toInt()), Tour.rulesFor(st, souvenirs), st.pieces, st.target)
    }

    /** À appeler quand l'étape est finie. Renvoie true si la run continue. */
    fun completeStage(game: Game): Boolean {
        totalScore += game.score
        if (!game.won) { finished = true; offer = emptyList(); return false }
        spare += game.piecesLeft ?: 0
        stageIndex++
        if (stageIndex >= Tour.STAGES) { finished = true; offer = emptyList(); return false }
        val pool = unlocked.filter { it !in souvenirs }.toMutableList()
        val picks = mutableListOf<Souvenir>()
        repeat(minOf(3, pool.size)) { picks.add(pool.removeAt(rng.nextInt(pool.size))) }
        offer = picks
        return true
    }

    fun choose(s: Souvenir?) {
        if (s != null && s in offer) souvenirs.add(s)
        offer = emptyList()
    }

    val miles get() = Tour.miles(stagesCleared, spare)
}
