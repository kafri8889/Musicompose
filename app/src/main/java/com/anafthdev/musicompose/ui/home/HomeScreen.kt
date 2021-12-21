package com.anafthdev.musicompose.ui.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavHostController
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.data.MusicomposeDestination
import com.anafthdev.musicompose.ui.components.MusicItem
import com.anafthdev.musicompose.ui.components.PopupMenu
import com.anafthdev.musicompose.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalUnitApi::class
)
@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel
) {

    val context = LocalContext.current

    val dropdownMenuItems = listOf(
        stringResource(id = R.string.scan_local_songs),
        stringResource(id = R.string.sort_by)
    )

    val sortMusicItem = listOf(
        stringResource(id = R.string.date_added),
        stringResource(id = R.string.song_name),
        stringResource(id = R.string.artist_name),
    )

    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val musicList by homeViewModel.musicList.observeAsState(initial = emptyList())

    var showDropdownMenu by remember { mutableStateOf(false) }
    var selectedSortOption by remember { mutableStateOf(1) }

    homeViewModel.getAllMusic()
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

                    sortMusicItem.forEachIndexed { i, s ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSortOption = i
                                }
                        ) {
                            Text(
                                text = s,
                                style = typographySkModernist().body1.copy(
                                    fontSize = TextUnit(16f, TextUnitType.Sp)
                                ),
                                modifier = Modifier
                                    .wrapContentWidth(Alignment.Start)
                                    .padding(top = 14.dp, bottom = 14.dp, start = 16.dp)
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            RadioButton(
                                selected = i == selectedSortOption,
                                onClick = {
                                    selectedSortOption = i
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
