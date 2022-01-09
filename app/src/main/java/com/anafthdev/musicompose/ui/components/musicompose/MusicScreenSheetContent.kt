package com.anafthdev.musicompose.ui.components.musicompose

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.rememberImagePainter
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.model.MusicControllerState
import com.anafthdev.musicompose.ui.MusicControllerViewModel
import com.anafthdev.musicompose.ui.components.IconButton
import com.anafthdev.musicompose.ui.components.Slider
import com.anafthdev.musicompose.ui.components.SliderDefaults
import com.anafthdev.musicompose.ui.theme.typographyDmSans
import com.anafthdev.musicompose.ui.theme.typographySkModernist
import com.anafthdev.musicompose.ui.theme.white
import com.anafthdev.musicompose.utils.ComposeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OptIn(
    ExperimentalUnitApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalAnimationApi::class,
)
@Composable
fun MusicScreenSheetContent(
    scope: CoroutineScope,
    dominantBackgroundColor: Color,
    maxStreamMusicVolume: Int,
    isMusicPlayed: Boolean,
    isMusicFavorite: Boolean,
    isVolumeMuted: Boolean,
    currentVolume: Int,
    currentProgress: Long,
    currentMusicPlayed: Music,
    currentMusicPlayMode: MusicControllerViewModel.MusicPlayMode,
    currentMusicDurationInMinute: Int,
    currentMusicDurationInSecond: Int,
    musicDurationInMinute: Int,
    musicDurationInSecond: Int,
    musicControllerState: MusicControllerState,
    musicControllerViewModel: MusicControllerViewModel
) {

    // Interaction source for slider
    val sliderInteractionSource = remember { MutableInteractionSource() }

    // When slider dragged, set slider value with sliderProgressFromUser
    // if not, then use currentProgress
    val isSliderDragged = sliderInteractionSource.collectIsDraggedAsState()

    var sliderProgressFromUser by remember { mutableStateOf(0f) }
    var currentMusicDuration by remember { mutableStateOf("") }

    currentMusicDuration = if (isSliderDragged.value) {
        run {
            val mMusicDurationInMinute = TimeUnit.MILLISECONDS.toMinutes(sliderProgressFromUser.toLong())
            val mMusicDurationInSecond = TimeUnit.MILLISECONDS.toSeconds(sliderProgressFromUser.toLong()) % 60
            "${mMusicDurationInMinute}:${if (mMusicDurationInSecond > 9) mMusicDurationInSecond else "0$mMusicDurationInSecond"}"
        }
    } else "$currentMusicDurationInMinute:${if (currentMusicDurationInSecond > 9) currentMusicDurationInSecond else "0$currentMusicDurationInSecond"}"

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
                                id = if (isMusicFavorite) R.drawable.ic_favorite_selected else R.drawable.ic_favorite_unselected
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
                                    musicControllerState.musicMoreOptionModalBottomSheetState.show()
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
                    value = if (isSliderDragged.value) {
                        sliderProgressFromUser
                    } else currentProgress.toFloat(),
                    valueRange = 0f..currentMusicPlayed.duration.toFloat(),
                    thumbRadius = 6.dp,
                    onValueChange = { progress ->
                        sliderProgressFromUser = progress
                    },
                    onValueChangeFinished = {
                        musicControllerViewModel.applyProgress(sliderProgressFromUser.toLong())

                        sliderProgressFromUser = 0f
                    },
                    colors = SliderDefaults.colors(
                        activeTrackColor = ComposeUtils.lightenColor(dominantBackgroundColor, 0.6f),
                        inactiveTrackColor = ComposeUtils.lightenColor(dominantBackgroundColor, 0.6f).copy(alpha = 0.24f),
                        thumbColor = ComposeUtils.lightenColor(dominantBackgroundColor, 0.6f)
                    ),
                    interactionSource = sliderInteractionSource,
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
                        text = currentMusicDuration,
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
                                MusicControllerViewModel.MusicPlayMode.REPEAT_ON -> R.drawable.ic_repeate_on
                                MusicControllerViewModel.MusicPlayMode.REPEAT_ONE -> R.drawable.ic_repeate_one
                            }
                        ),
                        tint = white,
                        contentDescription = null
                    )
                }



                androidx.compose.material.IconButton(
                    onClick = {
                        musicControllerViewModel.previous()
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
                        musicControllerViewModel.next()
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
}
