package io.animity.anime_player.cast

import androidx.mediarouter.app.MediaRouteChooserDialogFragment
import androidx.mediarouter.app.MediaRouteControllerDialogFragment
import androidx.mediarouter.app.MediaRouteDialogFactory

class AnimityCastThemeFactory : MediaRouteDialogFactory() {
    override fun onCreateChooserDialogFragment(): MediaRouteChooserDialogFragment {
        return AnimityMediaRouterChooserDialogFragment()
    }

    override fun onCreateControllerDialogFragment(): MediaRouteControllerDialogFragment {
        return AnimityMediaRouteControllerDialogFragment()
    }
}