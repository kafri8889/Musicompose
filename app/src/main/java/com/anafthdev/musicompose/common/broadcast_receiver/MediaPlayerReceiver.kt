package com.anafthdev.musicompose.common.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.anafthdev.musicompose.utils.AppUtils.toast

class MediaPlayerReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            intent?.action?.toast(it)
        }
    }

}