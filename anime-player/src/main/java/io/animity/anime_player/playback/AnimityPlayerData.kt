package io.animity.anime_player.playback

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimityPlayerData(
    val animeId: String,
    val playbackType: PlaybackType,
    val episodeTitle: String,
    val episodeNumber: Int,
    val episodeThumb: String,
) : Parcelable
