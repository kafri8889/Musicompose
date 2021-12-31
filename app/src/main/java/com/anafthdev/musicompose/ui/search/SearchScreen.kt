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
import androidx.compose.material.icons.rounded.KeyboardArrowRight
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
import com.anafthdev.musicompose.data.MusicomposeDestination
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.ui.MusicControllerViewModel
import com.anafthdev.musicompose.ui.components.AlbumItem
import com.anafthdev.musicompose.ui.components.MusicItem
import com.anafthdev.musicompose.ui.components.TransparentButton
import com.anafthdev.musicompose.ui.theme.*

@OptIn(
    ExperimentalUnitApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun SearchScreen(
    navController: NavHostController,
    searchViewModel: SearchViewModel,
    musicControllerViewModel: MusicControllerViewModel
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    val currentMusicPlayed by musicControllerViewModel.currentMusicPlayed.observeAsState(initial = Music.unknown)
    val filteredMusic by searchViewModel.filteredMusic.observeAsState(initial = emptyList())
    val filteredArtist by searchViewModel.filteredArtist.observeAsState(initial = emptyList())
    val filteredAlbum by searchViewModel.filteredAlbum.observeAsState(initial = emptyList())

    var query by remember { mutableStateOf("") }
    var hasNavigate by remember { mutableStateOf(false) }
    val searchTextFieldFocusRequester = remember { FocusRequester() }

    val albumList = filteredAlbum.groupBy { it.album }

    if (!hasNavigate) {
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
                                navController.navigate(MusicomposeDestination.HomeScreen) {
                                    popUpTo(0) {
                                        saveState = false
                                    }

                                    restoreState = false
                                    launchSingleTop = true
                                }
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
                        onClick = {
                            musicControllerViewModel.play(music.audioID)
                        },
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                    )
                }

                item {
                    if (filteredArtist.isNotEmpty()) {

                        if (filteredMusic.isNotEmpty()) {
                            Divider(
                                color = background_content_dark,
                                thickness = 1.4.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.artist),
                                style = typographyDmSans().body1.copy(
                                    fontSize = TextUnit(16f, TextUnitType.Sp),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 14.dp, top = 16.dp, bottom = 8.dp)
                            )

                            Text(
                                text = "(${filteredArtist.size})",
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

                items(filteredArtist) { music ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
                    ) {
                        Text(
                            text = music.artist,
                            style = typographySkModernist().body1.copy(
                                fontSize = TextUnit(16f, TextUnitType.Sp),
                                fontWeight = FontWeight.SemiBold
                            ),
                        )

                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                        )

                        IconButton(
                            onClick = {
                                val route = "${
                                    MusicomposeDestination.ArtistScreen
                                }/${
                                    music.artist
                                }"
                                navController.navigate(route) {
                                    popUpTo(MusicomposeDestination.HomeScreen) {
                                        saveState = false
                                    }

                                    restoreState = false
                                    launchSingleTop = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowRight,
                                tint = background_content_dark,
                                contentDescription = null
                            )
                        }
                    }
                }

                item {
                    if (albumList.isNotEmpty()) {

                        if (filteredMusic.isNotEmpty() and filteredArtist.isNotEmpty()) {
                            Divider(
                                color = background_content_dark,
                                thickness = 1.4.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.album),
                                style = typographyDmSans().body1.copy(
                                    fontSize = TextUnit(16f, TextUnitType.Sp),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 14.dp, top = 16.dp, bottom = 8.dp)
                            )

                            Text(
                                text = "(${albumList.size})",
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

                items(albumList.size) { i ->
                    AlbumItem(
                        musicList = albumList[albumList.keys.toList()[i]]!!,
                        onClick = {
                            val route = "${
                                MusicomposeDestination.AlbumScreen
                            }/${
                                albumList[albumList.keys.toList()[i]]?.get(0)?.albumID ?: Music.unknown.albumID
                            }"
                            navController.navigate(route) {
                                popUpTo(MusicomposeDestination.HomeScreen) {
                                    saveState = false
                                }

                                restoreState = false
                                launchSingleTop = true
                            }
                        }
                    )
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                    )
                }

            }
        }
    }
}
