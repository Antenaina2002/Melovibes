package com.example.melovibes.ui.components

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
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

    // Toggle the selection of a song
    fun onToggleSelection(song: Song) {
        if (selectedSongs.contains(song)) {
            selectedSongs.remove(song)
        } else {
            selectedSongs.add(song)
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
                            viewModel.enableSelectionMode() // Switch to selection mode on long press
                        }
                        onToggleSelection(song)
                    },
                    onToggleSelection = { onToggleSelection(song) },
                    onMoreOptionsClick = {
                        selectedSongForOptions = song // Set the song for options menu
                        showOptionsDialog = true
                    }
                )
                HorizontalDivider()
            }
        }
    }

    // Show the overlay button when multiple songs are selected
    if (showOverlayButton) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { showOptionsDialog = true }, // Show options dialog for multiple songs
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options") // Three dots icon
            }
        }
    }

    // Show the options dialog when the button is clicked (for single song or multiple songs)
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
                            // If a single song is selected, add it to the selectedSongs list
                            if (selectedSongForOptions != null) {
                                selectedSongs.clear() // Clear any previous selections
                                selectedSongs.add(selectedSongForOptions!!)
                            }
                            showAddToPlaylistDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add to Playlist")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            showOptionsDialog = false
                            // Delete selected song or songs
                            if (selectedSongForOptions != null) {
                                viewModel.deleteSongs(listOf(selectedSongForOptions!!))
                            } else {
                                viewModel.deleteSongs(selectedSongs)
                            }
                            selectedSongs.clear() // Clear selection after deletion
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(Color.Red)
                    ) {
                        Text("Delete Song(s)")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedSongForOptions != null) {
                        Button(
                            onClick = {
                                showOptionsDialog = false
                                viewModel.setAsRingtone(selectedSongForOptions!!)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Set as Ringtone")
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = { showOptionsDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }

    // Show the dialog to add songs to a playlist (for multiple selections)
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