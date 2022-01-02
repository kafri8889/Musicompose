package com.anafthdev.musicompose.data

object MusicomposeDestination {

    private const val HomeScreen = "Home"

    private const val ScanMusicScreen = "ScanMusic"

    private const val SearchScreen = "Search"

    private const val SearchSongScreen = "SearchSong"

    private const val ArtistScreen = "Artist"

    private const val AlbumScreen = "Album"

    private const val PlaylistScreen = "Playlist"

    sealed class Screen(val route: String)

    object Home: Screen(HomeScreen)

    object ScanMusic: Screen(ScanMusicScreen)

    object Search: Screen(SearchScreen)

    object SearchSong: Screen("$SearchSongScreen}/{playlistID}") {
        fun createRoute(playlistID: Int) = "$SearchSongScreen}/$playlistID"
    }

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