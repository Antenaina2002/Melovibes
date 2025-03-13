package com.example.melovibes.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth

@Composable
fun SongItem(
    song: Song,
    isSelected: Boolean, // Whether the song is selected
    onClick: () -> Unit, // Callback when the song is clicked
    onToggleSelection: () -> Unit // Callback when the selection is toggled
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox for selection
        Checkbox(
            checked = isSelected,
            onCheckedChange = { isChecked -> onToggleSelection() } // Forcer la mise à jour
        )

        // Song details
        Column(
            modifier = Modifier
                .weight(1f) // Make the column take up the remaining space
                .padding(start = 8.dp)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge
            )
            song.artist?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
