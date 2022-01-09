package com.anafthdev.musicompose.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.anafthdev.musicompose.model.MediaPlayerState

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
            MediaPlayerState.ACTION_PLAY -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(
                        serviceIntent.setAction(MediaPlayerState.ACTION_PLAY)
                    )
                } else context.startService(
                    serviceIntent.setAction(MediaPlayerState.ACTION_PLAY)
                )
            }
            MediaPlayerState.ACTION_PAUSE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(
                        serviceIntent.setAction(MediaPlayerState.ACTION_PAUSE)
                    )
                } else context.startService(
                    serviceIntent.setAction(MediaPlayerState.ACTION_PAUSE)
                )
            }
            MediaPlayerState.ACTION_NEXT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(
                        serviceIntent.setAction(MediaPlayerState.ACTION_NEXT)
                    )
                } else context.startService(
                    serviceIntent.setAction(MediaPlayerState.ACTION_NEXT)
                )
            }
            MediaPlayerState.ACTION_PREVIOUS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(
                        serviceIntent.setAction(MediaPlayerState.ACTION_PREVIOUS)
                    )
                } else context.startService(
                    serviceIntent.setAction(MediaPlayerState.ACTION_PREVIOUS)
                )
            }
        }
    }

}