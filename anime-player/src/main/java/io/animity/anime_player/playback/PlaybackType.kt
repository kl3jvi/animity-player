package io.animity.anime_player.playback

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class PlaybackType : Parcelable {
    @Parcelize
    data class Local(val localStream: String) : PlaybackType()

    @Parcelize
    data class Internet(val remoteStream: String) : PlaybackType()
}
