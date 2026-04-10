package com.cardvr.app.ui.screens

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.cardvr.app.data.model.ConnectionState
import com.cardvr.app.data.model.RecordingState
import com.cardvr.app.ui.MainViewModel
import com.cardvr.app.ui.components.ConnectionBadge
import com.cardvr.app.ui.theme.*
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun PreviewScreen(
    viewModel: MainViewModel,
    onNavigateToFiles: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onDisconnect: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val device = uiState.device

    // RTSP stream URL (uCar DVR standard endpoint)
    val rtspUrl = "rtsp://${device.ip}:${device.rtspPort}/stream0"

    // ExoPlayer for RTSP live view
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    LaunchedEffect(rtspUrl) {
        val mediaItem = MediaItem.fromUri(rtspUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    // Recording timer
    var recordSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(uiState.recordingState) {
        if (uiState.recordingState == RecordingState.RECORDING) {
            while (true) {
                delay(1000)
                recordSeconds++
            }
        } else {
            recordSeconds = 0
        }
    }

    val isRecording = uiState.recordingState == RecordingState.RECORDING
    val isConnected = uiState.connectionState == ConnectionState.CONNECTED

    // Blinking REC dot
    val recAlpha by rememberInfiniteTransition(label = "rec")
        .animateFloat(
            initialValue = 1f, targetValue = 0.2f,
            animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
            label = "recAlpha"
        )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ── Video Surface ─────────────────────────────────────────────────────
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

        // ── Gradient overlays ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
        )

        // ── Top bar ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Connection status
            ConnectionBadge(connected = isConnected)
            Spacer(Modifier.weight(1f))

            // Recording indicator
            AnimatedVisibility(visible = isRecording) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .alpha(recAlpha)
                            .background(RecordRed, CircleShape)
                    )
                    Text(
                        text = formatTime(recordSeconds),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Settings
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .size(38.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.width(6.dp))

            // Disconnect
            IconButton(
                onClick = {
                    viewModel.disconnect()
                    onDisconnect()
                },
                modifier = Modifier
                    .size(38.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.WifiOff, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        // ── Bottom Controls ───────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Resolution + SD info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoChip(uiState.settings.resolution.label)
                InfoChip(uiState.settings.recordLoop.label)
            }

            Spacer(Modifier.height(20.dp))

            // Main controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Photo
                ControlButton(
                    icon = Icons.Default.PhotoCamera,
                    label = "Фото",
                    size = 56.dp,
                    onClick = { viewModel.takePhoto() }
                )

                // Record button (large)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clickable {
                            if (isRecording) viewModel.stopRecording()
                            else viewModel.startRecording()
                        }
                ) {
                    // Outer ring
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .border(3.dp, if (isRecording) RecordRed else Color.White, CircleShape)
                    )
                    // Inner shape
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                if (isRecording) RecordRed else Color.White,
                                if (isRecording) RoundedCornerShape(10.dp) else CircleShape
                            )
                    )
                }

                // Files
                ControlButton(
                    icon = Icons.Default.VideoLibrary,
                    label = "Видео",
                    size = 56.dp,
                    onClick = {
                        viewModel.loadFiles()
                        onNavigateToFiles()
                    }
                )
            }
        }

        // ── ADAS Overlay (if enabled) ─────────────────────────────────────────
        if (uiState.settings.adasEnabled) {
            AdasOverlay(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    size: Dp,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(size)
                .background(Color.White.copy(alpha = 0.15f), CircleShape)
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(size * 0.45f))
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun InfoChip(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun AdasOverlay(modifier: Modifier) {
    // Simple ADAS lane guide overlay
    Box(modifier = modifier.fillMaxWidth().height(200.dp)) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val strokeWidth = 3.dp.toPx()
            drawLine(
                color = Color(0xFF00FF88),
                start = androidx.compose.ui.geometry.Offset(w * 0.35f, h),
                end = androidx.compose.ui.geometry.Offset(w * 0.48f, 0f),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = Color(0xFF00FF88),
                start = androidx.compose.ui.geometry.Offset(w * 0.65f, h),
                end = androidx.compose.ui.geometry.Offset(w * 0.52f, 0f),
                strokeWidth = strokeWidth
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val h = TimeUnit.SECONDS.toHours(seconds.toLong())
    val m = TimeUnit.SECONDS.toMinutes(seconds.toLong()) % 60
    val s = seconds % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
