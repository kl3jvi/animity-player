package io.animity.anime_player.playback

enum class PlayBackState {
    IDLE,
    BUFFERING,
    READY,
    ENDED,
}

fun Int.toPlayBackState(): PlayBackState {
    return when (this) {
        1 -> PlayBackState.IDLE
        2 -> PlayBackState.BUFFERING
        3 -> PlayBackState.READY
        4 -> PlayBackState.ENDED
        else -> throw IllegalArgumentException("Invalid PlayBackState")
    }
}
