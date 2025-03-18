package com.example.melovibes.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val items = listOf(
        Triple("Songs", Icons.Default.MusicNote, 0),
        Triple("Playlists", Icons.AutoMirrored.Filled.QueueMusic, 3),
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        items.forEach { (title, icon, index) ->
            NavigationBarItem(
                icon = {
                    Icon(
                        icon,
                        contentDescription = title,
                        tint = if (selectedTab == index) Color.Black else Color.Gray,


                    )
                },
                label = {
                    Text(
                        title,
                        color = if (selectedTab == index) Color.Black else Color.Gray,

                    )
                },
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
            )
        }
    }
}
