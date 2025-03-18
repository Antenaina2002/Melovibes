package com.example.musicplayer.ui.screens

import SongItem
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.melovibes.model.Playlist
import com.example.melovibes.model.Song
import com.example.melovibes.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    viewModel: MusicViewModel,
    onBackClick: () -> Unit,
    onPlaySongClick: (Song) -> Unit, // Callback to play the song
    onAddSongsClick: () -> Unit // Callback to open the MusicList for adding songs
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showAddSongsDialog by remember { mutableStateOf(false) }
    var showRemoveSongsDialog by remember { mutableStateOf(false) }
    var showSongOptionsDialog by remember { mutableStateOf(false) }
    var selectedSongForOptions by remember { mutableStateOf<Song?>(null) }
    val selectedSongs = remember { mutableStateListOf<Song>() }
    val addDialogSelectedSongs = remember { mutableStateListOf<Song>() }
    val playlists by viewModel.playlists.collectAsState()
    val currentPlaylist = playlists.find { it.id == playlist.id } ?: playlist

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(currentPlaylist.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Delete playlist button in the top app bar
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(Icons.Default.Delete, "Delete Playlist")
                    }
                }
            )

            if (currentPlaylist.songs.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
                    items(currentPlaylist.songs) { song ->
                        SongItem(
                            song = song,
                            isSelected = selectedSongs.contains(song),
                            onClick = {
                                if (selectedSongs.isEmpty()) {
                                    onPlaySongClick(song)
                                } else {
                                    if (selectedSongs.contains(song)) {
                                        selectedSongs.remove(song)
                                    } else {
                                        selectedSongs.add(song)
                                    }
                                }
                            },
                            onLongPress = {
                                if (!selectedSongs.contains(song)) {
                                    selectedSongs.add(song)
                                }
                            },
                            onToggleSelection = {
                                if (selectedSongs.contains(song)) {
                                    selectedSongs.remove(song)
                                } else {
                                    selectedSongs.add(song)
                                }
                            },
                            onMoreOptionsClick = {
                                selectedSongForOptions = song
                                showSongOptionsDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        HorizontalDivider()
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Button(onClick = { showAddSongsDialog = true }) {
                        Text("Add Songs")
                    }
                }
            }
        }

        // Floating Action Buttons (FAB)
        if (selectedSongs.isNotEmpty()) {
            FloatingActionButton(
                onClick = { showRemoveSongsDialog = true },
                modifier = Modifier.padding(16.dp).align(Alignment.BottomEnd)
            ) {
                Icon(Icons.Default.Delete, "Remove Songs")
            }
        } else if (currentPlaylist.songs.isNotEmpty()) {
            FloatingActionButton(
                onClick = { showAddSongsDialog = true },
                modifier = Modifier.padding(16.dp).align(Alignment.BottomEnd)
            ) {
                Icon(Icons.Default.Add, "Add Songs")
            }
        }
    }

    // Show the Add Songs Dialog
    if (showAddSongsDialog) {
        val context = LocalContext.current
        val snackbarHostState = remember { SnackbarHostState() }
        var showDuplicateWarning by remember { mutableStateOf(false) }

        Box {
            Dialog(onDismissRequest = { showAddSongsDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Select Songs to Add", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                        LazyColumn(modifier = Modifier.height(400.dp)) {
                            items(viewModel.songs.value) { song ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = addDialogSelectedSongs.contains(song),
                                        onCheckedChange = { isChecked ->
                                            if (isChecked) {
                                                addDialogSelectedSongs.add(song)
                                            } else {
                                                addDialogSelectedSongs.remove(song)
                                            }
                                        }
                                    )
                                    Text(
                                        text = song.title,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                                HorizontalDivider()
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(onClick = {
                            val existingSongIds = currentPlaylist.songs.map { it.id }.toSet()
                            val duplicateSongs = addDialogSelectedSongs.filter { it.id in existingSongIds }
                            val newSongs = addDialogSelectedSongs.filterNot { it.id in existingSongIds }

                            if (duplicateSongs.isNotEmpty()) {
                                showDuplicateWarning = true
                            } else {
                                viewModel.addSongToPlaylist(currentPlaylist, newSongs)
                                addDialogSelectedSongs.clear()
                                showAddSongsDialog = false
                            }
                        }) {
                            Text("Confirm Add")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(onClick = { showAddSongsDialog = false }) {
                            Text("Cancel")
                        }
                    }
                }
            }

            // Snackbar at the bottom
            if (showDuplicateWarning) {
                LaunchedEffect(Unit) {
                    snackbarHostState.showSnackbar("Some songs are already in the playlist.")
                    showDuplicateWarning = false
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    // Show Remove Songs Dialog
    if (showRemoveSongsDialog) {
        Dialog(onDismissRequest = { showRemoveSongsDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Remove Songs from Playlist", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    LazyColumn {
                        items(selectedSongs) { song ->
                            Text(
                                text = song.title,
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        viewModel.removeSongsFromPlaylist(currentPlaylist, selectedSongs)
                        selectedSongs.clear()
                        showRemoveSongsDialog = false
                    }) {
                        Text("Confirm Remove")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showRemoveSongsDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }

    // Delete Playlist Confirmation
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Playlist") },
            text = { Text("Are you sure you want to delete '${playlist.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removePlaylist(playlist)
                        showDeleteConfirmation = false
                        onBackClick()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Show Song Options Dialog
    if (showSongOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showSongOptionsDialog = false },
            title = { Text("Song Options") },
            text = {
                Column {
                    Button(onClick = {
                        showAddSongsDialog = true
                        showSongOptionsDialog = false
                    }) {
                        Text("Add to Playlist")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        selectedSongs.add(selectedSongForOptions!!)
                        showRemoveSongsDialog = true
                        showSongOptionsDialog = false
                    }) {
                        Text("Remove from Playlist")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSongOptionsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}