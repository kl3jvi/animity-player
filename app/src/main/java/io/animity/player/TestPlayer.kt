@file:OptIn(UnstableApi::class)

package io.animity.player

import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.cronet.CronetDataSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.TrackSelector
import com.google.android.material.snackbar.Snackbar
import io.animity.anime_player.player.AnimityPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.chromium.net.CronetEngine
import java.io.File
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

class TestPlayer : AnimityPlayer(), CoroutineScope {
    fun test() =
        flow {
            kotlinx.coroutines.delay(4000)
            emit(
                mapOf(
                    "anime" to "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    "anime2" to "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    "anime3" to "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                ),
            )
        }.flowOn(Dispatchers.IO)

    override val getMediaStreamHandler: (String) -> Flow<Map<String, String>>
        get() = {
            test()
        }

    override fun showErrorMessage(string: String) {
        Snackbar.make(binding.root, string, Snackbar.LENGTH_LONG).show()
    }

    override fun getCacheDataSourceFactory(): DataSource.Factory {
        val databaseProvider = StandaloneDatabaseProvider(this)
        val cacheDirectory = File(filesDir, "downloads${Random.nextInt()}")
        val simpleCache = SimpleCache(cacheDirectory, NoOpCacheEvictor(), databaseProvider)
        val dataSource =
            CronetDataSource.Factory(
                CronetEngine.Builder(this).build(),
                Executors.newSingleThreadExecutor(),
            ).setUserAgent("Animity/1.0.0 (Linux;Android 11) ExoPlayerLib/2.14.1")
                .setConnectionTimeoutMs(10_000)
                .setReadTimeoutMs(10_000)

        return CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(dataSource)
            .setCacheWriteDataSinkFactory(null)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
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

    override fun isPictureInPictureEnabled(): Boolean {
        return true
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}
