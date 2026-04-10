package com.cardvr.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.cardvr.app.data.model.*
import com.cardvr.app.ui.MainViewModel
import com.cardvr.app.ui.components.*
import com.cardvr.app.ui.theme.*

// ── Settings Hub ──────────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        containerColor = Background,
        topBar = { DvrTopBar(title = "Настройки", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))
            SettingsSection("Регистратор") {
                SettingsNavRow(Icons.Default.Tune, "Общие", "Разрешение, петля, экспозиция", "settings/general", onNavigate)
                SettingsDivider()
                SettingsNavRow(Icons.Default.Shield, "Безопасность", "Парковка, столкновение, движение", "settings/safety", onNavigate)
                SettingsDivider()
                SettingsNavRow(Icons.Default.DirectionsCar, "ADAS", "Помощь водителю, предупреждения", "settings/adas", onNavigate)
            }
            SettingsSection("Подключение") {
                SettingsNavRow(Icons.Default.Wifi, "WiFi / Сеть", "IP, порт, SSID регистратора", "settings/connection", onNavigate)
                SettingsDivider()
                SettingsNavRow(Icons.Default.BatteryAlert, "Режим сна", "Автовыключение, охрана парковки", "settings/sleep", onNavigate)
            }
            SettingsSection("Система") {
                SettingsNavRow(Icons.Default.Info, "О приложении", "Версия, обновления", "settings/about", onNavigate)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    route: String,
    onNavigate: (String) -> Unit
) {
    SettingsRow(
        icon = icon,
        title = title,
        subtitle = subtitle,
        onClick = { onNavigate(route) },
        trailing = {
            Icon(Icons.Default.ChevronRight, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    )
}

// ── General Settings ──────────────────────────────────────────────────────────

@Composable
fun SettingsGeneralScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val s = uiState.settings

    fun update(block: DvrSettings.() -> DvrSettings) = viewModel.updateSettings(s.block())

    Scaffold(
        containerColor = Background,
        topBar = { DvrTopBar("Общие настройки", onBack = onBack) },
        snackbarHost = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                StatusMessage(uiState.errorMessage, uiState.successMessage, viewModel::clearMessage)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))

            // Resolution
            SettingsSection("Видео") {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Разрешение", style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                    Spacer(Modifier.height(8.dp))
                    ChipSelector(
                        options = VideoResolution.entries,
                        selected = s.resolution,
                        label = { it.label },
                        onSelect = { update { copy(resolution = it) } }
                    )
                }
                SettingsDivider()
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Петлевая запись", style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                    Spacer(Modifier.height(8.dp))
                    ChipSelector(
                        options = RecordLoop.entries,
                        selected = s.recordLoop,
                        label = { it.label },
                        onSelect = { update { copy(recordLoop = it) } }
                    )
                }
                SettingsDivider()
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Экспозиция", style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                    Spacer(Modifier.height(8.dp))
                    ChipSelector(
                        options = ExposureLevel.entries,
                        selected = s.exposure,
                        label = { it.label },
                        onSelect = { update { copy(exposure = it) } }
                    )
                }
                SettingsDivider()
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Баланс белого", style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                    Spacer(Modifier.height(8.dp))
                    ChipSelector(
                        options = WhiteBalance.entries,
                        selected = s.whiteBalance,
                        label = { it.label },
                        onSelect = { update { copy(whiteBalance = it) } }
                    )
                }
                SettingsDivider()
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Частота мерцания", style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                    Spacer(Modifier.height(8.dp))
                    ChipSelector(
                        options = Frequency.entries,
                        selected = s.frequency,
                        label = { it.label },
                        onSelect = { update { copy(frequency = it) } }
                    )
                }
            }

            SettingsSection("Отображение") {
                SwitchRow("Запись со звуком", s.videoSound, Icons.Default.VolumeUp) {
                    update { copy(videoSound = it) }
                }
                SettingsDivider()
                SwitchRow("Штамп даты и времени", s.dateStamp, Icons.Default.DateRange) {
                    update { copy(dateStamp = it) }
                }
                SettingsDivider()
                SwitchRow("Зеркальное отображение", s.mirror, Icons.Default.Flip) {
                    update { copy(mirror = it) }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Safety Settings ───────────────────────────────────────────────────────────

@Composable
fun SettingsSafetyScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val s = uiState.settings
    fun update(block: DvrSettings.() -> DvrSettings) = viewModel.updateSettings(s.block())

    Scaffold(
        containerColor = Background,
        topBar = { DvrTopBar("Безопасность", onBack = onBack) },
        snackbarHost = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                StatusMessage(uiState.errorMessage, uiState.successMessage, viewModel::clearMessage)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))
            SettingsSection("Парковка") {
                SwitchRow("Режим парковки", s.parkingMode, Icons.Default.LocalParking) {
                    update { copy(parkingMode = it) }
                }
                SettingsDivider()
                SwitchRow("Обнаружение движения", s.motionDetect, Icons.Default.DirectionsRun) {
                    update { copy(motionDetect = it) }
                }
            }

            SettingsSection("Датчик столкновения") {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Чувствительность", style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        when (s.collisionSensitivity) {
                            0 -> "Выключено"
                            1 -> "Низкая"
                            2 -> "Средняя"
                            3 -> "Высокая"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = s.collisionSensitivity.toFloat(),
                        onValueChange = { update { copy(collisionSensitivity = it.toInt()) } },
                        valueRange = 0f..3f,
                        steps = 2,
                        colors = SliderDefaults.colors(
                            thumbColor = Primary,
                            activeTrackColor = Primary,
                            inactiveTrackColor = SurfaceVariant
                        )
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("Выкл", "Низкая", "Средняя", "Высокая").forEach {
                            Text(it, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── ADAS Settings ─────────────────────────────────────────────────────────────

@Composable
fun SettingsAdasScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val s = uiState.settings
    fun update(block: DvrSettings.() -> DvrSettings) = viewModel.updateSettings(s.block())

    Scaffold(
        containerColor = Background,
        topBar = { DvrTopBar("ADAS — помощь водителю", onBack = onBack) },
        snackbarHost = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                StatusMessage(uiState.errorMessage, uiState.successMessage, viewModel::clearMessage)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))

            // Info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Info, null, tint = Primary, modifier = Modifier.size(18.dp))
                    Text(
                        "ADAS работает только при наличии встроенного датчика в регистраторе. " +
                                "Перед использованием выполните калибровку на ровной дороге.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurface,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            SettingsSection("Функции ADAS") {
                SwitchRow("ADAS активирован", s.adasEnabled, Icons.Default.DirectionsCar) {
                    update { copy(adasEnabled = it) }
                }
                SettingsDivider()
                SwitchRow("Выезд за полосу (LDWS)", s.adasLaneDeparture && s.adasEnabled, Icons.Default.LinearScale) {
                    update { copy(adasLaneDeparture = it) }
                }
                SettingsDivider()
                SwitchRow("Дистанция до авто (FCWS)", s.adasFollowDistance && s.adasEnabled, Icons.Default.Warning) {
                    update { copy(adasFollowDistance = it) }
                }
                SettingsDivider()
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Предупреждение о скорости", style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                        Text(
                            if (s.adasSpeedWarning == 0) "Выкл" else "${s.adasSpeedWarning} км/ч",
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = s.adasSpeedWarning.toFloat(),
                        onValueChange = { update { copy(adasSpeedWarning = (it / 10).toInt() * 10) } },
                        valueRange = 0f..200f,
                        colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary, inactiveTrackColor = SurfaceVariant)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Connection Settings ───────────────────────────────────────────────────────

@Composable
fun SettingsConnectionScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val s = uiState.settings

    var ip by remember(s.serverIp) { mutableStateOf(s.serverIp) }
    var port by remember(s.serverPort) { mutableStateOf(s.serverPort.toString()) }
    var ssid by remember(s.wifiSsid) { mutableStateOf(s.wifiSsid) }
    var password by remember(s.wifiPassword) { mutableStateOf(s.wifiPassword) }

    Scaffold(
        containerColor = Background,
        topBar = { DvrTopBar("WiFi / Сеть", onBack = onBack) },
        snackbarHost = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                StatusMessage(uiState.errorMessage, uiState.successMessage, viewModel::clearMessage)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            SettingsSection("Параметры WiFi") {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = ssid,
                        onValueChange = { ssid = it },
                        label = { Text("SSID регистратора") },
                        leadingIcon = { Icon(Icons.Default.Wifi, null, tint = Primary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = dvrTextFieldColors()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Пароль WiFi") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = Primary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = dvrTextFieldColors()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = ip,
                            onValueChange = { ip = it },
                            label = { Text("IP адрес") },
                            modifier = Modifier.weight(2f),
                            shape = RoundedCornerShape(12.dp),
                            colors = dvrTextFieldColors()
                        )
                        OutlinedTextField(
                            value = port,
                            onValueChange = { port = it.filter(Char::isDigit) },
                            label = { Text("Порт") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = dvrTextFieldColors()
                        )
                    }
                    DvrButton(
                        text = "Сохранить",
                        onClick = {
                            viewModel.saveConnectionPrefs(ip, port.toIntOrNull() ?: 7878, ssid, password)
                            viewModel.updateSettings(s.copy(serverIp = ip, serverPort = port.toIntOrNull() ?: 7878, wifiSsid = ssid, wifiPassword = password))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Sleep / Power Settings ────────────────────────────────────────────────────

@Composable
fun SettingsSleepScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val s = uiState.settings
    fun update(block: DvrSettings.() -> DvrSettings) = viewModel.updateSettings(s.block())
    val delayOptions = listOf(1, 3, 5, 10, 30)

    Scaffold(
        containerColor = Background,
        topBar = { DvrTopBar("Режим сна", onBack = onBack) },
        snackbarHost = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                StatusMessage(uiState.errorMessage, uiState.successMessage, viewModel::clearMessage)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))
            SettingsSection("Питание") {
                SwitchRow("Автовыключение", s.autoSleep, Icons.Default.PowerSettingsNew) {
                    update { copy(autoSleep = it) }
                }
                SettingsDivider()
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Задержка выключения", style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                    Spacer(Modifier.height(8.dp))
                    ChipSelector(
                        options = delayOptions,
                        selected = s.sleepDelay,
                        label = { "${it} мин" },
                        onSelect = { update { copy(sleepDelay = it) } }
                    )
                }
                SettingsDivider()
                SwitchRow("Охрана парковки", s.parkingGuard, Icons.Default.Security) {
                    update { copy(parkingGuard = it) }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── About ─────────────────────────────────────────────────────────────────────

@Composable
fun SettingsAboutScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = Background,
        topBar = { DvrTopBar("О приложении", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.linearGradient(listOf(Primary, Color(0xFF0066AA))),
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Videocam, null, tint = Color.White, modifier = Modifier.size(44.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("CarDVR", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.Bold)
            Text("Версия 1.0.0", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
            Spacer(Modifier.height(32.dp))
            SettingsSection("Информация") {
                SettingsRow(Icons.Default.PhoneAndroid, "Оптимизировано для Poco X6 Pro", "6.67\" 1220×2712 @ 144 Гц")
                SettingsDivider()
                SettingsRow(Icons.Default.Wifi, "Протокол", "uCam TCP / RTSP")
                SettingsDivider()
                SettingsRow(Icons.Default.Videocam, "Совместимые устройства", "Регистраторы с WiFi модулем SiCaDroid")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Helper composable ─────────────────────────────────────────────────────────

@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    icon: ImageVector,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingsRow(
        icon = icon,
        title = title,
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Primary,
                    uncheckedThumbColor = OnSurfaceVariant,
                    uncheckedTrackColor = SurfaceVariant
                )
            )
        }
    )
}
