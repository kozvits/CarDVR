package com.cardvr.app.ui

sealed class Screen(val route: String) {
    object Connect   : Screen("connect")
    object Preview   : Screen("preview")
    object FileList  : Screen("filelist")
    object Playback  : Screen("playback/{fileName}") {
        fun createRoute(fileName: String) = "playback/$fileName"
    }
    object Settings  : Screen("settings")
    object SettingsGeneral    : Screen("settings/general")
    object SettingsConnection : Screen("settings/connection")
    object SettingsSafety     : Screen("settings/safety")
    object SettingsAdas       : Screen("settings/adas")
    object SettingsSleep      : Screen("settings/sleep")
    object SettingsAbout      : Screen("settings/about")
}
