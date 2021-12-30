package com.anafthdev.musicompose.di

import com.anafthdev.musicompose.ui.MainActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        ApplicationModule::class
    ]
)
interface ApplicationComponent {

    fun inject(base: MainActivity)
}