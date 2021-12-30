package com.anafthdev.musicompose.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.AudioManager
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
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
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.palette.graphics.Palette
import coil.compose.rememberImagePainter
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.common.AppDatastore
import com.anafthdev.musicompose.data.MusicomposeDestination
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.ui.MainActivity
import com.anafthdev.musicompose.ui.MusicControllerViewModel
import com.anafthdev.musicompose.ui.components.MusicItem
import com.anafthdev.musicompose.ui.components.PopupMenu
import com.anafthdev.musicompose.ui.components.SliderDefaults
import com.anafthdev.musicompose.ui.theme.*
import com.anafthdev.musicompose.utils.AppUtils
import com.anafthdev.musicompose.utils.AppUtils.toast
import com.anafthdev.musicompose.utils.ComposeUtils
import com.anafthdev.musicompose.utils.ComposeUtils.LifecycleEventListener
import com.anafthdev.musicompose.utils.ComposeUtils.currentFraction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.FileNotFoundException
import timber.log.Timber
import java.util.concurrent.TimeUnit

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalUnitApi::class
)
@Composable
fun HomeScreen(
    navController: NavHostController,
    musicControllerViewModel: MusicControllerViewModel,
    homeViewModel: HomeViewModel,
    datastore: AppDatastore
) {

    val context = LocalContext.current

    val dropdownMenuItems = listOf(
        stringResource(id = R.string.scan_local_songs),
        stringResource(id = R.string.sort_by)
    )

    val sortMusicItem = listOf(
        stringResource(id = R.string.date_added) to AppUtils.PreferencesValue.SORT_MUSIC_BY_DATE_ADDED,
        stringResource(id = R.string.song_name) to AppUtils.PreferencesValue.SORT_MUSIC_BY_NAME,
        stringResource(id = R.string.artist_name) to AppUtils.PreferencesValue.SORT_MUSIC_BY_ARTIST_NAME,
    )

    val bsMusicInfoItem = listOf(
        stringResource(id = R.string.artist) to R.drawable.ic_profile,
        stringResource(id = R.string.album) to R.drawable.ic_cd,
        stringResource(id = R.string.add_to_playlist) to R.drawable.ic_music_playlist,
        stringResource(id = R.string.set_timer) to R.drawable.ic_timer
    )

    val scope = rememberCoroutineScope()
    val modalBottomSheetSortOptionState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val modalBottomSheetMusicInfoState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val musicScaffoldBottomSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    )
    val playlistScaffoldBottomSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
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

    val musicList by homeViewModel.musicList.observeAsState(initial = emptyList())
    val sortMusicOption by datastore.getSortMusicOption.collectAsState(initial = AppUtils.PreferencesValue.SORT_MUSIC_BY_NAME)

    var maxStreamMusicVolume by remember { mutableStateOf(0) }

    var hasNavigate by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }
    var dominantBackgroundColor by remember { mutableStateOf(primary_light) }

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

    if (!hasNavigate) {
        maxStreamMusicVolume = (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager).getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        musicControllerViewModel.playLastMusic()
        (context as MainActivity).LifecycleEventListener {
            if (it == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    datastore.getSortMusicOption.collect { option ->
                        withContext(Dispatchers.Main) {
                            homeViewModel.getAllMusic(context, option)
                        }
                    }
                }
            }
        }
        true.also { hasNavigate = it }
    }

    BackHandler {
        when {
            modalBottomSheetSortOptionState.isVisible -> scope.launch {
                modalBottomSheetSortOptionState.hide()
            }
            musicScaffoldBottomSheetState.bottomSheetState.isExpanded -> scope.launch {
                musicScaffoldBottomSheetState.bottomSheetState.collapse()
            }
            else -> (context as MainActivity).finishAffinity()
        }
    }

    (context as MainActivity).window.statusBarColor = if (musicScaffoldBottomSheetState.bottomSheetState.isExpanded) {
        ComposeUtils.darkenColor(dominantBackgroundColor, 0.7f).toArgb()
    } else { if (isSystemInDarkTheme()) background_dark.toArgb() else background_light.toArgb() }

    // BottomSheet sort option
    ModalBottomSheetLayout(
        sheetElevation = 8.dp,
        sheetShape = RoundedCornerShape(32.dp),
        scrimColor = pure_black.copy(alpha = 0.6f),
        sheetBackgroundColor = if (isSystemInDarkTheme()) background_content_dark else white,
        sheetState = modalBottomSheetSortOptionState,
        sheetContent = {

            // BottomSheet sort option sheet content
            Column(
                modifier = Modifier
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.sort_by),
                    style = typographyDmSans().body1.copy(
                        fontSize = TextUnit(18f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp, top = 16.dp)
                )

                sortMusicItem.forEach { pair ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                datastore.setSortMusicOption(pair.second) {
                                    scope.launch { modalBottomSheetSortOptionState.hide() }
                                    homeViewModel.getAllMusic(context, pair.second)
                                }
                            }
                    ) {
                        Text(
                            text = pair.first,
                            style = typographySkModernist().body1.copy(
                                fontSize = TextUnit(16f, TextUnitType.Sp)
                            ),
                            modifier = Modifier
                                .wrapContentWidth(Alignment.Start)
                                .padding(top = 14.dp, bottom = 14.dp, start = 16.dp)
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        RadioButton(
                            selected = pair.second == sortMusicOption,
                            onClick = {
                                datastore.setSortMusicOption(pair.second) {
                                    scope.launch { modalBottomSheetSortOptionState.hide() }
                                    homeViewModel.getAllMusic(context, pair.second)
                                }
                            },
                            modifier = Modifier
                                .wrapContentSize(Alignment.CenterEnd)
                                .padding(end = 16.dp)
                        )
                    }
                }
            }

            // BottomSheet sort option sheet content ~
        }
    ) {

        // BottomSheet MusicScreen
        BottomSheetScaffold(
            scaffoldState = musicScaffoldBottomSheetState,
            sheetPeekHeight = 0.dp,
            sheetContent = {

                // BottomSheet MusicScreen sheet content
                // BottomSheet Music info
                ModalBottomSheetLayout(
                    sheetState = modalBottomSheetMusicInfoState,
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
                                    .size(32.dp, 4.dp)
                                    .clip(RoundedCornerShape(100))
                                    .background(white.copy(alpha = 0.6f))
                                    .align(Alignment.CenterHorizontally)
                            )

                            LazyColumn {
                                items(bsMusicInfoItem) { pair ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
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
                    }
                ) {
                    // BottomSheet Playlist
                    BottomSheetScaffold(
                        scaffoldState = playlistScaffoldBottomSheetState,
                        sheetBackgroundColor = ComposeUtils.darkenColor(dominantBackgroundColor, 0.6f),
                        sheetShape = RoundedCornerShape(
                            topStart = if (playlistScaffoldBottomSheetState.bottomSheetState.isExpanded) 0.dp else 32.dp,
                            topEnd = if (playlistScaffoldBottomSheetState.bottomSheetState.isExpanded) 0.dp else 32.dp
                        ),
                        sheetPeekHeight = 64.dp,
                        sheetContent = {

                            // BottomSheet Playlist sheet content
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {

                                if (playlistScaffoldBottomSheetState.bottomSheetState.isCollapsed or (playlistScaffoldBottomSheetState.bottomSheetState.targetValue == BottomSheetValue.Collapsed)) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .alpha(1f - playlistScaffoldBottomSheetState.currentFraction)
                                            .height(64.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 10.dp, bottom = 8.dp)
                                                .size(36.dp, 4.dp)
                                                .clip(RoundedCornerShape(100))
                                                .background(white.copy(alpha = 0.6f))
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
                                        .alpha(playlistScaffoldBottomSheetState.currentFraction)
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
                                        IconButton(
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
                                        IconButton(
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
                                        IconButton(
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
                        }
                    ) {

                        // Music Screen
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(ComposeUtils.darkenColor(dominantBackgroundColor, 0.7f))
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
                                            text = currentMusicPlayed.artist,
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
                                        IconButton(
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
                                                        modalBottomSheetMusicInfoState.show()
                                                    }
                                                }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
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
                                    com.anafthdev.musicompose.ui.components.Slider(
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
                                            activeTrackColor = dominantBackgroundColor,
                                            inactiveTrackColor = dominantBackgroundColor.copy(alpha = 0.24f),
                                            thumbColor = dominantBackgroundColor
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

                                    IconButton(
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



                                    IconButton(
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



                                    com.anafthdev.musicompose.ui.components.IconButton(
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



                                    IconButton(
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



                                    IconButton(
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
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        elevation = 0.dp,
                        backgroundColor = if (isSystemInDarkTheme()) background_dark else background_light
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = {
                                    navController.navigate(MusicomposeDestination.SearchScreen) {
                                        popUpTo(MusicomposeDestination.HomeScreen) {
                                            saveState = true
                                        }

                                        restoreState = true
                                        launchSingleTop = true
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null
                                )
                            }

                            IconButton(
                                onClick = {
                                    showDropdownMenu = !showDropdownMenu
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = null
                                )

                                PopupMenu(
                                    expanded = showDropdownMenu,
                                    items = dropdownMenuItems,
                                    shape = RoundedCornerShape(14.dp),
                                    elevation = 4.dp,
                                    backgroundColor = if (isSystemInDarkTheme()) background_content_dark else white,
                                    paddingValues = PaddingValues(end = 16.dp),
                                    verticalPadding = 14.dp,
                                    onItemClicked = { i ->
                                        when (i) {
                                            0 -> {
                                                navController.navigate(MusicomposeDestination.ScanMusicScreen) {
                                                    popUpTo(MusicomposeDestination.HomeScreen) {
                                                        saveState = true
                                                    }

                                                    restoreState = true
                                                    launchSingleTop = true
                                                }
                                            }
                                            1 -> scope.launch {
                                                modalBottomSheetSortOptionState.show()
                                            }
                                        }
                                    },
                                    onDismissRequest = {
                                        showDropdownMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {

                    CompositionLocalProvider(
                        LocalOverScrollConfiguration provides null
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            indication = rememberRipple(color = Color.Transparent),
                                            interactionSource = MutableInteractionSource(),
                                            onClick = {
                                                musicControllerViewModel.playAll(musicList)
                                            }
                                        )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .padding(bottom = 16.dp, start = 8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(100))
                                                .background(sunset_orange)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_play_filled_rounded),
                                                tint = white,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .align(Alignment.Center)
                                            )
                                        }

                                        Text(
                                            text = stringResource(id = R.string.play_all),
                                            style = typographyDmSans().body1.copy(
                                                fontSize = TextUnit(14f, TextUnitType.Sp),
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                        )

                                        Text(
                                            text = "${musicControllerViewModel.musicSize} ${stringResource(id = R.string.song).lowercase()}",
                                            style = typographyDmSans().body1.copy(
                                                color = typographyDmSans().body1.color.copy(alpha = 0.6f),
                                                fontSize = TextUnit(14f, TextUnitType.Sp),
                                                fontWeight = FontWeight.Light
                                            ),
                                            modifier = Modifier
                                                .padding(start = 8.dp)
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

                            items(musicList) { music ->
                                MusicItem(
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
                                        musicScaffoldBottomSheetState.bottomSheetState.expand()
                                    }
                                }
                            )
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
                                IconButton(
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
                                IconButton(
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
                                IconButton(
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
                }

            }  // Scaffold
        }  // BottomSheet MusicScreen
    }
}
