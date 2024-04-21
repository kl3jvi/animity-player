package io.animity.anime_player.player

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_READY
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.TrackSelector
import io.animity.anime_player.R
import io.animity.anime_player.databinding.AnimityPlayerBinding
import io.animity.anime_player.ext.getImageButton
import io.animity.anime_player.playback.AnimityPlayerData
import io.animity.anime_player.playback.PlayBackState
import io.animity.anime_player.playback.PlaybackType
import io.animity.anime_player.playback.toPlayBackState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import androidx.media3.ui.R as Media3R

@UnstableApi
abstract class AnimityPlayer : AppCompatActivity(), Player.Listener {

    private lateinit var player: ExoPlayer
    private lateinit var dataPassed: AnimityPlayerData
    private lateinit var binding: AnimityPlayerBinding

    val playBackState: Flow<PlayBackState> = callbackFlow {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                trySend(playbackState.toPlayBackState())
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                Log.e("AnimityPlayer", "onPlayerError: ${error.message}")
            }
        }
        player.addListener(listener)
        awaitClose { player.removeListener(listener) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = AnimityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideSystemUi()
        dataPassed = getPlaybackData()
        setupPlayer()
    }

    @OptIn(UnstableApi::class)
    private fun setupPlayer() {
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(getAudioAttributes(), true)
            .setTrackSelector(getTrackSelector())
            .setSeekForwardIncrementMs(getSeekSettings().first)
            .setSeekBackIncrementMs(getSeekSettings().second)
            .build()
        player.addListener(this)

        when (val type = dataPassed.playbackType) {
            is PlaybackType.Local -> preparePlayer(type.localStream)
            is PlaybackType.Internet -> showStreamChooserDialog(
                type.streamList,
                ::preparePlayer
            )
        }
    }

    private fun preparePlayer(uri: String) {
        val mediaItem = MediaItem.fromUri(uri)
        binding.videoView.player = player
        player.isCommandAvailable(Player.COMMAND_SET_REPEAT_MODE)
        binding.videoView.getImageButton(Media3R.id.exo_fullscreen) {
            Log.e("AnimityPlayer", "FullScreen Clicked")
        }
        player.setMediaSource(
            DefaultMediaSourceFactory(this)
                .setDataSourceFactory(getCacheDataSourceFactory())
                .createMediaSource(mediaItem)
        )
        player.prepare()
        player.playWhenReady = true
    }


    private fun showStreamChooserDialog(
        streamList: List<Pair<String, String>>,
        selectedStream: (String) -> Unit
    ) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.choose_stream))
            .setSingleChoiceItems(streamList.map { it.first }.toTypedArray(), 0) { _, which ->
                selectedStream((streamList.map { it.second }[which]))
            }
            .setCancelable(false)
            .show()
    }

    val currentProgress = flow<Long> {
        while (true) {
            emit(player.currentPosition)
            delay(1000)
        }
    }.flowOn(Dispatchers.Main)

    private fun getPlaybackData(): AnimityPlayerData {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(ANIME_DATA, AnimityPlayerData::class.java)
                ?: throw IllegalArgumentException("Intent must have AnimityPlayerData")
        } else {
            intent.getParcelableExtra(ANIME_DATA)
                ?: throw IllegalArgumentException("Intent must have AnimityPlayerData")
        }
    }

    abstract fun getCacheDataSourceFactory(): DataSource.Factory

    open fun getSeekSettings(): Pair<Long, Long> {
        return Pair(10_000, 10_000)
    }

    @OptIn(UnstableApi::class)
    abstract fun getTrackSelector(): TrackSelector

    abstract fun getAudioAttributes(): AudioAttributes


    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == STATE_READY) {
            binding.videoView.useController = true
            binding.videoView.controllerAutoShow = true
        }
    }

    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.videoView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.removeListener(this)
        player.release()
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Log.e("AnimityPlayer", "onPlayerError: ${error.errorCodeName}")
    }

    companion object {
        const val ANIME_DATA = "anime_data"
    }
}