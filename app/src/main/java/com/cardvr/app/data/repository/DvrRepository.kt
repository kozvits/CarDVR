package com.cardvr.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.cardvr.app.data.model.*
import com.cardvr.app.data.network.DvrTcpClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dvr_prefs")

@Singleton
class DvrRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tcpClient: DvrTcpClient
) {
    companion object {
        val KEY_IP       = stringPreferencesKey("server_ip")
        val KEY_PORT     = intPreferencesKey("server_port")
        val KEY_SSID     = stringPreferencesKey("wifi_ssid")
        val KEY_PASSWORD = stringPreferencesKey("wifi_password")
        val KEY_RESOLUTION     = stringPreferencesKey("resolution")
        val KEY_RECORD_LOOP    = stringPreferencesKey("record_loop")
        val KEY_EXPOSURE       = stringPreferencesKey("exposure")
        val KEY_WHITE_BALANCE  = stringPreferencesKey("white_balance")
        val KEY_FREQUENCY      = stringPreferencesKey("frequency")
        val KEY_VIDEO_SOUND    = booleanPreferencesKey("video_sound")
        val KEY_DATE_STAMP     = booleanPreferencesKey("date_stamp")
        val KEY_MIRROR         = booleanPreferencesKey("mirror")
        val KEY_ROTATE         = booleanPreferencesKey("rotate")
        val KEY_PARKING_MODE   = booleanPreferencesKey("parking_mode")
        val KEY_MOTION_DETECT  = booleanPreferencesKey("motion_detect")
        val KEY_COLLISION_SENS = intPreferencesKey("collision_sensitivity")
        val KEY_ADAS_ENABLED   = booleanPreferencesKey("adas_enabled")
        val KEY_AUTO_SLEEP     = booleanPreferencesKey("auto_sleep")
        val KEY_SLEEP_DELAY    = intPreferencesKey("sleep_delay")
        val KEY_PARKING_GUARD  = booleanPreferencesKey("parking_guard")
    }

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _device = MutableStateFlow(DvrDevice())
    val device: StateFlow<DvrDevice> = _device

    // ── Settings stored locally ───────────────────────────────────────────────

    val savedSettings: Flow<DvrSettings> = context.dataStore.data.map { prefs ->
        DvrSettings(
            resolution     = VideoResolution.entries.firstOrNull { it.value == prefs[KEY_RESOLUTION] } ?: VideoResolution.FHD_1080P,
            recordLoop     = RecordLoop.entries.firstOrNull { it.minutes.toString() == prefs[KEY_RECORD_LOOP] } ?: RecordLoop.MIN_3,
            exposure       = ExposureLevel.entries.firstOrNull { it.value.toString() == prefs[KEY_EXPOSURE] } ?: ExposureLevel.ZERO,
            whiteBalance   = WhiteBalance.entries.firstOrNull { it.name == prefs[KEY_WHITE_BALANCE] } ?: WhiteBalance.AUTO,
            frequency      = Frequency.entries.firstOrNull { it.name == prefs[KEY_FREQUENCY] } ?: Frequency.HZ_50,
            videoSound     = prefs[KEY_VIDEO_SOUND] ?: true,
            dateStamp      = prefs[KEY_DATE_STAMP] ?: true,
            mirror         = prefs[KEY_MIRROR] ?: false,
            rotate         = prefs[KEY_ROTATE] ?: false,
            parkingMode    = prefs[KEY_PARKING_MODE] ?: false,
            motionDetect   = prefs[KEY_MOTION_DETECT] ?: false,
            collisionSensitivity = prefs[KEY_COLLISION_SENS] ?: 2,
            adasEnabled    = prefs[KEY_ADAS_ENABLED] ?: false,
            autoSleep      = prefs[KEY_AUTO_SLEEP] ?: false,
            sleepDelay     = prefs[KEY_SLEEP_DELAY] ?: 5,
            parkingGuard   = prefs[KEY_PARKING_GUARD] ?: false,
            wifiSsid       = prefs[KEY_SSID] ?: "UCAM_DVR",
            wifiPassword   = prefs[KEY_PASSWORD] ?: "12345678",
            serverIp       = prefs[KEY_IP] ?: "192.168.1.1",
            serverPort     = prefs[KEY_PORT] ?: 7878,
        )
    }

    suspend fun saveConnectionPrefs(ip: String, port: Int, ssid: String, password: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IP] = ip
            prefs[KEY_PORT] = port
            prefs[KEY_SSID] = ssid
            prefs[KEY_PASSWORD] = password
        }
    }

    suspend fun saveSettings(settings: DvrSettings) {
        context.dataStore.edit { prefs ->
            prefs[KEY_RESOLUTION]    = settings.resolution.value
            prefs[KEY_RECORD_LOOP]   = settings.recordLoop.minutes.toString()
            prefs[KEY_EXPOSURE]      = settings.exposure.value.toString()
            prefs[KEY_WHITE_BALANCE] = settings.whiteBalance.name
            prefs[KEY_FREQUENCY]     = settings.frequency.name
            prefs[KEY_VIDEO_SOUND]   = settings.videoSound
            prefs[KEY_DATE_STAMP]    = settings.dateStamp
            prefs[KEY_MIRROR]        = settings.mirror
            prefs[KEY_ROTATE]        = settings.rotate
            prefs[KEY_PARKING_MODE]  = settings.parkingMode
            prefs[KEY_MOTION_DETECT] = settings.motionDetect
            prefs[KEY_COLLISION_SENS] = settings.collisionSensitivity
            prefs[KEY_ADAS_ENABLED]  = settings.adasEnabled
            prefs[KEY_AUTO_SLEEP]    = settings.autoSleep
            prefs[KEY_SLEEP_DELAY]   = settings.sleepDelay
            prefs[KEY_PARKING_GUARD] = settings.parkingGuard
        }
    }

    // ── Connection ────────────────────────────────────────────────────────────

    suspend fun connect(ip: String, port: Int): Result<Unit> {
        _connectionState.value = ConnectionState.CONNECTING
        return tcpClient.connect(ip, port).also { result ->
            _connectionState.value = if (result.isSuccess) {
                _device.value = _device.value.copy(ip = ip, port = port, connected = true)
                ConnectionState.CONNECTED
            } else {
                ConnectionState.ERROR
            }
        }
    }

    suspend fun disconnect() {
        tcpClient.disconnect()
        _connectionState.value = ConnectionState.DISCONNECTED
        _device.value = _device.value.copy(connected = false)
    }

    // ── Camera commands ───────────────────────────────────────────────────────

    suspend fun startRecording() = tcpClient.startRecording()
    suspend fun stopRecording()  = tcpClient.stopRecording()
    suspend fun takePhoto()      = tcpClient.takePhoto()
    suspend fun lockFile(name: String) = tcpClient.lockFile(name)
    suspend fun deleteFile(name: String) = tcpClient.deleteFile(name)
    suspend fun formatSd() = tcpClient.formatSd()

    suspend fun getFileList(): Result<List<DvrFile>> = tcpClient.getFileList()

    suspend fun pushSettings(settings: DvrSettings): Result<Unit> = runCatching {
        val ip = _device.value.ip
        tcpClient.setSetting("video_resolution", settings.resolution.value)
        tcpClient.setSetting("record_duration", settings.recordLoop.minutes.toString())
        tcpClient.setSetting("exposure_val", settings.exposure.value.toString())
        tcpClient.setSetting("white_balance", settings.whiteBalance.name.lowercase())
        tcpClient.setSetting("frequency", if (settings.frequency == Frequency.HZ_50) "50hz" else "60hz")
        tcpClient.setSetting("video_stamp", if (settings.dateStamp) "date&time" else "off")
        tcpClient.setSetting("audio_switch", if (settings.videoSound) "on" else "off")
        tcpClient.setSetting("mirror", if (settings.mirror) "on" else "off")
        tcpClient.setSetting("motion_detection", if (settings.motionDetect) "on" else "off")
    }

    val isConnected: Boolean get() = tcpClient.isConnected
}
