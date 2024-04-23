@file:UnstableApi

package io.animity.player

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.util.UnstableApi
import io.animity.anime_player.playback.AnimityPlayerData
import io.animity.anime_player.playback.PlaybackType
import io.animity.anime_player.player.AnimityPlayer.Companion.ANIME_DATA

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<Button>(R.id.button).setOnClickListener {
            val intent =
                Intent(this, TestPlayer::class.java).apply {
                    putExtra(
                        ANIME_DATA,
                        AnimityPlayerData(
                            animeId = "1",
                            playbackType =
                                PlaybackType.Internet("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"),
                            episodeTitle = "Naruto Shippuden Episode 1",
                            episodeNumber = "123123",
                            episodeThumb = "https://www.example.com/thumb.jpg",
                        ),
                    )
                }
            startActivity(intent)
        }
    }
}
