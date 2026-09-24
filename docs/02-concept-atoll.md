# ATOLL — concept et game design (v0.1)

> *Nom de travail.* Vérifier la disponibilité du nom (Play Store, INPI, EUIPO) avant de s'y attacher. Alternatives : **Enclave**, **Lagon**, **Perles & Nations**.
>
> Contexte et données : [`01-benchmark-mondial.md`](01-benchmark-mondial.md). Prototype jouable : [`../prototype/`](../prototype/).

---

## 1. Le pitch

**« Le block puzzle que tu connais… sauf que chaque fois que tu encercles des cases vides, la mer se retire et tu fais pousser des perles. Et chaque jour, toute la planète joue la même partie : ton score compte pour ton pays. »**

Slogan : *Encercle. Fais des perles. Représente ton pays.*

**Vidéo de 6 secondes (fiche Play Store, TikTok, Shorts)** : une grille avec un anneau presque fermé → le doigt pose la dernière pièce → l'intérieur se remplit de perles dans une onde nacrée → la ligne se complète et explose en ×2 ×2 ×2 → les chiffres s'envolent → « 🇧🇷 3e nation aujourd'hui ».

---

## 2. Pourquoi ça peut casser les codes

1. **Une seule règle nouvelle, comprise en 3 secondes**, sur la mécanique la plus jouée au monde (*Block Blast* : n°1 des téléchargements en 2025, 17 à 70 M de joueurs quotidiens selon les sources). On ne rééduque pas le joueur : on lui donne un super-pouvoir.
2. **Elle transforme la frustration n°1 du genre en objectif.** Dans un block puzzle, les trous isolés sont ce qui fait perdre. Ici, **encercler** devient une stratégie : on construit des anneaux, on remplit des lagons, on encaisse des perles.
3. **Du « regardable »** : lagon qui se remplit, perles qui explosent, multiplicateurs qui s'empilent. C'est la dopamine de *Balatro* dans le corps de *Block Blast*, et ça se filme.
4. **Un rituel mondial sans serveur** : le *Défi des Nations* (même suite de pièces pour tout le monde, un seul essai officiel par jour, tableau des médailles par pays) crée de la fierté, de la rivalité et du partage, comme *Wordle*, avec la dimension « coupe du monde » en plus.
5. **Coût par joueur ≈ 0** : tout tourne sur le téléphone et les services Google. Le jeu est rentable même à 0,01 $ par joueur et par jour.

**Validation par simulation** (bot glouton, 100 parties, `node prototype/tools/simulate.js 100`) :

| Variante | Poses par partie (moy. / méd.) | Lagons | Effacements avec perle |
|---|---|---|---|
| Block puzzle standard (sans lagon) | 54 / 47 | — | — |
| **ATOLL, lagon ≥ 2 cases (règle retenue)** | **99 / 74** | **18 % des poses** | **58 %** |
| ATOLL, lagon ≥ 1 case (bonus « Plongeur ») | 126 / 98 | 33 % des poses | 68 % |
| ATOLL, lagon ≥ 3 cases | 85 / 71 | 12 % des poses | 49 % |

→ La règle **allonge les parties d'environ 80 %** (plus de temps de jeu) et se déclenche **environ une pose sur cinq** : assez souvent pour être le cœur du jeu, pas au point d'être automatique. La version « 1 case » est trop généreuse par défaut : elle devient un bonus du mode roguelite.

---

## 3. Public cible

- **Principal** : joueurs de block puzzle et de puzzle casual, 18-55 ans, femmes et hommes, dans tous les pays.
- **Secondaire** : seniors (mode grandes cases, pas de chrono), compétiteurs (classements, fierté nationale), amateurs de roguelite (mode Tour du monde).
- **Pays** : volume = Indonésie, Inde, Brésil, Mexique, Philippines, Vietnam, Turquie, Égypte, Nigeria ; revenus = États-Unis, Allemagne, Royaume-Uni, France, Japon, Corée, Canada, Australie, Émirats, Arabie saoudite.

---

## 4. Règles du cœur

- Grille **9 × 9**. Une **main de 3 pièces** (1 à 9 cases : barres, carrés, L, T, S, coins…), sans rotation. On les pose où l'on veut. Nouvelle main quand les 3 sont posées.
- **Ligne ou colonne pleine → elle s'efface.**
- **Règle du lagon** : les cases vides reliées au bord forment **la mer**. Toute zone vide **entièrement encerclée** par des blocs (sans contact avec le bord) et d'**au moins 2 cases** devient un **lagon** : elle se remplit de **perles**.
  - Une perle **compte comme pleine** : remplir un lagon peut compléter une ligne, donc déclencher une réaction en chaîne.
  - Une perle **multiplie la ligne qui l'efface** : 1 perle = ×2, 2 perles = ×3, etc.
- **Aperçu pendant le glisser** : on voit les cases qui deviendront perles (contour doré) et les lignes qui vont sauter. Le joueur apprend la règle en la voyant.
- **Fin de partie** quand aucune pièce de la main ne rentre. Mode Classique : **« Continuer »** une fois par partie (pub récompensée), avec une nouvelle main garantie jouable.

**Score (valeurs du prototype, à équilibrer)**

| Action | Points |
|---|---|
| Poser une pièce | +1 par case |
| Lagon | 20 × surface, +100 si surface ≥ 6 (« Grand lagon ! ») |
| Ligne effacée | 90 × (1 + perles dans la ligne) |
| Plusieurs lignes d'un coup | × nombre de lignes |
| Combo (effacer au moins une fois toutes les 3 poses) | × (1 + 0,5 × (combo − 1)) |

---

## 5. Les modes

### 5.1 Classique (infini, hors-ligne) — le mode volume
La boucle *Block Blast* avec la règle du lagon. Meilleur score local et classement « Classique ». C'est le mode qui porte la publicité.

### 5.2 Défi des Nations (quotidien) — le rituel
- Chaque jour, **la même suite de 45 pièces pour toute la planète**, générée à partir de la date (hasard déterministe, sans serveur).
- Le jour du défi suit le **reset quotidien des classements Play Games (UTC-7)** : nouveau défi à 9 h en France l'été, 8 h l'hiver.
- **Un seul essai officiel** (celui qui compte pour ton pays), puis entraînement libre qui ne compte pas. Aucun bonus payant ou publicitaire dans ce mode : **l'équité est la promesse**.
- Résultat : score, percentile, rang dans ton pays, **tableau des médailles des nations** et **carte de partage** (image + texte + lien de défi) :
  ```
  ATOLL · Défi des Nations 24/09 🇫🇷
  18 450 pts · top 7 % · 🇫🇷 #214
  🐚×23  🌊×6  🔥combo 9
  Bats-moi : https://<pseudo>.github.io/atoll/d/…
  ```
- Simulation (bot) : les scores du défi s'étalent de 5 300 (p10) à 18 100 (p90), soit un rapport de 1 à 3,4. Le classement départage bien les joueurs.

### 5.3 Tour du monde (roguelite) — la profondeur et les longues sessions
- 6 mers × 3 étapes (Île, Archipel, Tempête) = 18 étapes. Chaque étape impose un **score à atteindre en N pièces** (le principe des *blinds* de *Balatro*).
- Après chaque étape, on choisit **1 souvenir parmi 3** (reliques qui modifient les règles). Une run dure 15 à 25 minutes.
- **Souvenirs** (exemples) : *Plongeur* (lagons dès 1 case) · *Nacre* (perle ×3) · *Ancre* (ligne 100 % perles ×3) · *Phare* (le combo tient 5 poses) · *Corail* (un lagon de 6+ cases efface aussi son contour) · *Filet* (annuler une pose par étape) · *Boussole* (voir la main suivante) · *Marée basse* (une pièce de 1 case par main) · *Vent arrière* (+3 pièces par étape) · *Palmier* (chaque grand lagon donne +1 multiplicateur pour l'étape) · *Coquillage* (+50 % de points de lagon) · *Carte au trésor* (premier lagon de l'étape ×5).
- **Tempêtes** (étapes boss) : *Brouillard* (main suivante cachée) · *Houle* (toutes les 5 poses, une case de bord devient rocher) · *Récif* (6 rochers au départ) · *Grosse mer* (pièces de 5 cases ×2) · *Contre-courant* (lagons à partir de 4 cases) · *Calme plat* (pas de combo).
- **Méta** : des *Miles* gagnés à chaque run débloquent de nouveaux souvenirs dans le pool et des thèmes cosmétiques. Le **Grand Tour hebdo** propose la même graine à tout le monde, avec son classement de la semaine.

### 5.4 Défi entre amis (asynchrone, sans serveur)
Un lien contient la graine, le score à battre, le pseudo et le drapeau. Si l'ami a le jeu, le lien l'ouvre directement (App Links). Sinon, il passe par le Play Store avec un paramètre `referrer`, lu au premier lancement grâce à **Play Install Referrer** : l'ami arrive **directement dans le défi** après l'installation. Chaque défi envoyé est une installation potentielle : c'est la boucle virale.

---

## 6. Le social sans serveur : comment ça marche

| Fonction | Service | Détail technique |
|---|---|---|
| Identité | Play Games Services v2 | Connexion automatique ; le jeu reste jouable sans compte |
| Classement mondial du jour | 1 classement « Défi des Nations » (variante *daily*) | *Score tag* = `v1-FR-xxxx` (code pays ISO + contrôle), 64 caractères max, caractères URL-safe |
| **Tableau des médailles** | Calcul **dans le téléphone** | Le client lit le top 100 du jour (4 pages de 25), cache 60 min, compte par code pays : or, argent, bronze pour le podium et *points des nations* = Σ (101 − rang) par pays |
| Rang dans son pays | 60 classements « pays » + 6 « reste du continent » | Limite PGS : 70 classements (+ Monde, Classique, Tour = 69) |
| Amis | PGS Friends | Classement « amis » du jour |
| Défis par lien | App Links + Play Install Referrer | Page statique sur GitHub Pages (`assetlinks.json`) |
| Sauvegarde | PGS Saved Games | Passeport, Miles, déblocages, série |
| Succès | PGS Achievements | 40 succès (requis par Level Up) |
| Engagement Google | Sidekick, Streaks, Quests, Leagues | Intégrer l'API *Game Stats* pour être éligible aux *Leagues* |
| « Live ops » automatiques | Calendrier embarqué | ≈ 190 fêtes nationales : le Défi du jour prend les couleurs du pays fêté, avec un tampon de passeport à gagner |
| Réglages | Firebase Remote Config | Fréquence des pubs, poids des pièces, barèmes |
| Mesure | Firebase Analytics + Crashlytics | Entonnoirs, rétention, crashs |
| Anti-triche | Protection PGS (activée par défaut) + score max par classement | Les *tags* invalides et les scores hors bornes sont ignorés dans le calcul des médailles |

**Quotas** : environ 6 à 10 appels PGS par joueur et par jour (lecture du top 100 en cache horaire, rang pays, envoi du score). À surveiller dans la console Google Cloud ; demander une hausse si le jeu décolle.

---

## 7. Les boucles de rétention

| Échelle | Boucle | Mécanisme |
|---|---|---|
| Seconde | Aperçu → pose → lagon → explosion | Retour visuel, sonore et haptique immédiat |
| Partie | « Encore une » | Record, quasi-victoire, combo en cours |
| Session | Run du Tour du monde | Choix de souvenirs, tempête à battre, Miles |
| Jour | Défi des Nations + série | Rituel à heure fixe, partage, rang du pays, notification locale |
| Semaine | Grand Tour hebdo, classement hebdo | Objectif commun |
| Long terme | Passeport (≈ 190 tampons), succès, thèmes | Collection, identité |

Notifications **locales uniquement** (pas de serveur de push) : « Le Défi du jour est prêt 🌊 », « 🇧🇷 est 2e, la France 5e… », « Ta série de 6 jours est en jeu ». Maximum une par jour, désactivables.

---

## 8. Monétisation

**Objectif** : 75 à 85 % publicité, 15 à 25 % achats intégrés. Pas de pay-to-win.

| Emplacement | Type | Règle |
|---|---|---|
| Continuer (Classique) | Rewarded | 1 fois par partie |
| Relancer la main (Classique) | Rewarded | 1 fois par partie |
| 4e souvenir au choix (Tour) | Rewarded | 1 fois par run |
| Miles ×2 (fin de run) | Rewarded | Optionnel |
| Fin de partie (Classique, Tour) | Interstitiel | À partir de la 3e partie, au plus 1 toutes les 3 min (réglable à distance) |
| Bannière | Bannière | Classique uniquement, à tester (A/B via Remote Config) |
| **Défi des Nations** | **Aucune pub, aucun bonus** | Le rituel reste propre et équitable |

**Achats (Play Billing)** : *Sans pub* 4,99 € (retire interstitiels et bannières ; les rewarded restent optionnels) · *Pack Supporter* 2,99 € (thèmes et cadres de drapeau) · thèmes cosmétiques 0,99 à 1,99 € · plus tard, un *Pass Voyageur* cosmétique saisonnier (contenu embarqué, débloqué par date, sans serveur).

---

## 9. Direction artistique, son et accessibilité

- **Style** : aplats lumineux, mer turquoise animée sur les cases « mer », perles nacrées, blocs couleur corail et sable. Lisible sur petit écran et appareils d'entrée de gamme.
- **Son** : ambiance vagues (ASMR), « pop » cristallin des perles, montée tonale des combos ; retours haptiques.
- **Accessibilité** : palettes daltonisme (formes et couleurs), mode **grandes cases** et contraste renforcé (seniors), aucun chrono, jouable à une main, textes minimaux.
- **Sensibilités culturelles et géopolitiques** : codes ISO 3166-1, libellé « pays et régions » (comme Google Play), **aucune carte avec frontières**, vocabulaire positif (« représenter » plutôt que « conquérir »), option « 🌍 Citoyen du monde », drapeau choisi par le joueur (proposé à partir de la langue ou de la SIM, jamais du GPS). Calendrier des fêtes nationales vérifié, sans dates contestées.

---

## 10. Stack technique recommandée

**Kotlin + Jetpack Compose (Canvas), natif Android.**
- Tous les SDK Google sont officiels et à jour : PGS v2, AdMob + UMP, Play Billing, Install Referrer, In-App Review, In-App Updates, Firebase, Play Integrity.
- **APK < 15 Mo** (essentiel pour l'Inde, l'Afrique et l'Asie du Sud-Est), 60 fps sur entrée de gamme, zéro licence de moteur.
- Un jeu de grille 2D n'a pas besoin d'un moteur ; moins de dépendances, c'est moins de maintenance.
- `minSdk` 24, `targetSdk` 36. Hors-ligne d'abord : les services Google sont des bonus, jamais des prérequis pour jouer.

**Architecture** : `core` (logique pure en Kotlin, portage direct de `prototype/atoll-core.js`, testée, hasard déterministe en entiers) · `ui` (Compose) · `services` (PGS, pubs, achats, referrer, Firebase) derrière des interfaces, avec des implémentations factices pour tester hors-ligne.

*Alternative* : Godot 4, si un portage iOS est envisagé plus tard (plugins PGS et AdMob communautaires, donc plus de maintenance).

---

## 11. Indicateurs et jalons go / no-go

| Jalon | Quand | Critères pour continuer |
|---|---|---|
| **G0 — Prototype** | Semaine 2 | 10 à 20 testeurs ; ≥ 70 % comprennent la règle du lagon en moins de 3 parties sans explication ; ≥ 60 % demandent « une de plus » ; 1re session ≥ 8 min |
| **G1 — Test fermé** (obligatoire : 12 testeurs, 14 jours) | Semaines 13-14 | Sans crash ≥ 99,5 % ; J1 ≥ 40 % (public bienveillant) ; ≥ 50 % font le Défi 3 jours de suite |
| **G2 — Soft launch** (4-6 semaines) | Semaines 15-20 | J1 ≥ 35 %, J7 ≥ 12 %, J30 ≥ 5 % ; ≥ 20 min de jeu par jour ; ≥ 5 % des joueurs du Défi partagent ; ARPDAU ≥ 0,02 $ (émergents), ≥ 0,08 $ (Canada, Australie) |
| **Pivot** | — | Si J1 < 28 % après 2 itérations : garder la couche sociale (Défi des Nations) et tester le cœur B ou C (voir §16) |

---

## 12. Ordres de grandeur de revenus (hypothèses explicites)

ARPDAU mixte de 0,03-0,035 $ (audience mondiale majoritairement émergente, à la *Block Blast*, plus un peu d'IAP et de pays riches). Hors impôts ; revenus publicitaires nets éditeur.

| Scénario | Joueurs actifs / jour | Revenu / jour | Revenu / mois |
|---|---|---|---|
| Démarrage | 2 000 | ≈ 60 $ | ≈ 1 800 $ |
| Bon | 20 000 | ≈ 700 $ | ≈ 21 000 $ |
| Succès | 200 000 | ≈ 7 000 $ | ≈ 210 000 $ |

**À garder en tête** : avec une courbe de rétention de puzzle (J1 32 %, J7 12 %, J30 5 %), un joueur reste en moyenne ≈ 8 à 9 jours actifs (estimation). Tenir 20 000 joueurs quotidiens demande donc ≈ 2 300 nouvelles installations **par jour**. Sans budget d'acquisition, elles doivent venir du partage (Défi des Nations, défis entre amis), de la mise en avant Google (Level Up, festivals indé) et des créateurs de contenu. **La plupart des jeux indépendants restent sous le premier scénario** : les jalons ci-dessus servent à savoir tôt si l'on est parti pour faire mieux.

---

## 13. Plan de lancement par pays

1. **Vague 0 — test fermé** (obligatoire) : France + proches + communautés (Discord, Reddit r/AndroidGaming, forums), FR et EN.
2. **Vague 1 — soft launch (4-6 semaines)** : Philippines (EN), Brésil (PT-BR), Mexique (ES), Indonésie (ID) pour le volume et la rétention ; Canada et Australie (EN) comme témoins « Tier 1 » de monétisation. Budget d'acquisition optionnel de 300 à 1 000 € pour obtenir des cohortes lisibles.
3. **Vague 2 — lancement mondial** : tous les pays sauf les exclus (Chine, Russie, Biélorussie, pays sous sanctions). **12 langues** : EN, FR, ES-LATAM, PT-BR, DE, IT, ID, TR, VI, JA, KO, AR (sens de lecture droite-gauche). Puis TH, PL et HI.
4. **En continu** : candidater aux programmes *Indie Games* de Google Play et viser la conformité **Level Up** dès la v1 ; publier les podiums des nations (des classements par pays intéressent la presse locale) ; envoyer des codes aux créateurs de vidéos « satisfying » et puzzle.

---

## 14. Feuille de route (solo, ~16 semaines)

| Semaines | Livrable |
|---|---|
| 1-2 | Prototype web (dans ce dépôt) + tests joueurs → ajuster règles, barème et poids des pièces (**G0**) |
| 3-6 | MVP Android : mode Classique, animations, sons, haptique, sauvegarde locale |
| 7-8 | PGS v2 (connexion, classements, succès, sauvegarde cloud) + Défi des Nations + carte de partage |
| 9-10 | Tableau des médailles, classements pays, défis par lien (App Links + Install Referrer), Remote Config, Analytics, Crashlytics |
| 11-12 | Tour du monde (18 étapes, ~30 souvenirs, 6 tempêtes), passeport ; AdMob + UMP ; Play Billing |
| 13-14 | Localisation (12 langues), fiche Play Store (icône, captures, vidéo), accessibilité ; **test fermé de 14 jours (G1)** |
| 15-16 | Corrections → production → **soft launch (G2)** |

---

## 15. Risques et parades

| Risque | Parade |
|---|---|
| Copie rapide par un gros éditeur | Avantage du premier arrivé, marque, communauté des nations, itération rapide. *Pixel Flow* a été copié et a quand même fait 94 M$. |
| Découvrabilité (pas de budget d'acquisition) | Boucle de partage et défis, Level Up et festivals indé, ASO en 12 langues, créateurs de contenu, presse sur les classements par pays |
| La règle ne « prend » pas chez les humains | Jalon G0 dès la semaine 2 ; paramètres réglables (taille minimale, barème) ; pivot vers B ou C en gardant la couche sociale |
| Triche dans le Défi des Nations | Protection PGS, bornes de score, filtre sur les *tags* ; aucun gain matériel en jeu → peu d'incitation à tricher |
| Quotas PGS si succès | Cache horaire, lecture limitée au top 100, *backoff* ; demande de hausse de quota |
| Sensibilités géopolitiques | Voir §9 : codes ISO, pas de cartes, vocabulaire neutre |
| Politiques Google Play | Public 13+, UMP, *Data safety*, API cible annuelle, test fermé anticipé (recruter les 12 testeurs dès le prototype) |
| Dépendance aux eCPM | IAP cosmétiques, marchés palier 1 soignés, médiation publicitaire |

---

## 16. Alternatives si le prototype déçoit

- **B — Merge vers les merveilles du monde** (4,05/5) : poser des éléments sur un plateau 6 × 6 ; 3 identiques fusionnent (herbe → buisson → maison → … → monument). Le Défi du jour « construit » un pays (Japon aujourd'hui, Brésil demain) et chaque monument se collectionne dans un album. Plus profond et plus long, mais demande plus d'art et une prise en main plus lente.
- **C — Fusion physique roguelite** (*Suika* × *Balatro*, 4,00/5) : on lâche des billes qui fusionnent, avec des reliques entre les manches. Très « regardable » et viral, mais un marché de clones plus encombré et une physique plus coûteuse sur l'entrée de gamme.

Dans les deux cas, la couche **Défi des Nations / médailles / défis par lien** se réutilise telle quelle.

---

## 17. Prochaines étapes concrètes

1. Jouer au prototype (`prototype/index.html`) et le faire tester à 10-20 personnes : c'est le jalon G0. Noter : compréhension de la règle, envie de rejouer, durée des sessions.
2. Ajuster les paramètres (`MIN_LAGOON`, barème, poids des pièces) dans `prototype/atoll-core.js` ; relancer `node prototype/tools/simulate.js` à chaque changement.
3. Créer le compte Google Play Console (25 $) et commencer à constituer la liste des 12 testeurs.
4. Lancer le projet Android (Kotlin + Compose) en portant `atoll-core.js` dans un module `core`.
