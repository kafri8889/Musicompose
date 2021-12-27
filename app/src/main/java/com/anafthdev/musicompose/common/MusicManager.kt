package com.anafthdev.musicompose.common

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.anafthdev.musicompose.R
import com.anafthdev.musicompose.model.Music
import timber.log.Timber

object MusicManager {

	fun getMusicCount(context: Context): Int {
		var count = 0

		val audioUriExternal = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
		val musicProjection = listOf(MediaStore.Audio.Media._ID)

		val musicCursor = context.contentResolver.query(
			audioUriExternal,
			musicProjection.toTypedArray(),
			null,
			null,
			null
		)

		if (musicCursor != null) {
			while (musicCursor.moveToNext()) {
				count += 1
			}

			musicCursor.close()
		}

		return count
	}
	
	fun getMusic(
		context: Context,
		scannedMusicCount: (Int) -> Unit,
	): ArrayList<Music> {
		val audioList = ArrayList<Music>()
		var scannedMusic = 0
		
		val audioUriExternal = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

		val musicProjection = listOf(
			MediaStore.Audio.Media._ID,
			MediaStore.Audio.Media.DISPLAY_NAME,
			MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.ARTIST,
			MediaStore.Audio.Media.ALBUM,
			MediaStore.Audio.Media.DURATION,
			MediaStore.Audio.Media.ALBUM_ID,
			MediaStore.Audio.Media.DATE_ADDED,
		)

		val cursorIndexMusicID: Int
		val cursorIndexMusicDisplayName: Int
		val cursorIndexMusicTitle: Int
		val cursorIndexMusicArtist: Int
		val cursorIndexMusicAlbum: Int
		val cursorIndexMusicDuration: Int
		val cursorIndexMusicAlbumID: Int
		val cursorIndexMusicDateAdded: Int

		val musicCursor = context.contentResolver.query(
			audioUriExternal,
			musicProjection.toTypedArray(),
			null,
			null,
			null
		)

		if (musicCursor != null) {
			cursorIndexMusicID = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
			cursorIndexMusicDisplayName = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
			cursorIndexMusicTitle = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
			cursorIndexMusicArtist = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
			cursorIndexMusicAlbum = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
			cursorIndexMusicDuration = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
			cursorIndexMusicAlbumID = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
			cursorIndexMusicDateAdded = musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

			while (musicCursor.moveToNext()) {
				val audioID = musicCursor.getLong(cursorIndexMusicID)
				val displayName = musicCursor.getString(cursorIndexMusicDisplayName)
				val title = musicCursor.getString(cursorIndexMusicTitle)
				val artist = musicCursor.getString(cursorIndexMusicArtist)
				val album = musicCursor.getString(cursorIndexMusicAlbum)
				val duration = musicCursor.getLong(cursorIndexMusicDuration)
				val albumId = musicCursor.getString(cursorIndexMusicAlbumID)
				val dateAdded = musicCursor.getLong(cursorIndexMusicDateAdded)

				val albumPath = Uri.withAppendedPath(Uri.parse("content://media/external/audio/albumart"), albumId)
				val path = Uri.withAppendedPath(audioUriExternal, "" + audioID)

				audioList.add(
					Music(
						audioID = audioID,
						displayName = displayName,
						title = title,
						artist = if (artist.equals("<unknown>", true)) context.getString(R.string.unknown) else artist,
						album = album,
						albumID = albumId,
						duration = duration,
						albumPath = albumPath.toString(),
						path = path.toString(),
						dateAdded = dateAdded
					)
				)

				scannedMusic += 1
				scannedMusicCount(scannedMusic)
			}

			musicCursor.close()
		}
		
		Timber.i("Audio List: $audioList")
		return audioList
	}
}