package com.anafthdev.musicompose.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.anafthdev.musicompose.data.MusicomposeDestination
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.ui.MusicControllerViewModel
import com.anafthdev.musicompose.ui.components.AlbumItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumPagerScreen(
    homeViewModel: HomeViewModel,
    navController: NavHostController,
) {

    val albumList by homeViewModel.albumList.observeAsState(initial = emptyMap())

    homeViewModel.getAllAlbum()

    CompositionLocalProvider(
        LocalOverScrollConfiguration provides null
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp)
        ) {
            itemsIndexed(albumList.toList()) { i, musicPair ->
                AlbumItem(
                    musicList = musicPair.second,
                    onClick = {
                        val route = MusicomposeDestination.Album.createRoute(
                            albumList[albumList.keys.toList()[i]]?.get(0)?.albumID ?: Music.unknown.albumID
                        )

                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
