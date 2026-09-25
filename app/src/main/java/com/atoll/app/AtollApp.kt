package com.atoll.app

import android.app.Application
import com.atoll.app.data.Store
import com.atoll.app.services.Haptics
import com.atoll.app.services.PlayGamesService
import com.atoll.app.services.Reminder
import com.atoll.app.services.Sounds
import com.google.android.gms.games.PlayGamesSdk

class AtollApp : Application() {
    lateinit var store: Store
        private set
    lateinit var games: PlayGamesService
        private set
    lateinit var sounds: Sounds
        private set
    lateinit var haptics: Haptics
        private set

    override fun onCreate() {
        super.onCreate()
        store = Store(this)
        games = PlayGamesService(this)
        if (games.configured) PlayGamesSdk.initialize(this)
        sounds = Sounds(this).also { it.enabled = store.sound }
        haptics = Haptics(this).also { it.enabled = store.haptics }
        Reminder.ensureChannel(this)
        Reminder.schedule(this, store.reminders)
    }
}
