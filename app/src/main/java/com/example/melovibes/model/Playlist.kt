package com.example.melovibes.model

import android.net.Uri
import com.google.gson.Gson

data class Playlist(
    val id: String,
    val name: String,
    val songs: List<Song> = emptyList(),
    val coverUri: Uri? = null
) {
    fun toJson(): String {
        return Gson().toJson(this)
    }

    companion object {
        fun fromJson(json: String): Playlist {
            return Gson().fromJson(json, Playlist::class.java)
        }
    }
}
