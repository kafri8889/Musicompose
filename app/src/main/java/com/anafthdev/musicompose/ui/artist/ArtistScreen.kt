package com.anafthdev.musicompose.ui.artist

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.ui.MusicControllerViewModel
import com.anafthdev.musicompose.ui.components.MusicItem
import com.anafthdev.musicompose.ui.components.PlayAllSongButton
import com.anafthdev.musicompose.ui.theme.*

@OptIn(ExperimentalUnitApi::class)
@Composable
fun ArtistScreen(
    artistName: String,
    artistViewModel: ArtistViewModel,
    musicControllerViewModel: MusicControllerViewModel,
    navController: NavHostController
) {

    val currentMusicPlayed by musicControllerViewModel.currentMusicPlayed.observeAsState(initial = Music.unknown)
    val filteredMusicList by artistViewModel.filteredMusicList.observeAsState(initial = emptyList())

    var hasNavigate by remember { mutableStateOf(false) }

    if (!hasNavigate) {
        artistViewModel.filterMusic(artistName)
        true.also { hasNavigate = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = if (isSystemInDarkTheme()) background_dark else background_light,
                title = {
                    Text(
                        text = artistName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = typographyDmSans().body1.copy(
                            fontSize = TextUnit(16f, TextUnitType.Sp),
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .padding(end = 8.dp)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            tint = if (isSystemInDarkTheme()) white else black,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(bottom = 64.dp)
        ) {
            item {
                PlayAllSongButton(
                    musicList = filteredMusicList,
                    musicControllerViewModel = musicControllerViewModel
                )
            }

            items(filteredMusicList) { music ->
                MusicItem(
                    music = music,
                    isMusicPlayed = currentMusicPlayed.audioID == music.audioID,
                    onClick = {
                        if (currentMusicPlayed.audioID != music.audioID) {
                            musicControllerViewModel.play(music.audioID)
                            musicControllerViewModel.getPlaylist()
                        }
                    }
                )
            }
        }
    }
}
