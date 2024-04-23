@file:UnstableApi

package io.animity.anime_player.downloader

import android.app.Notification
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.scheduler.Scheduler
import io.animity.anime_player.R

abstract class AnimeDownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    CHANNEL_ID,
    R.string.channel_name,
    R.string.anime_description,
) {
    open val notificationHelper: DownloadNotificationHelper by lazy {
        DownloadNotificationHelper(
            this,
            CHANNEL_ID,
        )
    }

    override fun getScheduler(): Scheduler = PlatformScheduler(this, JOB_SCHEDULER_SERVICE_JOB_ID)

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int,
    ): Notification = buildNotification(downloads, notMetRequirements)

    abstract fun buildNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int,
    ): Notification

    companion object {
        const val CHANNEL_ID = "ANIME_DOWNLOADER_CHANNEL_ID"
        const val FOREGROUND_NOTIFICATION_ID = 0x04152000
        const val JOB_SCHEDULER_SERVICE_JOB_ID = 0x08172000
    }
}
