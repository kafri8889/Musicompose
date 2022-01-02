package com.anafthdev.musicompose.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.ViewTreeObserver
import android.view.WindowInsets
import androidx.activity.ComponentActivity
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.palette.graphics.Palette
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.ui.theme.primary_light
import timber.log.Timber
import java.io.FileNotFoundException

object ComposeUtils {

    @SuppressWarnings("ComposableNaming")
    @Composable
    fun getDominantColor(context: Context, uri: Uri, onGenerated: (Palette) -> Unit) {
        Palette.Builder(
            run {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(
                            ImageDecoder.createSource(
                                context.contentResolver,
                                uri
                            )
                        ).copy(Bitmap.Config.RGBA_F16, true)
                    } else MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } catch (e: FileNotFoundException) {
                    Timber.e(e)
                    return@run ContextCompat.getDrawable(context, R.drawable.ic_music_unknown)!!.toBitmap()
                }
            }
        ).generate { it?.let { palette ->
            onGenerated(palette)
        } }
    }

    /**
     * Activity Lifecycle
     * @author kafri8889
     */
    @Composable
    fun ComponentActivity.LifecycleEventListener(event: (Lifecycle.Event) -> Unit) {
        val eventHandler by rememberUpdatedState(newValue = event)
        val lifecycle = this@LifecycleEventListener.lifecycle
        DisposableEffect(lifecycle) {
            val observer = LifecycleEventObserver { _, event ->
                eventHandler(event)
            }

            lifecycle.addObserver(observer)

            onDispose {
                lifecycle.removeObserver(observer)
            }
        }
    }

    /**
     * darken the color
     * @param color the color
     * @param factor shade factor (from 0 to 1)
     * @author kafri8889
     */
    fun darkenColor(color: Color, factor: Float): Color {
        return Color(
            red = color.red * (1 - factor),
            green = color.green * (1 - factor),
            blue = color.blue * (1 - factor)
        )
    }

    /**
     * lighten the color
     * @param color the color
     * @param factor tint factor (from 0 to 1)
     * @author kafri8889
     */
    fun lightenColor(color: Color, factor: Float): Color {
        return Color(
            red = color.red + (1 - color.red) * factor,
            green = color.green + (1 - color.green) * factor,
            blue = color.blue + (1 - color.blue) * factor
        )
    }

    @OptIn(ExperimentalMaterialApi::class)
    val BottomSheetScaffoldState.currentFraction: Float
        get() {
            val fraction = bottomSheetState.progress.fraction
            val targetValue = bottomSheetState.targetValue
            val currentValue = bottomSheetState.currentValue

            return when {
                currentValue == BottomSheetValue.Collapsed && targetValue == BottomSheetValue.Collapsed -> 0f
                currentValue == BottomSheetValue.Expanded && targetValue == BottomSheetValue.Expanded -> 1f
                currentValue == BottomSheetValue.Collapsed && targetValue == BottomSheetValue.Expanded -> fraction
                else -> 1f - fraction
            }
        }
}