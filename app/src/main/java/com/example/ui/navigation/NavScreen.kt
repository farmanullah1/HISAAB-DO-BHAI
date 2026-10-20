package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : NavScreen("home", "Home", Icons.Default.Home)
    object Karz : NavScreen("karz", "Karz", Icons.Default.Handshake)
    object Add : NavScreen("add", "Add", Icons.Default.AddCircle)
    object Plans : NavScreen("plans", "Plans", Icons.Default.Savings)
    object Settings : NavScreen("settings", "Settings", Icons.Default.Settings)

    // Sub-screens under Plans
    object Kameti : NavScreen("kameti", "Kameti", Icons.Default.Savings)
    object Zakat : NavScreen("zakat", "Zakat", Icons.Default.VolunteerActivism)
    object Needs : NavScreen("needs", "Needs", Icons.Default.Checklist)

    companion object {
        val items = listOf(Home, Karz, Add, Plans, Settings)
    }
}
