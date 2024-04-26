@file:OptIn(UnstableApi::class)

package io.animity.player

import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.cronet.CronetDataSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.TrackSelector
import com.google.android.material.snackbar.Snackbar
import io.animity.anime_player.player.AnimityPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.chromium.net.CronetEngine
import org.intellij.lang.annotations.Language
import org.json.JSONObject
import java.io.File
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

@Language("JSON")
var mediaJSON = """{ "categories" : [ { "name" : "Movies",
    "videos" : [
    { "description" : "Big Buck Bunny tells the story of a giant rabbit with a heart bigger than himself. When one sunny day three rodents rudely harass him, something snaps... and the rabbit ain't no bunny anymore! In the typical cartoon tradition he prepares the nasty rodents a comical revenge.\n\nLicensed under the Creative Commons Attribution license\nhttp://www.bigbuckbunny.org",
        "sources" : [ "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4" ],
        "subtitle" : "By Blender Foundation",
        "thumb" : "images/BigBuckBunny.jpg",
        "title" : "Big Buck Bunny"
    },
    { "description" : "The first Blender Open Movie from 2006",
        "sources" : [ "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4" ],
        "subtitle" : "By Blender Foundation",
        "thumb" : "images/ElephantsDream.jpg",
        "title" : "Elephant Dream"
    },
    { "description" : "HBO GO now works with Chromecast -- the easiest way to enjoy online video on your TV. For when you want to settle into your Iron Throne to watch the latest episodes. For $35.\nLearn how to use Chromecast with HBO GO and more at google.com/chromecast.",
        "sources" : [ "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4" ],
        "subtitle" : "By Google",
        "thumb" : "images/ForBiggerBlazes.jpg",
        "title" : "For Bigger Blazes"
    },
    { "description" : "Introducing Chromecast. The easiest way to enjoy online video and music on your TV—for when Batman's escapes aren't quite big enough. For $35. Learn how to use Chromecast with Google Play Movies and more at google.com/chromecast.",
        "sources" : [ "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4" ],
        "subtitle" : "By Google",
        "thumb" : "images/ForBiggerEscapes.jpg",
        "title" : "For Bigger Escape"
    },
    { "description" : "Introducing Chromecast. The easiest way to enjoy online video and music on your TV. For $35.  Find out more at google.com/chromecast.",
        "sources" : [ "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4" ],
        "subtitle" : "By Google",
        "thumb" : "images/ForBiggerFun.jpg",
        "title" : "For Bigger Fun"
    },
    { "description" : "Introducing Chromecast. The easiest way to enjoy online video and music on your TV—for the times that call for bigger joyrides. For $35. Learn how to use Chromecast with YouTube and more at google.com/chromecast.",
        "sources" : [ "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4" ],
        "subtitle" : "By Google",
        "thumb" : "images/ForBiggerJoyrides.jpg",
        "title" : "For Bigger Joyrides"
    },
    { "description" :"Introducing Chromecast. The easiest way to enjoy online video and music on your TV—for when you want to make Buster's big meltdowns even bigger. For $35. Learn how to use Chromecast with Netflix and more at google.com/chromecast.",
        "sources" : [ "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4" ],
        "subtitle" : "By Google",
        "thumb" : "images/ForBiggerMeltdowns.jpg",
        "title" : "For Bigger Meltdowns"
    },
    { "description" : "Sintel is an independently produced short film, initiated by the Blender Foundation as a means to further improve and validate the free/open source 3D creation suite Blender. With initial funding provided by 1000s of donations via the internet community, it has again proven to be a viable development model for both open 3D technology as for independent animation film.\nThis 15 minute film has been realized in the studio of the Amsterdam Blender Institute, by an international team of artists and developers. In addition to that, several crucial technical and creative targets have been realized online, by developers and artists and teams all over the world.\nwww.sintel.org",
        "sources" : [ "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4" ],
        "subtitle" : "By Blender Foundation",
        "thumb" : "images/Sintel.jpg",
        "title" : "Sintel"
    },
    { "description" : "Smoking Tire takes the all-new Subaru Outback to the highest point we can find in hopes our customer-appreciation Balloon Launch will get some free T-shirts into the hands of our viewers.",
        "sources" : [ "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4" ],
        "subtitle" : "By Garage419",
        "thumb" : "images/SubaruOutbackOnStreetAndDirt.jpg",
        "title" : "Subaru Outback On Street And Dirt"
    },
    { "description" : "Tears of Steel was realized with crowd-funding by users of the open source 3D creation tool Blender. Target was to improve and test a complete open and free pipeline for visual effects in film - and to make a compelling sci-fi film in Amsterdam, the Netherlands.  The film itself, and all raw material used for making it, have been released under the Creatieve Commons 3.0 Attribution license. Visit the tearsofsteel.org website to find out more about this, or to purchase the 4-DVD box with a lot of extras.  (CC) Blender Foundation - http://www.tearsofsteel.org",
        "sources" : [ "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4" ],
        "subtitle" : "By Blender Foundation",
        "thumb" : "images/TearsOfSteel.jpg",
        "title" : "Tears of Steel"
    },
    { "description" : "The Smoking Tire heads out to Adams Motorsports Park in Riverside, CA to test the most requested car of 2010, the Volkswagen GTI. Will it beat the Mazdaspeed3's standard-setting lap time? Watch and see...",
        "sources" : [ "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/VolkswagenGTIReview.mp4" ],
        "subtitle" : "By Garage419",
        "thumb" : "images/VolkswagenGTIReview.jpg",
        "title" : "Volkswagen GTI Review"
    },
    { "description" : "The Smoking Tire is going on the 2010 Bullrun Live Rally in a 2011 Shelby GT500, and posting a video from the road every single day! The only place to watch them is by subscribing to The Smoking Tire or watching at BlackMagicShine.com",
        "sources" : [ "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4" ],
        "subtitle" : "By Garage419",
        "thumb" : "images/WeAreGoingOnBullrun.jpg",
        "title" : "We Are Going On Bullrun"
    },
    { "description" : "The Smoking Tire meets up with Chris and Jorge from CarsForAGrand.com to see just how far $1,000 can go when looking for a car.The Smoking Tire meets up with Chris and Jorge from CarsForAGrand.com to see just how far $1,000 can go when looking for a car.",
        "sources" : [ "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4" ],
        "subtitle" : "By Garage419",
        "thumb" : "images/WhatCarCanYouGetForAGrand.jpg",
        "title" : "What care can you get for a grand?"
    }
    ]}]}"""

class TestPlayer : AnimityPlayer(), CoroutineScope {


    fun test() =
        flow {
            // parse media json to map title to sources first
            emit(
                mapOf(
                    "mp4" to "https://samples.mplayerhq.hu/F4V/H263_NM_f.mp4",
                    "mp4_2" to "https://samples.mplayerhq.hu/MPEG-4/CDR-Dinner_LAN_800k.mp4",
                    "mp4_3" to "https://samples.mplayerhq.hu/MPEG-4/test_qcif_200_aac_64.mp4",
                    "avi" to "https://samples.mplayerhq.hu/camera-dvr/nc_sample.avi",
                    "mkv" to "https://avtshare01.rz.tu-ilmenau.de/avt-vqdb-uhd-1/test_1/segments/bigbuck_bunny_8bit_40000kbps_2160p_60.0fps_vp9.mkv",
                ),
            )
        }.flowOn(Dispatchers.IO)

    override val getMediaStreamHandler: (String) -> Flow<Map<String, String>>
        get() = {
            test()
        }

    override fun showErrorMessage(string: String) {
        Snackbar.make(binding.root, string, Snackbar.LENGTH_LONG).show()
    }

    override fun getCacheDataSourceFactory(): DataSource.Factory {
        val databaseProvider = StandaloneDatabaseProvider(this)
        val cacheDirectory = File(filesDir, "downloads${Random.nextInt()}")
        val simpleCache = SimpleCache(cacheDirectory, NoOpCacheEvictor(), databaseProvider)
        val dataSource =
            CronetDataSource.Factory(
                CronetEngine.Builder(this).build(),
                Executors.newSingleThreadExecutor(),
            ).setUserAgent("Animity/1.0.0 (Linux;Android 11) ExoPlayerLib/2.14.1")
                .setConnectionTimeoutMs(10_000)
                .setReadTimeoutMs(10_000)

        return CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(dataSource)
            .setCacheWriteDataSinkFactory(null)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    override fun getTrackSelector(): TrackSelector {
        return DefaultTrackSelector(this).apply {
            setParameters(buildUponParameters().clearVideoSizeConstraints())
        }.apply {
            buildUponParameters()
                .setMaxVideoSize(1, 1)
                .build()
        }
    }

    override fun getAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
    }

    override fun isPictureInPictureEnabled(): Boolean {
        return true
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}
