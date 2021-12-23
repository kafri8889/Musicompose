package com.anafthdev.musicompose.ui.home

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
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
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toColor
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.palette.graphics.Palette
import coil.compose.rememberImagePainter
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.data.AppDatastore
import com.anafthdev.musicompose.data.MusicomposeDestination
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.ui.MainActivity
import com.anafthdev.musicompose.ui.MusicControllerViewModel
import com.anafthdev.musicompose.ui.components.MusicItem
import com.anafthdev.musicompose.ui.components.PopupMenu
import com.anafthdev.musicompose.ui.theme.*
import com.anafthdev.musicompose.utils.AppUtils
import com.anafthdev.musicompose.utils.AppUtils.toast
import com.anafthdev.musicompose.utils.ComposeUtils.LifecycleEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val configuration = LocalConfiguration.current

    val dropdownMenuItems = listOf(
        stringResource(id = R.string.scan_local_songs),
        stringResource(id = R.string.sort_by)
    )

    val sortMusicItem = listOf(
        stringResource(id = R.string.date_added) to AppUtils.PreferencesValue.SORT_MUSIC_BY_DATE_ADDED,
        stringResource(id = R.string.song_name) to AppUtils.PreferencesValue.SORT_MUSIC_BY_NAME,
        stringResource(id = R.string.artist_name) to AppUtils.PreferencesValue.SORT_MUSIC_BY_ARTIST_NAME,
    )

    val scope = rememberCoroutineScope()
    val modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scaffoldBottomSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    )

    val currentMusicPlayed by musicControllerViewModel.currentMusicPlayed.observeAsState(initial = Music.unknown)
    val isMusicPlayed by musicControllerViewModel.isMusicPlayed.observeAsState(initial = false)

    val musicList by homeViewModel.musicList.observeAsState(initial = emptyList())
    val sortMusicOption by datastore.getSortMusicOption.collectAsState(initial = AppUtils.PreferencesValue.SORT_MUSIC_BY_NAME)

    var hasNavigate by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }
    var darkBackgroundColor by remember { mutableStateOf(black) }
    var dominantBackgroundColor by remember { mutableStateOf(primary_light) }
    var lightBackgroundColor by remember { mutableStateOf(white) }

    Palette.Builder(
        run {
            if (currentMusicPlayed.albumPath != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, currentMusicPlayed.albumPath!!.toUri())).copy(
                        Bitmap.Config.RGBA_F16,
                        true
                    )
                } else MediaStore.Images.Media.getBitmap(context.contentResolver, currentMusicPlayed.albumPath!!.toUri())
            } else ContextCompat.getDrawable(context, R.drawable.ic_music_unknown)!!.toBitmap()
        }
    ).generate { it?.let { palette ->
        darkBackgroundColor = Color(palette.getDarkMutedColor(black.toArgb()))
        dominantBackgroundColor = Color(palette.getDominantColor(primary_light.toArgb()))
        lightBackgroundColor = Color(palette.getDominantColor(white.toArgb()))
    } }

    if (!hasNavigate) {
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
            modalBottomSheetState.isVisible -> scope.launch {
                modalBottomSheetState.hide()
            }
            scaffoldBottomSheetState.bottomSheetState.isExpanded -> scope.launch {
                scaffoldBottomSheetState.bottomSheetState.collapse()
            }
            else -> (context as MainActivity).finishAffinity()
        }
    }

    // BottomSheet sort option
    ModalBottomSheetLayout(
        sheetElevation = 8.dp,
        sheetShape = RoundedCornerShape(32.dp),
        scrimColor = pure_black.copy(alpha = 0.6f),
        sheetBackgroundColor = if (isSystemInDarkTheme()) background_content_dark else white,
        sheetState = modalBottomSheetState,
        sheetContent = {
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
                                scope.launch {
                                    datastore.setSortMusicOption(pair.second)
                                    modalBottomSheetState.hide()
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
                                scope.launch {
                                    datastore.setSortMusicOption(pair.second)
                                    modalBottomSheetState.hide()
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
        }
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldBottomSheetState,
            sheetPeekHeight = 0.dp,
            sheetContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    dominantBackgroundColor.copy(alpha = 0.3f),
                                    darkBackgroundColor.copy(alpha = 0.6f),
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(background_content_dark.copy(alpha = 0.3f))
                    ) {
                        Card(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .padding(top = 32.dp, start = 32.dp, end = 32.dp)
                                .size(288.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Image(
                                painter = rememberImagePainter(
                                    data = run {
                                        if (currentMusicPlayed.albumPath != null) {
                                            currentMusicPlayed.albumPath!!.toUri()
                                        } else ContextCompat.getDrawable(context, R.drawable.ic_music_unknown)!!
                                    },
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
                    }
                }
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
                                            saveState = false
                                        }

                                        restoreState = false
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
                                                        saveState = false
                                                    }

                                                    restoreState = false
                                                    launchSingleTop = true
                                                }
                                            }
                                            1 -> scope.launch {
                                                modalBottomSheetState.show()
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
                                .padding(bottom = 8.dp)
                                .weight(1f)
                        ) {
                            items(musicList) { music ->
                                MusicItem(
                                    music = music,
                                    onClick = {
                                        musicControllerViewModel.play(context, music.audioID)
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
                                        scaffoldBottomSheetState.bottomSheetState.expand()
                                    }
                                }
                            )
                    ) {
                        LinearProgressIndicator(
                            progress = 0.2f,
                            modifier = Modifier
                                .fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {

                            Row(
                                modifier = Modifier
                                    .weight(0.6f)
                                    .padding(start = 32.dp)
                            ) {
                                Image(
                                    painter = rememberImagePainter(
                                        data = run {
                                            if (currentMusicPlayed.albumPath != null) {
                                                currentMusicPlayed.albumPath!!.toUri()
                                            } else ContextCompat.getDrawable(context, R.drawable.ic_music_unknown)!!
                                        },
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
                                    onClick = {}
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
                                        if (!target) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_play_filled_rounded),
                                                tint = if (isSystemInDarkTheme()) background_light else background_dark,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(20.dp)
                                            )
                                        } else {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_pause_filled_rounded),
                                                tint = if (isSystemInDarkTheme()) background_light else background_dark,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(20.dp)
                                            )
                                        }
                                    }
                                }

                                // Next Button
                                IconButton(
                                    onClick = {}
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

            }
        }
    }
}
