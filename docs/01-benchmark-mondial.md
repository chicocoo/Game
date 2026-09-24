# Benchmark mondial du jeu mobile Android — septembre 2026

> **Objectif** : trouver un jeu mobile **original**, avec un **temps de jeu élevé** et une forte envie d'**y revenir**, publiable sur le **Play Store** et exploitable **sans serveur** (tout passe par les services Google), pour générer un revenu avec un minimum de gestion après la création.
>
> Le concept recommandé est détaillé dans [`02-concept-atoll.md`](02-concept-atoll.md).

---

## 0. Résumé en 10 points

1. **Le marché plafonne, la rétention devient l'arme principale.** 50,4 Md de téléchargements de jeux en 2025 (−7,2 %), −12 % au 1er semestre 2026, mais le revenu par téléchargement augmente (+11 %). Pour un développeur solo sans budget d'acquisition, tout se joue sur la **rétention** et le **bouche-à-oreille**.
2. **Le puzzle est le seul grand genre casual en croissance continue** : 8,2 Md$ en 2025 (+7,6 %), environ +20 % au S1 2026. C'est aussi **le genre qui retient le mieux** : J1 ≈ 32 %, J7 ≈ 12 %, J30 ≈ 5 %, contre une médiane tous genres de 22 %, < 4 % et 0,8 %.
3. **Le jeu le plus téléchargé au monde en 2025 est un puzzle hors-ligne sans serveur** : *Block Blast* (≈ 356 à 368 M d'installations), monétisé quasiment à 100 % par la publicité (≈ 17,5 M$/mois). *Vita Mahjong*, pensé pour les seniors et jouable hors-ligne, fait ≈ 55 M$/an de publicité. **Le modèle « puzzle hors-ligne + pub » est prouvé et compatible zéro serveur.**
4. **Mais le block puzzle « nu » est saturé** : plus de 2 000 nouveaux block puzzles sortis au S1 2026 (+89 %). Un nouvel entrant doit avoir un **twist visible en 3 secondes**.
5. **Les joueurs et l'argent ne sont pas au même endroit.** Téléchargements : Inde (18-20 % du total mondial), Indonésie, Brésil, États-Unis, Vietnam, Philippines. Revenus : États-Unis, Japon, Corée, Allemagne, Royaume-Uni, France, pays du Golfe. Android pèse ~72 % du parc mondial, mais seulement ~42 % aux États-Unis et ~31 % au Japon.
6. **L'eCPM (revenu pour 1 000 pubs) varie d'un facteur 10 à 20 selon le pays** : ~16-19 $ en rewarded Android aux États-Unis, en Australie et au Japon, ~14,5 $ aux Émirats, ~1-2 $ en Amérique latine et en Asie du Sud-Est, moins de 1 $ en Inde. Il faut donc **du volume mondial** (pub) **et quelques marchés riches** (pub + achats intégrés).
7. **Ce qui marche partout** : hors-ligne, APK léger, zéro texte indispensable, parties courtes mais rejouables à l'infini, pub rewarded acceptée dans tous les pays.
8. **Les espaces libres** : (a) **la compétition sociale sans serveur** (défi quotidien mondial + classement des nations), absente des gros puzzles ; (b) **plus de profondeur** (roguelite) pour le public de *Block Blast* ; (c) **la fierté et la représentation culturelle** (Afrique, MENA, Asie du Sud-Est) ; (d) **les seniors**, via la lisibilité.
9. **Côté Google, 2026 favorise le zéro serveur** : Play Games Services v2 (classements, succès, sauvegarde cloud, amis, overlay *Sidekick*, *Leagues*), programme **Level Up** (visibilité + frais réduits), commission Play abaissée à **10 % sur le premier million de dollars**.
10. **Recommandation : ATOLL.** Un block puzzle avec **une seule règle nouvelle** (« encercle des cases vides → elles deviennent des perles qui multiplient tes lignes »), un **Défi des Nations** quotidien (la même partie pour toute la planète, médailles par pays calculées à partir des classements Google, sans serveur) et un mode **Tour du monde** roguelite. Score de 4,20/5 contre 3,45 à 4,05 pour les sept alternatives étudiées.

---

## 1. Méthode et limites

- **Sources** : Sensor Tower (*State of Mobile / State of Gaming 2026*, bilan S1 2026), AppMagic (rapports Casual 2025 et S1 2026, classements 2025), GameAnalytics (benchmarks 2025 et 2026 : 11 600 à 16 000+ jeux), Appodeal (eCPM), Niko Partners (MENA), StatCounter (parts d'OS), documentation officielle Google Play et Play Games Services, presse spécialisée (PocketGamer.biz, mobilegamer.biz, Deconstructor of Fun…). Liste complète en fin de document.
- **Collecte** : l'environnement utilisé bloquait l'ouverture directe des pages sources (politique réseau). Les chiffres proviennent des synthèses publiées par ces sources et récupérées via moteur de recherche. **Ils sont à revérifier** avant toute décision d'investissement importante.
- **Conventions** : « ≈ » = arrondi ; « *estim.* » = ordre de grandeur non sourcé ligne à ligne (connaissance du marché), à confirmer sur StatCounter / AppMagic.
- **« Tous les pays »** : 51 pays sont détaillés (§5.1), plus d'une centaine d'autres sont classés par palier (§5.3). Un pays non listé suit le profil de sa région.

---

## 2. Le marché mondial en chiffres (2025 → S1 2026)

| Indicateur | Valeur | Source |
|---|---|---|
| Téléchargements de jeux mobiles 2025 | 50,4 Md (−7,2 %) | Sensor Tower |
| Dépenses in-app jeux mobiles 2025 | ≈ 82 Md$ (+1,3 %) | Sensor Tower |
| S1 2026 | 40 Md$ (stable), téléchargements −12 %, revenu/téléchargement +11 % | Sensor Tower via PocketGamer.biz |
| Genre n°1 en revenus | Stratégie (4X) : 9,1 Md$ au S1 2026 (−4 %) — *Last War*, *Whiteout Survival* | Sensor Tower |
| Genre en plus forte hausse | **Puzzle** : 8,2 Md$ en 2025 (+7,6 %) ; ≈ +20 % au S1 2026 | AppMagic, Sensor Tower |
| Sous-genres puzzle (S1 2026) | Match-3 2,5 Md$ (−0,4 %), **Merge 1,3 Md$ (+74 %)**, Match-2 Blast 257 M$ (+2 %) | AppMagic |
| Hybridcasual puzzle | +137 % sur un an (T2 2025) ; *Pixel Flow* ≈ 94 M$ d'IAP depuis août 2025 | AppMagic, Deconstructor of Fun |
| Block puzzles | > 2 000 sorties au S1 2026 (+89 %) → **genre saturé** | AppMagic via Gamigion |
| Mahjong solitaire / tile match | 258 M d'installs (+189 %) / 503 M (+28 %) en 2025 | AppMagic |
| RPG | −14 % au S1 2026 (de la 1re à la 3e place en deux ans) | Sensor Tower |

**Top téléchargements 2025 (AppMagic)** : *Block Blast* 356 M · *Roblox* 286 M · *Free Fire + Free Fire MAX* 280 M · *Subway Surfers* 187 M · *Pizza Ready* 161 M. En Inde, *Ludo King* reste n°1 (71 M). Quatorze jeux ont dépassé 100 M d'installations en 2025. AppMagic observe un basculement vers **les puzzles jouables hors-ligne et les simulations**.

**Top revenus 2025** : *Honor of Kings* 1,68 Md$ (Chine) · *Last War* · *Roblox* ≈ 1,5 Md$ · *Whiteout Survival* > 1,4 Md$ · *Royal Match* 1,37 Md$ · *Monopoly GO!*. Ce sont tous des jeux à **live-ops lourds** (serveurs, événements, équipes de dizaines de personnes) : **à éviter** pour un projet sans gestion.

**Ce que ça implique** : la croissance ne vient plus du volume de joueurs mais de la capacité à les garder. Le puzzle est la seule catégorie qui combine croissance, rétention et compatibilité hors-ligne.

---

## 3. Rétention et temps de jeu : ce qui fait revenir

Benchmarks GameAnalytics (2025-2026) :

| Métrique | Médiane (tous jeux) | Puzzle | Top 1 % |
|---|---|---|---|
| Rétention J1 | ≈ 22 % | **31,9 %** | 64-68 % |
| Rétention J7 | < 4 % | **12,2 %** | — |
| Rétention J30 | 0,7-0,8 % | **5,4 %** | 13-15 % |
| Durée de session | 3,1-3,5 min | — | 22 min |
| Sessions par jour | ≈ 3,8 | — | 12+ |
| Temps de jeu quotidien | ≈ 12 min | — | 94+ min |

- Les genres **match / puzzle** dominent à tous les horizons de rétention.
- **Régions** : l'Amérique du Nord, l'Europe et l'Océanie retiennent le mieux à J7/J30 ; l'Europe préfère les sessions longues ; **l'Afrique a le temps de jeu le plus élevé** (5,5 sessions/jour en médiane, 115+ min au P99) ; Moyen-Orient, Afrique et Amérique du Sud jouent en sessions plus courtes.
- **Mécaniques de retour éprouvées** : le rituel quotidien (*Wordle* et ses grilles partagées), la série de jours (*streak*), la méta-progression (collection, déblocages), la comparaison sociale (classements), la « partie de plus » d'un roguelite (*Balatro*, *Ball x Pit*), la tension d'une partie qui peut tout basculer (*Suika Game*).

> **Cible réaliste pour notre jeu** (niveau d'un bon puzzle) : J1 ≥ 35 %, J7 ≥ 12 %, J30 ≥ 5 %, session moyenne ≥ 8 min (top 10 %), ≥ 4 sessions/jour.

---

## 4. Monétisation : publicité, achats, eCPM par pays

### 4.1 Les modèles qui marchent sans serveur

| Modèle | Exemple | Chiffres |
|---|---|---|
| **100 % pub**, hors-ligne | *Block Blast* | ≈ 584 k$/jour de pub sur Android (≈ 17,5 M$/mois) ; un seul emplacement rewarded (ressusciter), interstitiels entre les niveaux, bannière ; IAP négligeables |
| Pub + « retirer les pubs » | *Vita Mahjong* (seniors) | ≈ 55 M$/an de pub ; interstitiel après presque chaque niveau ; « Remove Ads » à 5,99 $ peu mis en avant |
| Premium (achat unique) | *Balatro* | 21,3 M$ et 3,1 M de téléchargements sur mobile (juillet 2026) |
| Essai gratuit puis achat | *Ball x Pit* (dev solo) | 10 M$ sur Steam, > 2 M d'unités ; sortie mobile en mars 2026 sans pub |
| Hybride pub + IAP | Hybridcasual puzzle | ≈ 59 % IAP / 41 % pub ; ARPDAU ≈ 0,15-0,50 $ (vs 0,03-0,08 $ en hypercasual) |

**ARPDAU de référence** (revenu quotidien par joueur actif) : *Block Blast* ≈ 0,02-0,03 $ (estim. : 584 k$/jour ÷ 17-25 M de joueurs actifs Android, audience très émergente), puzzle ≈ 0,08 $, hybridcasual 0,15-0,50 $.

### 4.2 eCPM publicitaire (ordre de grandeur, Android)

| Palier | Pays | Rewarded Android | Référence |
|---|---|---|---|
| **1** | 🇦🇺 Australie | 18,9 $ | Appodeal T4 2024 (n°1) |
| **1** | 🇯🇵 Japon | 17,4 $ | Appodeal T4 2024 |
| **1** | 🇺🇸 États-Unis | 16,5 $ (interstitiel Android 14,1 $) | Appodeal T4 2024 |
| **1** | 🇦🇪 Émirats | 14,6 $ (moyenne iOS + Android) | Appodeal 2025 (n°2 mondial) |
| **1** | 🇰🇷 Corée du Sud | élevé (interstitiel Android 11,2 $, n°2) | Appodeal T4 2024 |
| **1** | 🇨🇦 Canada | élevé (interstitiel Android 8,5 $, n°3) | Appodeal T4 2024 |
| **1-2** | 🇩🇪 🇬🇧 🇫🇷 🇳🇱 Nordiques | ≈ 8-15 $ *estim.* ; l'Allemagne a la plus forte hausse de 2025 | Bidlogic |
| **3** | Amérique latine, Turquie, Thaïlande | ≈ 1-3 $ (moyenne LATAM Android ≈ 2 $) | MAF / Appodeal |
| **4** | Inde, Indonésie, Philippines, Vietnam, Pakistan, Égypte, Nigeria | < 1-2 $ (l'Inde a connu la baisse la plus large en 2025) | MAF / Bidlogic |

**Conséquence** : 1 joueur américain rapporte autant en pub que 10 à 20 joueurs indiens. Le jeu doit plaire **universellement** (volume, viralité, classements animés) tout en soignant les **marchés à fort eCPM** (localisation, fiche Play Store, qualité).

### 4.3 Commission Google Play (2026)

- Nouvelle grille en cours de déploiement (États-Unis, EEE et Royaume-Uni depuis le 30 juin 2026 ; Australie en septembre ; Japon et Corée en décembre ; reste du monde en 2027) : **10 % sur le premier million de dollars annuel**. Au-delà, le taux dépend du type d'installation (20 % pour les nouvelles installations, davantage pour les installations existantes), et les jeux du programme **Level Up** ont un taux réduit (15 % / 20 %).
- Les **revenus publicitaires AdMob** ne passent pas par cette commission.

---

## 5. Benchmark par pays

### 5.1 Tableau de synthèse (51 pays)

Légende priorité : ★★★ cœur de cible · ★★ important · ★ bonus (même build) · ☆ secondaire · ✖ exclu.

| Pays | Android | Poids et signal clé | Pub (palier eCPM) | Goûts dominants / remarque | Langue | Priorité |
|---|---|---|---|---|---|---|
| 🇺🇸 États-Unis | ≈ 42 % | ≈ 48,7 Md$ de jeux mobiles en 2025 (+10 %), n°1 mondial ; 7 % des téléchargements | 1 | Puzzle (match-3, block, word, solitaire, mahjong), casino, 4X ; 28 % des joueurs ont 50 ans ou plus | EN | ★★★ |
| 🇨🇦 Canada | < 40 % | Proxy classique de soft launch « Tier 1 » | 1 | Comme les États-Unis | EN/FR | ★★ |
| 🇩🇪 Allemagne | ≈ 72 % | 1er marché européen (jeu vidéo ≈ 6,5 Md€, 37,7 M de joueurs) ; plus forte hausse d'eCPM en 2025 | 1 | Puzzle, simulation, stratégie ; exigeant sur la qualité | DE | ★★★ |
| 🇬🇧 Royaume-Uni | ≈ 50 % | ≈ 5,5 Md€, 25 M de joueurs | 1 | Puzzle, casual, casino | EN | ★★★ |
| 🇫🇷 France | ≈ 64 % | ≈ 3,8 Md€, 31,5 M de joueurs ; marché d'origine (presse, festivals) | 1-2 | Puzzle, simulation, arcade | FR | ★★★ |
| 🇪🇸 Espagne | majoritaire *estim.* | ≈ 2,3 Md€ | 2 | Casual, puzzle | ES | ★★ |
| 🇮🇹 Italie | majoritaire *estim.* | ≈ 1,8 Md€ | 2 | Casual, puzzle | IT | ★★ |
| 🇳🇱 🇧🇪 🇨🇭 🇦🇹 + Nordiques | variable | Petits volumes, eCPM élevés ; pays fréquents de soft launch | 1 | Anglais souvent suffisant (NL, Nordiques) | EN/DE/FR | ★ |
| 🇵🇱 Pologne | très majoritaire *estim.* | Marché en croissance, studios nombreux | 2-3 | Casual, stratégie | PL | ★ |
| 🇹🇷 Turquie | > 85 % | **+28 % de dépenses en 2025 (n°1 de croissance)** ; IAP domestiques 347 M$ ; plaque tournante mondiale du casual (97 % du revenu des studios turcs vient du puzzle) | 3 | Puzzle, casual | TR | ★★ |
| 🇷🇺 Russie / 🇧🇾 Biélorussie | très majoritaire | Paiements Play bloqués pour les comptes russes depuis le 26/12/2024 ; le réseau AdMob ne sert plus les utilisateurs russes | — | Téléchargements possibles, monétisation quasi nulle | RU | ✖ |
| 🇯🇵 Japon | ≈ 31 % | 11 Md$ d'IAP en 2025 ; **11,5 % des revenus mondiaux avec 2 % des joueurs** | 1 | Simulation 47 %, RPG 46 % ; culture puzzle et « chaînes » (*Puyo*, *Suika*) ; exigence de finition | JA | ★★ |
| 🇰🇷 Corée du Sud | Google Play ≈ 75 % des revenus | 5,3 Md$ (4e mondial), 490 M de téléchargements | 1 | RPG 47 % des revenus, idle RPG en hausse ; puzzle et cartes dominent les téléchargements | KO | ★★ |
| 🇨🇳 Chine | — | > 40 Md$ mais **pas de Google Play** | — | Hors périmètre | — | ✖ |
| 🇹🇼 Taïwan / 🇭🇰 Hong Kong | variable | Marchés riches | 1-2 | Casual, RPG | ZH-TW | ☆ |
| 🇸🇬 Singapour | variable | Riche, anglophone | 1-2 | Casual | EN | ★ |
| 🇮🇩 Indonésie | ≈ 87 % | 3,3 Md de téléchargements/an ; **n°1 mondial de *Block Blast*** (73 M d'installs en 2025) | 4 | *Free Fire*, *Mobile Legends*, puzzle | ID | ★★★ |
| 🇵🇭 Philippines | très majoritaire *estim.* | 366 M de téléchargements au T1 2025 ; anglophone → **soft launch idéal** | 3-4 | Casual, battle royale | EN | ★★ |
| 🇻🇳 Vietnam | > 85 % | 329 M de téléchargements au T1 2025 ; *Block Blast* 24 M ; éditeurs locaux très actifs | 4 | Puzzle, arcade | VI | ★★ |
| 🇹🇭 Thaïlande | majoritaire *estim.* | **n°1 des revenus en Asie du Sud-Est** (162 M$ au T1 2025) | 3 | Stratégie, RPG, casual | TH | ★★ |
| 🇲🇾 Malaisie | majoritaire *estim.* | 103 M$ au T1 2025 | 2-3 | Casual | EN/MS | ★ |
| 🇮🇳 Inde | ≈ 95 % | **8,45 Md de téléchargements** (avril 2024 - mars 2025) ; 18-20 % des téléchargements mondiaux | 4 (le plus bas) | *Ludo King*, *Free Fire MAX*, *BGMI*, *Block Blast* ; **jeux d'argent réel interdits depuis août 2025** → des millions de joueurs compétitifs sans jeu ; APK léger et hors-ligne indispensables | EN (+HI) | ★★ |
| 🇵🇰 Pakistan / 🇧🇩 Bangladesh | ≈ 95 % *estim.* | Gros volumes | 4 | Casual, arcade | EN/UR/BN | ☆ |
| 🇧🇷 Brésil | ≈ 81 % | 93 M de joueurs ; ARPU ≈ 8 $ ; **marché n°1 des soft launch** ; 6,5 % des téléchargements mondiaux | 3 | *Free Fire*, *Roblox*, *Coin Master*, *Candy Crush*, *Royal Match* | PT-BR | ★★★ |
| 🇲🇽 Mexique | ≈ 80 % *estim.* | 66,7 M de joueurs ; **+21 % de dépenses en 2025** | 3 | Casual et puzzle très appréciés ; pub bien acceptée | ES-LATAM | ★★★ |
| 🇦🇷 🇨🇴 🇨🇱 🇵🇪 | très majoritaire *estim.* | Amérique latine : IAP +13 % en 2025 ; sessions longues, acquisition peu chère | 3 (Chili : 2) | Simulation, stratégie, RPG, shooters (60 % du CA) | ES | ★ |
| 🇸🇦 Arabie saoudite | ≈ moitié *estim.* (iOS en forte hausse) | ARPU ≈ 55 $ ; ≈ 40 % du marché MENA ; +14 % de dépenses en 2025 | 1-2 | Stratégie (la plus rentable), shooters, casual | AR | ★★ |
| 🇦🇪 Émirats | ≈ moitié *estim.* | **ARPU ≈ 84,6 $ (n°1 MENA)** ; entré dans le top 30 mondial en 2025 | 1 | Idem | AR/EN | ★★ |
| 🇪🇬 Égypte | très majoritaire *estim.* | **n°1 en Afrique (368 M$)** ; ARPU ≈ 3,4 $ | 4 | Casual, football | AR | ★ |
| 🇲🇦 🇩🇿 🇹🇳 Maghreb | très majoritaire *estim.* | Volume, francophonie | 3-4 | Casual | FR/AR | ★ |
| 🇮🇱 Israël | variable | Marché riche | 1-2 | Casual | HE | ☆ |
| 🇳🇬 Nigeria | très majoritaire | 300 M$ ; 90 % jouent sur smartphone ; data −20 % entre 2023 et 2025 | 4 | Football, casual ; forte demande de représentation culturelle | EN | ★ |
| 🇿🇦 Afrique du Sud | majoritaire *estim.* | 278 M$ | 3 | Casual | EN | ★ |
| 🇰🇪 Kenya | très majoritaire *estim.* | 46 M$ | 4 | Casual | EN/SW | ☆ |
| 🇦🇺 Australie | < 40 % (iOS en hausse) | **eCPM rewarded Android n°1 mondial** | 1 | Comme les États-Unis ; soft launch classique | EN | ★★ |
| 🇳🇿 Nouvelle-Zélande | < 50 % *estim.* | Soft launch classique | 1 | Idem | EN | ★ |

### 5.2 Fiches régionales

**Amérique du Nord — l'argent.** Premier marché mondial en revenus, eCPM les plus élevés, forte population de joueurs de 50 ans et plus (28 % aux États-Unis), qui sont aussi les plus dépensiers (13 % des 55+ dépensent plus de 500 $/an). Android y est minoritaire (≈ 42 %), mais ses utilisateurs rapportent quand même le plus en pub. → Soigner la fiche Play Store en anglais, la lisibilité (seniors) et les emplacements rewarded.

**Europe de l'Ouest — sessions longues, qualité exigée.** Puzzle, simulation et arcade dominent les téléchargements : les joueurs préfèrent des expériences courtes, accessibles et sans pression. Android est majoritaire sauf au Royaume-Uni (≈ 50/50). **Consentement RGPD obligatoire** (UMP de Google, CMP certifiée). L'Allemagne est le meilleur marché publicitaire européen en 2025. → Localiser DE/FR/ES/IT dès le lancement.

**Turquie et Europe de l'Est.** La Turquie est la plus forte croissance de dépenses de 2025 (+28 %) et le vivier mondial des studios casual et puzzle (Dream Games, Peak, Rollic…). La Russie est à exclure de toute stratégie de monétisation depuis fin 2024.

**Japon — riche mais iOS.** Android ne représente qu'un tiers du parc, mais le Japon rapporte 11,5 % des revenus mondiaux avec 2 % des joueurs. Culture du puzzle et des chaînes (*Puyo Puyo*, phénomène *Suika Game*), exigence de finition et d'animation. → Localisation japonaise soignée en vague 2.

**Corée du Sud — Android roi.** Google Play pèse ≈ 75 % des revenus. Les revenus viennent des RPG (47 %), mais le casual domine les téléchargements. eCPM élevés. → Vague 2.

**Asie du Sud-Est — le volume et la viralité.** L'Indonésie est le premier pays de *Block Blast* au monde ; Philippines et Vietnam suivent en volume ; la Thaïlande dépense le plus. Les genres accessibles (arcade, simulation) font les téléchargements. → Indonésien et vietnamien dès le lancement, anglais pour les Philippines.

**Asie du Sud — l'échelle.** L'Inde, c'est ≈ 20 % des téléchargements mondiaux, 95 % d'Android et les eCPM les plus bas. Deux signaux clés : (1) les jeux culturellement familiers dominent (*Ludo King*, carrom) ; (2) **l'interdiction des jeux d'argent réel (août 2025)** a privé des dizaines de millions de joueurs de leurs jeux compétitifs : un jeu **gratuit, compétitif et fondé sur le talent** (classements, fierté) répond à ce vide. Contraintes : APK < 20 Mo, hors-ligne, appareils d'entrée de gamme.

**Amérique latine — le laboratoire.** Le Brésil est le marché n°1 des soft launch (acquisition bon marché, sessions longues, Android ≈ 81 %). Le Mexique accélère (+21 %) et apprécie casual et puzzle, pub comprise. → PT-BR et ES-LATAM dès le lancement.

**MENA — ARPU record, localisation négligée.** Arabie saoudite et Émirats combinent ARPU élevés (55 à 85 $) et eCPM de palier 1. Les jeux sont rarement bien localisés en arabe (RTL). 46 % des joueurs préfèrent l'arabe standard moderne, et un jeu cité en exemple a multiplié ses ventes régionales par 25 après localisation. → Arabe en vague 2 : une opportunité peu disputée.

**Afrique — la croissance.** 2,3 Md$ en 2025, +12,3 %/an (contre 7,5 % au niveau mondial), 350 M de joueurs, 87-91 % sur smartphone, **le temps de jeu le plus élevé du monde**. Plus de la moitié des joueurs veulent des jeux culturellement pertinents (personnages et décors africains). → Représentation dans les thèmes et drapeaux, APK léger, hors-ligne.

**Océanie.** L'Australie a le meilleur eCPM rewarded Android du monde ; avec la Nouvelle-Zélande, c'est un terrain de soft launch « Tier 1 » classique.

**Exclus ou limités** : Chine (pas de Play Store), Russie et Biélorussie (monétisation Google suspendue), pays sous sanctions (Iran, Corée du Nord, Syrie, Cuba…).

### 5.3 Paliers indicatifs pour tous les pays

| Palier | Profil | Pays (liste indicative) |
|---|---|---|
| **1** — eCPM élevé | Pub + IAP | États-Unis, Canada, Australie, Nouvelle-Zélande, Royaume-Uni, Irlande, Allemagne, Autriche, Suisse, Pays-Bas, Belgique, Luxembourg, pays nordiques, Islande, Japon, Corée du Sud, Singapour, Hong Kong, Taïwan, Émirats, Qatar, Koweït, Israël |
| **2** — intermédiaire | Pub, IAP modérés | France, Italie, Espagne, Portugal, Arabie saoudite, Bahreïn, Oman, Tchéquie, Slovaquie, Slovénie, pays baltes, Pologne, Grèce, Chypre, Malte, Croatie, Chili, Uruguay, Malaisie |
| **3** — volume monétisable | Pub surtout | Brésil, Mexique, Turquie, Thaïlande, Argentine, Colombie, Pérou, Costa Rica, Panama, République dominicaine, Roumanie, Bulgarie, Hongrie, Serbie, Afrique du Sud, Maroc, Tunisie, Jordanie, Liban |
| **4** — volume pur | Viralité, classements | Inde, Indonésie, Philippines, Vietnam, Pakistan, Bangladesh, Égypte, Nigeria, Kenya, Ghana, Éthiopie, Algérie, Irak, Népal, Sri Lanka, Myanmar, Cambodge, Laos, Bolivie, Venezuela, Honduras, Guatemala, Nicaragua, Ukraine… |
| **Exclu** | — | Chine, Russie, Biélorussie, pays sous sanctions |

---

## 6. Les invariants mondiaux : ce qui marche partout

1. **Le puzzle** est le seul genre présent dans le top des téléchargements de *toutes* les régions, et le mieux retenu.
2. **Hors-ligne et léger** : environ 46 % des joueurs disent préférer les modes hors-ligne (étude de marché à confirmer) et 58 % des joueurs mobiles préfèrent le solo. Les deux plus gros succès publicitaires (*Block Blast*, *Vita Mahjong*) sont hors-ligne.
3. **Zéro texte indispensable** : icônes, couleurs et chiffres permettent de localiser pour presque rien et de toucher 190 pays.
4. **Des parties courtes mais infinies** : pas de niveaux à produire (le contenu se génère tout seul), donc pas de *live ops*.
5. **La pub rewarded** est acceptée partout (le joueur choisit) ; l'interstitiel doit rester rare au début.
6. **La fierté et la rivalité** : du football à l'Eurovision, la compétition entre pays mobilise dans toutes les cultures.

---

## 7. Tendances 2025-2026 et espaces libres

**Tendances fortes**
- **Hybridcasual puzzle** : sort, screw et block puzzles, et mécanique de tapis roulant (*Pixel Flow*, *Color Block Jam*, *Screwdom*), qui mêlent la simplicité de l'hypercasual et la progression du casual. Marché dominé par de gros éditeurs (Rollic, Voodoo, Moon Active) qui achètent massivement de l'acquisition.
- **Merge** : le sous-genre puzzle qui croît le plus (+74 à +80 %), mais sous forme narrative et *live ops* (*Gossip Harbor*, *Travel Town*).
- **Mahjong / tile match et seniors** : +189 % d'installations ; *Vita Mahjong*, jeu le plus promu sur Android et iOS pendant sept mois d'affilée (mars 2026).
- **Roguelites** : *Balatro* (21 M$ sur mobile), *Ball x Pit* (dev solo, 10 M$ sur Steam), *CloverPit*. La « partie de plus » séduit un public de plus en plus large.
- **Physique « regardable »** : *Suika Game* est devenu viral grâce aux streamers (11 M de téléchargements, suite en 2025). Les vidéos de billes, de balles rebondissantes et de dominos cumulent des centaines de millions de vues sur TikTok et Shorts.
- **Rituel quotidien** : *Wordle* et ses clones géographiques (*Worldle*, *Countryle*) prouvent la force de « tout le monde joue la même partie aujourd'hui » et du partage de résultats.

**Espaces libres (ce que presque personne ne fait)**
1. **La compétition sociale sans serveur** : les gros puzzles font du social via leurs propres serveurs (équipes, événements). Un défi quotidien identique pour toute la planète, avec un tableau des médailles par pays calculé à partir des classements Google, **n'existe pas à grande échelle** (nous n'avons trouvé qu'un petit Sudoku iOS avec un classement par pays).
2. **La profondeur pour le public *Block Blast*** : des dizaines de millions de joueurs quotidiens sur une mécanique sans objectif à long terme. Des roguelites de block puzzle existent (*Rogueblock*, *Roguelike Block Puzzle*, *Drop Duchy* sur PC), mais aucun n'a percé : la profondeur seule ne suffit pas, **il faut un twist visible**.
3. **La représentation culturelle** : Afrique, MENA et Asie du Sud-Est demandent à se voir dans les jeux, et l'arabe est mal servi.
4. **L'après-argent réel en Inde** : un public compétitif privé de ses jeux depuis 2025.

---

## 8. Contraintes Play Store 2026 et stack « zéro serveur »

### 8.1 Obligations à connaître

| Sujet | Règle 2026 |
|---|---|
| Compte développeur | 25 $ une fois ; pièce d'identité (compte personnel) ou D-U-N-S (organisation) ; vérification des développeurs Android à partir du 30/09/2026 (d'abord au Brésil, en Indonésie, à Singapour et en Thaïlande) |
| Nouveau compte personnel | **Test fermé obligatoire : 12 testeurs actifs pendant 14 jours consécutifs** avant la production (depuis 2026, Google vérifie que les testeurs ont réellement joué) |
| API cible | **Android 16 (API 36)** pour les nouvelles apps et mises à jour depuis le 31/08/2026 |
| Play Games Services | v1 obsolète (mai 2026) → **PGS v2 obligatoire** |
| Pub en Europe | CMP certifiée Google (UMP) pour l'EEE, le Royaume-Uni et la Suisse |
| Public | Viser **13 ans et plus** (hors programme Familles) pour garder une pub standard |
| Liens profonds | *Firebase Dynamic Links* est fermé depuis le 25/08/2025 → App Links + **Play Install Referrer** |

### 8.2 Ce que Google fait à la place d'un serveur

| Besoin | Service Google (gratuit) | Remarque |
|---|---|---|
| Compte joueur | PGS v2 (connexion automatique) | Pas de login à gérer |
| Classements | PGS Leaderboards | **70 max par jeu** ; variantes jour/semaine/toujours automatiques ; reset quotidien à UTC-7 ; *score tag* de 64 caractères ; **protection anti-triche activée par défaut** |
| Succès | PGS Achievements | Requis par Level Up |
| Sauvegarde cloud | PGS Saved Games | Requis par Level Up (novembre 2026) |
| Amis | PGS Friends | Classement « amis » |
| Engagement | *Sidekick* (overlay), *Streaks*, *Quests*, *Leagues* | Les *Leagues* distribuent des Play Points (+45 % d'engagement pour *Subway Surfers*) ; préparer l'API *Game Stats* |
| Pub | AdMob (+ médiation) + UMP | |
| Achats | Play Billing | Commission de 10 % sur le 1er M$ |
| Parrainage / défis | Play Install Referrer + App Links | Paramètre `referrer` lu au premier lancement (valable 90 jours) |
| Réglages à distance | Firebase Remote Config | Ajuster pubs et difficulté sans mise à jour |
| Stats et crashs | Firebase Analytics + Crashlytics | |
| Visibilité | **Programme Level Up** | Sidekick + succès PGS (juillet 2026) + sauvegarde cloud (novembre 2026) → mises en avant, onglet « Vous » (160 M d'utilisateurs mensuels), Play Points, taux réduits |
| Page web | GitHub Pages (hors Google, gratuit) | Politique de confidentialité + fichier `assetlinks.json` des App Links |

### 8.3 Ce qui demandera quand même un peu de temps (honnêtement)

Réponses aux avis, mise à jour annuelle de l'API cible, mises à jour des SDK (AdMob, PGS, Billing), déclarations Play Console (*Data safety*, contenu), réglages Remote Config : **environ 2 à 4 h par semaine après le lancement**, surtout les premiers mois. Aucun serveur, aucune base de données, aucun événement à produire.

---

## 9. Concepts évalués

Pondération : Demande 20 % · Rétention 20 % · Monétisation 15 % · Originalité 15 % · Viralité / croissance organique 15 % · Faisabilité solo et zéro serveur 15 %. Notes de 1 à 5 : ce sont des **jugements argumentés** à partir des données ci-dessus, pas des mesures.

| # | Concept | Dem. | Rét. | Mon. | Orig. | Vir. | Faisa. | **Total** |
|---|---|---|---|---|---|---|---|---|
| **A** | **ATOLL** — block puzzle + règle « lagon » + Défi des Nations + Tour du monde roguelite | 5 | 4 | 4 | 4 | 4 | 4 | **4,20** |
| B | *Merge-3* vers les merveilles du monde (type *Triple Town*, monuments de chaque pays) | 4 | 5 | 4 | 4 | 4 | 3 | 4,05 |
| C | *Suika* × roguelite (fusion physique + reliques) | 4 | 4 | 4 | 3 | 5 | 4 | 4,00 |
| D | Block puzzle roguelite « pur » (*Block Blast* × *Balatro*) | 5 | 4 | 4 | 2 | 3 | 5 | 3,90 |
| E | Pose de tuiles zen quotidienne (type *Dorfromantik*) | 3 | 5 | 3 | 4 | 4 | 4 | 3,85 |
| F | Civilisation idle alimentée par un puzzle | 4 | 5 | 4 | 3 | 3 | 3 | 3,75 |
| G | Classique senior XXL (mahjong / solitaire) | 5 | 4 | 5 | 1 | 1 | 5 | 3,60 |
| H | Course de billes des nations (construction + fantômes stockés dans les *score tags*) | 3 | 3 | 3 | 5 | 5 | 2 | 3,45 |

**Pourquoi A gagne** : il combine **le plus grand public prouvé au monde** (le block puzzle, n°1 des téléchargements dans les 5 premiers pays de *Block Blast*), **un twist qu'on comprend dans une vidéo de 6 secondes**, **une couche sociale mondiale qui ne coûte rien** et **le risque technique le plus faible** (grille 2D). La couche « Défi des Nations » peut aussi s'appliquer à B ou C si le prototype de A déçoit.

---

## 10. Recommandation

➡️ **ATOLL** — voir le game design complet : [`02-concept-atoll.md`](02-concept-atoll.md). Un prototype web jouable de la mécanique centrale est disponible dans [`../prototype/`](../prototype/).

---

## Sources

**Marché mondial et genres**
- Sensor Tower — [State of Mobile 2026](https://sensortower.com/blog/state-of-mobile-2026) · [State of Gaming 2026](https://sensortower.com/report/state-of-gaming-2026) · [synthèse BYYD](https://www.byyd.me/en/blog/2026/03/mobile-game-app-market-in-2026-key-insights-from-the-sensor-tower-report/)
- PocketGamer.biz — [IAP −2 % au S1 2026](https://www.pocketgamer.biz/mobile-gaming-iap-dipped-2-as-ad-spend-climbed-in-h1-2026/) · [Analyse des genres S1 2026](https://www.pocketgamer.biz/h1-2026-genre-analysis-strategy-stumbles-rpgs-fall-and-puzzle-revenue-ramps-up/) · [Top téléchargements 2025](https://www.pocketgamer.biz/the-most-downloaded-mobile-games-of-2025/) · [Top revenus 2025](https://www.pocketgamer.biz/the-top-grossing-mobile-games-of-2025/) · [Balatro sur mobile](https://www.pocketgamer.biz/balatro-nears-44m-on-mobile-amid-a-sudden-spending-surge/)
- mobilegamer.biz — [Top téléchargements 2025](https://mobilegamer.biz/the-top-mobile-game-downloads-of-2025/)
- AppMagic — [Casual Report 2025](https://appmagic.rocks/research/casual-report-2025) · [Casual Report S1 2026](https://appmagic.rocks/research/casual-report-H12026/?hl=en) · [Hybridcasual T1 2025](https://appmagic.rocks/blog/hybridcasual-q1-2025/?hl=en) · [Ball x Pit à 10 M$](https://appmagic.rocks/blog/BallxPit-10M-Steam)
- Gamigion — [Top 10 hybridcasual puzzles S1 2026](https://www.gamigion.com/top-10-hybridcasual-puzzles-in-h1-2026/) · [Block Blast à 1 M$/jour](https://www.gamigion.com/block-blast-by-hungry-studio-is-doing-1m-a-day/)
- Deconstructor of Fun — [Pixel Flow et l'essor des sort puzzles](https://www.deconstructoroffun.com/blog/pixel-flow-and-the-rise-of-sort-puzzles)
- Felix Braberg — [Top 6 des jeux Android qui font fortune avec la pub](https://felixbraberg.substack.com/p/the-top-6-android-games-making-a) · Udonis — [Statistiques Block Blast](https://www.blog.udonis.co/statistics/block-blast)
- Pocket Tactics — [Essor du premium mobile (Balatro)](https://www.pockettactics.com/premium-mobile-games-increase) · Wikipedia — [Suika Game](https://en.wikipedia.org/wiki/Suika_Game) · Shacknews — [Ball x Pit sur mobile](https://www.shacknews.com/article/147972/ball-x-pit-mobile-release-date)
- Udonis — [Statistiques du marché mobile 2026](https://www.blog.udonis.co/mobile-marketing/mobile-games/mobile-gaming-statistics)

**Rétention et monétisation**
- GameAnalytics — [Benchmarks 2026](https://www.gameanalytics.com/reports/2026-mobile-pc-gaming-benchmarks) · [Benchmarks 2025](https://www.gameanalytics.com/reports/2025-mobile-gaming-benchmarks)
- Appodeal — [eCPM Report 2025](https://appodeal.com/the-mobile-ecpm-report-updated-q4-2024/) · [T1 2025](https://appodeal.com/quarterly-mobile-ecpm-report-q1-2025/)
- MAF — [eCPM : données récentes](https://maf.ad/en/blog/mobile-ads-ecpm/) · Bidlogic — [eCPM S1 2025](https://bidlogic.io/2025/07/25/ecpm-growth-in-mobile-apps-q1-q2-2025-analysis-and-insights/) · [eCPM T4 2025](https://bidlogic.io/2026/01/30/what-happened-to-mobile-app-ecpms-in-q4-2025/)
- Game Growth Advisor — [Monétisation hybride 2026](https://gamegrowthadvisor.com/blog/2026-06-02-hybrid-monetization-mobile-games-iap-ads-guide-2026/) · [Hybrid casual 2026](https://gamegrowthadvisor.com/blog/2026-04-16-hybrid-casual-game-design-strategy-2026/)

**Pays et régions**
- Parts d'OS — [Techjury](https://techjury.net/industry-analysis/android-market-share/) · [CommandLinux](https://commandlinux.com/android/android-global-market-share-statistics/) · [Digital Applied](https://www.digitalapplied.com/blog/mobile-os-market-share-2026-ios-vs-android) · [StatCounter](https://gs.statcounter.com/os-market-share/mobile/worldwide)
- Allcorrect — [Mobile Game Market Index 2025](https://allcorrectgames.com/insights/mobile-game-market-index-2025-top-30-markets-growth-trends-opportunities/)
- Japon — [Sensor Tower Japon 2025](https://sensortower.com/blog/state-of-japan-gaming-2025) · [Mobile Marketing Reads](https://www.mobilemarketingreads.com/japan-mobile-games-generate-over-10-billion-as-downloads-fall-5-4/)
- Corée — [Udonis](https://www.blog.udonis.co/top-games/south-korea) · [adjoe](https://adjoe.io/blog/south-korean-mobile-games-market/)
- Asie du Sud-Est — [Sensor Tower SEA 2025](https://sensortower.com/blog/southeast-asia-mobile-gaming-2025) · [GAMES.GG T1 2025](https://games.gg/news/almost-2-billion-game-installs-during-q1-2025/)
- Inde — [GAMES.GG](https://games.gg/news/india-tops-charts-for-mobile-gaming/) · [TechCrunch : interdiction des jeux d'argent réel](https://techcrunch.com/2025/08/20/india-bans-real-money-gaming-threatening-a-23-billion-industry) · [Outlook Respawn : top Inde 2025](https://respawn.outlookindia.com/gaming/gaming-news/top-10-most-downloaded-mobile-games-in-india-in-2025)
- Amérique latine — [Tenjin Brésil 2025](https://tenjin.com/blog/the-state-of-mobile-gaming-in-brazil-2025-data-trends-and-market-analysis/) · [AppFollow Brésil/Mexique](https://appfollow.io/blog/how-to-win-brazil-and-mexicos-mobile-gaming-market)
- Turquie — [Udonis Türkiye](https://www.blog.udonis.co/mobile-game-market/turkiye)
- MENA — [Niko Partners](https://nikopartners.com/the-future-of-gaming-in-mena-3-understanding-the-opportunity/) · [Localisation arabe (Taylor & Francis)](https://www.taylorfrancis.com/chapters/oa-edit/10.4324/9781032628677-9/arabic-mobile-game-localizations-mohammed-al-batineh) · [Localization Advisors](https://www.localizationadvisors.com/2025/01/08/playing-to-win-how-arabic-localization-drives-gaming-success-in-mena/)
- Afrique — [Ecofin](https://www.ecofinagency.com/news-digital/1402-52911-africa-s-gaming-market-hits-2-3bn-driven-by-mobile-boom-spielfabrique) · [Games Industry Africa 2025](https://gamesindustryafrica.com/2025/12/10/new-2025-gaming-in-africa-report-reveals-mobile-first-market-and-strong-demand-for-cultural-representation/)
- Europe — [MAF Europe](https://maf.ad/en/blog/mobile-games-in-europe/)
- Russie — [Play Console : suspension en Russie](https://support.google.com/googleplay/android-developer/answer/15685001?hl=en) · [Pause de monétisation régionale](https://support.google.com/publisherpolicies/answer/15766875?hl=en)
- Seniors — [AARP : joueurs 50+](https://www.aarp.org/pri/topics/social-leisure/activities-interests/2023-gamers-50-plus/) · Hors-ligne — [ASOMobile 2025](https://asomobile.net/en/blog/mobile-gaming-market-2025-trends-and-forecasts/)

**Google Play et Play Games Services**
- [Frais Play 2026 (blog Android)](https://android-developers.googleblog.com/2026/06/play-expanded-billing.html) · [Aide Play Console : frais réduits](https://support.google.com/googleplay/android-developer/answer/16954621?hl=en)
- [Level Up : Sidekick et jalons](https://android-developers.googleblog.com/2026/03/level-up-your-game.html) · [Recommandations Level Up](https://developer.android.com/games/guidelines) · [Présentation PGS](https://developer.android.com/games/pgs/overview) · [Migration PGS v2](https://android-developers.googleblog.com/2025/06/get-ready-for-next-generation-gameplay-play-games-services.html)
- [Classements PGS (limite de 70, reset UTC-7, anti-triche)](https://developers.google.com/games/services/common/concepts/leaderboards) · [Quotas PGS](https://developer.android.com/games/pgs/quota) · [Game Stats](https://developer.android.com/games/pgs/gamestats)
- [Play Games Leagues](https://support.google.com/googleplay/answer/16561562?hl=en) · [Étude de cas Subway Surfers](https://play.google.com/console/about/subway-surfers-leagues-casestudy/)
- [Test fermé : 12 testeurs / 14 jours](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en) · [API cible](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en) · [Vérification des développeurs (TechRepublic)](https://www.techrepublic.com/article/news-google-android-developer-verification-september-2026/)
- [Install Referrer](https://developer.android.com/google/play/installreferrer) · [Fin de Firebase Dynamic Links](https://firebase.google.com/support/dynamic-links-faq) · [Consentement UMP / RGPD](https://developers.google.com/admob/android/privacy/gdpr) · [Play Pass](https://play.google.com/console/about/programs/googleplaypass/) · [Programmes Indie Games](https://play.google.com/console/about/programs/indiegames/)

**Concurrence étudiée (originalité)**
- [Roguelike Block Puzzle](https://apps.apple.com/no/app/roguelike-block-puzzle/id6776018567) · [Rogueblock (Steam)](https://store.steampowered.com/app/4213460) · [Drop Duchy (Inverse)](https://www.inverse.com/gaming/drop-duchy-tetris-puzzle-roguelike-deckbuilder-steam) · [Simple Sudoku (classement par pays)](https://apps.apple.com/gb/app/simple-sudoku-daily-puzzles/id6740351533) · [Fill Qix](https://play.google.com/store/apps/details?id=com.Irusu.FillQix) · [Bounce Ball Escape](https://play.google.com/store/apps/details?id=com.kayac.BounceBallEscape)
