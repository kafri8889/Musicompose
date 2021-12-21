package com.anafthdev.musicompose.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
import com.anafthdev.musicompose.utils.MusicManager
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalUnitApi::class
)
@Composable
fun MusicItem(
    music: Music,
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
                        .padding(top = 2.dp)
                )

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
