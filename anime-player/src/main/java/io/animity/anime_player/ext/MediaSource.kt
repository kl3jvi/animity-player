@file:OptIn(UnstableApi::class)

package io.animity.anime_player.ext

import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import io.animity.anime_player.playback.PlaybackType

fun DefaultMediaSourceFactory.createMediaSourceBasedOnPlaybackType(
    playbackType: PlaybackType,
    playLocally: () -> MediaItem,
    playInternet: () -> MediaItem,
): MediaSource {
    return when (playbackType) {
        is PlaybackType.Local -> createMediaSource(playLocally())
        is PlaybackType.Internet -> createMediaSource(playInternet())
    }
}
