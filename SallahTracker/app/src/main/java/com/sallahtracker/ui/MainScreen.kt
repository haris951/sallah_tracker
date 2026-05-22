package com.sallahtracker.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.sallahtracker.SallahApp
import com.sallahtracker.ui.home.HomeScreen
import com.sallahtracker.ui.home.HomeViewModel
import com.sallahtracker.ui.qaza.QazaScreen
import com.sallahtracker.ui.qaza.QazaViewModel
import com.sallahtracker.ui.history.HistoryScreen
import com.sallahtracker.ui.history.HistoryViewModel
import com.sallahtracker.ui.settings.LocationSettingsScreen
import com.sallahtracker.ui.settings.SettingsScreen
import com.sallahtracker.ui.settings.SettingsViewModel
import com.sallahtracker.ui.navigation.Screen
import com.sallahtracker.ui.navigation.bottomNavItems
import com.sallahtracker.ui.theme.PrimaryGreen
import com.sallahtracker.ui.theme.BeigeBackground
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.app.Application

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sallahApp = context.applicationContext as SallahApp
    val repository = sallahApp.repository
    
    // Use a shared SettingsViewModel for both Settings and LocationSettings screens
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(context.applicationContext as Application) as T
            }
        }
    )
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = PrimaryGreen
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryGreen,
                            selectedTextColor = PrimaryGreen,
                            indicatorColor = BeigeBackground
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return HomeViewModel(repository, sallahApp) as T
                        }
                    }
                )
                HomeScreen(homeViewModel)
            }
            composable(Screen.Qaza.route) {
                val qazaViewModel: QazaViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return QazaViewModel(repository) as T
                        }
                    }
                )
                QazaScreen(qazaViewModel)
            }
            composable(Screen.History.route) {
                val historyViewModel: HistoryViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return HistoryViewModel(repository) as T
                        }
                    }
                )
                HistoryScreen(historyViewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToLocationSettings = {
                        navController.navigate(Screen.LocationSettings.route)
                    }
                )
            }
            composable(Screen.LocationSettings.route) {
                LocationSettingsScreen(
                    viewModel = settingsViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}