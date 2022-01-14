package com.anafthdev.musicompose.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.common.MediaPlayerReceiver
import com.anafthdev.musicompose.model.MediaPlayerState
import com.anafthdev.musicompose.ui.MainActivity

object NotificationUtil {

    private const val channelID = "player_notification"
    private const val channelName = "Media Player"

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannel(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            channel.setAllowBubbles(false)
        }

        channel.enableLights(false)
        channel.setBypassDnd(true)

        notificationManager.createNotificationChannel(channel)
    }

    @Suppress("deprecation")
    fun foregroundNotification(context: Context): Notification {
        val pi = PendingIntent.getActivity(
            context,
            123,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, channelID)
                .setContentTitle("Musicompose")
                .setContentText("Musicompose running in the foreground")
                .setContentIntent(pi)
                .build()
        } else {
            Notification.Builder(context)
                .setContentTitle("Musicompose")
                .setContentText("Musicompose running in the foreground")
                .setContentIntent(pi)
                .build()
        }
    }

    @Suppress("deprecation")
    fun notificationMediaPlayer(
        context: Context,
        mediaStyle: Notification.MediaStyle,
        state: MediaPlayerState
    ): Notification {

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, channelID)
        } else Notification.Builder(context)

        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPI = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )



        val playPauseIntent = Intent(context, MediaPlayerReceiver::class.java)
            .setAction(
                if (state.isMusicPlayed) MediaPlayerState.ACTION_PAUSE else MediaPlayerState.ACTION_PLAY
            )
        val playPausePI = PendingIntent.getBroadcast(
            context,
            1,
            playPauseIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val playPauseAction = Notification.Action.Builder(
            Icon.createWithResource(
                context,
                if (state.isMusicPlayed) R.drawable.ic_pause_filled_rounded else R.drawable.ic_play_filled_rounded
            ),
            "PlayPause",
            playPausePI
        ).build()



        val previousIntent = Intent(context, MediaPlayerReceiver::class.java)
            .setAction(MediaPlayerState.ACTION_PREVIOUS)
        val previousPI = PendingIntent.getBroadcast(
            context,
            2,
            previousIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val previousAction = Notification.Action.Builder(
            Icon.createWithResource(context, R.drawable.ic_previous_filled_rounded),
            "Previous",
            previousPI
        ).build()



        val nextIntent = Intent(context, MediaPlayerReceiver::class.java)
            .setAction(MediaPlayerState.ACTION_NEXT)
        val nextPI = PendingIntent.getBroadcast(
            context,
            3,
            nextIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val nextAction = Notification.Action.Builder(
            Icon.createWithResource(context, R.drawable.ic_next_filled_rounded),
            "Previous",
            nextPI
        ).build()

        return builder
            .setStyle(mediaStyle)
            .setSmallIcon(R.drawable.ic_play_filled_rounded)
            .setOnlyAlertOnce(true)
            .addAction(previousAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .setContentIntent(contentPI)
            .build()
    }

}