package com.cardvr.app.ui.components

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.cardvr.app.ui.theme.*

// ── Top App Bar ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DvrTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = OnSurface)
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Surface,
            scrolledContainerColor = Surface
        )
    )
}

// ── Connection Status Badge ───────────────────────────────────────────────────

@Composable
fun ConnectionBadge(connected: Boolean) {
    val color = if (connected) Success else OnSurfaceVariant
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = if (connected) 1.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .scale(scale)
                .background(color, CircleShape)
        )
        Text(
            text = if (connected) "Подключено" else "Нет связи",
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

// ── Settings Section ──────────────────────────────────────────────────────────

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), content = content)
        }
        Spacer(Modifier.height(12.dp))
    }
}

// ── Settings Row ──────────────────────────────────────────────────────────────

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: @Composable () -> Unit = {},
    onClick: (() -> Unit)? = null
) {
    val modifier = if (onClick != null)
        Modifier.clickable(onClick = onClick)
    else Modifier

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(SurfaceVariant, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = OnSurface)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        trailing()
        if (onClick != null && trailing == {}) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = OnSurfaceVariant)
        }
    }
}

// ── Settings Divider ──────────────────────────────────────────────────────────

@Composable
fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 66.dp, end = 16.dp),
        color = SurfaceVariant,
        thickness = 0.5.dp
    )
}

// ── Chip Selector ─────────────────────────────────────────────────────────────

@Composable
fun <T> ChipSelector(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Surface(
                onClick = { onSelect(option) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) Primary else SurfaceVariant,
                modifier = Modifier.height(32.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 14.dp)
                ) {
                    Text(
                        text = label(option),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color(0xFF001F2A) else OnSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ── Status Snackbar ───────────────────────────────────────────────────────────

@Composable
fun StatusMessage(
    error: String?,
    success: String?,
    onDismiss: () -> Unit
) {
    val message = error ?: success
    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        val isError = error != null
        Snackbar(
            modifier = Modifier.padding(16.dp),
            containerColor = if (isError) Error.copy(alpha = 0.95f) else Success.copy(alpha = 0.95f),
            contentColor = Color.White,
            action = {
                TextButton(onClick = onDismiss) {
                    Text("OK", color = Color.White)
                }
            }
        ) {
            Text(message ?: "")
        }
    }
    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(3000)
            onDismiss()
        }
    }
}

// ── Primary Action Button ─────────────────────────────────────────────────────

@Composable
fun DvrButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isDestructive: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDestructive) Error else Primary,
            contentColor = if (isDestructive) Color.White else Color(0xFF001F2A),
            disabledContainerColor = SurfaceVariant,
            disabledContentColor = OnSurfaceVariant
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}
