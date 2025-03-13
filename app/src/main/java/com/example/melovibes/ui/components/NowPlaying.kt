package com.example.melovibes.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.melovibes.model.Song
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import okhttp3.internal.concurrent.formatDuration

@Composable
fun NowPlaying(
    song: Song,
    songList: List<Song>, // Add this parameter
    isPlaying: Boolean,
    progress: Float,
    currentPosition: Long,
    duration: Long,
    isShuffleOn: Boolean,
    repeatMode: Int,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Float) -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit
) {
    var isFullScreen by remember { mutableStateOf(false) }

    val toggleSize = {
        isFullScreen = !isFullScreen
    }

    Surface(
        tonalElevation = 8.dp,
        shape = if (isFullScreen) RoundedCornerShape(0.dp) else RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(height = animateDpAsState(if (isFullScreen) 550.dp else 72.dp).value) // Increased height
            .clickable { toggleSize() }
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mini player
            AnimatedVisibility(visible = !isFullScreen) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(song.albumArtUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Album Art",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            song.artist?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    IconButton(onClick = { onPlayPause() }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pause"
                        )
                    }
                }
            }

            // Expanded Now Playing (full-screen)
            AnimatedVisibility(
                visible = isFullScreen,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Large Album Art
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(song.albumArtUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Album Art",
                        modifier = Modifier
                            .size(250.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,  // Prevents text from wrapping
                        modifier = Modifier.fillMaxWidth()
                    )

                    song.artist?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            maxLines = 1,  // Ensures artist name stays in a single line
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    }

                    // Seek bar
                    Slider(
                        value = progress,
                        onValueChange = onSeekTo,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatDuration(currentPosition), style = MaterialTheme.typography.bodySmall)
                        Text(formatDuration(duration), style = MaterialTheme.typography.bodySmall)
                    }

                    // Playback controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onShuffleClick, // Trigger shuffle change
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (isShuffleOn) Icons.Filled.Shuffle else Icons.Outlined.Shuffle,
                                contentDescription = "Shuffle",
                                modifier = Modifier.size(32.dp),
                                tint = if (isShuffleOn) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }

                        // Other buttons: Previous, Play/Pause, Next, Repeat
                        IconButton(
                            onClick = onPrevious,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipPrevious,
                                contentDescription = "Previous",
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        IconButton(
                            onClick = onPlayPause,
                            modifier = Modifier
                                .size(72.dp)
                                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        IconButton(
                            onClick = onNext,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipNext,
                                contentDescription = "Next",
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        IconButton(
                            onClick = onRepeatClick,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = when (repeatMode) {
                                    2 -> Icons.Filled.RepeatOne
                                    1 -> Icons.Filled.Repeat
                                    else -> Icons.Filled.RepeatOn
                                },
                                contentDescription = "Repeat",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                }
            }
        }
    }
}
