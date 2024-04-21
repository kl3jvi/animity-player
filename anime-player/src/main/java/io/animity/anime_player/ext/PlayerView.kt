package io.animity.anime_player.ext

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.media3.ui.PlayerView

fun PlayerView.getButton(id: Int, clickListener: (View) -> Unit): AppCompatImageButton? {
    return findViewById<AppCompatImageButton?>(id).also {
        it?.setOnClickListener(clickListener)
    }
}

fun PlayerView.getTextView(id: Int, text: String): TextView? {
    return findViewById<TextView?>(id).also {
        it.text = text
    }
}

fun PlayerView.getImageButton(id: Int, clickListener: (View) -> Unit): AppCompatImageButton? {
    return findViewById<AppCompatImageButton?>(id).also {
        it?.setOnClickListener(clickListener)
    }
}