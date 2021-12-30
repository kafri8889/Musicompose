package com.anafthdev.musicompose.ui.album

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.data.MusicomposeDestination
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.ui.MusicControllerViewModel
import com.anafthdev.musicompose.ui.components.MusicItem
import com.anafthdev.musicompose.ui.theme.*

@OptIn(ExperimentalUnitApi::class)
@Composable
fun AlbumScreen(
    albumID: String,
    albumViewModel: AlbumViewModel,
    musicControllerViewModel: MusicControllerViewModel,
    navController: NavHostController
) {

    val currentMusicPlayed by musicControllerViewModel.currentMusicPlayed.observeAsState(initial = Music.unknown)
    val artist by albumViewModel.artist.observeAsState(initial = Music.unknown.artist)
    val album by albumViewModel.album.observeAsState(initial = Music.unknown.album)
    val filteredMusicList by albumViewModel.filteredMusicList.observeAsState(initial = emptyList())

    var hasNavigate by remember { mutableStateOf(false) }

    if (!hasNavigate) {
        albumViewModel.get(albumID)
        true.also { hasNavigate = it }
    }

    Scaffold(
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                IconButton(
                    onClick = {
                        navController.navigate(MusicomposeDestination.HomeScreen) {
                            popUpTo(0)
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        tint = if (isSystemInDarkTheme()) white else black,
                        contentDescription = null
                    )
                }

                Text(
                    text = album,
                    overflow = TextOverflow.Ellipsis,
                    style = typographyDmSans().body1.copy(
                        fontSize = TextUnit(16f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(bottom = 64.dp)
        ) {

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSystemInDarkTheme()) background_content_dark else background_content_light)
                        .padding(14.dp)
                ) {
                    Image(
                        painter = rememberImagePainter(
                            data = run {
                                if (filteredMusicList.isEmpty()) {
                                    Music.unknown.albumPath
                                } else filteredMusicList[0].albumPath
                            },
                            builder = {
                                error(R.drawable.ic_music_unknown)
                                placeholder(R.drawable.ic_music_unknown)
                            }
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(start = 12.dp)
                    ) {
                        Text(
                            text = artist,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = typographyDmSans().body1.copy(
                                fontSize = TextUnit(14f, TextUnitType.Sp),
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Text(
                            text = album,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = typographySkModernist().body1.copy(
                                color = typographySkModernist().body1.color.copy(alpha = 0.7f),
                                fontSize = TextUnit(12f, TextUnitType.Sp),
                            ),
                            modifier = Modifier
                                .padding(top = 6.dp)
                        )
                    }
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = rememberRipple(color = Color.Transparent),
                            interactionSource = MutableInteractionSource(),
                            onClick = {
                                musicControllerViewModel.playAll(filteredMusicList)
                            }
                        )
                        .padding(bottom = 16.dp, top = 8.dp, start = 8.dp)
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
                        text = "${filteredMusicList.size} ${stringResource(id = R.string.song).lowercase()}",
                        style = typographyDmSans().body1.copy(
                            color = typographyDmSans().body1.color.copy(alpha = 0.6f),
                            fontSize = TextUnit(14f, TextUnitType.Sp),
                            fontWeight = FontWeight.Light
                        ),
                        modifier = Modifier
                            .padding(start = 8.dp)
                    )
                }

                Divider(
                    color = background_content_dark,
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

            items(filteredMusicList) { music ->
                MusicItem(
                    music = music,
                    showImage = false,
                    isMusicPlayed = currentMusicPlayed.audioID == music.audioID,
                    onClick = {
                        musicControllerViewModel.play(music.audioID)
                        musicControllerViewModel.getPlaylist()
                    }
                )
            }
        }
    }
}
