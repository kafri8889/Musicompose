package com.anafthdev.musicompose.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.anafthdev.musicompose.common.MediaPlayerReceiver
import com.anafthdev.musicompose.model.MediaPlayerState

object AlarmUtil {

    fun setTimer(context: Context, durationInMs: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + durationInMs,
            pauseMusicPendingIntent(context)
        )
    }

    fun cancelTimer(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(pauseMusicPendingIntent(context))
    }

    private fun pauseMusicPendingIntent(context: Context): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            138,
            Intent(context, MediaPlayerReceiver::class.java).apply {
                action = MediaPlayerState.ACTION_PAUSE
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )
    }
}