# Publier ATOLL sur Google Play : guide pas à pas

Tu as déjà un compte développeur. Ce guide va de la compilation à la mise en production. Compte 2 à 3 heures pour la configuration, puis les 14 jours obligatoires de test fermé si ton compte personnel a été créé après le 13 novembre 2023.

---

## 1. Récupérer l'application compilée

L'app est compilée automatiquement par GitHub Actions à chaque push (onglet **Actions** du dépôt, workflow **Android**).

- **`atoll-debug-apk`** : APK de test à installer directement sur ton téléphone (paramètres Android : autoriser l'installation depuis cette source). Il utilise les pubs de test Google.
- **`atoll-release-aab`** : le bundle à envoyer sur la Play Console. Il n'est signé que si les secrets de signature sont configurés (§3).

Pour compiler sur ton ordinateur : installe Android Studio, ouvre le dossier du dépôt, puis `./gradlew :app:assembleDebug`.

## 2. Choisir l'identifiant de l'application

Par défaut, l'identifiant est `com.chicocoo.atoll`. Il est **définitif** une fois l'app publiée. Pour le changer, crée un fichier `atoll.properties` à la racine (non versionné) :

```properties
app.id=com.tonnom.atoll
```

ou définis la variable `ATOLL_APP_ID` dans la CI.

## 3. Clé de signature (upload key)

1. Génère une clé (une seule fois, garde-la précieusement, avec ses mots de passe) :
   ```bash
   keytool -genkeypair -v -keystore upload.jks -alias atoll -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Dans GitHub : **Settings > Secrets and variables > Actions > New repository secret** :
   - `ATOLL_KEYSTORE_BASE64` : le résultat de `base64 -w0 upload.jks`
   - `ATOLL_KEYSTORE_PASSWORD`, `ATOLL_KEY_ALIAS` (`atoll`), `ATOLL_KEY_PASSWORD`
3. Relance le workflow : l'artefact `atoll-release-aab` est maintenant signé.
4. À la première publication, accepte **Play App Signing** : Google conserve la clé finale, la tienne sert seulement à envoyer les mises à jour.

## 4. Créer l'app dans la Play Console

1. **Créer une application** : nom « ATOLL » (vérifie qu'il est libre), langue par défaut, jeu, gratuit.
2. **Contenu de l'application** : politique de confidentialité (URL, voir §9), accès aux applications (aucun identifiant requis), publicités : **oui**, classification du contenu (questionnaire IARC : jeu de puzzle, pas de violence), **public cible : 13 ans et plus** (ne pas cocher les tranches enfants, sinon le programme Familles impose d'autres règles publicitaires), *Data safety* (§8).
3. **Fiche du Play Store** : description courte et longue (FR, EN, puis les autres langues), icône 512×512, image de présentation 1024×500, au moins 4 captures d'écran téléphone, vidéo YouTube facultative (la vidéo de 6 secondes du GDD).

## 5. Play Games Services (classements, succès, sauvegarde cloud)

Dans la Play Console : **Croissance > Play Games Services > Configuration**.

1. Crée un projet Play Games et associe-le à l'app. Crée les identifiants OAuth Android demandés (empreinte SHA-1 de **la clé de signature d'application** fournie par Play App Signing, et celle de ta clé d'upload pour les tests).
2. Active **Saved Games** (sauvegarde cloud).
3. Crée les **classements** (tous en ordre décroissant, format numérique, **protection anti-triche activée**) :
   | Nom | Limite haute du score | Ressource |
   |---|---|---|
   | Défi des Nations (monde) | 400 000 | `lb_daily_world` |
   | Classique | illimitée | `lb_classic` |
   | Tour du monde | illimitée | `lb_tour` |
   | (facultatif) un classement par pays, jusqu'à 60, + 6 « reste du continent » | 400 000 | `lb_countries` |
4. Crée les **succès**. Clés reconnues par l'app (fichier `Achievement.kt`) : `FIRST_LAGOON`, `GRAND_LAGOON`, `HUGE_LAGOON`, `PEARL_LINE`, `COMBO_5`, `COMBO_10`, `TRIPLE_LINES`, `SCORE_10K`, `SCORE_50K`, `SCORE_200K`, `FIRST_DAILY`, `STREAK_7`, `STREAK_30`, `TOUR_SEA`, `TOUR_HALF`, `TOUR_COMPLETE`, `CHALLENGE_SENT`, `CHALLENGE_WON`.
5. Copie les identifiants dans `app/src/main/res/values/game_config.xml` :
   ```xml
   <string name="game_services_project_id">123456789012</string>
   <string name="lb_daily_world">CgkI…</string>
   <string-array name="lb_countries">
       <item>FR=CgkI…</item>
       <item>BR=CgkI…</item>
       <item>R_AF=CgkI…</item>
   </string-array>
   <string-array name="achievements">
       <item>FIRST_LAGOON=CgkI…</item>
   </string-array>
   ```
6. Ajoute ton compte (et ceux des testeurs) dans **Testeurs** de Play Games Services tant que la configuration n'est pas publiée, puis **publie** la configuration Play Games.
7. Programme **Level Up** : active **Sidekick** dans la configuration Play Games. Les succès et la sauvegarde cloud sont déjà intégrés.

## 6. AdMob

1. Crée un compte sur [admob.google.com](https://admob.google.com), ajoute l'app (une fois publiée sur Play, ou « non publiée » en attendant).
2. Crée deux blocs d'annonces : **Avec récompense** (« Continuer ») et **Interstitiel**.
3. Crée un message **RGPD** (Confidentialité et messages > RGPD) : c'est lui que l'app affiche via UMP en Europe, au Royaume-Uni et en Suisse. Ajoute aussi le message des États américains si tu le souhaites.
4. Renseigne les identifiants dans les secrets GitHub (ou `atoll.properties`) :
   - `ATOLL_ADMOB_APP_ID` = `ca-app-pub-…~…`
   - `ATOLL_ADMOB_REWARDED`, `ATOLL_ADMOB_INTERSTITIAL` = `ca-app-pub-…/…`
   
   Sans ces valeurs, l'app utilise les identifiants de **test** de Google (aucun revenu, aucun risque).
5. Publie le fichier `app-ads.txt` sur le site de développeur indiqué dans ta fiche Play (GitHub Pages convient).
6. **Ne clique jamais sur tes propres pubs** en production : utilise l'APK debug ou déclare ton téléphone comme appareil de test.

## 7. Achat « Retirer les pubs »

Play Console : **Monétiser > Produits > Produits intégrés à l'application** : crée `remove_ads` (achat unique, ~4,99 €), active-le. Un compte de paiement marchand doit être configuré. Teste avec un **compte testeur de licence** (Paramètres > Test de licence).

## 8. Data safety (déclaration de sécurité des données)

À déclarer (les SDK Google le font eux-mêmes) :
- **Identifiants de l'appareil** (identifiant publicitaire) : collectés par AdMob, pour la publicité et l'analyse, partagés avec Google.
- **Activité dans l'app** (scores, progression) : via Play Games Services, pour le fonctionnement de l'app.
- **Diagnostics** : plantages collectés par Google Play.
- Chiffrement en transit : oui. Suppression des données : l'utilisateur peut supprimer son profil Play Games.
- Le pays est **choisi par le joueur** et n'utilise pas la localisation.

## 9. Politique de confidentialité et liens de défi

Une page web gratuite suffit (GitHub Pages sur ton compte). Des modèles prêts à remplir sont dans [`site/`](../site/) : `privacy.html`, `d/index.html` (page de défi qui renvoie vers le Play Store) et `.well-known/assetlinks.json`.
- `privacy.html` : quelles données (voir §8), qui les traite (Google AdMob, Google Play Games), contact.
- Facultatif : un domaine pour les **liens de défi** `https://<domaine>/d/?s=…`. Publie `/.well-known/assetlinks.json` avec l'empreinte SHA-256 de la clé d'app, puis définis la variable GitHub `ATOLL_LINK_HOST`. Sans domaine, les défis passent par un lien Play Store avec paramètre `referrer` : l'ami installe le jeu et arrive directement dans le défi.

## 10. Test fermé obligatoire, puis production

1. **Tests > Test fermé** : crée une piste, envoie le bundle signé, ajoute au moins **12 testeurs** (liste d'e-mails ou groupe Google). Ils doivent installer et **jouer réellement** pendant **14 jours consécutifs**.
2. Pendant ce temps : corrige ce qu'ils remontent, prépare les traductions de la fiche (priorités du benchmark : EN, FR, ES, PT-BR, DE, IT, ID, TR, VI, JA, KO, AR).
3. **Demander l'accès à la production** depuis le tableau de bord.
4. **Soft launch** recommandé : production limitée aux Philippines, au Brésil, au Mexique, à l'Indonésie, au Canada et à l'Australie pendant 4 à 6 semaines. Surveille J1 / J7 / J30 dans la Play Console (Statistiques > Rétention). Objectifs : 35 % / 12 % / 5 %.
5. **Lancement mondial** : tous les pays sauf ceux où la monétisation Google est suspendue (Russie, Biélorussie) ; la Chine n'a pas de Play Store.

## 11. Entretien (≈ 2 à 4 h par semaine)

- Répondre aux avis.
- Chaque été : relever `targetSdk` (obligation Google annuelle) et mettre à jour les bibliothèques dans `app/build.gradle.kts`.
- Suivre les revenus AdMob et la rétention ; ajuster la fréquence des interstitiels si la rétention baisse (`AppState.maybeInterstitial`).
