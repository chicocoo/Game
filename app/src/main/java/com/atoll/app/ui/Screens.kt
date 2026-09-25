package com.atoll.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atoll.app.BuildConfig
import com.atoll.app.MainActivity
import com.atoll.app.R
import com.atoll.core.Daily
import com.atoll.core.Nations
import com.atoll.core.ShareText
import com.atoll.core.Tour
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.util.Locale

@Composable
fun AtollRoot(state: AppState) {
    val p = LocalPalette.current
    Box(Modifier.fillMaxSize().background(p.bg).safeDrawingPadding()) {
        when (val s = state.screen) {
            Screen.Home -> HomeScreen(state)
            is Screen.Play -> PlayScreen(state, s.ctrl, onExit = { state.saveCurrent(); state.screen = Screen.Home })
            Screen.TourHub -> TourScreen(state)
            Screen.Nations -> NationsScreen(state)
            Screen.Settings -> SettingsScreen(state)
            is Screen.ChallengeIntro -> ChallengeScreen(state, s.challenge)
        }
    }
}

// ---------- Éléments communs ----------
@Composable
private fun Title(text: String, size: Int = 26) {
    Text(text, color = LocalPalette.current.ink, fontSize = size.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
}

@Composable
private fun Label(text: String) {
    Text(text.uppercase(Locale.getDefault()), color = LocalPalette.current.inkSoft, fontSize = 11.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Medium)
}

@Composable
private fun Body(text: String, soft: Boolean = true, center: Boolean = false) {
    val p = LocalPalette.current
    Text(text, color = if (soft) p.inkSoft else p.ink, fontSize = 15.sp, lineHeight = 21.sp, textAlign = if (center) TextAlign.Center else TextAlign.Start)
}

@Composable
private fun Primary(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    val p = LocalPalette.current
    Button(onClick, modifier.height(52.dp), enabled = enabled, shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.accentInk)) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Secondary(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val p = LocalPalette.current
    OutlinedButton(onClick, modifier.height(48.dp), shape = RoundedCornerShape(14.dp)) {
        Text(text, color = p.ink, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun Card(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    val p = LocalPalette.current
    Box(
        modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(p.panel)
            .border(1.dp, p.line, RoundedCornerShape(20.dp))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(18.dp),
    ) { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { content() } }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    val p = LocalPalette.current
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onBack) { Text("‹ " + stringResource(R.string.back), color = p.ink, fontSize = 16.sp) }
        Spacer(Modifier.weight(1f))
        Text(title, color = p.ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.size(72.dp))
    }
}

private fun countryName(code: String): String =
    if (code == Nations.WORLD) "" else Locale("", code).getDisplayCountry(Locale.getDefault())

@Composable
private fun countdown(): String {
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) { while (true) { delay(1000); now = Instant.now() } }
    val sec = Duration.between(now, Daily.nextReset(now)).seconds.coerceAtLeast(0)
    return String.format(Locale.ROOT, "%02d:%02d:%02d", sec / 3600, (sec / 60) % 60, sec % 60)
}

// ---------- Accueil ----------
@Composable
private fun HomeScreen(state: AppState) {
    val p = LocalPalette.current
    @Suppress("UNUSED_VARIABLE") val r = state.refresh
    val store = state.store
    val day = Daily.key()
    val official = store.dailyScore(day)
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("ATOLL", color = p.ink, fontSize = 40.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                Text(stringResource(R.string.tagline), color = p.inkSoft, fontSize = 14.sp)
            }
            TextButton({ state.screen = Screen.Settings }) {
                Text(Nations.flag(state.country) + "  ⚙", fontSize = 22.sp, color = p.ink)
            }
        }

        // Défi des Nations
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(p.board).padding(20.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.daily_label, Daily.number(day)).uppercase(Locale.getDefault()),
                    color = p.nacre3, fontSize = 12.sp, letterSpacing = 1.4.sp)
                Text(stringResource(R.string.mode_daily), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                if (official == null) {
                    Text(stringResource(R.string.mode_daily_desc), color = p.nacre1, fontSize = 15.sp)
                    Button({ state.startDaily() }, Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = p.nacre1, contentColor = Color(0xFF0F3E4B))) {
                        Text(stringResource(R.string.play_daily), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                } else {
                    Text(stringResource(R.string.daily_done_home, ShareText.fmt(official)), color = p.nacre1, fontSize = 15.sp)
                    Text(stringResource(R.string.next_in, countdown()), color = p.nacre3, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton({ state.screen = Screen.Nations }, shape = RoundedCornerShape(14.dp)) {
                            Text(stringResource(R.string.nations), color = Color.White)
                        }
                        OutlinedButton({ state.startDaily() }, shape = RoundedCornerShape(14.dp)) {
                            Text(stringResource(R.string.practice), color = Color.White)
                        }
                    }
                }
                if (store.streak > 0) Text("🔥 " + stringResource(R.string.daily_streak, store.streak), color = p.nacre1, fontSize = 14.sp)
            }
        }

        Card(onClick = { state.startClassic() }) {
            Label(stringResource(R.string.mode_classic))
            Title(if (state.hasSavedClassic) stringResource(R.string.resume) else stringResource(R.string.play), 22)
            Body(stringResource(R.string.mode_classic_desc))
            if (store.bestClassic > 0) Body(stringResource(R.string.best_is, ShareText.fmt(store.bestClassic)), soft = false)
        }

        Card(onClick = { state.startTour(weekly = false) }) {
            Label(stringResource(R.string.mode_tour))
            Title(stringResource(R.string.tour_start), 22)
            Body(stringResource(R.string.mode_tour_desc))
            Body(stringResource(R.string.tour_meta, store.miles, store.bestStage, Tour.STAGES), soft = false)
            TextButton({ state.startTour(weekly = true) }, contentPadding = PaddingValues(0.dp)) {
                Text(stringResource(R.string.tour_weekly), color = p.accent, fontWeight = FontWeight.SemiBold)
            }
        }

        Card(onClick = { state.screen = Screen.Nations }) {
            Label(stringResource(R.string.nations))
            Body(stringResource(R.string.nations_desc))
        }
        Spacer(Modifier.height(8.dp))
    }
}

// ---------- Partie ----------
@Composable
fun PlayScreen(state: AppState, ctrl: GameController, onExit: () -> Unit, header: (@Composable () -> Unit)? = null, onResultDone: (() -> Unit)? = null) {
    val p = LocalPalette.current
    BackHandler { onExit() }
    @Suppress("UNUSED_VARIABLE") val v = ctrl.version
    val g = ctrl.game
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onExit) { Text("‹ " + stringResource(R.string.back), color = p.ink, fontSize = 16.sp) }
            Spacer(Modifier.weight(1f))
            val modeName = when (ctrl.kind) {
                Kind.CLASSIC -> stringResource(R.string.mode_classic)
                Kind.DAILY -> stringResource(R.string.mode_daily)
                Kind.PRACTICE -> stringResource(R.string.practice)
                Kind.CHALLENGE -> stringResource(R.string.challenge)
                Kind.TOUR -> stringResource(R.string.stage_n, (ctrl.stage?.index ?: 0) + 1, Tour.STAGES)
            }
            Label(modeName)
        }
        header?.invoke()
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Label(stringResource(R.string.score))
                Text(ShareText.fmt(g.score.toLong()), color = p.ink, fontSize = 40.sp, fontWeight = FontWeight.Black)
            }
            Column(horizontalAlignment = Alignment.End) {
                g.target?.let {
                    Label(stringResource(R.string.target))
                    Text(ShareText.fmt(it.toLong()), color = p.ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                g.piecesLeft?.let {
                    Label(stringResource(R.string.pieces_left))
                    Text(it.toString(), color = p.ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                if (g.target == null && g.piecesLeft == null) {
                    Label(stringResource(R.string.pearls))
                    Text(g.pearlsCashed.toString(), color = p.ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (g.target != null) {
            val k = (g.score.toFloat() / g.target!!).coerceIn(0f, 1f)
            Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(p.line)) {
                Box(Modifier.fillMaxWidth(k).height(8.dp).clip(CircleShape).background(p.accent))
            }
        }
        if (g.rules.showNextHand && g.nextHand.isNotEmpty()) Body(stringResource(R.string.next_hand, g.nextHand.joinToString(" · ") { it.size.toString() }))

        Box {
            BoardView(ctrl, state.store.bigCells, stringResource(R.string.board_description))
            ctrl.result?.let { res ->
                ResultSheet(state, ctrl, res, onDone = onResultDone ?: onExit, Modifier.align(Alignment.BottomCenter))
            }
        }

        val hint = when {
            ctrl.tutorialStep == 1 -> stringResource(R.string.tuto_1)
            ctrl.tutorialStep == 2 -> stringResource(R.string.tuto_2)
            g.rules.fog -> stringResource(R.string.fog_notice)
            ctrl.kind == Kind.DAILY -> stringResource(R.string.daily_rule)
            ctrl.kind == Kind.PRACTICE -> stringResource(R.string.practice_rule)
            ctrl.kind == Kind.CHALLENGE -> stringResource(R.string.challenge_vs, ShareText.fmt(ctrl.challenge?.score ?: 0))
            else -> stringResource(R.string.hint_rule, g.rules.minLagoon)
        }
        Body(hint)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (g.canUndo) Secondary(stringResource(R.string.undo, g.undosLeft)) { ctrl.undo() }
            if (ctrl.kind == Kind.CLASSIC && !g.over) Secondary(stringResource(R.string.new_game)) { state.newClassic() }
        }
    }
}

@Composable
private fun ResultSheet(state: AppState, ctrl: GameController, res: Result, onDone: () -> Unit, modifier: Modifier) {
    val p = LocalPalette.current
    val g = ctrl.game
    Box(modifier.fillMaxWidth().padding(8.dp).clip(RoundedCornerShape(22.dp)).background(p.panel)
        .border(1.dp, p.line, RoundedCornerShape(22.dp)).padding(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Title(res.title, 24)
            Text(ShareText.fmt(res.score), color = p.ink, fontSize = 46.sp, fontWeight = FontWeight.Black)
            Body(res.subtitle)
            res.rank?.let { Body(it, soft = false) }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Stat(stringResource(R.string.stat_lagoons), g.lagoons)
                Stat(stringResource(R.string.stat_pearls), g.pearlsCashed)
                Stat(stringResource(R.string.stat_combo), g.bestCombo)
                Stat(stringResource(R.string.stat_moves), g.piecesPlaced)
            }
            if (res.canRevive) {
                Primary(if (state.adsRemoved) stringResource(R.string.revive_free) else stringResource(R.string.revive_ad), Modifier.fillMaxWidth()) {
                    state.revive(ctrl)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                when (ctrl.kind) {
                    Kind.CLASSIC -> Secondary(stringResource(R.string.play_again), Modifier.weight(1f)) { state.newClassic() }
                    Kind.DAILY, Kind.PRACTICE -> Secondary(stringResource(R.string.practice), Modifier.weight(1f)) { state.startDaily() }
                    Kind.TOUR -> Secondary(stringResource(R.string.next), Modifier.weight(1f)) { onDone() }
                    Kind.CHALLENGE -> Secondary(stringResource(R.string.home), Modifier.weight(1f)) { state.screen = Screen.Home }
                }
                res.share?.let { txt -> Secondary(stringResource(R.string.share), Modifier.weight(1f)) { state.share(txt) } }
            }
            if (ctrl.kind == Kind.CLASSIC || ctrl.kind == Kind.PRACTICE) {
                TextButton({ state.sendChallenge(ctrl) }) { Text(stringResource(R.string.send_challenge), color = p.accent) }
            }
            if (ctrl.kind != Kind.TOUR) TextButton(onDone) { Text(stringResource(R.string.home), color = p.inkSoft) }
        }
    }
}

@Composable
private fun Stat(label: String, value: Int) {
    val p = LocalPalette.current
    Column {
        Text(label.uppercase(Locale.getDefault()), color = p.inkSoft, fontSize = 10.sp, letterSpacing = 1.sp)
        Text(value.toString(), color = p.ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

// ---------- Tour du monde ----------
@Composable
private fun TourScreen(state: AppState) {
    val session = state.tour ?: run { state.screen = Screen.Home; return }
    val p = LocalPalette.current
    val seas = stringArrayResource(R.array.sea_names)
    val stormNames = stringArrayResource(R.array.storm_names)
    val stormDescs = stringArrayResource(R.array.storm_descs)
    val souvNames = stringArrayResource(R.array.souvenir_names)
    val souvDescs = stringArrayResource(R.array.souvenir_descs)
    val run = session.run
    val back = { state.tour = null; state.screen = Screen.Home }

    val ctrl = session.ctrl
    if (session.phase == TourPhase.PLAYING && ctrl != null) {
        PlayScreen(state, ctrl, onExit = back, onResultDone = { state.afterStage() }, header = {
            val st = run.stage
            Body(seas[st.sea] + (st.storm?.let { " · " + stormNames[it.ordinal] } ?: ""), soft = false)
        })
        return
    }
    BackHandler { back() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TopBar(stringResource(if (session.weekly) R.string.tour_weekly else R.string.mode_tour), back)
        when (session.phase) {
            TourPhase.INTRO -> {
                val st = run.stage
                Label(stringResource(R.string.stage_n, st.index + 1, Tour.STAGES))
                Title(seas[st.sea], 28)
                Body(stringResource(R.string.stage_goal, ShareText.fmt(st.target.toLong()), st.pieces), soft = false)
                st.storm?.let { storm ->
                    Card { Label(stringResource(R.string.storm)); Title(stormNames[storm.ordinal], 20); Body(stormDescs[storm.ordinal]) }
                }
                if (run.souvenirs.isNotEmpty()) {
                    Label(stringResource(R.string.your_souvenirs))
                    run.souvenirs.forEach { Body("• " + souvNames[it.ordinal] + " : " + souvDescs[it.ordinal]) }
                }
                Primary(stringResource(R.string.set_sail), Modifier.fillMaxWidth()) { state.playStage() }
            }
            TourPhase.SOUVENIR -> {
                Title(stringResource(R.string.choose_souvenir), 26)
                Body(stringResource(R.string.choose_souvenir_desc))
                run.offer.forEach { s ->
                    Card(onClick = { state.chooseSouvenir(s) }) { Title(souvNames[s.ordinal], 20); Body(souvDescs[s.ordinal]) }
                }
                TextButton({ state.chooseSouvenir(null) }) { Text(stringResource(R.string.skip), color = p.inkSoft) }
            }
            TourPhase.END -> {
                Title(stringResource(if (run.stagesCleared >= Tour.STAGES) R.string.tour_complete else R.string.run_over), 28)
                Body(stringResource(R.string.run_summary, run.stagesCleared, run.miles), soft = false)
                Body(stringResource(R.string.best_is, ShareText.fmt(run.totalScore)))
                if (session.newlyUnlocked.isNotEmpty()) {
                    Card {
                        Label(stringResource(R.string.unlocked))
                        session.newlyUnlocked.forEach { Body("• " + souvNames[it.ordinal] + " : " + souvDescs[it.ordinal], soft = false) }
                    }
                }
                Primary(stringResource(R.string.tour_again), Modifier.fillMaxWidth()) { state.startTour(session.weekly) }
                Secondary(stringResource(R.string.home), Modifier.fillMaxWidth()) { back() }
            }
            TourPhase.PLAYING -> {}
        }
    }
}

// ---------- Nations ----------
@Composable
private fun NationsScreen(state: AppState) {
    val p = LocalPalette.current
    val activity = LocalContext.current as MainActivity
    val scope = rememberCoroutineScope()
    BackHandler { state.screen = Screen.Home }
    LaunchedEffect(state.signedIn) { if (state.signedIn) state.loadStandings() }
    val day = Daily.key()
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TopBar(stringResource(R.string.nations), { state.screen = Screen.Home })
        Label(stringResource(R.string.daily_label, Daily.number(day)))
        Body(stringResource(R.string.nations_explain))
        Body(stringResource(R.string.next_in, countdown()))
        when {
            !state.games.configured -> Body(stringResource(R.string.pgs_not_configured), soft = false)
            !state.signedIn -> Primary(stringResource(R.string.sign_in), Modifier.fillMaxWidth()) { state.signIn() }
            state.standingsLoading && state.standings == null -> Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) { CircularProgressIndicator() }
            state.standings.isNullOrEmpty() -> Body(stringResource(R.string.nations_empty), soft = false)
            else -> {
                val list = state.standings!!
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(list.size) { i ->
                        val s = list[i]
                        val mine = s.country == state.country
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(if (mine) p.accent.copy(alpha = 0.18f) else p.panel)
                            .padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${i + 1}", color = p.inkSoft, fontSize = 15.sp, modifier = Modifier.widthIn(min = 28.dp))
                            Text(Nations.flag(s.country) + "  " + countryName(s.country), color = p.ink, fontSize = 16.sp,
                                fontWeight = if (mine) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
                            val medals = "🥇".repeat(s.gold) + "🥈".repeat(s.silver) + "🥉".repeat(s.bronze)
                            Text("$medals  ${s.points}", color = p.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Body(stringResource(R.string.nations_points_explain))
            }
        }
        if (state.signedIn) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Secondary(stringResource(R.string.refresh)) { state.standings = null; state.loadStandings() }
            Secondary(stringResource(R.string.leaderboards)) { scope.launch { state.games.showLeaderboard(activity, state.games.lbDaily) } }
        }
        Spacer(Modifier.height(8.dp))
    }
}

// ---------- Réglages ----------
@Composable
private fun SettingsScreen(state: AppState) {
    val p = LocalPalette.current
    val activity = LocalContext.current as MainActivity
    val scope = rememberCoroutineScope()
    val store = state.store
    var pickCountry by remember { mutableStateOf(false) }
    var sound by remember { mutableStateOf(store.sound) }
    var haptics by remember { mutableStateOf(store.haptics) }
    var big by remember { mutableStateOf(store.bigCells) }
    var reminders by remember { mutableStateOf(store.reminders) }
    BackHandler { state.screen = Screen.Home }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TopBar(stringResource(R.string.settings), { state.screen = Screen.Home })

        Card(onClick = { pickCountry = true }) {
            Label(stringResource(R.string.your_country))
            Title(Nations.flag(state.country) + "  " + countryName(state.country).ifBlank { stringResource(R.string.world_citizen) }, 20)
            Body(stringResource(R.string.country_explain))
        }

        Toggle(stringResource(R.string.sound), sound) { sound = it; store.sound = it; state.app.sounds.enabled = it }
        Toggle(stringResource(R.string.haptics), haptics) { haptics = it; store.haptics = it; state.app.haptics.enabled = it }
        Toggle(stringResource(R.string.big_cells), big) { big = it; store.bigCells = it }
        Toggle(stringResource(R.string.reminders), reminders) {
            reminders = it; store.reminders = it
            com.atoll.app.services.Reminder.schedule(activity, it)
            if (it) activity.askNotificationPermission()
        }
        HorizontalDivider(color = p.line)

        if (state.adsRemoved) Body(stringResource(R.string.ads_removed), soft = false)
        else Primary(stringResource(R.string.remove_ads, state.billing.price ?: ""), Modifier.fillMaxWidth()) { state.billing.buy(activity) }
        Secondary(stringResource(R.string.restore_purchases), Modifier.fillMaxWidth()) { state.billing.restore() }
        if (state.ads.privacyOptionsRequired) Secondary(stringResource(R.string.privacy_options), Modifier.fillMaxWidth()) { state.ads.showPrivacyOptions(activity) }
        HorizontalDivider(color = p.line)

        if (state.games.configured) {
            if (!state.signedIn) Secondary(stringResource(R.string.sign_in), Modifier.fillMaxWidth()) { state.signIn() }
            else {
                Secondary(stringResource(R.string.achievements), Modifier.fillMaxWidth()) { scope.launch { state.games.showAchievements(activity) } }
                Secondary(stringResource(R.string.leaderboards), Modifier.fillMaxWidth()) { scope.launch { state.games.showLeaderboard(activity, null) } }
            }
        }
        Secondary(stringResource(R.string.replay_tutorial), Modifier.fillMaxWidth()) { store.tutorialDone = false; store.savedGame = null; state.startClassic() }
        Body(stringResource(R.string.about, BuildConfig.VERSION_NAME))
        Spacer(Modifier.height(16.dp))
    }

    if (pickCountry) CountryDialog(current = state.country, onPick = { state.setCountry(it); pickCountry = false }, onDismiss = { pickCountry = false })
}

@Composable
private fun Toggle(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    val p = LocalPalette.current
    Row(Modifier.fillMaxWidth().clickable { onChange(!value) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = p.ink, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Switch(value, onChange)
    }
}

@Composable
private fun CountryDialog(current: String, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    val p = LocalPalette.current
    val countries = remember {
        listOf(Nations.WORLD) + Locale.getISOCountries().filter { Nations.isKnown(it) }.sortedBy { countryName(it) }
    }
    val world = stringResource(R.string.world_citizen)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onDismiss) { Text(stringResource(R.string.close)) } },
        title = { Text(stringResource(R.string.your_country)) },
        text = {
            LazyColumn(Modifier.height(420.dp)) {
                items(countries) { code ->
                    Text(Nations.flag(code) + "  " + (if (code == Nations.WORLD) world else countryName(code)),
                        color = p.ink, fontSize = 16.sp, fontWeight = if (code == current) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.fillMaxWidth().clickable { onPick(code) }.padding(vertical = 10.dp))
                }
            }
        },
    )
}

// ---------- Défi reçu ----------
@Composable
private fun ChallengeScreen(state: AppState, ch: com.atoll.core.Challenge) {
    BackHandler { state.screen = Screen.Home }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(Nations.flag(ch.country), fontSize = 56.sp)
        Title(stringResource(R.string.challenge_title, ch.name.ifBlank { "?" }), 26)
        Body(stringResource(R.string.challenge_desc, ShareText.fmt(ch.score)), center = true)
        Primary(stringResource(R.string.challenge_accept), Modifier.fillMaxWidth()) { state.startChallenge(ch) }
        TextButton({ state.screen = Screen.Home }) { Text(stringResource(R.string.later), color = LocalPalette.current.inkSoft) }
    }
}
