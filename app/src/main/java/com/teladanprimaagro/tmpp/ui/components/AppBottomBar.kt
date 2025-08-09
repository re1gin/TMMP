package com.teladanprimaagro.tmpp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.teladanprimaagro.tmpp.data.UserRole
import com.teladanprimaagro.tmpp.ui.theme.BackgroundDarkGray
import com.teladanprimaagro.tmpp.ui.theme.TextGray

@Composable
fun AppBottomBar(navController: NavController, userRole: UserRole) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Mendefinisikan item navigasi dasar untuk semua pengguna
    val navItems = mutableListOf(
        NavigationItem("Home", Icons.Default.Home, "home_screen"),
        NavigationItem("Lokasi", Icons.Default.LocationOn, "peta_screen")
    )
    if (userRole == UserRole.DRIVER) {
        navItems.add(NavigationItem("Laporan", Icons.Default.Description, "laporan_screen"))
    }
    navItems.add(NavigationItem("Pengaturan", Icons.Default.Settings, "pengaturan_screen"))

    BottomAppBar(
        containerColor = BackgroundDarkGray,
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = currentRoute == item.route
                val iconColor = if (isSelected) Color.White else TextGray
                val textColor = if (isSelected) Color.White else TextGray

                BottomNavItem(
                    icon = item.icon,
                    label = item.label,
                    iconColor = iconColor,
                    textColor = textColor,
                    onClick = {
                        navController.navigate(item.route) {
                            navController.graph.startDestinationRoute?.let { startDestination ->
                                popUpTo(startDestination) {
                                    saveState = true
                                }
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}


@Composable
private fun RowScope.BottomNavItem(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = iconColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

data class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)