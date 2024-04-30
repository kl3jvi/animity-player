package io.animity.anime_player.cast

enum class PlayingType {
    CASTING,
    LOCAL;

    override fun toString(): String {
        return when (this) {
            CASTING -> "Casting"
            LOCAL -> "Local"
        }
    }
}