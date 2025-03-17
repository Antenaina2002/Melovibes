package com.example.melovibes.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.example.melovibes.model.Playlist

class PlaylistRepository(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("playlists", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Save playlists to SharedPreferences
    fun savePlaylists(playlists: List<Playlist>) {
        val playlistsJson = gson.toJson(playlists)
        sharedPreferences.edit().putString("playlists", playlistsJson).apply()
    }

    // Load playlists from SharedPreferences
    fun loadPlaylists(): List<Playlist> {
        val playlistsJson = sharedPreferences.getString("playlists", null)
        return if (playlistsJson != null) {
            gson.fromJson(playlistsJson, Array<Playlist>::class.java).toList()
        } else {
            emptyList()
        }
    }
}

