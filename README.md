# ATOLL — jeu mobile Play Store sans serveur

Projet de jeu mobile Android conçu pour rapporter de l'argent avec un minimum de gestion : pas de serveur, tout passe par les services Google (Play Games Services, AdMob, Play Billing, Firebase).

## Contenu

| Dossier / fichier | Rôle |
|---|---|
| [`docs/01-benchmark-mondial.md`](docs/01-benchmark-mondial.md) | Benchmark du marché mobile par pays et par région (2025 → S1 2026) : téléchargements, revenus, rétention, eCPM, genres, règles Play Store 2026, concepts comparés |
| [`docs/02-concept-atoll.md`](docs/02-concept-atoll.md) | Game design du concept retenu, **ATOLL** : règles, modes, social sans serveur, monétisation, stack, KPIs, plan de lancement par pays, feuille de route |
| [`prototype/`](prototype/) | Prototype web jouable de la mécanique centrale (règle du lagon + Défi du jour) |

## Le concept en une phrase

Un block puzzle où **encercler des cases vides crée un lagon de perles** qui multiplient tes lignes, avec un **Défi des Nations** quotidien : la même partie pour toute la planète, et un tableau des médailles par pays calculé à partir des classements Google Play Games, sans serveur.

## Lancer le prototype

Ouvrir `prototype/index.html` dans un navigateur (sur ordinateur ou téléphone). Aucune installation n'est nécessaire.

```bash
# Tests de la logique de jeu (Node 18+)
node --test prototype/tools/core.test.js

# Simulation par bot pour calibrer les règles (durée des parties, fréquence des lagons, scores)
node prototype/tools/simulate.js 100

# Version autonome en un seul fichier (pour publication web)
node prototype/tools/build-artifact.js /chemin/sortie.html
```

## Prochaines étapes

1. Faire tester le prototype à 10-20 personnes (jalon G0 du GDD).
2. Ajuster `MIN_LAGOON`, le barème et le poids des pièces dans `prototype/atoll-core.js`, puis relancer la simulation.
3. Créer le compte Google Play Console et recruter les 12 testeurs obligatoires.
4. Démarrer l'app Android (Kotlin + Jetpack Compose) en portant `atoll-core.js` dans un module `core`.
