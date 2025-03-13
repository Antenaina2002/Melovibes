package com.example.melovibes.model

import android.net.Uri

data class Playlist(
    val id: String,
    val name: String,
    val songs: List<Song> = emptyList(),
    val coverUri: Uri? = null
)
