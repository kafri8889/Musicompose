package com.anafthdev.musicompose.di

import com.anafthdev.musicompose.MusicomposeApplication
import com.anafthdev.musicompose.common.AppDatastore
import com.anafthdev.musicompose.data.MusicomposeRepository
import com.anafthdev.musicompose.ui.MusicControllerViewModel
import com.anafthdev.musicompose.ui.album.AlbumViewModel
import com.anafthdev.musicompose.ui.artist.ArtistViewModel
import com.anafthdev.musicompose.ui.home.HomeViewModel
import com.anafthdev.musicompose.ui.playlist.PlaylistViewModel
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
        application,
        MusicomposeRepository(provideDatabaseUtil())
    )

    @Singleton
    @Provides
    fun provideScanMusicViewModel(): ScanMusicViewModel = ScanMusicViewModel(
        MusicomposeRepository(provideDatabaseUtil())
    )

    @Singleton
    @Provides
    fun provideSearchViewModel(): SearchViewModel = SearchViewModel(
        MusicomposeRepository(provideDatabaseUtil())
    )

    @Singleton
    @Provides
    fun provideArtistViewModel(): ArtistViewModel = ArtistViewModel(
        MusicomposeRepository(provideDatabaseUtil())
    )

    @Singleton
    @Provides
    fun provideAlbumViewModel(): AlbumViewModel = AlbumViewModel(
        MusicomposeRepository(provideDatabaseUtil())
    )

    @Singleton
    @Provides
    fun providePlaylistViewModel(): PlaylistViewModel = PlaylistViewModel(
        MusicomposeRepository(provideDatabaseUtil())
    )

    @Singleton
    @Provides
    fun provideMusicControllerViewModel(): MusicControllerViewModel = MusicControllerViewModel(
        application,
        MusicomposeRepository(provideDatabaseUtil()),
        provideAppDatastore()
    )

}