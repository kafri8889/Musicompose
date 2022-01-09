package com.anafthdev.musicompose

import android.app.Application
import android.os.Build
import com.anafthdev.musicompose.di.ApplicationComponent
import com.anafthdev.musicompose.di.ApplicationModule
import com.anafthdev.musicompose.di.DaggerApplicationComponent
import com.anafthdev.musicompose.utils.NotificationUtil

class MusicomposeApplication: Application() {

    val appComponent: ApplicationComponent = DaggerApplicationComponent.builder()
        .applicationModule(ApplicationModule(this))
        .build()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtil.createChannel(this)
        }
    }

}