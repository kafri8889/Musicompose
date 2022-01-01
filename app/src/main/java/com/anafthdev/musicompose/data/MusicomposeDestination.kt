package com.anafthdev.musicompose.data

object MusicomposeDestination {

    const val HomeScreen = "Home"

    const val ScanMusicScreen = "ScanMusic"

    const val SearchScreen = "Search"

    const val ArtistScreen = "Artist"

    const val AlbumScreen = "Album"

    const val PlaylistScreen = "Playlist"

    sealed class Screen(val route: String) {
        object Home: Screen(HomeScreen)

        object ScanMusic: Screen(ScanMusicScreen)

        object Search: Screen(SearchScreen)

        object Artist: Screen("${ArtistScreen}/{artistName}") {
            fun createRoute(artistName: String) = "${ArtistScreen}/$artistName"
        }

        object Album: Screen("${AlbumScreen}/{albumID}") {
            fun createRoute(albumID: String) = "${AlbumScreen}/$albumID"
        }

        object Playlist: Screen("$PlaylistScreen}/{playlistID}") {
            fun createRoute(playlistID: Int) = "$PlaylistScreen}/$playlistID"
        }
    }
}