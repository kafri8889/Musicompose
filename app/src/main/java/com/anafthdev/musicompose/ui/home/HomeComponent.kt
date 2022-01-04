package com.anafthdev.musicompose.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.anafthdev.musicompose.ui.theme.sunset_orange
import com.anafthdev.musicompose.ui.theme.typographyDmSans
import com.anafthdev.musicompose.ui.theme.typographySkModernist
import com.anafthdev.musicompose.ui.theme.white
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.draggedItem
import org.burnoutcrew.reorderable.rememberReorderState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun PlaylistList(
    items: List<Music>,
    currentMusicPlayed: Music,
    itemBackgroundColor: Color,
    musicControllerViewModel: MusicControllerViewModel
) {

    val scope = rememberCoroutineScope()
    val state = rememberReorderState()

    musicControllerViewModel.onNext = { currentMusicIndex ->
        scope.launch {
            state.listState.animateScrollToItem(currentMusicIndex)
        }
    }

    musicControllerViewModel.onPrevious = { currentMusicIndex ->
        scope.launch {
            state.listState.animateScrollToItem(currentMusicIndex)
        }
    }

    LazyColumn(
        state = state.listState,
        modifier = Modifier
            .reorderable(
                state = state,
                onMove = { oldPos, newPos ->
                    musicControllerViewModel.onPlaylistReordered(oldPos, newPos)
                }
            )
            .detectReorderAfterLongPress(state)
    ) {
        items(items, key = { it.audioID }) { music ->
            MusicItem(
                music = music,
                itemBackgroundColor = itemBackgroundColor,
                isMusicPlayed = currentMusicPlayed.audioID == music.audioID,
                onClick = {
                    if (currentMusicPlayed.audioID != music.audioID) {
                        musicControllerViewModel.play(music.audioID, shufflePlaylist = false)
                        scope.launch {
                            state.listState.animateScrollToItem(musicControllerViewModel.currentMusicPlayedIndexInPlaylist())
                        }
                    }
                },
                modifier = Modifier
                    .draggedItem(state.offsetByKey(music.audioID))
            )
        }
    }
}





@OptIn(
    ExperimentalUnitApi::class,
    ExperimentalMaterialApi::class
)
@Composable
private fun MusicItem(
    music: Music,
    isMusicPlayed: Boolean,
    itemBackgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Card(
        elevation = 0.dp,
        backgroundColor = itemBackgroundColor,
        shape = RoundedCornerShape(14.dp),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 12.dp, bottom = 12.dp, end = 8.dp)
                .background(Color.Transparent)
        ) {

            Image(
                painter = rememberImagePainter(
                    data = music.albumPath.toUri(),
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
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
            ) {
                Text(
                    text = music.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = typographyDmSans().body1.copy(
                        color = if (isMusicPlayed) sunset_orange else white,
                        fontSize = TextUnit(14f, TextUnitType.Sp),
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Text(
                    text = music.artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = typographySkModernist().body1.copy(
                        color = if (isMusicPlayed) sunset_orange else white.copy(alpha = 0.7f),
                        fontSize = TextUnit(14f, TextUnitType.Sp),
                    ),
                    modifier = Modifier
                        .padding(top = 6.dp)
                )
            }
        }
    }
}
