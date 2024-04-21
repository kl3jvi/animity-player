package io.animity.anime_player.di.setup

import io.animity.anime_player.di.mediaModule
import org.koin.dsl.koinApplication

internal object AnimityIsolatedKoinContext {
    private val koinApp = koinApplication {
        modules(mediaModule)
    }
    val koin = koinApp.koin
}