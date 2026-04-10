package com.cardvr.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cardvr.app.ui.MainViewModel
import com.cardvr.app.ui.Screen
import com.cardvr.app.ui.screens.*
import com.cardvr.app.ui.theme.CarDVRTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarDVRTheme {
                val navController = rememberNavController()
                val viewModel: MainViewModel = hiltViewModel()

                NavHost(
                    navController = navController,
                    startDestination = Screen.Connect.route
                ) {
                    composable(Screen.Connect.route) {
                        ConnectScreen(
                            viewModel = viewModel,
                            onConnected = {
                                navController.navigate(Screen.Preview.route) {
                                    popUpTo(Screen.Connect.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.Preview.route) {
                        PreviewScreen(
                            viewModel = viewModel,
                            onNavigateToFiles = { navController.navigate(Screen.FileList.route) },
                            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                            onDisconnect = {
                                navController.navigate(Screen.Connect.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Screen.FileList.route) {
                        FileListScreen(
                            viewModel = viewModel,
                            onPlayVideo = { fileName ->
                                navController.navigate(Screen.Playback.createRoute(fileName))
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.Playback.route) { backStack ->
                        val fileName = backStack.arguments?.getString("fileName") ?: ""
                        PlaybackScreen(
                            viewModel = viewModel,
                            fileName = fileName,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            onBack = { navController.popBackStack() },
                            onNavigate = { route -> navController.navigate(route) }
                        )
                    }

                    composable(Screen.SettingsGeneral.route) {
                        SettingsGeneralScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }

                    composable(Screen.SettingsConnection.route) {
                        SettingsConnectionScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }

                    composable(Screen.SettingsSafety.route) {
                        SettingsSafetyScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }

                    composable(Screen.SettingsAdas.route) {
                        SettingsAdasScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }

                    composable(Screen.SettingsSleep.route) {
                        SettingsSleepScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                    }

                    composable(Screen.SettingsAbout.route) {
                        SettingsAboutScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
