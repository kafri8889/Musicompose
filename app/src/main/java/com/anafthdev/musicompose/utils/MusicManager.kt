package com.anafthdev.musicompose.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import com.anafthdev.musicompose.model.Music
import timber.log.Timber

object MusicManager {

	fun getMusicCount(context: Context): Int {
		var count = 0

		val audioUriExternal = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
		val projection = listOf(MediaStore.Audio.Media._ID)

		val cursor = context.contentResolver.query(
			audioUriExternal,
			projection.toTypedArray(),
			null,
			null,
			null
		)

		if (cursor != null) {
			while (cursor.moveToNext()) {
				count += 1
			}

			cursor.close()
		}

		return count
	}
	
	fun getMusic(context: Context, scannedMusicCount: (Int) -> Unit): ArrayList<Music> {
		val audioList = ArrayList<Music>()
		var scannedMusic = 0
		
		val audioUriExternal = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
		val projection = listOf(
			MediaStore.Audio.Media._ID,
			MediaStore.Audio.Media.DISPLAY_NAME,
			MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.ARTIST,
			MediaStore.Audio.Media.ALBUM,
			MediaStore.Audio.Media.DURATION,
			MediaStore.Audio.Media.ALBUM_ID,
			MediaStore.Audio.Media.DATE_ADDED,
		)
		
		val cursorIndexID: Int
		val cursorIndexDisplayName: Int
		val cursorIndexTitle: Int
		val cursorIndexArtist: Int
		val cursorIndexAlbum: Int
		val cursorIndexDuration: Int
		val cursorIndexAlbumID: Int
		val cursorIndexDateAdded: Int

		val cursor = context.contentResolver.query(
			audioUriExternal,
			projection.toTypedArray(),
			null,
			null,
			null
		)
		
		if (cursor != null) {
			cursorIndexID = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
			cursorIndexDisplayName = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
			cursorIndexTitle = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
			cursorIndexArtist = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
			cursorIndexAlbum = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
			cursorIndexDuration = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
			cursorIndexAlbumID = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
			cursorIndexDateAdded = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

			while (cursor.moveToNext()) {
				val audioID = cursor.getLong(cursorIndexID)
				val displayName = cursor.getString(cursorIndexDisplayName)
				val title = cursor.getString(cursorIndexTitle)
				val artist = cursor.getString(cursorIndexArtist)
				val album = cursor.getString(cursorIndexAlbum)
				val duration = cursor.getLong(cursorIndexDuration)
				val albumId = cursor.getString(cursorIndexAlbumID)
				val dateAdded = cursor.getLong(cursorIndexDateAdded)

				val albumPath = Uri.withAppendedPath(Uri.parse("content://media/external/audio/albumart"), albumId)
				val path = Uri.withAppendedPath(audioUriExternal, "" + audioID)

				audioList.add(
					Music(
						audioID = audioID,
						displayName = displayName,
						title = title,
						artist = artist,
						album = album,
						duration = duration,
						albumPath = albumPath.toString(),
						path = path.toString(),
						dateAdded = dateAdded
					)
				)

				scannedMusic += 1
				scannedMusicCount(scannedMusic)
			}

			cursor.close()
		}
		
		Timber.i("Audio List: $audioList")
		return audioList
	}

	private fun getMusicThumbnailByteArray(context: Context, uri: Uri): ByteArray? {
		val mmr = MediaMetadataRetriever().apply { setDataSource(context, uri) }

		val byteArray = mmr.embeddedPicture
		mmr.release()

		return byteArray
	}
}