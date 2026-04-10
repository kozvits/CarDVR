package com.cardvr.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardvr.app.data.model.*
import com.cardvr.app.data.repository.DvrRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val device: DvrDevice = DvrDevice(),
    val recordingState: RecordingState = RecordingState.IDLE,
    val settings: DvrSettings = DvrSettings(),
    val files: List<DvrFile> = emptyList(),
    val isLoadingFiles: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val sdSizeGb: Int = 0,
    val sdUsedGb: Int = 0
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: DvrRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.connectionState.collect { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
        }
        viewModelScope.launch {
            repository.device.collect { device ->
                _uiState.update { it.copy(device = device) }
            }
        }
        viewModelScope.launch {
            repository.savedSettings.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
    }

    // ── Connection ────────────────────────────────────────────────────────────

    fun connect(ip: String, port: Int) {
        viewModelScope.launch {
            repository.connect(ip, port).onFailure { e ->
                _uiState.update { it.copy(errorMessage = "Ошибка подключения: ${e.message}") }
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch { repository.disconnect() }
    }

    fun saveConnectionPrefs(ip: String, port: Int, ssid: String, password: String) {
        viewModelScope.launch {
            repository.saveConnectionPrefs(ip, port, ssid, password)
        }
    }

    // ── Recording ─────────────────────────────────────────────────────────────

    fun startRecording() {
        viewModelScope.launch {
            repository.startRecording()
                .onSuccess { _uiState.update { it.copy(recordingState = RecordingState.RECORDING) } }
                .onFailure { showError("Ошибка запуска записи") }
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            repository.stopRecording()
                .onSuccess { _uiState.update { it.copy(recordingState = RecordingState.IDLE) } }
                .onFailure { showError("Ошибка остановки записи") }
        }
    }

    fun takePhoto() {
        viewModelScope.launch {
            repository.takePhoto()
                .onSuccess { showSuccess("Фото сохранено") }
                .onFailure { showError("Ошибка съёмки") }
        }
    }

    // ── Files ─────────────────────────────────────────────────────────────────

    fun loadFiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingFiles = true) }
            repository.getFileList()
                .onSuccess { files -> _uiState.update { it.copy(files = files, isLoadingFiles = false) } }
                .onFailure { _uiState.update { it.copy(isLoadingFiles = false, errorMessage = "Ошибка загрузки файлов") } }
        }
    }

    fun lockFile(file: DvrFile) {
        viewModelScope.launch {
            repository.lockFile(file.name)
                .onSuccess {
                    val updated = _uiState.value.files.map {
                        if (it.name == file.name) it.copy(isLocked = !it.isLocked) else it
                    }
                    _uiState.update { it.copy(files = updated) }
                    showSuccess(if (!file.isLocked) "Файл защищён" else "Защита снята")
                }
                .onFailure { showError("Ошибка блокировки файла") }
        }
    }

    fun deleteFile(file: DvrFile) {
        viewModelScope.launch {
            repository.deleteFile(file.name)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(files = state.files.filter { it.name != file.name })
                    }
                    showSuccess("Файл удалён")
                }
                .onFailure { showError("Ошибка удаления файла") }
        }
    }

    fun formatSd() {
        viewModelScope.launch {
            repository.formatSd()
                .onSuccess { showSuccess("SD карта отформатирована") }
                .onFailure { showError("Ошибка форматирования SD") }
        }
    }

    // ── Settings ──────────────────────────────────────────────────────────────

    fun updateSettings(settings: DvrSettings) {
        viewModelScope.launch {
            repository.saveSettings(settings)
            if (repository.isConnected) {
                repository.pushSettings(settings)
                    .onSuccess { showSuccess("Настройки сохранены") }
                    .onFailure { showError("Ошибка применения настроек") }
            } else {
                showSuccess("Настройки сохранены локально")
            }
        }
    }

    // ── Messages ──────────────────────────────────────────────────────────────

    fun clearMessage() = _uiState.update { it.copy(errorMessage = null, successMessage = null) }

    private fun showError(msg: String) = _uiState.update { it.copy(errorMessage = msg) }
    private fun showSuccess(msg: String) = _uiState.update { it.copy(successMessage = msg) }
}
