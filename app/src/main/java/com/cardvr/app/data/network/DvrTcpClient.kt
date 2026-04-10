package com.cardvr.app.data.network

import com.cardvr.app.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.Socket
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TCP-based command protocol for uCar DVR cameras.
 * Commands are JSON objects sent over a persistent TCP socket.
 *
 * Message IDs follow the uCam/SiCa protocol:
 *   257  = START_SESSION
 *   258  = STOP_SESSION
 *   259  = GET_SETTINGS
 *   260  = SET_SETTINGS
 *   513  = START_RECORD
 *   514  = STOP_RECORD
 *   515  = TAKE_PHOTO
 *   769  = GET_FILE_LIST
 *   770  = DELETE_FILE
 *   771  = LOCK_FILE
 *   1025 = FORMAT_SD
 *   1281 = GET_SD_INFO
 */
@Singleton
class DvrTcpClient @Inject constructor() {

    companion object {
        const val MSG_START_SESSION   = 257
        const val MSG_STOP_SESSION    = 258
        const val MSG_GET_SETTINGS    = 259
        const val MSG_SET_SETTINGS    = 260
        const val MSG_START_RECORD    = 513
        const val MSG_STOP_RECORD     = 514
        const val MSG_TAKE_PHOTO      = 515
        const val MSG_GET_FILE_LIST   = 769
        const val MSG_DELETE_FILE     = 770
        const val MSG_LOCK_FILE       = 771
        const val MSG_FORMAT_SD       = 1025
        const val MSG_GET_SD_INFO     = 1281
        const val CONNECT_TIMEOUT_MS  = 5000
        const val READ_TIMEOUT_MS     = 8000
    }

    private var socket: Socket? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null

    val isConnected: Boolean get() = socket?.isConnected == true && socket?.isClosed == false

    suspend fun connect(ip: String, port: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            disconnect()
            socket = Socket().apply {
                soTimeout = READ_TIMEOUT_MS
                connect(java.net.InetSocketAddress(ip, port), CONNECT_TIMEOUT_MS)
            }
            writer = BufferedWriter(OutputStreamWriter(socket!!.getOutputStream()))
            reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
            sendCommand(MSG_START_SESSION, JSONObject())
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        runCatching {
            sendCommand(MSG_STOP_SESSION, JSONObject())
            writer?.close()
            reader?.close()
            socket?.close()
        }
        socket = null
        writer = null
        reader = null
    }

    suspend fun startRecording(): Result<CommandResponse> =
        command(MSG_START_RECORD, JSONObject())

    suspend fun stopRecording(): Result<CommandResponse> =
        command(MSG_STOP_RECORD, JSONObject())

    suspend fun takePhoto(): Result<CommandResponse> =
        command(MSG_TAKE_PHOTO, JSONObject())

    suspend fun lockFile(fileName: String): Result<CommandResponse> =
        command(MSG_LOCK_FILE, JSONObject().put("param", fileName))

    suspend fun deleteFile(fileName: String): Result<CommandResponse> =
        command(MSG_DELETE_FILE, JSONObject().put("param", fileName))

    suspend fun formatSd(): Result<CommandResponse> =
        command(MSG_FORMAT_SD, JSONObject())

    suspend fun getSdInfo(): Result<CommandResponse> =
        command(MSG_GET_SD_INFO, JSONObject())

    suspend fun getFileList(folder: String = "/tmp/SD0/Movie"): Result<List<DvrFile>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val payload = JSONObject().put("param", folder)
                sendCommand(MSG_GET_FILE_LIST, payload)
                val response = readResponse()
                parseFileList(response, folder)
            }
        }

    suspend fun getSetting(key: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val payload = JSONObject().put("param", key)
                sendCommand(MSG_GET_SETTINGS, payload)
                val response = readResponse()
                JSONObject(response).optString("param", "")
            }
        }

    suspend fun setSetting(key: String, value: String): Result<CommandResponse> =
        command(MSG_SET_SETTINGS, JSONObject().put("param", key).put("value", value))

    // ── Private helpers ───────────────────────────────────────────────────────

    private suspend fun command(msgId: Int, payload: JSONObject): Result<CommandResponse> =
        withContext(Dispatchers.IO) {
            runCatching {
                sendCommand(msgId, payload)
                val raw = readResponse()
                val json = JSONObject(raw)
                CommandResponse(rval = json.optInt("rval", -1), msgId = json.optInt("msg_id", 0))
            }
        }

    private fun sendCommand(msgId: Int, payload: JSONObject) {
        val msg = payload.put("msg_id", msgId).toString()
        writer?.apply {
            write(msg)
            newLine()
            flush()
        }
    }

    private fun readResponse(): String {
        return try {
            reader?.readLine() ?: "{}"
        } catch (e: SocketTimeoutException) {
            "{\"rval\":-10}"
        }
    }

    private fun parseFileList(raw: String, baseFolder: String): List<DvrFile> {
        val files = mutableListOf<DvrFile>()
        runCatching {
            val json = JSONObject(raw)
            val listing: JSONArray = json.optJSONArray("listing") ?: return emptyList()
            val ip = socket?.inetAddress?.hostAddress ?: "192.168.1.1"
            for (i in 0 until listing.length()) {
                val entry = listing.getJSONObject(i)
                val name = entry.optString("name", "")
                if (name.isEmpty()) continue
                val isPhoto = name.endsWith(".jpg", ignoreCase = true) ||
                        name.endsWith(".jpeg", ignoreCase = true)
                files.add(
                    DvrFile(
                        name = name,
                        path = "$baseFolder/$name",
                        url = "http://$ip/$baseFolder/$name",
                        thumbnailUrl = "http://$ip/thumbnail/$name.jpg",
                        sizeMb = entry.optLong("size", 0) / 1_048_576f,
                        durationSec = entry.optInt("time", 0),
                        timestamp = entry.optLong("date", System.currentTimeMillis()),
                        isLocked = name.startsWith("RO_") || name.contains("lock"),
                        isPhoto = isPhoto
                    )
                )
            }
        }
        return files.sortedByDescending { it.timestamp }
    }
}
