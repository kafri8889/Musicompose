package com.anafthdev.musicompose.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.anafthdev.musicompose.utils.AppUtils.toast

const val MediaPlayerReceiver_Tag = "MediaPlayerReceiver"

class MediaPlayerReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, MediaPlayerService::class.java)

        when (
            requireNotNull(
                intent.action,
                lazyMessage = { "$MediaPlayerReceiver_Tag:null action" }
            )
        ) {
            MediaPlayerManager.ACTION_PLAY_PAUSE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(
                        serviceIntent.setAction(MediaPlayerManager.ACTION_PLAY_PAUSE)
                    )
                } else context.startService(
                    serviceIntent.setAction(MediaPlayerManager.ACTION_PLAY_PAUSE)
                )
            }
            MediaPlayerManager.ACTION_NEXT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(
                        serviceIntent.setAction(MediaPlayerManager.ACTION_NEXT)
                    )
                } else context.startService(
                    serviceIntent.setAction(MediaPlayerManager.ACTION_NEXT)
                )
            }
            MediaPlayerManager.ACTION_PREVIOUS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(
                        serviceIntent.setAction(MediaPlayerManager.ACTION_PREVIOUS)
                    )
                } else context.startService(
                    serviceIntent.setAction(MediaPlayerManager.ACTION_PREVIOUS)
                )
            }
        }
    }

}