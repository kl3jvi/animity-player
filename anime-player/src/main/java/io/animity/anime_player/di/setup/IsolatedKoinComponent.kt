package io.animity.anime_player.di.setup

import org.koin.core.Koin
import org.koin.core.component.KoinComponent

interface IsolatedKoinComponent : KoinComponent {
    override fun getKoin(): Koin = AnimityIsolatedKoinContext.koin
}