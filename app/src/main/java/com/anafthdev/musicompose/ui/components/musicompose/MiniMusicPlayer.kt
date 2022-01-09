package com.anafthdev.musicompose.ui.components.musicompose

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.anafthdev.musicompose.ui.MusicControllerViewModel
import com.anafthdev.musicompose.ui.theme.*
import com.anafthdev.musicompose.utils.ComposeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(
    ExperimentalUnitApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun MiniMusicPlayer(
    scope: CoroutineScope,
    isMusicPlayed: Boolean,
    currentProgress: Long,
    currentMusicPlayed: Music,
    musicControllerViewModel: MusicControllerViewModel,
    musicControllerState: MusicControllerViewModel.MusicControllerState,
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
                    val normalizedProgress = currentProgress.toFloat() / currentMusicPlayed.duration

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
