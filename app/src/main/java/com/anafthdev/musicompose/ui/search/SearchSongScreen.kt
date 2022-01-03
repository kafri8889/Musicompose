package com.anafthdev.musicompose.ui.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.model.Playlist
import com.anafthdev.musicompose.ui.MusicControllerViewModel
import com.anafthdev.musicompose.ui.components.MusicItem
import com.anafthdev.musicompose.ui.components.TransparentButton
import com.anafthdev.musicompose.ui.theme.*

@OptIn(
    ExperimentalUnitApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun SearchSongScreen(
    playlistID: Int,
    navController: NavHostController,
    searchViewModel: SearchViewModel,
    musicControllerViewModel: MusicControllerViewModel
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    val playlist by searchViewModel.playlist.observeAsState(initial = Playlist.unknown)
    val filteredMusic by searchViewModel.filteredMusic.observeAsState(initial = emptyList())
    val currentMusicPlayed by musicControllerViewModel.currentMusicPlayed.observeAsState(initial = Music.unknown)

    var query by remember { mutableStateOf("") }
    var hasNavigate by remember { mutableStateOf(false) }
    val musicListInPlaylist = remember { mutableStateListOf<Music>() }
    val searchTextFieldFocusRequester = remember { FocusRequester() }

    if (!hasNavigate) {
        searchViewModel.getPlaylist(playlistID) {
            musicListInPlaylist.addAll(playlist.musicList)
        }
        LaunchedEffect(Unit) {
            searchTextFieldFocusRequester.requestFocus()
        }
        true.also { hasNavigate = it }
    }

    searchViewModel.filter(query)

    Scaffold(
        topBar = {
            TopAppBar(
                elevation = 0.dp,
                backgroundColor = if (isSystemInDarkTheme()) background_dark else background_light
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        TextField(
                            value = query,
                            singleLine = true,
                            onValueChange = { s ->
                                query = s
                            },
                            trailingIcon = {
                                if (query.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            query = ""
                                        }
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.x_mark_outlined_filled),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(16.dp)
                                        )
                                    }
                                }
                            },
                            placeholder = {
                                Text(
                                    text = stringResource(id = R.string.search_placeholder),
                                    style = typographySkModernist().body1.copy(
                                        color = background_content_dark
                                    )
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                }
                            ),
                            colors = TextFieldDefaults.textFieldColors(
                                backgroundColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(0.5f)
                                .padding(end = 8.dp)
                                .focusRequester(searchTextFieldFocusRequester)
                        )

                        Divider(
                            color = background_content_dark,
                            modifier = Modifier
                                .weight(0.01f, fill = false)
                                .size(1.dp, 16.dp)
                        )

                        TransparentButton(
                            indication = rememberRipple(color = Color.Transparent),
                            onClick = {
                                navController.popBackStack()
                            },
                            modifier = Modifier
                                .weight(0.18f)
                                .padding(start = 8.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.cancel),
                                style = typographySkModernist().body1.copy(
                                    fontSize = TextUnit(12f, TextUnitType.Sp),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Divider(
                        color = background_content_dark,
                        thickness = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }
    ) {
        CompositionLocalProvider(
            LocalOverScrollConfiguration provides null
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(bottom = 64.dp)
            ) {

                item {
                    if (filteredMusic.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.song),
                                style = typographyDmSans().body1.copy(
                                    fontSize = TextUnit(16f, TextUnitType.Sp),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 14.dp, top = 16.dp, bottom = 8.dp)
                            )

                            Text(
                                text = "(${filteredMusic.size})",
                                style = typographyDmSans().body1.copy(
                                    color = typographyDmSans().body1.color.copy(alpha = 0.6f),
                                    fontSize = TextUnit(14f, TextUnitType.Sp),
                                    fontWeight = FontWeight.Normal
                                ),
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 14.dp, top = 16.dp, bottom = 16.dp)
                            )
                        }
                    }
                }

                items(filteredMusic) { music ->
                    MusicItem(
                        music = music,
                        isMusicPlayed = currentMusicPlayed.audioID == music.audioID,
                        showImage = false,
                        showDuration = false,
                        showTrailingIcon = true,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (musicListInPlaylist.contains(music)) musicListInPlaylist.remove(music)
                                    else musicListInPlaylist.add(music)

                                    searchViewModel.updatePlaylist(playlist.apply { musicList = musicListInPlaylist })
                                }
                            ) {
                                Icon(
                                    imageVector = if (musicListInPlaylist.contains(music)) Icons.Rounded.Check else Icons.Rounded.Add,
                                    tint = if (isSystemInDarkTheme()) background_light else background_dark,
                                    contentDescription = null
                                )
                            }
                        },
                        onClick = {
                            if (musicListInPlaylist.contains(music)) musicListInPlaylist.remove(music)
                            else musicListInPlaylist.add(music)

                            searchViewModel.updatePlaylist(playlist.apply { musicList = musicListInPlaylist })
                        },
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                    )
                }

            }
        }
    }
}
