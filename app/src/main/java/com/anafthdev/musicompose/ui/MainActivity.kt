package com.anafthdev.musicompose.ui

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.anafthdev.musicompose.BuildConfig
import com.anafthdev.musicompose.MusicomposeApplication
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.common.AppDatastore
import com.anafthdev.musicompose.common.MediaPlayerManager
import com.anafthdev.musicompose.common.MediaPlayerService
import com.anafthdev.musicompose.common.SettingsContentObserver
import com.anafthdev.musicompose.model.Playlist
import com.anafthdev.musicompose.ui.album.AlbumViewModel
import com.anafthdev.musicompose.ui.artist.ArtistViewModel
import com.anafthdev.musicompose.ui.home.HomeViewModel
import com.anafthdev.musicompose.ui.playlist.PlaylistViewModel
import com.anafthdev.musicompose.ui.scan_music.ScanMusicViewModel
import com.anafthdev.musicompose.ui.search.SearchViewModel
import com.anafthdev.musicompose.ui.theme.MusicomposeTheme
import com.anafthdev.musicompose.utils.AppUtils.containBy
import com.anafthdev.musicompose.utils.AppUtils.toast
import com.anafthdev.musicompose.utils.DatabaseUtil
import com.google.accompanist.insets.ProvideWindowInsets
import timber.log.Timber
import javax.inject.Inject


/**
 * Project started	:	18-12-2021
 * @author			:	kafri8889
 *
 */

class MainActivity : ComponentActivity(), ServiceConnection {

	@Inject lateinit var datastore: AppDatastore
	@Inject lateinit var databaseUtil: DatabaseUtil
	@Inject lateinit var musicControllerViewModel: MusicControllerViewModel
	@Inject lateinit var homeViewModel: HomeViewModel
	@Inject lateinit var scanMusicViewModel: ScanMusicViewModel
	@Inject lateinit var searchViewModel: SearchViewModel
	@Inject lateinit var artistViewModel: ArtistViewModel
	@Inject lateinit var albumViewModel: AlbumViewModel
	@Inject lateinit var playlistViewModel: PlaylistViewModel

	private var mediaPlayerService: MediaPlayerService? = null

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

		val serviceIntent = Intent(this, MediaPlayerService::class.java)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			ContextCompat.startForegroundService(this, serviceIntent)
			startForegroundService(serviceIntent)
		} else startService(serviceIntent)
		bindService(
			serviceIntent,
			this,
			BIND_AUTO_CREATE
		)

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
		databaseUtil.getAllPlaylist { playlistList ->
			defaultPlaylist.forEach { playlist ->
				if (!playlistList.containBy { it.id == playlist.id }) {
					databaseUtil.insertPlaylist(playlist) {
						Timber.i("playlist \"${playlist.name}\" created")
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

	override fun onStart() {
		super.onStart()
		// register SettingsContentObserver, used to observe changes in volume
		contentResolver.registerContentObserver(
			android.provider.Settings.System.CONTENT_URI,
			true,
			settingsContentObserver
		)
	}

	override fun onStop() {
		super.onStop()
		contentResolver.unregisterContentObserver(settingsContentObserver)
	}

	override fun onServiceConnected(name: ComponentName?, service: IBinder) {
		val binder = service as MediaPlayerService.MediaPlayerServiceBinder
		mediaPlayerService = binder.getService()
		mediaPlayerService!!.setMediaPlayerAction(object : MediaPlayerManager.MediaPLayerAction {
			override fun playPause() {
				if (musicControllerViewModel.isMusicPlayed.value == true) {
					musicControllerViewModel.pause()
				} else musicControllerViewModel.play()
			}

			override fun next() {
				musicControllerViewModel.next()
			}

			override fun previous() {
				musicControllerViewModel.previous()
			}
		})
	}

	override fun onServiceDisconnected(name: ComponentName?) {
		mediaPlayerService = null
	}
}
