package com.example.melovibes.model

import com.google.gson.Gson

data class Song(
    val id: Long,
    val path: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val duration: Long,
    val albumArtUri: String? = null,

){
    fun toJson(): String {
        return Gson().toJson(this)
    }

    companion object {
        fun fromJson(json: String): Song {
            return Gson().fromJson(json, Song::class.java)
        }
    }
}
