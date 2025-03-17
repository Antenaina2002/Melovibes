package com.example.melovibes.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.melovibes.model.Song
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongItem(
    song: Song,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onToggleSelection: () -> Unit,
    onMoreOptionsClick: () -> Unit, // This is required for the dots button
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelected) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() }
            )
        }

        Column(
            modifier = Modifier.weight(1f).padding(start = 8.dp)
        ) {
            Text(song.title, fontWeight = FontWeight.Bold)
            Text(song.artist ?: "Unknown Artist", fontSize = 14.sp, color = Color.Gray)
        }

        IconButton(onClick = onMoreOptionsClick) {
            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
        }
    }
}