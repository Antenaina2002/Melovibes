package com.example.melovibes.model

data class Song(
    val id: Long,
    val path: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val duration: Long,
    val albumArtUri: String? = null,

)
