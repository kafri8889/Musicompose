package com.anafthdev.musicompose.ui.components.musicompose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.data.MusicomposeDestination
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.model.MusicControllerState
import com.anafthdev.musicompose.ui.theme.background_dark
import com.anafthdev.musicompose.ui.theme.typographySkModernist
import com.anafthdev.musicompose.ui.theme.white
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalUnitApi::class,
    ExperimentalMaterialApi::class,
)
@Composable
fun MusicScreenMoreOptionsSheetContent(
    scope: CoroutineScope,
    navController: NavHostController,
    currentMusicPlayed: Music,
    musicControllerState: MusicControllerState
) {

    val moreOptionItems = listOf(
        stringResource(id = R.string.artist) to R.drawable.ic_profile,
        stringResource(id = R.string.album) to R.drawable.ic_cd,
        stringResource(id = R.string.add_to_playlist) to R.drawable.ic_music_playlist,
        stringResource(id = R.string.set_timer) to R.drawable.ic_timer
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
                                    val route = MusicomposeDestination.Artist.createRoute(
                                        currentMusicPlayed.artist
                                    )

                                    scope.launch {
                                        musicControllerState.musicMoreOptionModalBottomSheetState.hide()
                                        delay(200)
                                        musicControllerState.musicScaffoldBottomSheetState.bottomSheetState.collapse()
                                        delay(100)
                                        navController.navigate(route) {
                                            launchSingleTop = true
                                        }
                                    }
                                }
                                moreOptionItems[1].first -> {
                                    val route = MusicomposeDestination.Album.createRoute(
                                        currentMusicPlayed.albumID
                                    )

                                    scope.launch {
                                        musicControllerState.musicMoreOptionModalBottomSheetState.hide()
                                        delay(200)
                                        musicControllerState.musicScaffoldBottomSheetState.bottomSheetState.collapse()
                                        delay(100)
                                        navController.navigate(route) {
                                            launchSingleTop = true
                                        }
                                    }
                                }
                                moreOptionItems[2].first -> {
                                    scope.launch {
                                        musicControllerState.musicMoreOptionModalBottomSheetState.hide()
                                        delay(200)
                                        musicControllerState.addToPlaylistModalBottomSheetState.show()
                                    }
                                }
                                moreOptionItems[3].first -> {

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
                        .size(20.dp)
                )

                Text(
                    overflow = TextOverflow.Ellipsis,
                    text = when (pair.first) {
                        moreOptionItems[0].first -> {
                            "${pair.first}: ${currentMusicPlayed.artist}"
                        }
                        moreOptionItems[1].first -> {
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

        item {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
            )
        }
    }
}
