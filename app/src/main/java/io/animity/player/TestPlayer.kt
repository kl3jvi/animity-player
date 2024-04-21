@file:OptIn(UnstableApi::class)

package io.animity.player

import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.TrackSelector
import io.animity.anime_player.player.AnimityPlayer
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File
import kotlin.random.Random

class TestPlayer : AnimityPlayer() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentProgress.onEach {
            Log.e("TestPlayer", "Current progress: $it")
        }.launchIn(lifecycleScope)
    }

    override fun getCacheDataSourceFactory(): DataSource.Factory {
        val databaseProvider = StandaloneDatabaseProvider(this)
        val cacheDirectory = File(filesDir, "downloads${Random.nextInt()}")
        val simpleCache = SimpleCache(cacheDirectory, NoOpCacheEvictor(), databaseProvider)
        val dataSource = DefaultHttpDataSource.Factory()
            .setUserAgent("Animity/1.0.0 (Linux;Android 11) ExoPlayerLib/2.14.1")
            .setConnectTimeoutMs(10_000)
            .setReadTimeoutMs(10_000)
        return CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(dataSource)
    }

    override fun getTrackSelector(): TrackSelector {
        return DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters().clearVideoSizeConstraints())
        }.apply {
            buildUponParameters()
                .setMaxVideoSize(1, 1)
                .build()
        }
    }

    override fun getAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
    }
}