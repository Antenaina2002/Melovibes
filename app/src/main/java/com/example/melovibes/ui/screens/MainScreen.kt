package com.example.melovibes.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.melovibes.model.Playlist
import com.example.melovibes.ui.components.BottomNavigationBar
import com.example.melovibes.ui.components.MusicList
import com.example.melovibes.ui.components.NowPlaying
import com.example.melovibes.viewmodel.MusicViewModel
import com.example.musicplayer.ui.screens.PlaylistDetailScreen
import com.example.musicplayer.ui.screens.PlaylistScreen

@Composable
fun MainScreen(viewModel: MusicViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }

    // Observe StateFlow properties using collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isShuffleOn by viewModel.isShuffleOn.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val songs by viewModel.songs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

    val context = LocalContext.current // Access context here

    Scaffold(
        bottomBar = {
            Column {
                if (currentSong != null) {
                    NowPlaying(
                        song = currentSong!!,
                        isPlaying = isPlaying,
                        progress = progress,
                        currentPosition = currentPosition,
                        duration = duration,
                        isShuffleOn = isShuffleOn,
                        repeatMode = repeatMode,
                        onPlayPause = { viewModel.togglePlayPause() },
                        onNext = { viewModel.skipToNext() },
                        onPrevious = { viewModel.skipToPrevious() },
                        onSeekTo = { viewModel.seekTo(it) },
                        onShuffleClick = { viewModel.toggleShuffle() },
                        onRepeatClick = { viewModel.toggleRepeatMode() }
                    )
                }
                BottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                selectedPlaylist != null -> {
                    PlaylistDetailScreen(
                        playlist = selectedPlaylist!!,
                        viewModel = viewModel,
                        onBackClick = { selectedPlaylist = null },
                        onAddSongsClick = {
                            // Open the MusicList for adding songs
                            selectedTab = 0 // Switch to the MusicList tab
                        }
                    )
                }
                else -> when (selectedTab) {
                    0 -> MusicList(
                        songs = songs,
                        playlists = playlists,
                        onSongClick = { viewModel.playSong(it) },
                        onAddToPlaylist = { selectedSongs, targetPlaylist ->
                            // Add the selected songs to the target playlist
                            viewModel.addSongToPlaylist(targetPlaylist, selectedSongs)
                        }
                    )
                    1 -> Text("Albums")
                    2 -> Text("Artists")
                    3 -> PlaylistScreen(
                        viewModel = viewModel,
                        onPlaylistClick = { selectedPlaylist = it },
                        onImageSelected = { playlist, uri ->
                            // Handle the selected image URI for the playlist
                            viewModel.setPlaylistCover(playlist.id, uri)
                        }
                    )
                    4 -> Text("Favorites")
                }
            }
        }
    }
}