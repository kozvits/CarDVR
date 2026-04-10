package com.cardvr.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.cardvr.app.data.model.DvrFile
import com.cardvr.app.ui.MainViewModel
import com.cardvr.app.ui.components.*
import com.cardvr.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileListScreen(
    viewModel: MainViewModel,
    onPlayVideo: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<DvrFile?>(null) }
    var showFormatDialog by remember { mutableStateOf(false) }
    var filterPhotos by remember { mutableStateOf(false) }

    val displayFiles = if (filterPhotos)
        uiState.files.filter { it.isPhoto }
    else
        uiState.files.filter { !it.isPhoto }

    LaunchedEffect(Unit) {
        if (uiState.files.isEmpty()) viewModel.loadFiles()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            DvrTopBar(
                title = "Видеозаписи",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { viewModel.loadFiles() }) {
                        Icon(Icons.Default.Refresh, null, tint = OnSurface)
                    }
                    IconButton(onClick = { showFormatDialog = true }) {
                        Icon(Icons.Default.SdCard, null, tint = OnSurfaceVariant)
                    }
                }
            )
        },
        snackbarHost = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                StatusMessage(
                    error = uiState.errorMessage,
                    success = uiState.successMessage,
                    onDismiss = { viewModel.clearMessage() }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !filterPhotos,
                    onClick = { filterPhotos = false },
                    label = { Text("Видео") },
                    leadingIcon = { Icon(Icons.Default.Videocam, null, modifier = Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color(0xFF001F2A),
                        selectedLeadingIconColor = Color(0xFF001F2A),
                        containerColor = SurfaceVariant,
                        labelColor = OnSurface
                    )
                )
                FilterChip(
                    selected = filterPhotos,
                    onClick = { filterPhotos = true },
                    label = { Text("Фото") },
                    leadingIcon = { Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color(0xFF001F2A),
                        selectedLeadingIconColor = Color(0xFF001F2A),
                        containerColor = SurfaceVariant,
                        labelColor = OnSurface
                    )
                )

                Spacer(Modifier.weight(1f))

                Text(
                    "${displayFiles.size} файлов",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }

            if (uiState.isLoadingFiles) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Primary)
                        Spacer(Modifier.height(16.dp))
                        Text("Загрузка файлов...", color = OnSurfaceVariant)
                    }
                }
            } else if (displayFiles.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.VideoLibrary,
                            null,
                            tint = OnSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("Файлы не найдены", color = OnSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayFiles, key = { it.name }) { file ->
                        FileCard(
                            file = file,
                            onPlay = { onPlayVideo(file.name) },
                            onLock = { viewModel.lockFile(file) },
                            onDelete = { showDeleteDialog = file }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }

    // Delete dialog
    showDeleteDialog?.let { file ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            containerColor = CardBackground,
            title = { Text("Удалить файл?", color = OnSurface) },
            text = {
                Text(
                    "Файл «${file.name}» будет удалён с SD карты.",
                    color = OnSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteFile(file)
                    showDeleteDialog = null
                }) {
                    Text("Удалить", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Отмена", color = OnSurfaceVariant)
                }
            }
        )
    }

    // Format SD dialog
    if (showFormatDialog) {
        AlertDialog(
            onDismissRequest = { showFormatDialog = false },
            containerColor = CardBackground,
            title = { Text("Форматировать SD?", color = OnSurface) },
            text = {
                Text(
                    "Все файлы на SD карте будут безвозвратно удалены!",
                    color = Warning
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.formatSd()
                    showFormatDialog = false
                }) {
                    Text("Форматировать", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFormatDialog = false }) {
                    Text("Отмена", color = OnSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun FileCard(
    file: DvrFile,
    onPlay: () -> Unit,
    onLock: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy  HH:mm", Locale.getDefault()) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!file.isPhoto) onPlay() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(88.dp)) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = file.thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Play overlay
                if (!file.isPhoto) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                // Lock badge
                if (file.isLocked) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .size(20.dp)
                            .background(LockGold, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, null, tint = Color.Black, modifier = Modifier.size(11.dp))
                    }
                }
            }

            // Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    file.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!file.isPhoto) {
                        InfoTag(Icons.Default.Timer, formatDuration(file.durationSec))
                    }
                    InfoTag(Icons.Default.Storage, "%.1f MB".format(file.sizeMb))
                }
                Text(
                    dateFormat.format(Date(file.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }

            // Action buttons
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = onLock, modifier = Modifier.size(36.dp)) {
                    Icon(
                        if (file.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        null,
                        tint = if (file.isLocked) LockGold else OnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, null, tint = Error.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoTag(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(icon, null, tint = OnSurfaceVariant, modifier = Modifier.size(12.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
    }
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}
