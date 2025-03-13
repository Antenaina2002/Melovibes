package com.example.melovibes.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar {
        val items = listOf(
            Triple("Songs", Icons.Default.MusicNote, 0),
            Triple("Playlists", Icons.Default.QueueMusic, 3),
        )

        items.forEach { (title, icon, index) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = title) },
                label = { Text(title) },
                selected = selectedTab == index,
                onClick = { onTabSelected(index) }
            )
        }
    }
}