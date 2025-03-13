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
    var searchQuery by remember { mutableStateOf("") }
    val filteredSongs = songs.filter { song ->
        song.title.contains(searchQuery, ignoreCase = true) ||
                (song.artist?.contains(searchQuery, ignoreCase = true) ?: false) ||
                (song.album?.contains(searchQuery, ignoreCase = true) ?: false)
    }

    val selectedSongs = remember { mutableStateListOf<Song>() }
    var showOverlayButton by remember { mutableStateOf(false) }
    var showOptionsDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    val viewModel: MusicViewModel = viewModel()

    Column {
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        LazyColumn {
            items(filteredSongs) { song ->
                SongItem(
                    song = song,
                    isSelected = selectedSongs.contains(song),
                    onClick = { onSongClick(song) },
                    onToggleSelection = {
                        if (selectedSongs.contains(song)) {
                            selectedSongs.remove(song)
                        } else {
                            selectedSongs.add(song)
                        }
                        showOverlayButton = selectedSongs.isNotEmpty()
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
                Icon(Icons.Default.MoreVert, contentDescription = "Options")
            }
        }
    }

    // Options Dialog in the Center of the Screen
    if (showOptionsDialog) {
        Dialog(onDismissRequest = { showOptionsDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Select an Action", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            showOptionsDialog = false
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
                            viewModel.deleteSongs(selectedSongs)
                            selectedSongs.clear()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(Color.Red)
                    ) {
                        Text("Delete Song")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            showOptionsDialog = false
                            viewModel.setAsRingtone(selectedSongs.firstOrNull())
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set as Ringtone")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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

    // Playlist Selection Dialog
    if (showAddToPlaylistDialog) {
        Dialog(onDismissRequest = { showAddToPlaylistDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Playlist", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    LazyColumn {
                        items(playlists) { playlist ->
                            Text(
                                text = playlist.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAddToPlaylist(selectedSongs, playlist)
                                        showAddToPlaylistDialog = false
                                    }
                                    .padding(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showAddToPlaylistDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

