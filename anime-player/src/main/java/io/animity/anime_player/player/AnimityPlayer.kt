package io.animity.anime_player.player

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.View
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.STATE_READY
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.TrackSelector
import androidx.media3.ui.TrackSelectionDialogBuilder
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import io.animity.anime_player.R
import io.animity.anime_player.cast.CustomCastThemeFactory
import io.animity.anime_player.cast.PlayingType
import io.animity.anime_player.databinding.AnimityPlayerBinding
import io.animity.anime_player.ext.getImageButton
import io.animity.anime_player.ext.getTextView
import io.animity.anime_player.playback.AnimityPlayerData
import io.animity.anime_player.playback.PlayBackState
import io.animity.anime_player.playback.PlaybackType
import io.animity.anime_player.playback.toPlayBackState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import androidx.media3.ui.R as Media3R

@UnstableApi
abstract class AnimityPlayer : AppCompatActivity(), Player.Listener {
    private lateinit var player: ExoPlayer
    open lateinit var dataPassed: AnimityPlayerData
    lateinit var binding: AnimityPlayerBinding
    open val currentSelectedStream: MutableMap<String, String?> = LinkedHashMap()

    private lateinit var castPlayer: CastPlayer
    private lateinit var castContext: CastContext

    private val castSessionAvailable = callbackFlow<PlayingType> {
        val listener = object : SessionAvailabilityListener {
            override fun onCastSessionAvailable() {
                Log.e("AnimityPlayer", "onCastSessionAvailable: ")
                trySend(PlayingType.CASTING)
            }

            override fun onCastSessionUnavailable() {
                Log.e("AnimityPlayer", "onCastSessionUnavailable: ")
                trySend(PlayingType.LOCAL)
            }
        }
        castContext = CastContext.getSharedInstance(this@AnimityPlayer)
        castPlayer = CastPlayer(castContext)
        castPlayer.setSessionAvailabilityListener(listener)
        awaitClose { castPlayer.setSessionAvailabilityListener(null) }
    }

    abstract val getMediaStreamHandler: (String) -> Flow<Map<String, String?>>

    val playBackState: Flow<PlayBackState> =
        callbackFlow {
            val listener =
                object : Player.Listener {
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

    val currentProgress = flow {
        while (true) {
            emit(player.currentPosition)
            delay(1000)
        }
    }.distinctUntilChanged()
        .flowOn(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = AnimityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideSystemUi()
        binding.videoView.getImageButton(Media3R.id.exo_settings, ::showQualityDialog)
        dataPassed = getPlaybackData()
        setupViews()
        setupPlayer()
        setupCast(false)
    }

    private fun setupViews() =
        with(binding.videoView) {
            getTextView(R.id.video_title, dataPassed.episodeTitle)
            getTextView(R.id.video_description, dataPassed.episodeNumber)
            getImageButton(R.id.exit_video_player) { finish() }
        }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isPictureInPictureEnabled()) {
            val aspectRatio = Rational(16, 9)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                enterPictureInPictureMode(
                    PictureInPictureParams
                        .Builder()
                        .setAspectRatio(aspectRatio)
                        .build(),
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                enterPictureInPictureMode()
            }
        }
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
            is PlaybackType.Internet -> showStreamChooserDialog(type.remoteStream, ::preparePlayer)
            is PlaybackType.Local -> preparePlayer(type.localStream)
        }
    }

    private fun preparePlayer(uri: String) {
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .build()
        binding.videoView.player = player
        player.setMediaSource(
            DefaultMediaSourceFactory(this)
                .setDataSourceFactory(getCacheDataSourceFactory())
                .createMediaSource(mediaItem),
        )
        player.prepare()
        player.seekTo(setPlaybackPosition())
        player.playWhenReady = true
    }

    private fun showStreamChooserDialog(
        stream: String,
        selectedStream: (String) -> Unit,
    ) {
        getMediaStreamHandler(stream)
            .onEach {
                val filteredMap = it.filterValues { value -> value != null }
                currentSelectedStream.putAll(filteredMap)
            }.catch { exception ->
                showErrorMessage(exception.message ?: "Error occurred while fetching media url")
            }.onCompletion {
                AlertDialog.Builder(this@AnimityPlayer)
                    .setTitle(getString(R.string.choose_stream))
                    .setSingleChoiceItems(
                        currentSelectedStream.keys.toTypedArray(),
                        0,
                    ) { dialog, which ->
                        selectedStream(currentSelectedStream.values.elementAt(which).orEmpty())
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()
            }.launchIn(lifecycleScope)
    }

    open fun getPlaybackData(): AnimityPlayerData {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(ANIME_DATA, AnimityPlayerData::class.java)
                ?: throw IllegalArgumentException("Intent must have AnimityPlayerData")
        } else {
            intent.getParcelableExtra(ANIME_DATA)
                ?: throw IllegalArgumentException("Intent must have AnimityPlayerData")
        }
    }

    abstract fun showErrorMessage(string: String)

    open fun setPlaybackPosition(): Long = 0L

    abstract fun getCacheDataSourceFactory(): DataSource.Factory

    abstract fun getTrackSelector(): TrackSelector

    abstract fun getAudioAttributes(): AudioAttributes

    abstract fun isPictureInPictureEnabled(): Boolean

    open fun getSeekSettings(): Pair<Long, Long> {
        return Pair(10_000, 10_000)
    }

    open fun setupCast(castFeatureEnabled: Boolean = false) {
        val castButton = binding.videoView.findViewById<MediaRouteButton>(R.id.exo_cast)
        castButton.isVisible = castFeatureEnabled
        castButton?.apply {
            CastButtonFactory.setUpMediaRouteButton(context, this)
            dialogFactory = CustomCastThemeFactory()
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == STATE_READY) {
            binding.videoView.useController = true
            binding.videoView.controllerAutoShow = true
        }
    }

    private fun showQualityDialog(view: View) {
        TrackSelectionDialogBuilder(
            view.context,
            getString(R.string.select_quality),
            player,
            C.TRACK_TYPE_VIDEO,
        ).setTrackNameProvider { format ->
            if (format.frameRate > 0f) format.height.toString() + "p" else format.height.toString() + "p (fps : N/A)"
        }.build().show()
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

    companion object {
        const val ANIME_DATA = "anime_data"
    }
}
