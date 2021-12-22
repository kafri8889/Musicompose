package com.anafthdev.musicompose.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import coil.compose.rememberImagePainter
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransparentButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    indication: Indication = rememberRipple(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = MaterialTheme.shapes.small,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        elevation = 0.dp,
        shape = shape,
        color = Color.Transparent,
        contentColor = Color.Transparent,
        border = null,
        modifier = modifier
            .then(
                Modifier
                    .clip(shape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = indication,
                        onClick = onClick
                    )
            ),
    ) {
        CompositionLocalProvider(LocalContentAlpha provides 1f) {
            ProvideTextStyle(
                value = MaterialTheme.typography.button
            ) {
                Row(
                    Modifier
                        .defaultMinSize(
                            minWidth = ButtonDefaults.MinWidth,
                            minHeight = ButtonDefaults.MinHeight
                        )
                        .padding(contentPadding),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }
        }
    }
}





@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalUnitApi::class
)
@Composable
fun MusicItem(
    music: Music,
    modifier: Modifier = Modifier,
    showImage: Boolean = true,
    showDuration: Boolean = true,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        elevation = 0.dp,
        backgroundColor = if (isSystemInDarkTheme()) background_dark else background_light,
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
                .background(if (isSystemInDarkTheme()) background_dark else background_light,)
        ) {

            if (showImage) {
                Image(
                    painter = rememberImagePainter(
                        data = run {
                            if (music.albumPath != null) {
                                music.albumPath.toUri()
                            } else ContextCompat.getDrawable(context, R.drawable.ic_music_unknown)!!
                        },
                        builder = {
                            error(R.drawable.ic_music_unknown)
                            placeholder(R.drawable.ic_music_unknown)
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
            ) {
                Text(
                    text = music.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = typographyDmSans().body1.copy(
                        fontSize = TextUnit(14f, TextUnitType.Sp),
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Text(
                    text = music.artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = typographySkModernist().body1.copy(
                        color = typographySkModernist().body1.color.copy(alpha = 0.7f),
                        fontSize = TextUnit(12f, TextUnitType.Sp),
                    ),
                    modifier = Modifier
                        .padding(top = if (showDuration) 2.dp else 6.dp)
                )

                if (showDuration) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .wrapContentSize(Alignment.BottomStart)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_clock),
                            tint = if (isSystemInDarkTheme()) white.copy(alpha = 0.7f) else black.copy(alpha = 0.7f),
                            contentDescription = null,
                            modifier = Modifier
                                .size(14.dp)
                        )

                        Text(
                            text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(music.duration),
                            style = typographySkModernist().body1.copy(
                                color = typographySkModernist().body1.color.copy(alpha = 0.7f),
                                fontSize = TextUnit(12f, TextUnitType.Sp)
                            ),
                            modifier = Modifier
                                .padding(start = 4.dp)
                        )
                    }
                }
            }

        }
    }
}





@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalUnitApi::class
)
@Composable
fun AlbumItem(
//    album: Album,
    musicList: List<Music>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    val context = LocalContext.current

    var musicIndexForAlbumThumbnail by remember { mutableStateOf(0) }

    Card(
        elevation = 0.dp,
        backgroundColor = if (isSystemInDarkTheme()) background_dark else background_light,
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
                .background(if (isSystemInDarkTheme()) background_dark else background_light,)
        ) {

            Image(
                painter = rememberImagePainter(
                    data = run {
                        if (musicList.isNotEmpty()) {
                            if (musicList[musicIndexForAlbumThumbnail].albumPath != null) {
                                musicList[musicIndexForAlbumThumbnail].albumPath!!.toUri()
                            } else ContextCompat.getDrawable(context, R.drawable.ic_music_unknown)!!
                        } else ContextCompat.getDrawable(context, R.drawable.ic_music_unknown)!!
                    },
                    builder = {
                        error(R.drawable.ic_music_unknown)
                        placeholder(R.drawable.ic_music_unknown)
                        listener(
                            onError = { _, _ ->
                                if (musicIndexForAlbumThumbnail < musicList.size - 1) {
                                    musicIndexForAlbumThumbnail += 1
                                    data(
                                        run {
                                            if (musicList[musicIndexForAlbumThumbnail].albumPath != null) {
                                                musicList[musicIndexForAlbumThumbnail].albumPath!!.toUri()
                                            } else ContextCompat.getDrawable(context, R.drawable.ic_music_unknown)!!
                                        }
                                    )
                                } else data(ContextCompat.getDrawable(context, R.drawable.ic_music_unknown)!!)
                            }
                        )
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
                    text = musicList[0].album,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = typographyDmSans().body1.copy(
                        fontSize = TextUnit(14f, TextUnitType.Sp),
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Text(
                    text = musicList[0].artist,
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
}
