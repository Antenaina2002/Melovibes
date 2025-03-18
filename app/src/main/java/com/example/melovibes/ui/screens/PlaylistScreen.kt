package com.example.musicplayer.ui.screens

import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.melovibes.model.Playlist
import com.example.melovibes.ui.screens.PlaylistItem
import com.example.melovibes.viewmodel.MusicViewModel

@Composable
fun PlaylistScreen(
    viewModel: MusicViewModel,
    onPlaylistClick: (Playlist) -> Unit,
    onImageSelected: (Playlist, Uri) -> Unit // Callback for image selection
) {
    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }
    var playlistToEdit by remember { mutableStateOf<Playlist?>(null) }

    val context = LocalContext.current // Access context here
    val playlists by viewModel.playlists.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Use LazyVerticalGrid to display playlists in a grid
            if (playlists.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // Display 2 items per row
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(playlists) { playlist ->
                        PlaylistItem(
                            playlist = playlist,
                            coverUri = viewModel.playlistCovers[playlist.id], // Get cover URI from ViewModel
                            onPlaylistClick = { onPlaylistClick(playlist) },
                            onDeleteClick = { playlistToDelete = playlist },
                            onEditCoverClick = { playlistToEdit = playlist }
                        )
                    }
                }
            } else {
                // Show centered message if there are no playlists
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¯\\_(ツ)_/¯",
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "no playlist yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // FloatingActionButton positioned above the footer
        FloatingActionButton(
            onClick = { showNewPlaylistDialog = true },
            modifier = Modifier
                .padding(16.dp)
                .size(70.dp)
                .align(Alignment.BottomEnd),
            shape = RoundedCornerShape(40.dp)
        ) {
            Icon(Icons.Default.Add, "Create Playlist")
        }
    }

    // Create playlist dialog
    if (showNewPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showNewPlaylistDialog = false },
            title = { Text("Create New Playlist") },
            text = {
                TextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Playlist Name") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            viewModel.createPlaylist(newPlaylistName)
                            showNewPlaylistDialog = false
                            newPlaylistName = ""
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewPlaylistDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = MaterialTheme.shapes.medium // Optional, adds rounded corners
        )

    }

    // Image Picker Dialog
    playlistToEdit?.let { playlist ->
        AlertDialog(
            onDismissRequest = { playlistToEdit = null },
            title = { Text("Change Playlist Cover") },
            text = {
                Text("Select an image from your gallery to set as the playlist cover.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Launch the image picker
                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "image/*"
                        }
                        context.startActivity(intent) // Use the pre-fetched context
                        playlistToEdit = null
                    }
                ) {
                    Text("Choose Image")
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}