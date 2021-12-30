package com.anafthdev.musicompose.ui.scan_music

import android.os.Handler
import android.os.Looper
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.data.FakeMusicRepository
import com.anafthdev.musicompose.data.MusicomposeDestination
import com.anafthdev.musicompose.ui.MusicControllerViewModel
import com.anafthdev.musicompose.ui.theme.black
import com.anafthdev.musicompose.ui.theme.secondary_dark
import com.anafthdev.musicompose.ui.theme.secondary_light
import com.anafthdev.musicompose.ui.theme.typographySkModernist

@OptIn(ExperimentalUnitApi::class)
@Composable
fun ScanMusicScreen(
    navController : NavHostController,
    scanMusicViewModel: ScanMusicViewModel,
    musicControllerViewModel: MusicControllerViewModel
) {
    val context = LocalContext.current

    val numberMarker = 90

    val scannedMusicInPercent by scanMusicViewModel.scannedMusicInPercent.observeAsState(initial = 0)

    val progressAngle by animateFloatAsState(
        targetValue = (scannedMusicInPercent.toFloat() / 100f) * 360f
    )

    val markerActive by animateFloatAsState(
        targetValue = (scannedMusicInPercent.toFloat() / 100) * numberMarker
    )

    musicControllerViewModel.hideMiniMusicPlayer()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            for (i in 0 until numberMarker) {
                ScanMusicProgressMarker(
                    angle = i * (360 / numberMarker),
                    active = i < markerActive
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(95.dp)
            ) {
                Text(
                    text = "$scannedMusicInPercent%",
                    style = typographySkModernist().body1.copy(
                        fontSize = TextUnit(36f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }

            ScanMusicProgress(
                angle = progressAngle
            )
        }

        Button(
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (isSystemInDarkTheme()) secondary_dark else secondary_light
            ),
            onClick = {
               scanMusicViewModel.scanLocalSong(context) {
                   Handler(Looper.getMainLooper()).postDelayed({
                       navController.navigate(MusicomposeDestination.HomeScreen) {
                           popUpTo(0) {
                               saveState = false
                           }

                           restoreState = false
                           launchSingleTop = true
                       }
                   }, 1300)
               }
            },
            modifier = Modifier
                .padding(top = 64.dp)
                .size(256.dp, 48.dp)
        ) {
            Text(
                text = stringResource(id = R.string.scan_local_songs),
                style = typographySkModernist().body1.copy(
                    color = black,
                    fontSize = TextUnit(16f, TextUnitType.Sp),
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
