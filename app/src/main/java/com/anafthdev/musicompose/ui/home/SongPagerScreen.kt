package com.anafthdev.musicompose.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.data.MusicomposeDestination
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.ui.MusicControllerViewModel
import com.anafthdev.musicompose.ui.components.PlayAllSongButton
import com.anafthdev.musicompose.ui.theme.*

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalUnitApi::class
)
@Composable
fun SongPagerScreen(
    homeViewModel: HomeViewModel,
    navController: NavHostController,
    musicControllerViewModel: MusicControllerViewModel,
) {

    val musicList by homeViewModel.musicList.observeAsState(initial = emptyList())
    val currentMusicPlayed by musicControllerViewModel.currentMusicPlayed.observeAsState(initial = Music.unknown)

    if (musicList.isEmpty()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_audio_square_outlined),
                tint = if (isSystemInDarkTheme()) background_content_dark else background_content_light,
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
            )

            Text(
                text = stringResource(id = R.string.no_song),
                style = typographyDmSans().body1.copy(
                    fontSize = TextUnit(18f, TextUnitType.Sp),
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .padding(top = 8.dp)
            )

            Button(
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    hoveredElevation = 0.dp,
                    focusedElevation = 0.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = sunset_orange.copy(alpha = 0.4f),
                    contentColor = Color.Transparent
                ),
                onClick = {
                    navController.navigate(MusicomposeDestination.ScanMusic.route) {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 12.dp,
                        start = 32.dp,
                        end = 32.dp
                    )
            ) {
                Text(
                    text = stringResource(id = R.string.scan_local_songs),
                    style = typographySkModernist().body1.copy(
                        color = sunset_orange,
                        fontSize = TextUnit(16f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                )
            }

        }
    }

    CompositionLocalProvider(
        LocalOverScrollConfiguration provides null
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(
                    fraction = if (musicList.isNotEmpty()) 1f else 0f
                )
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
