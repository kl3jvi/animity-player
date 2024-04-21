@file:UnstableApi

package io.animity.anime_player.di

import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val mediaModule = module {

    single<StandaloneDatabaseProvider> {
        StandaloneDatabaseProvider(androidContext())
    }

    single<SimpleCache> {
        val cacheDir = androidContext().cacheDir
        SimpleCache(cacheDir, NoOpCacheEvictor(), get())
    }



    single<CacheDataSource.Factory> {
        CacheDataSource.Factory()
            .setCache(get())
            .setUpstreamDataSourceFactory(get())
    }
}