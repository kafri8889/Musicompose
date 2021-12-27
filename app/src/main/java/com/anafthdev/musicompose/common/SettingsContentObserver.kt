package com.anafthdev.musicompose.common

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper

class SettingsContentObserver(
    private val onChange: () -> Unit
): ContentObserver(Handler(Looper.getMainLooper())) {

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        onChange()
    }
}