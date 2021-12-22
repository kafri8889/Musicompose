package com.anafthdev.musicompose.ui.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.data.AppDatastore
import com.anafthdev.musicompose.data.MusicomposeDestination
import com.anafthdev.musicompose.ui.MainActivity
import com.anafthdev.musicompose.ui.components.MusicItem
import com.anafthdev.musicompose.ui.components.PopupMenu
import com.anafthdev.musicompose.ui.theme.*
import com.anafthdev.musicompose.utils.AppUtils
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

    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val sortMusicOption by datastore.getSortMusicOption.collectAsState(initial = AppUtils.PreferencesValue.SORT_MUSIC_BY_NAME)
    val musicList by homeViewModel.musicList.observeAsState(initial = emptyList())

    var hasNavigate by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }

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
                                        bottomSheetState.show()
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
        ModalBottomSheetLayout(
            sheetElevation = 8.dp,
            sheetShape = RoundedCornerShape(32.dp),
            scrimColor = black.copy(alpha = 0.6f),
            sheetBackgroundColor = if (isSystemInDarkTheme()) background_content_dark else white,
            sheetState = bottomSheetState,
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
                                        bottomSheetState.hide()
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
                                        bottomSheetState.hide()
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
            CompositionLocalProvider(
                LocalOverScrollConfiguration provides null
            ) {
                LazyColumn {
                    items(musicList) { music ->
                        MusicItem(
                            music = music,
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}
