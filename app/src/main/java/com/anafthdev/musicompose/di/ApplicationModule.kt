package com.anafthdev.musicompose.di

import com.anafthdev.musicompose.MusicomposeApplication
import com.anafthdev.musicompose.common.AppDatastore
import com.anafthdev.musicompose.data.MusicRepository
import com.anafthdev.musicompose.ui.MusicControllerViewModel
import com.anafthdev.musicompose.ui.home.HomeViewModel
import com.anafthdev.musicompose.ui.scan_music.ScanMusicViewModel
import com.anafthdev.musicompose.ui.search.SearchViewModel
import com.anafthdev.musicompose.utils.DatabaseUtil
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: MusicomposeApplication) {

    @Singleton
    @Provides
    fun provideDatabaseUtil(): DatabaseUtil = DatabaseUtil.getInstance(application)

    @Singleton
    @Provides
    fun provideAppDatastore(): AppDatastore = AppDatastore.getInstance(application)

    @Singleton
    @Provides
    fun provideHomeViewModel(): HomeViewModel = HomeViewModel(
        MusicRepository(provideDatabaseUtil())
    )

    @Singleton
    @Provides
    fun provideScanMusicViewModel(): ScanMusicViewModel = ScanMusicViewModel(
        MusicRepository(provideDatabaseUtil())
    )

    @Singleton
    @Provides
    fun provideSearchViewModel(): SearchViewModel = SearchViewModel(
        MusicRepository(provideDatabaseUtil())
    )

    @Singleton
    @Provides
    fun provideMusicControllerViewModel(): MusicControllerViewModel = MusicControllerViewModel(
        application,
        MusicRepository(provideDatabaseUtil()),
        provideAppDatastore()
    )

}