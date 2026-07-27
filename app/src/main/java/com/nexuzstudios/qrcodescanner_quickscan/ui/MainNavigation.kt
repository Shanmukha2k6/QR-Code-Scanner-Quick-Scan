package com.nexuzstudios.qrcodescanner_quickscan.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nexuzstudios.qrcodescanner_quickscan.ui.components.BannerAdView
import com.nexuzstudios.qrcodescanner_quickscan.ui.screens.create.CreateScreen
import com.nexuzstudios.qrcodescanner_quickscan.ui.screens.history.HistoryScreen
import com.nexuzstudios.qrcodescanner_quickscan.ui.screens.scan.ScanScreen
import com.nexuzstudios.qrcodescanner_quickscan.ui.screens.settings.SettingsScreen

sealed class NavRoute(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Scan : NavRoute("scan", "Scan", Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner)
    object Create : NavRoute("create", "Create", Icons.Filled.QrCode2, Icons.Outlined.QrCode2)
    object History : NavRoute("history", "History", Icons.Filled.History, Icons.Outlined.History)
    object Settings : NavRoute("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

val navItems = listOf(NavRoute.Scan, NavRoute.Create, NavRoute.History, NavRoute.Settings)

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoute.Scan.route
    val isScan = currentRoute == NavRoute.Scan.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavBar(navController = navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavRoute.Scan.route,
            modifier = Modifier.padding(
                top = 0.dp,
                bottom = paddingValues.calculateBottomPadding()
            ),
            enterTransition = {
                fadeIn(animationSpec = tween(250)) + slideInVertically(
                    initialOffsetY = { it / 20 },
                    animationSpec = tween(250)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(250))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(200))
            }
        ) {
            composable(NavRoute.Scan.route) {
                ScanScreen()
            }
            composable(NavRoute.Create.route) {
                CreateScreen()
            }
            composable(NavRoute.History.route) {
                HistoryScreen()
            }
            composable(NavRoute.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoute.Scan.route

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        navItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        item.title,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
