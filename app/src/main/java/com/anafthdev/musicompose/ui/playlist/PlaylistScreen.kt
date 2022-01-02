package com.anafthdev.musicompose.ui.playlist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.data.MusicomposeDestination
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.model.Playlist
import com.anafthdev.musicompose.ui.MusicControllerViewModel
import com.anafthdev.musicompose.ui.components.MusicItem
import com.anafthdev.musicompose.ui.components.PlayAllSongButton
import com.anafthdev.musicompose.ui.components.TransparentButton
import com.anafthdev.musicompose.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(
    ExperimentalUnitApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun PlaylistScreen(
    playlistID: Int,
    navController: NavHostController,
    playlistViewModel: PlaylistViewModel,
    musicControllerViewModel: MusicControllerViewModel
) {

    val scope = rememberCoroutineScope()
    val deletePlaylistBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val playlist by playlistViewModel.playlist.observeAsState(initial = Playlist.unknown)

    var hasNavigate by remember { mutableStateOf(false) }

    if (!hasNavigate) {
        playlistViewModel.getPlaylist(playlistID)
        true.also { hasNavigate = it }
    }

    if (deletePlaylistBottomSheetState.isVisible) musicControllerViewModel.hideMiniMusicPlayer()
    else musicControllerViewModel.showMiniMusicPlayer()

    BackHandler {
        when {
            deletePlaylistBottomSheetState.isVisible -> scope.launch {
                deletePlaylistBottomSheetState.hide()
            }
            else -> navController.popBackStack()
        }
    }

    // BottomSheet delete
    ModalBottomSheetLayout(
        scrimColor = pure_black.copy(alpha = 0.6f),
        sheetState = deletePlaylistBottomSheetState,
        sheetElevation = 8.dp,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContent = {
            Text(
                text = stringResource(id = R.string.delete),
                style = typographyDmSans().body1.copy(
                    color = typographyDmSans().body1.color.copy(alpha = 0.4f),
                    fontSize = TextUnit(13f, TextUnitType.Sp)
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 24.dp)
            )

            TransparentButton(
                shape = RoundedCornerShape(0),
                onClick = {
                    navController.popBackStack().also {
                        musicControllerViewModel.deletePlaylist(playlist)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.ok),
                    style = typographyDmSans().body1.copy(
                        fontSize = TextUnit(16f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                )
            }

            Divider(
                color = background_content_dark,
                thickness = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
            )

            TransparentButton(
                shape = RoundedCornerShape(0),
                onClick = {
                    scope.launch {
                        deletePlaylistBottomSheetState.hide()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    style = typographyDmSans().body1.copy(
                        fontSize = TextUnit(16f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                )
            }

        }
    ) {
        ScreenContent(
            scope = scope,
            playlist = playlist,
            navController = navController,
            musicControllerViewModel = musicControllerViewModel,
            deletePlaylistBottomSheetState = deletePlaylistBottomSheetState
        )
    }

}

@OptIn(
    ExperimentalUnitApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class
)
@Composable
private fun ScreenContent(
    playlist: Playlist,
    scope: CoroutineScope,
    navController: NavHostController,
    musicControllerViewModel: MusicControllerViewModel,
    deletePlaylistBottomSheetState: ModalBottomSheetState
) {

    val context = LocalContext.current

    val currentMusicPlayed by musicControllerViewModel.currentMusicPlayed.observeAsState(initial = Music.unknown)

    var musicIndexForAlbumThumbnail by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = if (isSystemInDarkTheme()) background_dark else background_light,
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            tint = if (isSystemInDarkTheme()) white else black,
                            contentDescription = null
                        )
                    }
                },
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .padding(top = 16.dp, start = 16.dp)
            ) {

                Image(
                    painter = rememberImagePainter(
                        data = playlist.defaultImage ?: if (playlist.musicList.isNotEmpty()) {
                            playlist.musicList[musicIndexForAlbumThumbnail].albumPath.toUri()
                        } else ContextCompat.getDrawable(context, R.drawable.ic_music_unknown)!!,
                        builder = {
                            error(R.drawable.ic_music_unknown)
                            placeholder(R.drawable.ic_music_unknown)
                            listener(
                                onError = { _, _ ->
                                    if (musicIndexForAlbumThumbnail < playlist.musicList.size - 1) {
                                        musicIndexForAlbumThumbnail += 1
                                        data(
                                            run {
                                                if (playlist.musicList.isNotEmpty()) {
                                                    playlist.musicList[musicIndexForAlbumThumbnail].albumPath.toUri()
                                                } else ContextCompat.getDrawable(context, R.drawable.ic_music_unknown)!!
                                            }
                                        )
                                    } else data(ContextCompat.getDrawable(context, R.drawable.ic_music_unknown)!!)
                                }
                            )
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(24.dp))
                )



                // Playlist name, button edit, button delete
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 8.dp)
                        .weight(1f)
                ) {

                    // Playlist name
                    Text(
                        text = playlist.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = typographyDmSans().body1.copy(
                            fontSize = TextUnit(18f, TextUnitType.Sp),
                            fontWeight = FontWeight.ExtraBold
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (!playlist.isDefault) {
                        OutlinedButton(
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSystemInDarkTheme()) white else black
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                backgroundColor = Color.Transparent
                            ),
                            onClick = {

                            },
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    tint = if (isSystemInDarkTheme()) white else black,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(24.dp)
                                        .weight(0.5f, fill = false)
                                )

                                Text(
                                    text = "Edit",
                                    textAlign = TextAlign.Start,
                                    style = typographySkModernist().body1.copy(
                                        fontSize = TextUnit(14f, TextUnitType.Sp),
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .weight(0.5f)
                                )
                            }
                        }

                        Button(
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (isSystemInDarkTheme()) white else black
                            ),
                            onClick = {
                                scope.launch {
                                    deletePlaylistBottomSheetState.show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    tint = if (isSystemInDarkTheme()) black else white,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(24.dp)
                                        .weight(0.5f, fill = false)
                                )

                                Text(
                                    text = "Delete",
                                    textAlign = TextAlign.Start,
                                    style = typographySkModernist().body1.copy(
                                        color = if (isSystemInDarkTheme()) black else white,
                                        fontSize = TextUnit(14f, TextUnitType.Sp),
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .weight(0.5f)
                                )
                            }
                        }
                    }
                }  // Playlist name, button edit, button delete

            }

            CompositionLocalProvider(
                LocalOverScrollConfiguration provides null
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 64.dp, top = 8.dp)
                ) {
                    item {
                        PlayAllSongButton(
                            musicList = playlist.musicList,
                            musicControllerViewModel = musicControllerViewModel
                        )
                    }

                    items(playlist.musicList) { music ->
                        MusicItem(
                            music = music,
                            isMusicPlayed = currentMusicPlayed.audioID == music.audioID,
                            onClick = {
                                musicControllerViewModel.play(music.audioID)
                                musicControllerViewModel.getPlaylist()
                            }
                        )
                    }

                    item {
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
                                val route = MusicomposeDestination.SearchSong.createRoute(playlist.id ?: Playlist.unknown.id)
                                navController.navigate(route) {
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.add_song),
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
            }

        }
    }  // Scaffold ~
}
