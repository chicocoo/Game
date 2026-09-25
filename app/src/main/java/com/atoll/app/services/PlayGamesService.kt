package com.atoll.app.services

import android.app.Activity
import android.content.Context
import android.util.Log
import com.atoll.app.R
import com.atoll.core.Nations
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.SnapshotsClient
import com.google.android.gms.games.leaderboard.LeaderboardVariant
import com.google.android.gms.games.snapshot.SnapshotMetadataChange
import kotlinx.coroutines.tasks.await

/** Clés des succès ; leurs identifiants Play Games sont dans res/values/game_config.xml. */
enum class Achievement {
    FIRST_LAGOON, GRAND_LAGOON, HUGE_LAGOON, PEARL_LINE, COMBO_5, COMBO_10, TRIPLE_LINES,
    SCORE_10K, SCORE_50K, SCORE_200K, FIRST_DAILY, STREAK_7, STREAK_30,
    TOUR_SEA, TOUR_HALF, TOUR_COMPLETE, CHALLENGE_SENT, CHALLENGE_WON,
}

data class RankInfo(val rank: Long?, val total: Long?) {
    val percentile: Int? get() = if (rank != null && total != null && total > 0) ((rank * 100 + total - 1) / total).toInt().coerceIn(1, 100) else null
}

/**
 * Google Play Games Services v2 : connexion, classements, succès, sauvegarde cloud.
 * Tout est facultatif : sans identifiants configurés ou sans connexion, le jeu reste jouable.
 */
class PlayGamesService(private val context: Context) {
    private val tag = "PlayGames"
    private val appId = context.getString(R.string.game_services_project_id)
    val configured get() = appId != "0" && appId.isNotBlank()

    private fun res(id: Int) = context.getString(id).trim()
    val lbDaily get() = res(R.string.lb_daily_world)
    val lbClassic get() = res(R.string.lb_classic)
    val lbTour get() = res(R.string.lb_tour)

    private val countryBoards: Map<String, String> by lazy { pairs(R.array.lb_countries) }
    private val achievementIds: Map<String, String> by lazy { pairs(R.array.achievements) }

    private fun pairs(arrayId: Int): Map<String, String> =
        context.resources.getStringArray(arrayId).mapNotNull {
            val i = it.indexOf('=')
            if (i <= 0) null else it.substring(0, i).trim().uppercase() to it.substring(i + 1).trim()
        }.toMap()

    /** Classement du pays (ou du reste du continent), ou null si non configuré. */
    fun countryBoard(country: String): String? =
        countryBoards[country] ?: Nations.continent(country)?.let { countryBoards["R_$it"] }

    var signedIn = false
        private set
    var playerName: String? = null
        private set

    suspend fun checkSignIn(activity: Activity): Boolean {
        if (!configured) return false
        signedIn = runCatching { PlayGames.getGamesSignInClient(activity).isAuthenticated.await().isAuthenticated }.getOrDefault(false)
        if (signedIn) loadPlayer(activity)
        return signedIn
    }

    suspend fun signIn(activity: Activity): Boolean {
        if (!configured) return false
        signedIn = runCatching { PlayGames.getGamesSignInClient(activity).signIn().await().isAuthenticated }.getOrDefault(false)
        if (signedIn) loadPlayer(activity)
        return signedIn
    }

    private suspend fun loadPlayer(activity: Activity) {
        playerName = runCatching { PlayGames.getPlayersClient(activity).currentPlayer.await().displayName }.getOrNull()
    }

    // ---------- Classements ----------
    suspend fun submit(activity: Activity, board: String, score: Long, scoreTag: String? = null) {
        if (!signedIn || board.isBlank()) return
        runCatching {
            val client = PlayGames.getLeaderboardsClient(activity)
            if (scoreTag != null) client.submitScoreImmediate(board, score, scoreTag).await()
            else client.submitScoreImmediate(board, score).await()
        }.onFailure { Log.w(tag, "submit $board", it) }
    }

    /** Rang du joueur et nombre de participants sur le classement du jour. */
    suspend fun dailyRank(activity: Activity, board: String): RankInfo {
        if (!signedIn || board.isBlank()) return RankInfo(null, null)
        return runCatching {
            val client = PlayGames.getLeaderboardsClient(activity)
            val me = client.loadCurrentPlayerLeaderboardScore(board, LeaderboardVariant.TIME_SPAN_DAILY, LeaderboardVariant.COLLECTION_PUBLIC).await().get()
            val top = client.loadTopScores(board, LeaderboardVariant.TIME_SPAN_DAILY, LeaderboardVariant.COLLECTION_PUBLIC, 1).await().get()
            val total = top?.leaderboard?.variants?.firstOrNull {
                it.timeSpan == LeaderboardVariant.TIME_SPAN_DAILY && it.collection == LeaderboardVariant.COLLECTION_PUBLIC
            }?.numScores?.takeIf { it > 0 }
            top?.release()
            RankInfo(me?.rank?.takeIf { it > 0 }, total)
        }.getOrElse { Log.w(tag, "rank", it); RankInfo(null, null) }
    }

    /** Top 100 du jour (4 pages de 25) pour le tableau des médailles. */
    suspend fun dailyTop(activity: Activity, limit: Int = 100): List<Nations.Entry> {
        if (!signedIn || lbDaily.isBlank()) return emptyList()
        return runCatching {
            val client = PlayGames.getLeaderboardsClient(activity)
            val out = mutableListOf<Nations.Entry>()
            var page = client.loadTopScores(lbDaily, LeaderboardVariant.TIME_SPAN_DAILY, LeaderboardVariant.COLLECTION_PUBLIC, 25, true).await().get()
            while (page != null) {
                val buffer = page.scores
                val before = out.size
                for (s in buffer) out.add(Nations.Entry(s.rank, s.rawScore, s.scoreTag))
                val added = out.size - before
                if (out.size >= limit || added == 0) { page.release(); break }
                val next = client.loadMoreScores(buffer, 25, com.google.android.gms.games.PageDirection.NEXT).await().get()
                page.release()
                page = next
            }
            out.distinctBy { it.rank to it.score }.take(limit)
        }.getOrElse { Log.w(tag, "top", it); emptyList() }
    }

    suspend fun showLeaderboard(activity: Activity, board: String?) {
        if (!signedIn) return
        runCatching {
            val client = PlayGames.getLeaderboardsClient(activity)
            val intent = if (board.isNullOrBlank()) client.allLeaderboardsIntent.await() else client.getLeaderboardIntent(board).await()
            activity.startActivityForResult(intent, 9001)
        }
    }

    // ---------- Succès ----------
    fun unlock(activity: Activity, a: Achievement) {
        val id = achievementIds[a.name] ?: return
        if (signedIn) runCatching { PlayGames.getAchievementsClient(activity).unlock(id) }
    }

    suspend fun showAchievements(activity: Activity) {
        if (!signedIn) return
        runCatching { activity.startActivityForResult(PlayGames.getAchievementsClient(activity).achievementsIntent.await(), 9002) }
    }

    // ---------- Sauvegarde cloud ----------
    private val snapshotName = "atoll-progress"

    suspend fun loadCloud(activity: Activity): String? {
        if (!signedIn) return null
        return runCatching {
            val client = PlayGames.getSnapshotsClient(activity)
            val result = client.open(snapshotName, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED).await()
            val snap = result.data ?: return null
            val bytes = snap.snapshotContents.readFully()
            client.discardAndClose(snap)
            if (bytes.isEmpty()) null else String(bytes, Charsets.UTF_8)
        }.getOrElse { Log.w(tag, "loadCloud", it); null }
    }

    suspend fun saveCloud(activity: Activity, json: String, description: String) {
        if (!signedIn) return
        runCatching {
            val client = PlayGames.getSnapshotsClient(activity)
            val result = client.open(snapshotName, true, SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED).await()
            val snap = result.data ?: return
            snap.snapshotContents.writeBytes(json.toByteArray(Charsets.UTF_8))
            client.commitAndClose(snap, SnapshotMetadataChange.Builder().setDescription(description).build()).await()
        }.onFailure { Log.w(tag, "saveCloud", it) }
    }
}
