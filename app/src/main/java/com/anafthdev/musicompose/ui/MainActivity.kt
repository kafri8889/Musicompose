package com.anafthdev.musicompose.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anafthdev.musicompose.BuildConfig
import com.anafthdev.musicompose.MusicomposeApplication
import com.anafthdev.musicompose.common.AppDatastore
import com.anafthdev.musicompose.common.SettingsContentObserver
import com.anafthdev.musicompose.data.MusicomposeDestination
import com.anafthdev.musicompose.ui.home.HomeScreen
import com.anafthdev.musicompose.ui.home.HomeViewModel
import com.anafthdev.musicompose.ui.scan_music.ScanMusicScreen
import com.anafthdev.musicompose.ui.scan_music.ScanMusicViewModel
import com.anafthdev.musicompose.ui.search.SearchScreen
import com.anafthdev.musicompose.ui.search.SearchViewModel
import com.anafthdev.musicompose.ui.theme.MusicomposeTheme
import com.anafthdev.musicompose.utils.AppUtils.toast
import timber.log.Timber
import javax.inject.Inject

/**
 * Project started	:	18-12-2021
 * @author			:	kafri8889
 *
 */

class MainActivity : ComponentActivity() {

	@Inject lateinit var datastore: AppDatastore
	@Inject lateinit var musicControllerViewModel: MusicControllerViewModel
	@Inject lateinit var homeViewModel: HomeViewModel
	@Inject lateinit var scanMusicViewModel: ScanMusicViewModel
	@Inject lateinit var searchViewModel: SearchViewModel

	// if there is a volume change, it will call musicControllerViewModel.onVolumeChange()
	private val settingsContentObserver = SettingsContentObserver {
		musicControllerViewModel.onVolumeChange()
	}
	
	private val permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
		if (granted) {

		} else {
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

		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

		// register SettingsContentObserver, used to observe changes in volume
		contentResolver.registerContentObserver(
			android.provider.Settings.System.CONTENT_URI,
			true,
			settingsContentObserver
		)

		setContent {
			MusicomposeTheme {
				Surface(color = MaterialTheme.colors.background) {
					Screen()
				}
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	private fun Screen() {

		val navigationController = rememberNavController()

		NavHost(
			navController = navigationController,
			startDestination = MusicomposeDestination.HomeScreen
		) {

			composable(MusicomposeDestination.HomeScreen) {
				HomeScreen(
					navController = navigationController,
					musicControllerViewModel = musicControllerViewModel,
					homeViewModel = homeViewModel,
					datastore = datastore
				)
			}

			composable(MusicomposeDestination.ScanMusicScreen) {
				ScanMusicScreen(
					navController = navigationController,
					scanMusicViewModel = scanMusicViewModel
				)
			}

			composable(MusicomposeDestination.SearchScreen) {
				SearchScreen(
					navController = navigationController,
					searchViewModel = searchViewModel,
					musicControllerViewModel = musicControllerViewModel
				)
			}
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		contentResolver.unregisterContentObserver(settingsContentObserver)
	}
}
