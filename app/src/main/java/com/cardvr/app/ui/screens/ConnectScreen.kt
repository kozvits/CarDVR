package com.cardvr.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import com.cardvr.app.data.model.ConnectionState
import com.cardvr.app.ui.MainViewModel
import com.cardvr.app.ui.components.*
import com.cardvr.app.ui.theme.*

@Composable
fun ConnectScreen(
    viewModel: MainViewModel,
    onConnected: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings

    var ip by remember { mutableStateOf(settings.serverIp) }
    var port by remember { mutableStateOf(settings.serverPort.toString()) }
    var ssid by remember { mutableStateOf(settings.wifiSsid) }
    var password by remember { mutableStateOf(settings.wifiPassword) }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.connectionState) {
        if (uiState.connectionState == ConnectionState.CONNECTED) {
            onConnected()
        }
    }

    // animated background gradient pulse
    val pulse = rememberInfiniteTransition(label = "bg")
    val alpha by pulse.animateFloat(
        initialValue = 0.06f, targetValue = 0.12f,
        animationSpec = infiniteRepeatable(tween(2500), RepeatMode.Reverse),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // decorative gradient circle
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-60).dp, y = (-40).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Primary.copy(alpha = alpha), Color.Transparent)
                    ),
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))

            // Logo / icon
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Primary, Color(0xFF0066AA))
                        ),
                        RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Videocam,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "CarDVR",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
            Text(
                "Видеорегистратор",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )

            Spacer(Modifier.height(40.dp))

            // Instructions card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Wifi, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                    Column {
                        Text(
                            "Подключение к регистратору",
                            style = MaterialTheme.typography.labelMedium,
                            color = Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "1. Включите регистратор\n2. Подключитесь к WiFi регистратора: ${ssid}\n3. Нажмите «Подключиться»",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // WiFi SSID
            OutlinedTextField(
                value = ssid,
                onValueChange = { ssid = it },
                label = { Text("WiFi сеть (SSID)") },
                leadingIcon = { Icon(Icons.Default.Wifi, null, tint = Primary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = dvrTextFieldColors()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль WiFi") },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = Primary) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null,
                            tint = OnSurfaceVariant
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = dvrTextFieldColors()
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = ip,
                    onValueChange = { ip = it },
                    label = { Text("IP адрес") },
                    leadingIcon = { Icon(Icons.Default.Router, null, tint = Primary) },
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(14.dp),
                    colors = dvrTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it.filter { c -> c.isDigit() } },
                    label = { Text("Порт") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = dvrTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(Modifier.height(28.dp))

            val isConnecting = uiState.connectionState == ConnectionState.CONNECTING

            Button(
                onClick = {
                    viewModel.saveConnectionPrefs(ip, port.toIntOrNull() ?: 7878, ssid, password)
                    viewModel.connect(ip, port.toIntOrNull() ?: 7878)
                },
                enabled = !isConnecting && ip.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Color(0xFF001F2A)
                )
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color(0xFF001F2A),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Подключение...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Подключиться", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // Error
            AnimatedVisibility(
                visible = uiState.connectionState == ConnectionState.ERROR,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, Error.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.WifiOff, null, tint = Error, modifier = Modifier.size(18.dp))
                        Text(
                            "Не удалось подключиться. Проверьте WiFi и IP-адрес.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Error
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun dvrTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Primary,
    unfocusedBorderColor = SurfaceVariant,
    focusedLabelColor = Primary,
    unfocusedLabelColor = OnSurfaceVariant,
    cursorColor = Primary,
    focusedTextColor = OnSurface,
    unfocusedTextColor = OnSurface,
    focusedContainerColor = CardBackground,
    unfocusedContainerColor = CardBackground
)
