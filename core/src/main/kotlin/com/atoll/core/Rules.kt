package com.atoll.core

/**
 * Paramètres d'une partie. Le mode Classique et le Défi utilisent les valeurs par défaut ;
 * les souvenirs et tempêtes du Tour du monde les modifient.
 */
data class Rules(
    /** Surface minimale d'une zone encerclée pour devenir un lagon. */
    val minLagoon: Int = 2,
    /** Multiplicateur ajouté par perle dans une ligne (1 perle = ×2 par défaut). */
    val pearlBonus: Int = 1,
    /** Multiplicateur des points de lagon. */
    val lagoonPointsFactor: Double = 1.0,
    /** Nombre de poses sans effacement avant que le combo retombe (0 = pas de combo). */
    val comboWindow: Int = 3,
    /** Une ligne composée uniquement de perles vaut ×3 de plus (souvenir Ancre). */
    val fullPearlLineTriple: Boolean = false,
    /** Un lagon de 6 cases et plus efface aussi les blocs qui l'entourent (souvenir Corail). */
    val bigLagoonClearsRing: Boolean = false,
    /** Le premier lagon de l'étape vaut ×5 (souvenir Carte au trésor). */
    val firstLagoonTimesFive: Boolean = false,
    /** Chaque grand lagon ajoute +1 multiplicateur de lignes pour l'étape (souvenir Palmier). */
    val bigLagoonAddsMultiplier: Boolean = false,
    /** Annulations disponibles par étape (souvenir Filet). */
    val undos: Int = 0,
    /** Afficher la main suivante (souvenir Boussole). */
    val showNextHand: Boolean = false,
    /** Chaque main contient au moins une pièce d'une case (souvenir Marée basse). */
    val dotInEveryHand: Boolean = false,
    /** Pièces supplémentaires par étape (souvenir Vent arrière). */
    val extraPieces: Int = 0,
    /** Pièces de 5 cases et plus deux fois plus fréquentes (tempête Grosse mer). */
    val heavySea: Boolean = false,
    /** Rochers posés au départ (tempête Récif). */
    val startingRocks: Int = 0,
    /** Toutes les N poses, une case vide du bord devient un rocher (tempête Houle, 0 = jamais). */
    val swellEvery: Int = 0,
    /** Pas d'aperçu des perles et des lignes (tempête Brouillard). */
    val fog: Boolean = false,
)
