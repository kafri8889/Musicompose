package com.anafthdev.musicompose

import android.app.Application
import com.anafthdev.musicompose.di.ApplicationComponent
import com.anafthdev.musicompose.di.ApplicationModule
import com.anafthdev.musicompose.di.DaggerApplicationComponent

class MusicomposeApplication: Application() {

    val appComponent: ApplicationComponent = DaggerApplicationComponent.builder()
        .applicationModule(ApplicationModule(this))
        .build()
}