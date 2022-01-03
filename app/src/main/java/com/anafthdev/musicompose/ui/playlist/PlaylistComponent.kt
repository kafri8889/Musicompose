package com.anafthdev.musicompose.ui.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.data.MusicomposeDestination
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.model.Playlist
import com.anafthdev.musicompose.ui.MusicControllerViewModel
import com.anafthdev.musicompose.ui.components.TransparentButton
import com.anafthdev.musicompose.ui.theme.*
import com.anafthdev.musicompose.utils.AppUtils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalUnitApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun DeletePlaylistSheetContent(
    music: Music,
    playlist: Playlist,
    scope: CoroutineScope,
    navController: NavHostController,
    modalBottomSheetState: ModalBottomSheetState,
    musicControllerViewModel: MusicControllerViewModel,
    deleteType: PlaylistViewModel.PlaylistScreenDeleteType
)  {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
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
                if (deleteType == PlaylistViewModel.PlaylistScreenDeleteType.PLAYLIST) {
                    musicControllerViewModel.deletePlaylist(playlist) {
                        navController.popBackStack()
                    }
                } else musicControllerViewModel.deleteMusicFromPlaylist(music, playlist) {
                    scope.launch {
                        modalBottomSheetState.hide()
                    }
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
            color = if (isSystemInDarkTheme()) {
                background_content_light.copy(alpha = 0.4f)
            } else background_content_dark.copy(alpha = 0.4f),
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
        )

        TransparentButton(
            shape = RoundedCornerShape(0),
            onClick = {
                scope.launch {
                    modalBottomSheetState.hide()
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
}

@OptIn(
    ExperimentalUnitApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun PlaylistMoreOptionSheetContent(
    music: Music,
    scope: CoroutineScope,
    navController: NavHostController,
    playlistViewModel: PlaylistViewModel,
    modalBottomSheetState: ModalBottomSheetState
) {

    val moreOptionItems = listOf(
        stringResource(id = R.string.artist) to R.drawable.ic_profile,
        stringResource(id = R.string.album) to R.drawable.ic_cd,
        stringResource(id = R.string.delete) to R.drawable.ic_trash,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .padding(top = 12.dp, bottom = 24.dp)
                .size(32.dp, 2.dp)
                .clip(RoundedCornerShape(100))
                .background(
                    if (isSystemInDarkTheme()) white.copy(alpha = 0.2f) else black.copy(
                        alpha = 0.2f
                    )
                )
                .align(Alignment.CenterHorizontally)
        )

        LazyColumn {
            items(moreOptionItems) { pair ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = rememberRipple(color = Color.Transparent),
                            interactionSource = MutableInteractionSource(),
                            onClick = {
                                when (pair.first) {
                                    moreOptionItems[0].first -> {
                                        val route =
                                            MusicomposeDestination.Artist.createRoute(music.artist)
                                        scope.launch {
                                            modalBottomSheetState.hide()
                                            delay(100)
                                            navController.navigate(route) {
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                    moreOptionItems[1].first -> {
                                        val route =
                                            MusicomposeDestination.Album.createRoute(music.albumID)
                                        scope.launch {
                                            modalBottomSheetState.hide()
                                            delay(100)
                                            navController.navigate(route) {
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                    moreOptionItems[2].first -> {
                                        playlistViewModel.setSheetStateContent(
                                            PlaylistViewModel.PlaylistScreenSheetStateContent.DeletePlaylistSheetContent
                                        )

                                        playlistViewModel.setDeleteType(
                                            PlaylistViewModel.PlaylistScreenDeleteType.MUSIC
                                        )
                                    }
                                }
                            }
                        )
                        .padding(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = pair.second),
                        tint = if (isSystemInDarkTheme()) white else background_dark,
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                    )

                    Text(
                        overflow = TextOverflow.Ellipsis,
                        text = when (pair.first) {
                            moreOptionItems[0].first -> music.artist
                            moreOptionItems[1].first -> music.album
                            else -> pair.first
                        },
                        style = typographySkModernist().body1.copy(
                            fontSize = TextUnit(14f, TextUnitType.Sp),
                        ),
                        modifier = Modifier
                            .padding(start = 16.dp)
                    )
                }
            }
        }
    }

}





@OptIn(
    ExperimentalUnitApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class,
)
@Composable
fun ChangePlaylistNameSheetContent(
    playlist: Playlist,
    scope: CoroutineScope,
    modalBottomSheetState: ModalBottomSheetState,
    musicControllerViewModel: MusicControllerViewModel
) {

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var playlistName by remember { mutableStateOf(playlist.name) }
    val textFieldPlaylistNameFocusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 10.dp, bottom = 8.dp)
                .size(32.dp, 2.dp)
                .clip(RoundedCornerShape(100))
                .background(white.copy(alpha = 0.2f))
                .align(Alignment.CenterHorizontally)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 24.dp)
        ) {
            IconButton(
                onClick = {
                    scope.launch {
                        modalBottomSheetState.hide()
                        musicControllerViewModel.showMiniMusicPlayer()
                    }
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_x_mark),
                    tint = if (isSystemInDarkTheme()) white else black,
                    contentDescription = null,
                    modifier = Modifier
                        .size(14.dp)
                )
            }

            Text(
                text = stringResource(id = R.string.rename_playlist),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = typographyDmSans().body1.copy(
                    fontSize = TextUnit(16f, TextUnitType.Sp),
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(1f)
            )

            IconButton(
                onClick = {
                    if (playlistName.isNotBlank()) {
                        musicControllerViewModel.updatePlaylist(
                            playlist = playlist.apply {
                                name = playlistName
                            },
                            action = {
                                scope.launch {
                                    keyboardController?.hide()
                                    modalBottomSheetState.hide()
                                    musicControllerViewModel.showMiniMusicPlayer()
                                }
                            }
                        )
                    } else context.getString(R.string.playlist_name_cannot_be_empty).toast(context)
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    tint = if (isSystemInDarkTheme()) white else black,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                )
            }
        }

        TextField(
            value = playlistName,
            singleLine = true,
            textStyle = typographySkModernist().body1,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                }
            ),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = sunset_orange,
                cursorColor = sunset_orange
            ),
            onValueChange = { s ->
                if (playlistName.length < 25) playlistName = s
            },
            label = {
                Text(
                    text = stringResource(id = R.string.enter_playlist_name),
                    style = typographyDmSans().body1.copy(
                        color = sunset_orange
                    ),
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 32.dp)
                .focusRequester(textFieldPlaylistNameFocusRequester)
        )
    }
}
