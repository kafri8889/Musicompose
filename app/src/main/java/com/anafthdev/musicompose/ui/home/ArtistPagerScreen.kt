package com.anafthdev.musicompose.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.anafthdev.musicompose.data.MusicomposeDestination
import com.anafthdev.musicompose.ui.theme.background_content_dark
import com.anafthdev.musicompose.ui.theme.typographySkModernist

@OptIn(ExperimentalUnitApi::class)
@Composable
fun ArtistPagerScreen(
    homeViewModel: HomeViewModel,
    navController: NavHostController,
) {

    val artistList by homeViewModel.artistList.observeAsState(initial = emptyMap())

    homeViewModel.getAllArtist()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 64.dp)
    ) {
        items(artistList.toList()) { musicPair ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
            ) {
                Text(
                    text = musicPair.second[0].artist,
                    style = typographySkModernist().body1.copy(
                        fontSize = TextUnit(16f, TextUnitType.Sp),
                        fontWeight = FontWeight.SemiBold
                    ),
                )

                Spacer(
                    modifier = Modifier
                        .weight(1f)
                )

                IconButton(
                    onClick = {
                        val route = MusicomposeDestination.Artist.createRoute(musicPair.second[0].artist)
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowRight,
                        tint = background_content_dark,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
