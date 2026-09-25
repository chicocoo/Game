package com.atoll.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.atoll.app.AtollApp
import com.atoll.app.BuildConfig
import com.atoll.app.MainActivity
import com.atoll.app.R
import com.atoll.app.services.Achievement
import com.atoll.app.services.AdsService
import com.atoll.app.services.BillingService
import com.atoll.core.Challenge
import com.atoll.core.Daily
import com.atoll.core.Game
import com.atoll.core.Mode
import com.atoll.core.Nations
import com.atoll.core.ShareText
import com.atoll.core.Souvenir
import com.atoll.core.Tour
import com.atoll.core.TourRun
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.time.LocalDate

sealed interface Screen {
    data object Home : Screen
    data class Play(val ctrl: GameController) : Screen
    data object TourHub : Screen
    data object Nations : Screen
    data object Settings : Screen
    data class ChallengeIntro(val challenge: Challenge) : Screen
}

enum class TourPhase { INTRO, PLAYING, SOUVENIR, END }

class TourSession(val run: TourRun, val weekly: Boolean) {
    var phase by mutableStateOf(TourPhase.INTRO)
    var ctrl by mutableStateOf<GameController?>(null)
    var newlyUnlocked by mutableStateOf<List<Souvenir>>(emptyList())
}

/**
 * Cerveau de l'application : navigation, démarrage des parties, fin de partie
 * (scores, classements, succès, pubs, partage).
 */
class AppState(
    private val activity: MainActivity,
    private val scope: CoroutineScope,
    val ads: AdsService,
    val billing: BillingService,
) {
    val app = activity.application as AtollApp
    val store = app.store
    val games = app.games
    private fun str(id: Int, vararg args: Any): String = activity.getString(id, *args)

    var screen by mutableStateOf<Screen>(Screen.Home)
    var signedIn by mutableStateOf(false)
    var adsRemoved by mutableStateOf(store.adsRemoved)
    var country by mutableStateOf(store.country)
    var tour by mutableStateOf<TourSession?>(null)
    var standings by mutableStateOf<List<Nations.Standing>?>(null)
    var standingsLoading by mutableStateOf(false)
    var refresh by mutableStateOf(0)
        private set

    fun bump() { refresh++ }

    private val labels get() = Labels(str(R.string.fx_lagoon), str(R.string.fx_grand_lagoon), str(R.string.fx_lines), str(R.string.fx_combo))

    // ---------- Démarrage ----------
    fun onStart() {
        scope.launch {
            signedIn = games.checkSignIn(activity)
            if (signedIn) syncCloud()
        }
    }

    fun signIn() {
        scope.launch {
            signedIn = games.signIn(activity)
            if (signedIn) { syncCloud(); bump() }
        }
    }

    private suspend fun syncCloud() {
        games.loadCloud(activity)?.let { store.mergeJson(it); country = store.country; bump() }
        games.saveCloud(activity, store.exportJson(), "ATOLL")
    }

    fun setCountry(code: String) { store.country = code; country = code }

    fun setAdsRemoved(owned: Boolean) {
        if (owned) { store.adsRemoved = true; adsRemoved = true }
    }

    // ---------- Liens de défi ----------
    fun handleIntent(intent: Intent?) {
        val data: Uri = intent?.data ?: run {
            if (intent?.getStringExtra("open") == "daily") startDaily()
            return
        }
        Challenge.fromQuery(data.encodedQuery)?.let { screen = Screen.ChallengeIntro(it) }
    }

    fun handleReferrer(referrer: String) {
        Challenge.fromQuery(referrer)?.let { screen = Screen.ChallengeIntro(it) }
    }

    fun challengeLink(seed: Int, score: Long): String {
        val name = (games.playerName ?: store.playerName).ifBlank { "?" }
        val query = Challenge(seed, score, name, country).toQuery()
        return if (BuildConfig.LINK_HOST != "atoll.example.com") "https://${BuildConfig.LINK_HOST}/d/?$query"
        else "https://play.google.com/store/apps/details?id=${activity.packageName}&referrer=" + URLEncoder.encode(query, "UTF-8")
    }

    // ---------- Lancer une partie ----------
    private fun controller(game: Game, kind: Kind, challenge: Challenge? = null, tutorial: Boolean = false, stage: com.atoll.core.Stage? = null) =
        GameController(game, kind, challenge, stage, tutorial, app.sounds, app.haptics, labels).also { c ->
            c.onOver = { onGameOver(it) }
            c.onPlaced = { ev -> onMove(ev); if (kind == Kind.DAILY || kind == Kind.CLASSIC) saveCurrent(c) }
        }

    fun startClassic() {
        val saved = store.savedGame?.let { parseSave(it) }
        val ctrl = if (saved != null) saved else {
            val tutorial = !store.tutorialDone
            val g = Game(Mode.CLASSIC, (System.nanoTime() xor 0x5DEECE66DL).toInt())
            if (tutorial) g.applyTutorial()
            controller(g, Kind.CLASSIC, tutorial = tutorial)
        }
        screen = Screen.Play(ctrl)
        ads.preload(activity, !adsRemoved)
    }

    fun newClassic() { store.savedGame = null; startClassic() }

    fun startDaily() {
        val day = Daily.key()
        val official = store.dailyScore(day) == null
        val g = Game.daily(day)
        if (official) store.savedGame?.let { raw ->
            // Reprendre l'essai officiel en cours s'il y en a un.
            val parts = raw.split('|')
            if (parts.getOrNull(0) == "D" && parts.getOrNull(1) == day) g.replay(parts.getOrNull(2)?.split(',')?.filter { it.isNotBlank() } ?: emptyList())
        }
        screen = Screen.Play(controller(g, if (official) Kind.DAILY else Kind.PRACTICE))
    }

    fun startChallenge(ch: Challenge) {
        screen = Screen.Play(controller(Game.challenge(ch.seed), Kind.CHALLENGE, challenge = ch))
    }

    fun startTour(weekly: Boolean) {
        val seed = if (weekly) com.atoll.core.hashString("atoll-tour-" + Daily.weekKey()) else (System.nanoTime() xor 0x2545F491L).toInt()
        tour = TourSession(TourRun(seed, Tour.unlocked(store.miles.toInt())), weekly)
        screen = Screen.TourHub
    }

    fun playStage() {
        val s = tour ?: return
        s.ctrl = controller(s.run.newStageGame(), Kind.TOUR, stage = s.run.stage)
        s.phase = TourPhase.PLAYING
        ads.preload(activity, !adsRemoved)
    }

    fun chooseSouvenir(souvenir: Souvenir?) {
        val s = tour ?: return
        s.run.choose(souvenir)
        s.phase = TourPhase.INTRO
    }

    // ---------- Sauvegarde de la partie en cours ----------
    fun saveCurrent() { (screen as? Screen.Play)?.ctrl?.let { saveCurrent(it) } }

    private fun saveCurrent(play: GameController) {
        val g = play.game
        store.savedGame = when {
            g.over -> null
            play.kind == Kind.CLASSIC -> "C|${g.seed}|${if (play.tutorial) 1 else 0}|${g.moves.joinToString(",")}"
            play.kind == Kind.DAILY -> "D|${Daily.key()}|${g.moves.joinToString(",")}"
            else -> store.savedGame
        }
    }

    private fun parseSave(raw: String): GameController? = runCatching {
        val parts = raw.split('|')
        if (parts[0] != "C") return null
        val g = Game(Mode.CLASSIC, parts[1].toInt())
        val tutorial = parts[2] == "1"
        if (tutorial) g.applyTutorial()
        g.replay(parts.getOrElse(3) { "" }.split(',').filter { it.isNotBlank() })
        if (g.over) null else controller(g, Kind.CLASSIC, tutorial = tutorial && g.piecesPlaced == 0).also { it.touch() }
    }.getOrNull()

    val hasSavedClassic get() = store.savedGame?.startsWith("C|") == true

    // ---------- Fin de partie ----------
    private fun onGameOver(c: GameController) {
        val g = c.game
        val score = g.score.toLong()
        store.gamesPlayed += 1
        store.totalPearls += g.pearlsCashed
        store.totalLagoons += g.lagoons
        if (c.tutorial) store.tutorialDone = true
        achievements(g)
        when (c.kind) {
            Kind.CLASSIC -> {
                store.savedGame = null
                val record = score > store.bestClassic
                if (record) store.bestClassic = score
                if (score >= 10_000) games.unlock(activity, Achievement.SCORE_10K)
                if (score >= 50_000) games.unlock(activity, Achievement.SCORE_50K)
                if (score >= 200_000) games.unlock(activity, Achievement.SCORE_200K)
                c.result = Result(str(R.string.over_classic), score,
                    if (record) str(R.string.new_record) else str(R.string.best_is, ShareText.fmt(store.bestClassic)),
                    canRevive = g.revivesLeft > 0, share = shareClassic(score))
                scope.launch { games.submit(activity, games.lbClassic, score) }
                maybeInterstitial()
            }
            Kind.DAILY -> {
                val day = Daily.key()
                store.savedGame = null
                store.recordDaily(day, score, LocalDate.parse(day).minusDays(1).toString())
                games.unlock(activity, Achievement.FIRST_DAILY)
                if (store.streak >= 7) games.unlock(activity, Achievement.STREAK_7)
                if (store.streak >= 30) games.unlock(activity, Achievement.STREAK_30)
                val base = Result(str(R.string.over_daily), score, str(R.string.daily_streak, store.streak), false,
                    share = shareDaily(day, score, null, null))
                c.result = base
                scope.launch {
                    val tag = Nations.tag(country, score, day)
                    games.submit(activity, games.lbDaily, score, tag)
                    games.countryBoard(country)?.let { games.submit(activity, it, score) }
                    val world = games.dailyRank(activity, games.lbDaily)
                    val local = games.countryBoard(country)?.let { games.dailyRank(activity, it) }
                    val rankText = listOfNotNull(
                        world.percentile?.let { str(R.string.rank_top, it) },
                        local?.rank?.let { str(R.string.rank_country, it.toString(), Nations.flag(country)) },
                    ).joinToString(" · ").ifBlank { null }
                    c.result = base.copy(rank = rankText, share = shareDaily(day, score, world.percentile, local?.rank))
                    games.saveCloud(activity, store.exportJson(), "ATOLL")
                }
                maybeAskReview()
                if (!store.notificationAsked) activity.askNotificationPermission()
            }
            Kind.PRACTICE -> c.result = Result(str(R.string.over_practice), score,
                str(R.string.daily_official, ShareText.fmt(store.dailyScore(Daily.key()) ?: 0)), false)
            Kind.CHALLENGE -> {
                val ch = c.challenge!!
                val won = score > ch.score
                if (won) games.unlock(activity, Achievement.CHALLENGE_WON)
                c.result = Result(if (won) str(R.string.challenge_won, ch.name) else str(R.string.challenge_lost, ch.name), score,
                    str(R.string.challenge_vs, ShareText.fmt(ch.score)), false, won = won,
                    share = str(R.string.share_challenge_back, ShareText.fmt(score), challengeLink(ch.seed, score)))
            }
            Kind.TOUR -> onStageOver(c)
        }
        scope.launch { games.saveCloud(activity, store.exportJson(), "ATOLL") }
        bump()
    }

    private fun onStageOver(c: GameController) {
        val s = tour ?: return
        val g = c.game
        val cont = s.run.completeStage(g)
        val cleared = s.run.stagesCleared
        if (cleared >= 3) games.unlock(activity, Achievement.TOUR_SEA)
        if (cleared >= 9) games.unlock(activity, Achievement.TOUR_HALF)
        if (cleared >= Tour.STAGES) games.unlock(activity, Achievement.TOUR_COMPLETE)
        if (cont) {
            c.result = Result(str(R.string.stage_won), g.score.toLong(), str(R.string.stage_spare, g.piecesLeft ?: 0), false, won = true)
        } else {
            val before = Tour.unlocked(store.miles.toInt())
            store.miles += s.run.miles
            store.bestStage = maxOf(store.bestStage, cleared.toLong())
            s.newlyUnlocked = Tour.unlocked(store.miles.toInt()) - before.toSet()
            c.result = Result(if (g.won) str(R.string.tour_complete) else str(R.string.stage_lost), s.run.totalScore,
                str(R.string.run_summary, cleared, s.run.miles), false, won = g.won)
            scope.launch { games.submit(activity, games.lbTour, s.run.totalScore) }
            maybeInterstitial()
        }
    }

    /** Suite après l'écran de résultat d'une étape. */
    fun afterStage() {
        val s = tour ?: return
        s.ctrl = null
        s.phase = when {
            s.run.finished -> TourPhase.END
            s.run.offer.isNotEmpty() -> TourPhase.SOUVENIR
            else -> TourPhase.INTRO
        }
    }

    private fun achievements(g: Game) {
        if (g.lagoons > 0) games.unlock(activity, Achievement.FIRST_LAGOON)
        if (g.biggestLagoon >= 6) games.unlock(activity, Achievement.GRAND_LAGOON)
        if (g.biggestLagoon >= 12) games.unlock(activity, Achievement.HUGE_LAGOON)
        if (g.bestCombo >= 5) games.unlock(activity, Achievement.COMBO_5)
        if (g.bestCombo >= 10) games.unlock(activity, Achievement.COMBO_10)
    }

    /** Succès liés à un coup précis (appelé à chaque pose). */
    fun onMove(ev: com.atoll.core.PlaceResult) {
        if (ev.lines.any { it.pearls >= 4 }) games.unlock(activity, Achievement.PEARL_LINE)
        if (ev.multi >= 3) games.unlock(activity, Achievement.TRIPLE_LINES)
    }

    // ---------- Pubs ----------
    fun revive(c: GameController) {
        if (adsRemoved) { c.revive(); return }
        ads.showRewarded(activity) { ok -> if (ok) c.revive() }
    }

    private fun maybeInterstitial() {
        if (adsRemoved || store.gamesPlayed < 3) return
        val now = System.currentTimeMillis()
        if (now - store.lastInterstitialAt < 3 * 60_000) return
        if (ads.showInterstitial(activity) {}) store.lastInterstitialAt = now
    }

    private fun maybeAskReview() {
        if (store.reviewAsked || store.dailiesPlayed < 3) return
        store.reviewAsked = true
        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow().addOnSuccessListener { info -> manager.launchReviewFlow(activity, info) }
    }

    // ---------- Partage ----------
    private fun shareDaily(day: String, score: Long, percentile: Int?, countryRank: Long?): String = buildString {
        append(str(R.string.share_daily_head, Daily.number(day), Nations.flag(country))).append('\n')
        append(ShareText.fmt(score)).append(" pts")
        percentile?.let { append(" · ").append(str(R.string.rank_top, it)) }
        countryRank?.let { append(" · ").append(Nations.flag(country)).append(" #").append(it) }
        append('\n').append(str(R.string.share_daily_cta)).append('\n')
        append(challengeLink(Daily.seed(day), score))
    }

    private fun shareClassic(score: Long) =
        str(R.string.share_classic, ShareText.fmt(score)) + "\n" + "https://play.google.com/store/apps/details?id=${activity.packageName}"

    fun share(text: String) {
        games.unlock(activity, Achievement.CHALLENGE_SENT)
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        activity.startActivity(Intent.createChooser(send, null))
    }

    fun sendChallenge(c: GameController) {
        val g = c.game
        share(str(R.string.share_challenge, ShareText.fmt(g.score.toLong()), challengeLink(g.seed, g.score.toLong())))
    }

    // ---------- Nations ----------
    fun loadStandings() {
        if (standingsLoading) return
        standingsLoading = true
        scope.launch {
            val entries = games.dailyTop(activity)
            standings = Nations.standings(entries, Daily.key())
            standingsLoading = false
        }
    }
}
