package com.cardvr.app.ui.screens

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.cardvr.app.ui.MainViewModel
import com.cardvr.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun PlaybackScreen(
    viewModel: MainViewModel,
    fileName: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val device = uiState.device

    val file = uiState.files.find { it.name == fileName }
    val videoUrl = file?.url ?: "http://${device.ip}/$fileName"

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    LaunchedEffect(videoUrl) {
        val mediaItem = MediaItem.fromUri(videoUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    var showControls by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }
    val speeds = listOf(0.5f, 1f, 1.5f, 2f, 4f)

    // Auto-hide controls
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(3500)
            showControls = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { showControls = !showControls }
    ) {
        // Video
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Controls overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top gradient + back
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        fileName,
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.weight(1f))
                    // Lock indicator
                    file?.let {
                        if (it.isLocked) {
                            Icon(Icons.Default.Lock, null, tint = LockGold, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Bottom gradient + controls
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Progress slider
                    var progress by remember { mutableFloatStateOf(0f) }
                    LaunchedEffect(exoPlayer) {
                        while (true) {
                            val duration = exoPlayer.duration.coerceAtLeast(1)
                            progress = exoPlayer.currentPosition / duration.toFloat()
                            delay(500)
                        }
                    }

                    Slider(
                        value = progress,
                        onValueChange = { p ->
                            progress = p
                            exoPlayer.seekTo((p * exoPlayer.duration).toLong())
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Primary,
                            activeTrackColor = Primary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            formatMs(exoPlayer.currentPosition),
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            formatMs(exoPlayer.duration),
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Playback controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // -10s
                        IconButton(onClick = {
                            exoPlayer.seekTo((exoPlayer.currentPosition - 10_000).coerceAtLeast(0))
                        }) {
                            Icon(Icons.Default.Replay10, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }

                        // Play/Pause
                        IconButton(
                            onClick = {
                                isPlaying = !isPlaying
                                if (isPlaying) exoPlayer.play() else exoPlayer.pause()
                            },
                            modifier = Modifier
                                .size(60.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        // +10s
                        IconButton(onClick = {
                            exoPlayer.seekTo(
                                (exoPlayer.currentPosition + 10_000).coerceAtMost(exoPlayer.duration)
                            )
                        }) {
                            Icon(Icons.Default.Forward10, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }

                        // Speed
                        Surface(
                            onClick = {
                                val idx = speeds.indexOf(playbackSpeed)
                                playbackSpeed = speeds[(idx + 1) % speeds.size]
                                exoPlayer.setPlaybackSpeed(playbackSpeed)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(width = 52.dp, height = 36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "${playbackSpeed}x".removeSuffix(".0x") + "x",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    if (ms < 0) return "0:00"
    val totalSec = ms / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    return "%d:%02d".format(m, s)
}
