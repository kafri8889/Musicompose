package com.anafthdev.musicompose.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.navigation.compose.rememberNavController
import androidx.palette.graphics.Palette
import coil.compose.rememberImagePainter
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.common.AppDatastore
import com.anafthdev.musicompose.data.MusicomposeDestination
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.ui.album.AlbumViewModel
import com.anafthdev.musicompose.ui.artist.ArtistViewModel
import com.anafthdev.musicompose.ui.components.IconButton
import com.anafthdev.musicompose.ui.components.Slider
import com.anafthdev.musicompose.ui.components.SliderDefaults
import com.anafthdev.musicompose.ui.home.HomeViewModel
import com.anafthdev.musicompose.ui.home.PlaylistList
import com.anafthdev.musicompose.ui.playlist.PlaylistViewModel
import com.anafthdev.musicompose.ui.scan_music.ScanMusicViewModel
import com.anafthdev.musicompose.ui.search.SearchViewModel
import com.anafthdev.musicompose.ui.theme.*
import com.anafthdev.musicompose.utils.ComposeUtils
import com.anafthdev.musicompose.utils.ComposeUtils.currentFraction
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.imePadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit

@SuppressWarnings("deprecation")
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

    val bsMusicInfoItem = listOf(
        stringResource(id = R.string.artist) to R.drawable.ic_profile,
        stringResource(id = R.string.album) to R.drawable.ic_cd,
        stringResource(id = R.string.add_to_playlist) to R.drawable.ic_music_playlist,
        stringResource(id = R.string.set_timer) to R.drawable.ic_timer
    )

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()
    val systemUiController = rememberSystemUiController()
    val navigationController = rememberNavController()
    val musicControllerState by musicControllerViewModel.musicControllerState.observeAsState(
        initial = MusicControllerViewModel.MusicControllerState(
            playlistScaffoldBottomSheetState = BottomSheetScaffoldState(
                drawerState = DrawerState(DrawerValue.Closed),
                bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed),
                snackbarHostState = SnackbarHostState()
            ),
            musicScaffoldBottomSheetState = BottomSheetScaffoldState(
                drawerState = DrawerState(DrawerValue.Closed),
                bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed),
                snackbarHostState = SnackbarHostState()
            ),
            modalBottomSheetMusicInfoState = ModalBottomSheetState(
                ModalBottomSheetValue.Hidden
            ),
        )
    )

    val currentMusicPlayed by musicControllerViewModel.currentMusicPlayed.observeAsState(initial = Music.unknown)
    val currentMusicPlayMode by musicControllerViewModel.playMode.observeAsState(initial = MusicControllerViewModel.MusicPlayMode.REPEAT_ON)
    val currentProgress by musicControllerViewModel.currentProgress.observeAsState(initial = 0f)
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

    Palette.Builder(
        run {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            context.contentResolver,
                            currentMusicPlayed.albumPath.toUri()
                        )
                    ).copy(Bitmap.Config.RGBA_F16, true)
                } else MediaStore.Images.Media.getBitmap(context.contentResolver, currentMusicPlayed.albumPath.toUri())
            } catch (e: FileNotFoundException) {
                Timber.e(e)
                return@run ContextCompat.getDrawable(context, R.drawable.ic_music_unknown)!!.toBitmap()
            }
        }
    ).generate { it?.let { palette ->
        dominantBackgroundColor = Color(palette.getDominantColor(primary_light.toArgb()))
    } }

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
            // BottomSheet Music info
            ModalBottomSheetLayout(
                sheetState = musicControllerState.modalBottomSheetMusicInfoState,
                sheetElevation = 8.dp,
                sheetShape = RoundedCornerShape(32.dp),
                scrimColor = pure_black.copy(alpha = 0.6f),
                sheetBackgroundColor = if (isSystemInDarkTheme()) background_content_dark else white,
                sheetContent = {

                    // BottomSheet Music info sheet content
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
                                    if (isSystemInDarkTheme) white.copy(alpha = 0.2f) else black.copy(
                                        alpha = 0.2f
                                    )
                                )
                                .align(Alignment.CenterHorizontally)
                        )

                        LazyColumn {
                            items(bsMusicInfoItem) { pair ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            indication = rememberRipple(color = Color.Transparent),
                                            interactionSource = MutableInteractionSource(),
                                            onClick = {
                                                when (pair.first) {
                                                    bsMusicInfoItem[0].first -> {
                                                        val route =
                                                            MusicomposeDestination.Screen.Artist.createRoute(
                                                                currentMusicPlayed.artist
                                                            )
                                                        navigationController.navigate(route) {
                                                            popUpTo(MusicomposeDestination.HomeScreen) {
                                                                saveState = false
                                                            }

                                                            restoreState = false
                                                            launchSingleTop = true
                                                        }

                                                        scope.launch {
                                                            musicControllerState.modalBottomSheetMusicInfoState.hide()
                                                            delay(500)
                                                            musicControllerState.musicScaffoldBottomSheetState.bottomSheetState.collapse()
                                                        }
                                                    }
                                                    bsMusicInfoItem[1].first -> {
                                                        val route =
                                                            MusicomposeDestination.Screen.Album.createRoute(
                                                                currentMusicPlayed.albumID
                                                            )
                                                        navigationController.navigate(route) {
                                                            popUpTo(MusicomposeDestination.HomeScreen) {
                                                                saveState = false
                                                            }

                                                            restoreState = false
                                                            launchSingleTop = true
                                                        }

                                                        scope.launch {
                                                            musicControllerState.modalBottomSheetMusicInfoState.hide()
                                                            delay(500)
                                                            musicControllerState.musicScaffoldBottomSheetState.bottomSheetState.collapse()
                                                        }
                                                    }
                                                    bsMusicInfoItem[2].first -> {

                                                    }
                                                    bsMusicInfoItem[3].first -> {

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
                                            bsMusicInfoItem[0].first -> {
                                                "${pair.first}: ${currentMusicPlayed.artist}"
                                            }
                                            bsMusicInfoItem[1].first -> {
                                                "${pair.first}: ${currentMusicPlayed.album}"
                                            }
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

                    // BottomSheet Music info sheet content ~
                },
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
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {

                            if (musicControllerState.playlistScaffoldBottomSheetState.bottomSheetState.isCollapsed or (musicControllerState.playlistScaffoldBottomSheetState.bottomSheetState.targetValue == BottomSheetValue.Collapsed)) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .alpha(1f - musicControllerState.playlistScaffoldBottomSheetState.currentFraction)
                                        .height(64.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 10.dp, bottom = 8.dp)
                                            .size(32.dp, 2.dp)
                                            .clip(RoundedCornerShape(100))
                                            .background(white.copy(alpha = 0.2f))
                                            .align(Alignment.CenterHorizontally)
                                    )

                                    Text(
                                        text = stringResource(id = R.string.playlist),
                                        style = typographyDmSans().body1.copy(
                                            color = white.copy(alpha = 0.6f),
                                            fontSize = TextUnit(16f, TextUnitType.Sp),
                                            textAlign = TextAlign.Center
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    )
                                }
                            }



                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                                    .alpha(musicControllerState.playlistScaffoldBottomSheetState.currentFraction)
                            ) {

                                Row(
                                    modifier = Modifier
                                        .weight(0.6f)
                                        .padding(start = 16.dp)
                                ) {
                                    Image(
                                        painter = rememberImagePainter(
                                            data = currentMusicPlayed.albumPath.toUri(),
                                            builder = {
                                                error(R.drawable.ic_music_unknown)
                                                placeholder(R.drawable.ic_music_unknown)
                                            }
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )

                                    Column(
                                        modifier = Modifier
                                            .padding(start = 8.dp, end = 8.dp)
                                    ) {
                                        Text(
                                            text = currentMusicPlayed.title,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = typographyDmSans().body1.copy(
                                                color = white,
                                                fontSize = TextUnit(12f, TextUnitType.Sp),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )

                                        Text(
                                            text = "${currentMusicPlayed.artist} • ${currentMusicPlayed.album}",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = typographySkModernist().body1.copy(
                                                color = white.copy(alpha = 0.7f),
                                                fontSize = TextUnit(11f, TextUnitType.Sp),
                                            ),
                                            modifier = Modifier
                                                .padding(top = 6.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .weight(0.4f)
                                        .padding(end = 16.dp)
                                ) {

                                    // Previous Button
                                    androidx.compose.material.IconButton(
                                        onClick = {
                                            musicControllerViewModel.previous()
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_previous_filled_rounded),
                                            tint = background_light,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(20.dp)
                                        )
                                    }

                                    // Play or Pause Button
                                    androidx.compose.material.IconButton(
                                        onClick = {
                                            if (isMusicPlayed) {
                                                musicControllerViewModel.pause()
                                            } else musicControllerViewModel.resume()
                                        }
                                    ) {
                                        AnimatedContent(
                                            targetState = isMusicPlayed,
                                            transitionSpec = {
                                                scaleIn(animationSpec = tween(300)) with
                                                        scaleOut(animationSpec = tween(200))
                                            }
                                        ) { target ->
                                            Icon(
                                                painter = painterResource(
                                                    id = if (target) R.drawable.ic_pause_filled_rounded else R.drawable.ic_play_filled_rounded
                                                ),
                                                tint = background_light,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(20.dp)
                                            )
                                        }
                                    }

                                    // Next Button
                                    androidx.compose.material.IconButton(
                                        onClick = {
                                            musicControllerViewModel.next()
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_next_filled_rounded),
                                            tint = background_light,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(20.dp)
                                        )
                                    }
                                }
                            }



                            CompositionLocalProvider(
                                LocalOverScrollConfiguration provides null
                            ) {
                                PlaylistList(
                                    items = musicPlayList,
                                    itemBackgroundColor = ComposeUtils.darkenColor(dominantBackgroundColor, 0.6f),
                                    currentMusicPlayed = currentMusicPlayed,
                                    musicControllerViewModel = musicControllerViewModel,
                                )
                            }
                        }

                        // BottomSheet Playlist sheet content
                    },
                ) {

                    // Music Screen
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ComposeUtils.darkenColor(dominantBackgroundColor, 0.7f))
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp)
                        ) {
                            Card(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .padding(top = 32.dp)
                                    .size(288.dp)
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                Image(
                                    painter = rememberImagePainter(
                                        data = currentMusicPlayed.albumPath.toUri(),
                                        builder = {
                                            error(R.drawable.ic_music_unknown)
                                            placeholder(R.drawable.ic_music_unknown)
                                        }
                                    ),
                                    contentDescription = null,
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier
                                        .fillMaxSize()
                                )
                            }



                            // Title, Artist, Favorite button, More button
                            Row(
                                modifier = Modifier
                                    .padding(top = 64.dp)
                                    .fillMaxWidth()
                            ) {

                                // Title, Artist,
                                Column(
                                    modifier = Modifier
                                        .weight(0.76f)
                                        .padding(end = 8.dp)
                                ) {

                                    // Title
                                    Text(
                                        text = currentMusicPlayed.title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = typographySkModernist().body1.copy(
                                            color = white,
                                            fontSize = TextUnit(16f, TextUnitType.Sp),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )

                                    // Artist
                                    Text(
                                        text = "${currentMusicPlayed.artist} • ${currentMusicPlayed.album}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = typographyDmSans().body1.copy(
                                            color= white.copy(alpha = 0.7f),
                                            fontSize = TextUnit(14f, TextUnitType.Sp),
                                            fontWeight = FontWeight.Normal
                                        ),
                                        modifier = Modifier
                                            .padding(top = 12.dp)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier
                                        .weight(0.24f)
                                ) {
                                    // Favorite button
                                    androidx.compose.material.IconButton(
                                        onClick = {
                                            musicControllerViewModel.setMusicFavorite(!isMusicFavorite)
                                        },
                                    ) {
                                        Image(
                                            painter = painterResource(
                                                id = if (isMusicFavorite) R.drawable.ic_favorite_selected else R.drawable.ic_favorite
                                            ),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(24.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(100))
                                            .border(
                                                width = 1.dp,
                                                color = Color(0xFFAAACAE),
                                                shape = RoundedCornerShape(100)
                                            )
                                            .clickable {
                                                scope.launch {
                                                    musicControllerState.modalBottomSheetMusicInfoState.show()
                                                }
                                            }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            tint = white,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .rotate(90f)
                                                .align(Alignment.Center)
                                        )
                                    }
                                }
                            }  // Title, Artist, Favorite button, More button ~



                            // Slider, Music duration, Current music duration
                            Column(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .fillMaxWidth()
                            ) {

                                // Slider
                                Slider(
                                    value = currentProgress,
                                    valueRange = 0f..TimeUnit.MILLISECONDS.toSeconds(currentMusicPlayed.duration).toFloat(),
                                    thumbRadius = 6.dp,
                                    onValueChange = { progress ->
                                        musicControllerViewModel.setProgress(progress)
                                    },
                                    onValueChangeFinished = {
                                        musicControllerViewModel.applyProgress()
                                    },
                                    colors = SliderDefaults.colors(
                                        activeTrackColor = ComposeUtils.lightenColor(dominantBackgroundColor, 0.6f),
                                        inactiveTrackColor = ComposeUtils.lightenColor(dominantBackgroundColor, 0.6f).copy(alpha = 0.24f),
                                        thumbColor = ComposeUtils.lightenColor(dominantBackgroundColor, 0.6f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )

                                // Music duration, Current music duration
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {

                                    // Music duration
                                    Text(
                                        text = "$musicDurationInMinute:${if (musicDurationInSecond > 9) musicDurationInSecond else "0$musicDurationInSecond"}",
                                        style = typographySkModernist().body1.copy(
                                            color = white,
                                            fontSize = TextUnit(14f, TextUnitType.Sp)
                                        ),
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                    )



                                    // Current music duration
                                    Text(
                                        text = "$currentMusicDurationInMinute:${if (currentMusicDurationInSecond > 9) currentMusicDurationInSecond else "0$currentMusicDurationInSecond"}",
                                        style = typographySkModernist().body1.copy(
                                            color = white,
                                            fontSize = TextUnit(14f, TextUnitType.Sp)
                                        ),
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(end = 8.dp)
                                    )
                                }
                            }  // Slider, Music duration, Current music duration ~



                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp)
                            ) {

                                androidx.compose.material.IconButton(
                                    onClick = {
                                        musicControllerViewModel.setPlayMode(
                                            when (currentMusicPlayMode) {
                                                MusicControllerViewModel.MusicPlayMode.REPEAT_OFF -> MusicControllerViewModel.MusicPlayMode.REPEAT_ON
                                                MusicControllerViewModel.MusicPlayMode.REPEAT_ON -> MusicControllerViewModel.MusicPlayMode.REPEAT_ONE
                                                MusicControllerViewModel.MusicPlayMode.REPEAT_ONE -> MusicControllerViewModel.MusicPlayMode.REPEAT_OFF
                                            }
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(0.1f)
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            id = when (currentMusicPlayMode) {
                                                MusicControllerViewModel.MusicPlayMode.REPEAT_OFF -> R.drawable.ic_repeate_off
                                                MusicControllerViewModel.MusicPlayMode.REPEAT_ON -> R.drawable.ic_repeate_music
                                                MusicControllerViewModel.MusicPlayMode.REPEAT_ONE -> R.drawable.ic_repeate_one
                                            }
                                        ),
                                        tint = white,
                                        contentDescription = null
                                    )
                                }



                                androidx.compose.material.IconButton(
                                    onClick = {

                                    },
                                    modifier = Modifier
                                        .weight(0.25f)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_previous_filled_rounded),
                                        tint = white,
                                        contentDescription = null
                                    )
                                }



                                IconButton(
                                    rippleRadius = 72.dp,
                                    onClick = {
                                        if (isMusicPlayed) {
                                            musicControllerViewModel.pause()
                                        } else musicControllerViewModel.resume()
                                    },
                                    modifier = Modifier
                                        .weight(0.3f, fill = false)
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(100))
                                        .background(
                                            ComposeUtils.darkenColor(
                                                dominantBackgroundColor,
                                                0.3f
                                            )
                                        )
                                ) {
                                    AnimatedContent(
                                        targetState = isMusicPlayed,
                                        transitionSpec = {
                                            scaleIn(animationSpec = tween(300)) with
                                                    scaleOut(animationSpec = tween(200))
                                        }
                                    ) { target ->
                                        Icon(
                                            painter = painterResource(
                                                id = if (target) R.drawable.ic_pause_filled_rounded else R.drawable.ic_play_filled_rounded
                                            ),
                                            tint = white,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(40.dp)
                                        )
                                    }
                                }



                                androidx.compose.material.IconButton(
                                    onClick = {

                                    },
                                    modifier = Modifier
                                        .weight(0.25f)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_next_filled_rounded),
                                        tint = white,
                                        contentDescription = null
                                    )
                                }



                                androidx.compose.material.IconButton(
                                    onClick = {
                                        musicControllerViewModel.muteVolume(!isVolumeMuted)
                                    },
                                    modifier = Modifier
                                        .weight(0.1f)
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            id = when {
                                                isVolumeMuted -> R.drawable.ic_volume_mute_cross
                                                !isVolumeMuted -> {
                                                    if (currentVolume <= (maxStreamMusicVolume / 2)) R.drawable.ic_volume_low
                                                    else R.drawable.ic_volume_high
                                                }
                                                else -> R.drawable.ic_volume_high
                                            }
                                        ),
                                        tint = white,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                }  // BottomSheet Playlist ~
            } // BottomSheet Music info ~
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
                    animationSpec = tween(1000),
                    initialOffsetY = { fullHeight -> fullHeight }
                ),
                exit = slideOutVertically(
                    animationSpec = tween(1000),
                    targetOffsetY = { fullHeight -> fullHeight }
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(
                            if (isSystemInDarkTheme()) background_content_dark else background_light
                        )
                        .clickable(
                            indication = null,
                            interactionSource = MutableInteractionSource(),
                            onClick = {
                                scope.launch {
                                    musicControllerState.musicScaffoldBottomSheetState.bottomSheetState.expand()
                                }
                            }
                        )
                        .align(Alignment.BottomCenter)
                ) {
                    Box(
                        contentAlignment = Alignment.TopStart,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Divider(
                            thickness = 1.dp,
                            color = ComposeUtils.lightenColor(background_content_dark, 0.3f),
                            modifier = Modifier
                                .fillMaxWidth()
                        )

                        LinearProgressIndicator(
                            color = sunset_orange,
                            backgroundColor = Color.Transparent,
                            progress = run {
                                val normalizedProgress = (currentProgress - 0f) / (TimeUnit.MILLISECONDS.toSeconds(currentMusicPlayed.duration) - 0f)

                                Timber.i("Normalized Progress: $normalizedProgress")
                                return@run normalizedProgress
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .weight(0.6f)
                                .padding(start = 16.dp)
                        ) {
                            Image(
                                painter = rememberImagePainter(
                                    data = currentMusicPlayed.albumPath.toUri(),
                                    builder = {
                                        error(R.drawable.ic_music_unknown)
                                        placeholder(R.drawable.ic_music_unknown)
                                    }
                                ),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )

                            Column(
                                modifier = Modifier
                                    .padding(start = 8.dp, end = 8.dp)
                            ) {
                                Text(
                                    text = currentMusicPlayed.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = typographyDmSans().body1.copy(
                                        fontSize = TextUnit(12f, TextUnitType.Sp),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )

                                Text(
                                    text = currentMusicPlayed.artist,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = typographySkModernist().body1.copy(
                                        color = typographySkModernist().body1.color.copy(alpha = 0.7f),
                                        fontSize = TextUnit(11f, TextUnitType.Sp),
                                    ),
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .weight(0.4f)
                                .padding(end = 16.dp)
                        ) {

                            // Previous Button
                            androidx.compose.material.IconButton(
                                onClick = {
                                    musicControllerViewModel.previous()
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_previous_filled_rounded),
                                    tint = if (isSystemInDarkTheme()) background_light else background_dark,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(20.dp)
                                )
                            }

                            // Play or Pause Button
                            androidx.compose.material.IconButton(
                                onClick = {
                                    if (isMusicPlayed) {
                                        musicControllerViewModel.pause()
                                    } else musicControllerViewModel.resume()
                                }
                            ) {
                                AnimatedContent(
                                    targetState = isMusicPlayed,
                                    transitionSpec = {
                                        scaleIn(animationSpec = tween(300)) with
                                                scaleOut(animationSpec = tween(200))
                                    }
                                ) { target ->
                                    Icon(
                                        painter = painterResource(
                                            id = if (target) R.drawable.ic_pause_filled_rounded else R.drawable.ic_play_filled_rounded
                                        ),
                                        tint = if (isSystemInDarkTheme()) background_light else background_dark,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(20.dp)
                                    )
                                }
                            }

                            // Next Button
                            androidx.compose.material.IconButton(
                                onClick = {
                                    musicControllerViewModel.next()
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_next_filled_rounded),
                                    tint = if (isSystemInDarkTheme()) background_light else background_dark,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(20.dp)
                                )
                            }
                        }
                    }
                }
            }  // Mini music player

        }
    }  // BottomSheet MusicScreen
}
