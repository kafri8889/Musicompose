package com.anafthdev.musicompose.ui.components.musicompose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.model.MusicControllerState
import com.anafthdev.musicompose.model.Playlist
import com.anafthdev.musicompose.ui.MusicControllerViewModel
import com.anafthdev.musicompose.ui.theme.*
import com.anafthdev.musicompose.utils.AppUtils.containBy
import com.anafthdev.musicompose.utils.AppUtils.indexOf
import com.anafthdev.musicompose.utils.AppUtils.move
import com.anafthdev.musicompose.utils.AppUtils.removeBy
import com.anafthdev.musicompose.utils.AppUtils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalUnitApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun MusicScreenAddToPlaylistSheetContent(
    scope: CoroutineScope,
    currentMusicPlayed: Music,
    musicControllerState: MusicControllerState,
    musicControllerViewModel: MusicControllerViewModel
) {

    val context = LocalContext.current

    val playlistList = remember { mutableStateListOf<Playlist>() }

    musicControllerViewModel.getAllPlaylist { mPlaylistList ->
        playlistList.clear()
        playlistList.addAll(
            mPlaylistList
                .move(
                    fromIndex = mPlaylistList.indexOf { it.id == Playlist.favorite.id },
                    toIndex = 0
                )
                .removeBy { it.id == Playlist.justPlayed.id }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            IconButton(
                onClick = {
                    scope.launch {
                        musicControllerState.addToPlaylistModalBottomSheetState.hide()
                        delay(200)
                        musicControllerState.musicMoreOptionModalBottomSheetState.show()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    tint = if (isSystemInDarkTheme()) white else black,
                    contentDescription = null
                )
            }

            Text(
                text = stringResource(id = R.string.add_to_playlist),
                style = typographyDmSans().body1.copy(
                    fontSize = TextUnit(16f, TextUnitType.Sp),
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .padding(start = 8.dp)
            )
        }

        LazyColumn {

            items(playlistList) { playlist ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (playlist.id == Playlist.favorite.id) {
                                musicControllerViewModel.setMusicFavorite(true)
                                context.getString(R.string.successfully_added).toast(context)
                                scope.launch {
                                    delay(400)
                                    musicControllerState.addToPlaylistModalBottomSheetState.hide()
                                }
                            } else {
                                if (!playlist.musicList.containBy { it.audioID == currentMusicPlayed.audioID }) {
                                    musicControllerViewModel.updatePlaylist(
                                        playlist = playlist.apply {
                                            musicList = ArrayList(musicList).apply {
                                                add(currentMusicPlayed)
                                            }
                                        },
                                        action = {
                                            context.getString(R.string.successfully_added).toast(context)
                                            scope.launch {
                                                musicControllerState.addToPlaylistModalBottomSheetState.hide()
                                            }
                                        }
                                    )
                                } else {
                                    context.getString(R.string.added).toast(context)
                                    scope.launch {
                                        musicControllerState.addToPlaylistModalBottomSheetState.hide()
                                    }
                                }
                            }
                        }
                        .padding(24.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            id = when (playlist.id) {
                                Playlist.favorite.id -> R.drawable.ic_favorite_outlined
                                else -> R.drawable.ic_music_playlist
                            }
                        ),
                        tint = if (isSystemInDarkTheme()) white else background_dark,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                    )

                    Text(
                        overflow = TextOverflow.Ellipsis,
                        text = playlist.name,
                        style = typographySkModernist().body1.copy(
                            fontSize = TextUnit(14f, TextUnitType.Sp),
                        ),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                )
            }
        }
    }
}
