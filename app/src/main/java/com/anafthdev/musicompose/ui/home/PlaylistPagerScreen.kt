package com.anafthdev.musicompose.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.anafthdev.musicompose.data.MusicomposeDestination
import com.anafthdev.musicompose.ui.MusicControllerViewModel
import com.anafthdev.musicompose.ui.components.PlaylistItem

@Composable
fun PlaylistPagerScreen(
    homeViewModel: HomeViewModel,
    navController: NavHostController,
) {

    val playlist by homeViewModel.playlist.observeAsState(initial = emptyList())

    homeViewModel.getAllPlaylist()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 64.dp)
    ) {
        items(playlist) { playlist ->
            PlaylistItem(
                playlist = playlist,
                onClick = {
                    navController.navigate(MusicomposeDestination.PlaylistScreen) {
                        popUpTo(MusicomposeDestination.HomeScreen) {
                            saveState = false
                        }

                        restoreState = false
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
