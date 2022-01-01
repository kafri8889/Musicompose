package com.anafthdev.musicompose.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.anafthdev.musicompose.common.AppDatastore
import com.anafthdev.musicompose.data.MusicomposeDestination
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.model.Playlist
import com.anafthdev.musicompose.ui.album.AlbumScreen
import com.anafthdev.musicompose.ui.album.AlbumViewModel
import com.anafthdev.musicompose.ui.artist.ArtistScreen
import com.anafthdev.musicompose.ui.artist.ArtistViewModel
import com.anafthdev.musicompose.ui.home.HomeScreen
import com.anafthdev.musicompose.ui.home.HomeViewModel
import com.anafthdev.musicompose.ui.playlist.PlaylistScreen
import com.anafthdev.musicompose.ui.playlist.PlaylistViewModel
import com.anafthdev.musicompose.ui.scan_music.ScanMusicScreen
import com.anafthdev.musicompose.ui.scan_music.ScanMusicViewModel
import com.anafthdev.musicompose.ui.search.SearchScreen
import com.anafthdev.musicompose.ui.search.SearchViewModel

@Composable
fun MusicomposeNavigation(
    navigationController: NavHostController,
    datastore: AppDatastore,
    homeViewModel: HomeViewModel,
    searchViewModel: SearchViewModel,
    scanMusicViewModel: ScanMusicViewModel,
    playlistViewModel: PlaylistViewModel,
    albumViewModel: AlbumViewModel,
    artistViewModel: ArtistViewModel,
    musicControllerViewModel: MusicControllerViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navigationController,
        startDestination = MusicomposeDestination.HomeScreen,
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {

        composable(MusicomposeDestination.Screen.Home.route) {
            HomeScreen(
                navController = navigationController,
                musicControllerViewModel = musicControllerViewModel,
                homeViewModel = homeViewModel,
                datastore = datastore
            )
        }

        composable(MusicomposeDestination.Screen.ScanMusic.route) {
            ScanMusicScreen(
                navController = navigationController,
                scanMusicViewModel = scanMusicViewModel,
                musicControllerViewModel = musicControllerViewModel
            )
        }

        composable(MusicomposeDestination.Screen.Search.route) {
            SearchScreen(
                navController = navigationController,
                searchViewModel = searchViewModel,
                musicControllerViewModel = musicControllerViewModel
            )
        }

        composable(
            route = MusicomposeDestination.Screen.Artist.route,
            arguments = listOf(
                navArgument("artistName") {
                    type = NavType.StringType
                }
            )
        ) { entry ->
            val artistName = entry.arguments?.getString("artistName") ?: Music.unknown.artist
            ArtistScreen(
                artistName = artistName,
                artistViewModel = artistViewModel,
                musicControllerViewModel = musicControllerViewModel,
                navController = navigationController
            )
        }

        composable(
            route = MusicomposeDestination.Screen.Album.route,
            arguments = listOf(
                navArgument("albumID") {
                    type = NavType.StringType
                }
            )
        ) { entry ->
            val albumID = entry.arguments?.getString("albumID") ?: Music.unknown.albumID
            AlbumScreen(
                albumID = albumID,
                albumViewModel = albumViewModel,
                musicControllerViewModel = musicControllerViewModel,
                navController = navigationController
            )
        }

        composable(
            route = MusicomposeDestination.Screen.Playlist.route,
            arguments = listOf(
                navArgument("playlistID") {
                    type = NavType.IntType
                }
            )
        ) { entry ->
            val playlistID = entry.arguments?.getInt("playlistID") ?: Playlist.unknown.id
            PlaylistScreen(
                playlistID = playlistID,
                playlistViewModel = playlistViewModel,
                musicControllerViewModel = musicControllerViewModel,
                navController = navigationController
            )
        }

    }
}
