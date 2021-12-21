package com.anafthdev.musicompose.utils

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

object ComposeUtils {

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
}