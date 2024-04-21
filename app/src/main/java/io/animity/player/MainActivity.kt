@file:UnstableApi

package io.animity.player

import android.content.Intent
import android.os.Bundle
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

        val intent = Intent(this, TestPlayer::class.java).apply {
            putExtra(
                ANIME_DATA,
                AnimityPlayerData(
                    animeId = "1",
                    playbackType = PlaybackType.Local(
                        "http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8",
                    ),
                    episodeTitle = "Naruto Shippuden Episode 1",
                    episodeNumber = 1,
                    episodeThumb = "https://www.example.com/thumb.jpg"
                )
            )
        }
        startActivity(intent)
    }


}