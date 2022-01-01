package com.anafthdev.musicompose.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.navigation.compose.rememberNavController
import androidx.palette.graphics.Palette
import coil.compose.rememberImagePainter
import com.anafthdev.musicompose.BuildConfig
import com.anafthdev.musicompose.MusicomposeApplication
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.common.AppDatastore
import com.anafthdev.musicompose.common.SettingsContentObserver
import com.anafthdev.musicompose.data.MusicomposeDestination
import com.anafthdev.musicompose.model.Music
import com.anafthdev.musicompose.model.Playlist
import com.anafthdev.musicompose.ui.album.AlbumViewModel
import com.anafthdev.musicompose.ui.artist.ArtistViewModel
import com.anafthdev.musicompose.ui.components.SliderDefaults
import com.anafthdev.musicompose.ui.home.HomeViewModel
import com.anafthdev.musicompose.ui.home.PlaylistList
import com.anafthdev.musicompose.ui.playlist.PlaylistViewModel
import com.anafthdev.musicompose.ui.scan_music.ScanMusicViewModel
import com.anafthdev.musicompose.ui.search.SearchViewModel
import com.anafthdev.musicompose.ui.theme.*
import com.anafthdev.musicompose.utils.AppUtils.toast
import com.anafthdev.musicompose.utils.ComposeUtils
import com.anafthdev.musicompose.utils.ComposeUtils.currentFraction
import com.anafthdev.musicompose.utils.DatabaseUtil
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Project started	:	18-12-2021
 * @author			:	kafri8889
 *
 */

class MainActivity : ComponentActivity() {

	@Inject lateinit var datastore: AppDatastore
	@Inject lateinit var databaseUtil: DatabaseUtil
	@Inject lateinit var musicControllerViewModel: MusicControllerViewModel
	@Inject lateinit var homeViewModel: HomeViewModel
	@Inject lateinit var scanMusicViewModel: ScanMusicViewModel
	@Inject lateinit var searchViewModel: SearchViewModel
	@Inject lateinit var artistViewModel: ArtistViewModel
	@Inject lateinit var albumViewModel: AlbumViewModel
	@Inject lateinit var playlistViewModel: PlaylistViewModel

	// if there is a volume change, it will call musicControllerViewModel.onVolumeChange()
	private val settingsContentObserver = SettingsContentObserver {
		musicControllerViewModel.onVolumeChange()
	}

	private val permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
		if (!granted) {
			"You must grant permission!".toast(this, Toast.LENGTH_LONG)
			finishAffinity()
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		(applicationContext as MusicomposeApplication).appComponent.inject(this)
		if (BuildConfig.DEBUG) Timber.plant(object : Timber.DebugTree() {
			override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
				super.log(priority, "DEBUG_$tag", message, t)
			}
		})

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)  {
			permissionResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//			window.setDecorFitsSystemWindows(false)
		}

		// register SettingsContentObserver, used to observe changes in volume
		contentResolver.registerContentObserver(
			android.provider.Settings.System.CONTENT_URI,
			true,
			settingsContentObserver
		)

		val defaultPlaylist = listOf(
			Playlist(
				name = getString(R.string.favorite),
				musicList = emptyList(),
				defaultImage = R.drawable.ic_favorite_image,
				isDefault = true,
				id = 0
			),
			Playlist(
				name = getString(R.string.just_played),
				musicList = emptyList(),
				defaultImage = R.drawable.ic_just_played_image,
				isDefault = true,
				id = 1
			)
		)

		// check if default playlist exists or not, if not add playlist
		databaseUtil.getAllPlaylist { playlist ->
			defaultPlaylist.forEach {
				if (!playlist.contains(it)) {
					databaseUtil.insertPlaylist(it) {
						Timber.i("playlist \"${it.name}\" created")
					}
				}
			}
		}

		setContent {
			ProvideWindowInsets(
				windowInsetsAnimationsEnabled = true
			) {
				MusicomposeTheme {
					Surface(color = MaterialTheme.colors.background) {
						MusicomposeApp(
							datastore = datastore,
							homeViewModel = homeViewModel,
							searchViewModel = searchViewModel,
							scanMusicViewModel = scanMusicViewModel,
							playlistViewModel = playlistViewModel,
							albumViewModel = albumViewModel,
							artistViewModel = artistViewModel,
							musicControllerViewModel = musicControllerViewModel
						)
					}
				}
			}
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		contentResolver.unregisterContentObserver(settingsContentObserver)
	}
}
