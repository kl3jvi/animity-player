package io.animity.anime_player.cast

import android.content.Context
import androidx.mediarouter.app.MediaRouteActionProvider

class CustomCastProvider(context: Context) : MediaRouteActionProvider(context) {
    init {
        dialogFactory = CustomCastThemeFactory()
    }
}