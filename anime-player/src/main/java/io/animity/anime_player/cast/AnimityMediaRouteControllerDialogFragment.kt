package io.animity.anime_player.cast

import android.content.Context
import android.os.Bundle
import androidx.mediarouter.app.MediaRouteControllerDialog
import androidx.mediarouter.app.MediaRouteControllerDialogFragment
import io.animity.anime_player.R

class AnimityMediaRouteControllerDialogFragment : MediaRouteControllerDialogFragment() {
    override fun onCreateControllerDialog(
        context: Context,
        savedInstanceState: Bundle?
    ): MediaRouteControllerDialog =
        MediaRouteControllerDialog(context, R.style.CustomControllerTheme)
}
