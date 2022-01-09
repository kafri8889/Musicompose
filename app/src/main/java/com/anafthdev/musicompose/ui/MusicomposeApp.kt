package com.anafthdev.musicompose.ui

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.compose.rememberNavController
import com.anafthdev.musicompose.common.AppDatastore
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.model.MusicControllerState
import com.anafthdev.musicompose.ui.album.AlbumViewModel
import com.anafthdev.musicompose.ui.artist.ArtistViewModel
import com.anafthdev.musicompose.ui.components.musicompose.*
import com.anafthdev.musicompose.ui.home.HomeViewModel
import com.anafthdev.musicompose.ui.playlist.PlaylistViewModel
import com.anafthdev.musicompose.ui.scan_music.ScanMusicViewModel
import com.anafthdev.musicompose.ui.search.SearchViewModel
import com.anafthdev.musicompose.ui.theme.*
import com.anafthdev.musicompose.utils.ComposeUtils
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalUnitApi::class
)
@Composable
fun MusicomposeApp(
    datastore: AppDatastore,
    homeViewModel: HomeViewModel,
    searchViewModel: SearchViewModel,
    scanMusicViewModel: ScanMusicViewModel,
    playlistViewModel: PlaylistViewModel,
    albumViewModel: AlbumViewModel,
    artistViewModel: ArtistViewModel,
    musicControllerViewModel: MusicControllerViewModel
) {

    val context = LocalContext.current

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    val systemUiController = rememberSystemUiController()
    val navigationController = rememberNavController()
    val musicControllerState by musicControllerViewModel.musicControllerState.observeAsState(
        initial = MusicControllerState.initial
    )

    val currentMusicPlayed by musicControllerViewModel.currentMusicPlayed.observeAsState(initial = Music.unknown)
    val currentMusicPlayMode by musicControllerViewModel.playMode.observeAsState(initial = MusicControllerViewModel.MusicPlayMode.REPEAT_ON)
    val currentProgress by musicControllerViewModel.currentProgress.observeAsState(initial = 0L)
    val currentVolume by musicControllerViewModel.currentVolume.observeAsState(initial = 0)
    val currentMusicDurationInMinute by musicControllerViewModel.currentMusicDurationInMinute.observeAsState(initial = 0)
    val currentMusicDurationInSecond by musicControllerViewModel.currentMusicDurationInSecond.observeAsState(initial = 0)
    val musicPlayList by musicControllerViewModel.playlist.observeAsState(initial = emptyList())
    val musicDurationInMinute by musicControllerViewModel.musicDurationInMinute.observeAsState(initial = 0)
    val musicDurationInSecond by musicControllerViewModel.musicDurationInSecond.observeAsState(initial = 0)
    val isMusicPlayed by musicControllerViewModel.isMusicPlayed.observeAsState(initial = false)
    val isMusicFavorite by musicControllerViewModel.isMusicFavorite.observeAsState(initial = false)
    val isVolumeMuted by musicControllerViewModel.isVolumeMuted.observeAsState(initial = false)
    val isMiniMusicPlayerHidden by musicControllerViewModel.isMiniMusicPlayerHidden.observeAsState(initial = false)

    var hasNavigate by remember { mutableStateOf(false) }
    var maxStreamMusicVolume by remember { mutableStateOf(0) }
    var dominantBackgroundColor by remember { mutableStateOf(primary_light) }

    if (!hasNavigate) {
        maxStreamMusicVolume = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager).getStreamMaxVolume(
            AudioManager.STREAM_MUSIC)
        musicControllerViewModel.playLastMusic()

        true.also { hasNavigate = it }
    }

    ComposeUtils.getDominantColor(
        context = context,
        uri = currentMusicPlayed.albumPath.toUri(),
        onGenerated = { palette ->
            dominantBackgroundColor = Color(palette.getDominantColor(primary_light.toArgb()))
        }
    )

    musicControllerViewModel.setMusicFavorite(isMusicFavorite)

    systemUiController.setSystemBarsColor(
        color = if (musicControllerState.musicScaffoldBottomSheetState.bottomSheetState.isExpanded) {
            ComposeUtils.darkenColor(dominantBackgroundColor, 0.7f)
        } else { if (isSystemInDarkTheme) background_dark else background_light },
        darkIcons = !isSystemInDarkTheme
    )

    // BottomSheet MusicScreen
    BottomSheetScaffold(
        scaffoldState = musicControllerState.musicScaffoldBottomSheetState,
        sheetPeekHeight = 0.dp,
        sheetContent = {

            // BottomSheet MusicScreen sheet content
            // BottomSheet Music more option
            ModalBottomSheetLayout(
                sheetState = musicControllerState.musicMoreOptionModalBottomSheetState,
                sheetElevation = 8.dp,
                sheetShape = RoundedCornerShape(32.dp),
                scrimColor = pure_black.copy(alpha = 0.6f),
                sheetBackgroundColor = if (isSystemInDarkTheme()) background_content_dark else background_content_light,
                sheetContent = {

                    // BottomSheet Music more option sheet content
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

                        MusicScreenMoreOptionsSheetContent(
                            scope = scope,
                            navController = navigationController,
                            currentMusicPlayed = currentMusicPlayed,
                            musicControllerState = musicControllerState
                        )
                    }

                    // BottomSheet Music more option sheet content ~
                },
            ) {

                // BottomSheet Add To Playlist
                ModalBottomSheetLayout(
                    sheetState = musicControllerState.addToPlaylistModalBottomSheetState,
                    sheetElevation = 8.dp,
                    sheetShape = RoundedCornerShape(32.dp),
                    scrimColor = pure_black.copy(alpha = 0.6f),
                    sheetBackgroundColor = if (isSystemInDarkTheme()) background_content_dark else background_content_light,
                    sheetContent = {

                        // BottomSheet Add To Playlist sheet content
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

                            MusicScreenAddToPlaylistSheetContent(
                                scope = scope,
                                currentMusicPlayed = currentMusicPlayed,
                                musicControllerState = musicControllerState,
                                musicControllerViewModel = musicControllerViewModel
                            )
                        }

                        // BottomSheet Add To Playlist sheet content ~
                    }
                ) {

                    // BottomSheet Set Timer
                    ModalBottomSheetLayout(
                        sheetState = musicControllerState.setTimerModalBottomSheetState,
                        sheetElevation = 8.dp,
                        sheetShape = RoundedCornerShape(32.dp),
                        scrimColor = pure_black.copy(alpha = 0.6f),
                        sheetBackgroundColor = if (isSystemInDarkTheme()) background_content_dark else background_content_light,
                        sheetContent = {

                            // BottomSheet Set Timer sheet content
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

                                MusicScreenMoreOptionsSheetContent(
                                    scope = scope,
                                    navController = navigationController,
                                    currentMusicPlayed = currentMusicPlayed,
                                    musicControllerState = musicControllerState
                                )
                            }

                            // BottomSheet Set Timer sheet content ~
                        }
                    ) {

                        // BottomSheet Playlist
                        BottomSheetScaffold(
                            scaffoldState = musicControllerState.playlistScaffoldBottomSheetState,
                            sheetBackgroundColor = ComposeUtils.darkenColor(dominantBackgroundColor, 0.6f),
                            sheetShape = RoundedCornerShape(
                                topStart = if (musicControllerState.playlistScaffoldBottomSheetState.bottomSheetState.isExpanded) 0.dp else 32.dp,
                                topEnd = if (musicControllerState.playlistScaffoldBottomSheetState.bottomSheetState.isExpanded) 0.dp else 32.dp
                            ),
                            sheetPeekHeight = 64.dp,
                            sheetContent = {

                                // BottomSheet Playlist sheet content
                                MusicScreenPlaylistSheetContent(
                                    dominantBackgroundColor = dominantBackgroundColor,
                                    isMusicPlayed = isMusicPlayed,
                                    currentMusicPlayed = currentMusicPlayed,
                                    musicPlayList = musicPlayList,
                                    musicControllerState = musicControllerState,
                                    musicControllerViewModel = musicControllerViewModel
                                )

                                // BottomSheet Playlist sheet content ~
                            },
                        ) {

                            // Music Screen
                            MusicScreenSheetContent(
                                scope = scope,
                                dominantBackgroundColor = dominantBackgroundColor,
                                maxStreamMusicVolume = maxStreamMusicVolume,
                                isMusicPlayed = isMusicPlayed,
                                isMusicFavorite = isMusicFavorite,
                                isVolumeMuted = isVolumeMuted,
                                currentVolume = currentVolume,
                                currentProgress = currentProgress,
                                currentMusicPlayed = currentMusicPlayed,
                                currentMusicPlayMode = currentMusicPlayMode,
                                currentMusicDurationInMinute = currentMusicDurationInMinute,
                                currentMusicDurationInSecond = currentMusicDurationInSecond,
                                musicDurationInMinute = musicDurationInMinute,
                                musicDurationInSecond = musicDurationInSecond,
                                musicControllerState = musicControllerState,
                                musicControllerViewModel = musicControllerViewModel
                            )

                        }  // BottomSheet Playlist ~
                    }  // BottomSheet Set Timer ~
                }  // BottomSheet Add To Playlist ~
            }  // BottomSheet Music info ~

            // BottomSheet MusicScreen sheet content ~
        },
        modifier = Modifier
            .systemBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            MusicomposeNavigation(
                navigationController = navigationController,
                datastore = datastore,
                homeViewModel = homeViewModel,
                searchViewModel = searchViewModel,
                scanMusicViewModel = scanMusicViewModel,
                playlistViewModel = playlistViewModel,
                albumViewModel = albumViewModel,
                artistViewModel = artistViewModel,
                musicControllerViewModel = musicControllerViewModel,
            )

            AnimatedVisibility(
                visible = !isMiniMusicPlayerHidden,
                enter = slideInVertically(
                    animationSpec = tween(800),
                    initialOffsetY = { fullHeight -> fullHeight }
                ),
                exit = slideOutVertically(
                    animationSpec = tween(800),
                    targetOffsetY = { fullHeight -> fullHeight }
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {
                MiniMusicPlayer(
                    scope = scope,
                    isMusicPlayed = isMusicPlayed,
                    currentProgress = currentProgress,
                    currentMusicPlayed = currentMusicPlayed,
                    musicControllerViewModel = musicControllerViewModel,
                    musicControllerState = musicControllerState
                )
            }  // Mini music player

        }
    }  // BottomSheet MusicScreen
}
