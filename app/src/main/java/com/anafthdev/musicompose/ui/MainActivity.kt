package com.anafthdev.musicompose.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anafthdev.musicompose.BuildConfig
import com.anafthdev.musicompose.data.MusicRepository
import com.anafthdev.musicompose.data.MusicomposeDestination
import com.anafthdev.musicompose.ui.home.HomeScreen
import com.anafthdev.musicompose.ui.home.HomeViewModel
import com.anafthdev.musicompose.ui.home.HomeViewModelFactory
import com.anafthdev.musicompose.ui.scan_music.ScanMusicScreen
import com.anafthdev.musicompose.ui.scan_music.ScanMusicViewModel
import com.anafthdev.musicompose.ui.scan_music.ScanMusicViewModelFactory
import com.anafthdev.musicompose.ui.theme.MusicomposeTheme
import com.anafthdev.musicompose.utils.AppUtils.toast
import com.anafthdev.musicompose.utils.DatabaseUtil
import com.anafthdev.musicompose.utils.MusicManager
import timber.log.Timber

/**
 * Project started	:	18-12-2021
 * @author			:	kafri8889
 *
 */

class MainActivity : ComponentActivity() {

	private lateinit var databaseUtil: DatabaseUtil
	private lateinit var homeViewModel: HomeViewModel
	private lateinit var scanMusicViewModel: ScanMusicViewModel
	
	private val permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
		if (granted) {

		} else {
			"You must grant permission!".toast(this, Toast.LENGTH_LONG)
			finishAffinity()
		}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (BuildConfig.DEBUG) Timber.plant(object : Timber.DebugTree() {
			override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
				super.log(priority, "DEBUG_$tag", message, t)
			}
		})

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)  {
			permissionResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
		}

		databaseUtil = DatabaseUtil(this)
		homeViewModel = ViewModelProvider(this, HomeViewModelFactory(MusicRepository(databaseUtil)))[HomeViewModel::class.java]
		scanMusicViewModel = ViewModelProvider(this, ScanMusicViewModelFactory(MusicRepository(databaseUtil)))[ScanMusicViewModel::class.java]

		setContent {
			MusicomposeTheme {
				Surface(color = MaterialTheme.colors.background) {
					Screen()
				}
			}
		}
	}

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
					homeViewModel = homeViewModel
				)
			}

			composable(MusicomposeDestination.ScanMusicScreen) {
				ScanMusicScreen(
					navController = navigationController,
					scanMusicViewModel = scanMusicViewModel
				)
			}

		}
	}
}