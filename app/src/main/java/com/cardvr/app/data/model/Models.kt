package com.cardvr.app.data.model

import com.google.gson.annotations.SerializedName

// ── DVR Connection ────────────────────────────────────────────────────────────

data class DvrDevice(
    val ssid: String = "UCAM_DVR",
    val ip: String = "192.168.1.1",
    val port: Int = 7878,
    val rtspPort: Int = 554,
    val connected: Boolean = false,
    val firmwareVersion: String = "",
    val deviceName: String = "CarDVR"
)

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, ERROR }

// ── Camera / Recording ───────────────────────────────────────────────────────

enum class RecordingState { IDLE, RECORDING, PAUSED }

enum class VideoResolution(val label: String, val value: String) {
    FHD_1080P("1080P FHD", "1080p"),
    HD_720P("720P HD", "720p"),
    VGA_480P("480P VGA", "480p")
}

enum class RecordLoop(val label: String, val minutes: Int) {
    MIN_1("1 минута", 1),
    MIN_3("3 минуты", 3),
    MIN_5("5 минут", 5),
    MIN_10("10 минут", 10)
}

enum class ExposureLevel(val label: String, val value: Int) {
    PLUS_2("+2.0", 2),
    PLUS_1("+1.0", 1),
    ZERO("0", 0),
    MINUS_1("-1.0", -1),
    MINUS_2("-2.0", -2)
}

enum class WhiteBalance(val label: String) {
    AUTO("Авто"),
    DAYLIGHT("Дневной"),
    CLOUDY("Пасмурно"),
    TUNGSTEN("Лампа накаливания"),
    FLUORESCENT("Флуоресцентный")
}

enum class Frequency(val label: String) {
    HZ_50("50 Гц"),
    HZ_60("60 Гц")
}

// ── DVR Settings ─────────────────────────────────────────────────────────────

data class DvrSettings(
    // General
    val resolution: VideoResolution = VideoResolution.FHD_1080P,
    val recordLoop: RecordLoop = RecordLoop.MIN_3,
    val exposure: ExposureLevel = ExposureLevel.ZERO,
    val whiteBalance: WhiteBalance = WhiteBalance.AUTO,
    val frequency: Frequency = Frequency.HZ_50,
    val videoSound: Boolean = true,
    val dateStamp: Boolean = true,
    val mirror: Boolean = false,
    val rotate: Boolean = false,
    val parkingMode: Boolean = false,
    val motionDetect: Boolean = false,
    // Collision / ADAS
    val collisionSensitivity: Int = 2, // 0=off,1=low,2=mid,3=high
    val adasEnabled: Boolean = false,
    val adasLaneDeparture: Boolean = true,
    val adasFollowDistance: Boolean = true,
    val adasSpeedWarning: Int = 0, // 0=off, km/h
    // Sleep / Power
    val autoSleep: Boolean = false,
    val sleepDelay: Int = 5, // minutes
    val parkingGuard: Boolean = false,
    // WiFi
    val wifiSsid: String = "UCAM_DVR",
    val wifiPassword: String = "12345678",
    val serverIp: String = "192.168.1.1",
    val serverPort: Int = 7878,
    // SD
    val sdSizeGb: Int = 0,
    val sdUsedGb: Int = 0
)

// ── File List ─────────────────────────────────────────────────────────────────

data class DvrFile(
    val name: String,
    val path: String,
    val url: String,
    val thumbnailUrl: String,
    val sizeMb: Float,
    val durationSec: Int,
    val timestamp: Long,
    val isLocked: Boolean = false,
    val isPhoto: Boolean = false
)

// ── Network Requests/Responses ────────────────────────────────────────────────

data class CommandResponse(
    @SerializedName("rval") val rval: Int = 0,
    @SerializedName("msg_id") val msgId: Int = 0
)

data class FileListResponse(
    @SerializedName("rval") val rval: Int = 0,
    @SerializedName("listing") val listing: List<FileEntry> = emptyList()
)

data class FileEntry(
    @SerializedName("name") val name: String = "",
    @SerializedName("size") val size: Long = 0,
    @SerializedName("time") val time: Long = 0
)

data class SettingsResponse(
    @SerializedName("rval") val rval: Int = 0,
    @SerializedName("param") val param: String = ""
)

// ── ADAS Alerts ───────────────────────────────────────────────────────────────

sealed class AdasAlert {
    object LaneDeparture : AdasAlert()
    object ForwardCollision : AdasAlert()
    data class SpeedWarning(val speed: Int) : AdasAlert()
}

// ── Electronic Dog (Speed Camera DB) ─────────────────────────────────────────

data class SpeedCamera(
    val latitude: Double,
    val longitude: Double,
    val type: String,
    val speedLimit: Int,
    val direction: Float
)
