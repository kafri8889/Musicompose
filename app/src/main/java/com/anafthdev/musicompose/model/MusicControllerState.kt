package com.anafthdev.musicompose.model

import androidx.compose.material.*

data class MusicControllerState @OptIn(ExperimentalMaterialApi::class) constructor(
    val playlistScaffoldBottomSheetState: BottomSheetScaffoldState,
    val musicScaffoldBottomSheetState: BottomSheetScaffoldState,
    val musicMoreOptionModalBottomSheetState: ModalBottomSheetState,
    val addToPlaylistModalBottomSheetState: ModalBottomSheetState,
    val setTimerModalBottomSheetState: ModalBottomSheetState,
) {
    companion object {

        @OptIn(ExperimentalMaterialApi::class)
        val initial = MusicControllerState(
            playlistScaffoldBottomSheetState = BottomSheetScaffoldState(
                drawerState = DrawerState(DrawerValue.Closed),
                bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed),
                snackbarHostState = SnackbarHostState()
            ),
            musicScaffoldBottomSheetState = BottomSheetScaffoldState(
                drawerState = DrawerState(DrawerValue.Closed),
                bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed),
                snackbarHostState = SnackbarHostState()
            ),
            musicMoreOptionModalBottomSheetState = ModalBottomSheetState(
                ModalBottomSheetValue.Hidden
            ),
            addToPlaylistModalBottomSheetState = ModalBottomSheetState(
                ModalBottomSheetValue.Hidden
            ),
            setTimerModalBottomSheetState = ModalBottomSheetState(
                ModalBottomSheetValue.Hidden
            ),
        )

    }
}
