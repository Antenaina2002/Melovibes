package com.example.melovibes.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.internal.concurrent.formatDuration
import androidx.compose.ui.text.TextStyle

@Composable
fun NowPlaying(
    song: Song,
    songList: List<Song>,
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
            .height(height = animateDpAsState(if (isFullScreen) 550.dp else 72.dp).value)
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
                            MarqueeText(
                                text = song.title,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth()
                            )
                            song.artist?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // Add Previous Button
                    IconButton(onClick = onPrevious) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = "Previous"
                        )
                    }

                    // Play/Pause Button
                    IconButton(onClick = { onPlayPause() }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pause"
                        )
                    }

                    // Add Next Button
                    IconButton(onClick = onNext) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "Next"
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
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )

                    song.artist?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            maxLines = 1,
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
                            onClick = onShuffleClick,
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

@Composable
fun MarqueeText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    // State to track if the text overflows the available width
    var textWidth by remember { mutableStateOf(0) }
    var containerWidth by remember { mutableStateOf(0) }

    // Determine if the text needs to scroll
    val shouldScroll = textWidth > containerWidth

    // Scroll state for the animation
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Animation logic
    LaunchedEffect(shouldScroll, scrollState.maxValue) {
        if (shouldScroll) {
            while (true) {
                // Scroll to the end (left to right)
                coroutineScope.launch {
                    scrollState.animateScrollTo(
                        textWidth, // Scroll to the full width of the text
                        animationSpec = infiniteRepeatable(tween(20000)) // Slower animation (10 seconds)
                    )
                }
                delay(9000) // Pause before restarting the scroll
                // Reset scroll position to start
                coroutineScope.launch {
                    scrollState.scrollTo(0)
                }
                delay(2000) // Pause before restarting the scroll
            }
        }
    }

    // Measure the width of the text and the container
    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { containerWidth = it.width }
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState, reverseScrolling = false) // Scroll left to right
                .fillMaxWidth()
        ) {
            Text(
                text = text,
                style = style,
                maxLines = 1,
                modifier = Modifier
                    .padding(end = if (shouldScroll) 16.dp else 0.dp) // Add spacing only if scrolling
                    .onSizeChanged { textWidth = it.width }
            )
            if (shouldScroll) {
                Text(
                    text = text,
                    style = style,
                    maxLines = 1,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }
    }
}
