package io.animity.anime_player.ext

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.isVisible
import androidx.media3.ui.PlayerView

fun PlayerView.getButton(
    id: Int,
    clickListener: (View) -> Unit,
): AppCompatImageButton? {
    return findViewById<AppCompatImageButton?>(id).also {
        it?.setOnClickListener(clickListener)
    }
}

fun PlayerView.getTextView(
    id: Int,
    text: String,
): TextView? {
    val textView = findViewById<TextView?>(id)
    if (text.isEmpty()) {
        textView.isVisible = true
        return textView
    }
    return findViewById<TextView?>(id).also {
        it.text = text
    }
}

fun PlayerView.getImageButton(
    id: Int,
    clickListener: (View) -> Unit,
): ImageButton {
    val view =
        findViewById<ImageButton>(id)
            ?: throw IllegalArgumentException("View with id $id not found")
    return view.apply {
        setOnClickListener(clickListener)
    }
}
