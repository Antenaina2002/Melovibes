package com.example.melovibes.ui.components

import SongItem
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.melovibes.model.Playlist
import com.example.melovibes.model.Song
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.melovibes.viewmodel.MusicViewModel

@Composable
fun MusicList(
    songs: List<Song>,
    playlists: List<Playlist>,
    onSongClick: (Song) -> Unit,
    onAddToPlaylist: (List<Song>, Playlist) -> Unit
) {
    val viewModel: MusicViewModel = viewModel()
    val selectedSongs = remember { mutableStateListOf<Song>() }
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showOptionsDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var selectedSongForOptions by remember { mutableStateOf<Song?>(null) }
    var selectedPlaylistForAdd by remember { mutableStateOf<Playlist?>(null) }

    fun onToggleSelection(song: Song) {
        if (selectedSongs.contains(song)) {
            selectedSongs.remove(song)

            if (selectedSongs.isEmpty()) {
                viewModel.disableSelectionMode()
            }
        } else {
            selectedSongs.add(song)
            if (!isSelectionMode) {
                viewModel.enableSelectionMode()
            }
        }
    }

    // Show overlay button when there are selected songs
    val showOverlayButton = selectedSongs.isNotEmpty()

    // Search functionality and song list
    Column {
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        LazyColumn {
            items(songs.filter { it.title.contains(searchQuery, ignoreCase = true) }) { song ->
                SongItem(
                    song = song,
                    isSelected = selectedSongs.contains(song),
                    onClick = {
                        if (isSelectionMode) {
                            onToggleSelection(song)
                        } else {
                            onSongClick(song)
                        }
                    },
                    onLongPress = {
                        if (!isSelectionMode) {
                            viewModel.enableSelectionMode()
                        }
                        onToggleSelection(song)
                    },
                    onToggleSelection = { onToggleSelection(song) },
                    onMoreOptionsClick = {
                        selectedSongForOptions = song
                        showOptionsDialog = true
                    }
                )
                HorizontalDivider()
            }
        }
    }

    if (showOverlayButton) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { showOptionsDialog = true },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options") // Three dots icon
            }
        }
    }

    if (showOptionsDialog) {
        Dialog(onDismissRequest = { showOptionsDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        if (selectedSongForOptions != null) "Select an Action" else "Select an Action for Selected Songs",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            showOptionsDialog = false
                            if (selectedSongForOptions != null) {
                                selectedSongs.clear()
                                selectedSongs.add(selectedSongForOptions!!)
                            }
                            showAddToPlaylistDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add to Playlist",color = MaterialTheme.colorScheme.inverseOnSurface)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            showOptionsDialog = false
                            if (selectedSongForOptions != null) {
                                viewModel.deleteSongs(listOf(selectedSongForOptions!!))
                            } else {
                                viewModel.deleteSongs(selectedSongs)
                            }
                            selectedSongs.clear()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Delete Song(s)",color = MaterialTheme.colorScheme.inverseOnSurface)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedSongForOptions != null)
                    Button(
                        onClick = { showOptionsDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel",color = MaterialTheme.colorScheme.inverseOnSurface)
                    }
                }
            }
        }
    }

    if (showAddToPlaylistDialog) {
        Dialog(onDismissRequest = { showAddToPlaylistDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Playlist to Add Songs", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn {
                        items(playlists) { playlist ->
                            TextButton(
                                onClick = {
                                    selectedPlaylistForAdd = playlist
                                    // Call the onAddToPlaylist function with the selected songs
                                    onAddToPlaylist(selectedSongs, playlist)
                                    showAddToPlaylistDialog = false
                                    selectedSongs.clear() // Clear selected songs after adding
                                },
                                modifier = Modifier.fillMaxWidth().padding(8.dp)
                            ) {
                                Text(playlist.name)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showAddToPlaylistDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}