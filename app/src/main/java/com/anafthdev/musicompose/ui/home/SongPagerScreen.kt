package com.anafthdev.musicompose.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.ui.MusicControllerViewModel
import com.anafthdev.musicompose.ui.components.PlayAllSongButton

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalUnitApi::class
)
@Composable
fun SongPagerScreen(
    homeViewModel: HomeViewModel,
    musicControllerViewModel: MusicControllerViewModel,
) {

    val musicList by homeViewModel.musicList.observeAsState(initial = emptyList())
    val currentMusicPlayed by musicControllerViewModel.currentMusicPlayed.observeAsState(initial = Music.unknown)

    CompositionLocalProvider(
        LocalOverScrollConfiguration provides null
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp)
        ) {
            item {
                PlayAllSongButton(
                    musicList = musicList,
                    musicControllerViewModel = musicControllerViewModel
                )
            }

            items(musicList) { music ->
                com.anafthdev.musicompose.ui.components.MusicItem(
                    music = music,
                    isMusicPlayed = currentMusicPlayed.audioID == music.audioID,
                    onClick = {
                        musicControllerViewModel.play(music.audioID)
                        musicControllerViewModel.getPlaylist()
                    }
                )
            }
        }
    }
}
