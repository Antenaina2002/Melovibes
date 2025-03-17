package com.example.melovibes.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentUris
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.melovibes.model.Playlist
import com.example.melovibes.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import androidx.documentfile.provider.DocumentFile
import com.example.melovibes.repository.PlaylistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> get() = _currentSong

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> get() = _isPlaying

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> get() = _progress

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> get() = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> get() = _duration

    private val _isShuffleOn = MutableStateFlow(false)
    val isShuffleOn: StateFlow<Boolean> get() = _isShuffleOn

    private val _repeatMode = MutableStateFlow(0) // 0: Off, 1: All, 2: One
    val repeatMode: StateFlow<Int> get() = _repeatMode

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> get() = _songs

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> get() = _playlists

    private val playlistRepository = PlaylistRepository(application)

    private val _isFullScreen = mutableStateOf(false)
    val isFullScreen: State<Boolean> = _isFullScreen

    private val _selectedSongs = MutableStateFlow<List<Song>>(emptyList())
    val selectedSongs: StateFlow<List<Song>> = _selectedSongs

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var originalPlaylist: List<Song> = emptyList()
    private var shuffledPlaylist: List<Song> = emptyList()

    // Map to store playlist cover URIs (playlist ID to cover URI)
    private val _playlistCovers = mutableStateMapOf<String, Uri>() // Use String for playlist ID
    val playlistCovers: Map<String, Uri> get() = _playlistCovers

    init {
        setupPlayer()
        createNotificationChannel()
        loadSongs()
        _playlists.value = playlistRepository.loadPlaylists()
    }

    private fun setupPlayer() {
        exoPlayer = ExoPlayer.Builder(getApplication()).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }

                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        when (_repeatMode.value) {
                            0 -> if (isLastSong()) pause()
                            1 -> skipToNext()
                            2 -> seekTo(0)
                        }
                    }
                }
            })
            createPeriodicProgressUpdater()
        }
        mediaSession = MediaSession.Builder(getApplication(), exoPlayer!!).build()
    }

    private fun loadSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            val songs = mutableListOf<Song>()

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
            )

            // Simplified selection to catch more MP3 files
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            try {
                getApplication<Application>().contentResolver.query(
                    collection,
                    projection,
                    selection,
                    null,
                    sortOrder
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                    val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                    while (cursor.moveToNext()) {
                        try {
                            val path = cursor.getString(pathColumn)

                            // Only add files that end with .mp3
                            if (path.endsWith(".mp3", ignoreCase = true)) {
                                val id = cursor.getLong(idColumn)
                                val title = cursor.getString(titleColumn) ?: path.substringAfterLast("/")
                                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                                val album = cursor.getString(albumColumn) ?: "Unknown Album"
                                val duration = cursor.getLong(durationColumn)
                                val albumId = cursor.getLong(albumIdColumn)

                                val albumArtUri = ContentUris.withAppendedId(
                                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                                    albumId
                                ).toString()

                                val song = Song(
                                    id = id,
                                    title = title,
                                    artist = artist,
                                    album = album,
                                    duration = duration,
                                    path = path,
                                    albumArtUri = albumArtUri
                                )
                                songs.add(song)
                            }
                        } catch (e: Exception) {
                            Log.e("MusicViewModel", "Error processing song: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Error loading songs: ${e.message}")
            }

            withContext(Dispatchers.Main) {
                _songs.value = songs
                originalPlaylist = songs
                shuffledPlaylist = songs.shuffled()
            }
        }
    }

    private fun createPeriodicProgressUpdater() {
        viewModelScope.launch {
            while (true) {
                exoPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentPosition.value = player.currentPosition
                        _duration.value = player.duration
                        _progress.value = if (player.duration > 0) {
                            player.currentPosition.toFloat() / player.duration
                        } else 0f
                    }
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "music_playback",
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used for music playback controls"
                setShowBadge(false)
            }

            val notificationManager = getApplication<Application>().getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun playSong(song: Song) {
        _currentSong.value = song
        exoPlayer?.let { player ->
            val mediaItem = MediaItem.fromUri(song.path)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
    }

    fun togglePlayPause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
    }

    fun skipToNext() {
        val currentIndex = getCurrentPlaylistIndex()
        val playlist = getCurrentPlaylist()

        if (currentIndex < playlist.size - 1) {
            playSong(playlist[currentIndex + 1])
        } else if (_repeatMode.value == 1) {
            playSong(playlist.first()) // Loop back when repeat is ON
        }
    }


    fun skipToPrevious() {
        val currentIndex = getCurrentPlaylistIndex()
        val playlist = getCurrentPlaylist()

        if (currentIndex > 0) {
            playSong(playlist[currentIndex - 1])
        } else if (_repeatMode.value == 1) {
            playSong(playlist.last()) // Loop to the last song when repeat is ON
        }
    }

    fun setFullScreen(isFullScreen: Boolean) {
        _isFullScreen.value = isFullScreen
    }


    private fun isLastSong(): Boolean {
        val currentIndex = getCurrentPlaylistIndex()
        return currentIndex == getCurrentPlaylist().size - 1
    }

    private fun getCurrentPlaylist(): List<Song> {
        return if (_isShuffleOn.value) shuffledPlaylist else originalPlaylist
    }

    private fun getCurrentPlaylistIndex(): Int {
        return getCurrentPlaylist().indexOf(_currentSong.value)
    }

    fun toggleShuffle() {
        _isShuffleOn.value = !_isShuffleOn.value

        if (_isShuffleOn.value) {
            shuffledPlaylist = originalPlaylist.shuffled()
        } else {
            shuffledPlaylist = originalPlaylist
        }

        // Ensure the current song stays in place
        val current = _currentSong.value
        if (current != null && _isShuffleOn.value) {
            shuffledPlaylist = listOf(current) + shuffledPlaylist.filter { it != current }
        }
    }


    fun toggleRepeatMode() {
        _repeatMode.value = (_repeatMode.value + 1) % 3
    }

    fun seekTo(progress: Float) {
        exoPlayer?.let { player ->
            val position = (progress * player.duration).toLong()
            player.seekTo(position)
        }
    }

    fun createPlaylist(name: String) {
        val newPlaylist = Playlist(
            id = UUID.randomUUID().toString(), // Generate a unique String ID
            name = name
        )
        // Add the new playlist to the current list of playlists
        val updatedPlaylists = _playlists.value + newPlaylist
        _playlists.value = updatedPlaylists

        // Save updated playlists to SharedPreferences
        playlistRepository.savePlaylists(updatedPlaylists)
    }

    fun addSongToPlaylist(playlist: Playlist, songs: List<Song>) {
        val updatedPlaylist = playlist.copy(songs = playlist.songs + songs)
        _playlists.value = _playlists.value.map {
            if (it.id == playlist.id) updatedPlaylist else it
        }
    }

    fun removeSongFromPlaylist(playlist: Playlist, song: Song) {
        val updatedPlaylist = playlist.copy(songs = playlist.songs - song)
        _playlists.value = _playlists.value.map {
            if (it.id == playlist.id) updatedPlaylist else it
        }
    }

    fun removePlaylist(playlist: Playlist) {
        // Remove the playlist from the list
        val updatedPlaylists = _playlists.value.filter { it.id != playlist.id }
        _playlists.value = updatedPlaylists

        // Save the updated list of playlists to SharedPreferences
        playlistRepository.savePlaylists(updatedPlaylists)
    }

    // Function to set a playlist cover
    fun setPlaylistCover(playlistId: String, coverUri: Uri) {
        _playlistCovers[playlistId] = coverUri
    }

    fun getSongFromUri(uri: Uri): Song? {
        val context = getApplication<Application>().applicationContext

        // Log the URI for debugging
        Log.d("getSongFromUri", "URI: $uri")

        // Use DocumentFile to handle the URI
        val documentFile = DocumentFile.fromSingleUri(context, uri)
        if (documentFile == null || !documentFile.exists()) {
            Log.d("getSongFromUri", "DocumentFile is null or does not exist for URI: $uri")
            return null
        }

        // Extract basic information from the DocumentFile
        val title = documentFile.name ?: "Unknown Title"
        val size = documentFile.length() // File size in bytes
        val mimeType = documentFile.type ?: "audio/*"

        // Log the file details for debugging
        Log.d("getSongFromUri", "File: $title, Size: $size, MimeType: $mimeType")

        // Check if the file is a valid audio file
        if (mimeType.startsWith("audio/") != true) {
            Log.d("getSongFromUri", "Unsupported file type: $mimeType")
            return null
        }

        // Create a Song object with the available information
        return Song(
            id = -1, // Use a placeholder ID
            title = title,
            artist = "Unknown Artist",
            album = "Unknown Album",
            duration = 0, // Duration is not available from DocumentFile
            path = uri.toString(), // Use the URI as the path
            albumArtUri = null // No album art available
        )
    }

    fun deleteSongs(songsToDelete: List<Song>) {
        Log.d("MusicViewModel", "Deleting songs: $songsToDelete")
        _songs.value = _songs.value - songsToDelete.toSet()
        _selectedSongs.value = emptyList()
        _isSelectionMode.value = false
    }

    fun setAsRingtone(song: Song?) {
        song?.let {
            val context = getApplication<Application>().applicationContext
            val uri = Uri.parse(it.path)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!Settings.System.canWrite(context)) {
                    Log.e("MusicViewModel", "Permission to modify system settings is required.")
                    return
                }
            }

            try {
                Settings.System.putString(
                    context.contentResolver,
                    Settings.System.RINGTONE,
                    uri.toString()
                )
                Log.d("MusicViewModel", "Ringtone set successfully: ${it.title}")
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Failed to set ringtone: ${e.message}")
            }
        }
    }

    fun removeSongsFromPlaylist(playlist: Playlist, songsToRemove: List<Song>) {
        viewModelScope.launch {
            val updatedSongs = playlist.songs.filterNot { it in songsToRemove }
            val updatedPlaylist = playlist.copy(songs = updatedSongs)
            _playlists.value = _playlists.value.map { if (it.id == playlist.id) updatedPlaylist else it }
        }
    }

    fun toggleSelection(song: Song) {
        _selectedSongs.value = if (_selectedSongs.value.contains(song)) {
            _selectedSongs.value - song
        } else {
            _selectedSongs.value + song
        }
        _isSelectionMode.value = _selectedSongs.value.isNotEmpty()
    }

    fun enableSelectionMode() {
        _isSelectionMode.value = true
    }

    fun disableSelectionMode() {
        _isSelectionMode.value = false
        _selectedSongs.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer?.release()
        mediaSession?.release()
    }
}