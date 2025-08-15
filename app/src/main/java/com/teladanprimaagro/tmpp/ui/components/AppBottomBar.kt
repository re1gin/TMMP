package com.teladanprimaagro.tmpp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

@Composable
fun AppBottomBar(navController: NavController, userRole: UserRole) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navItems = mutableListOf(
        NavigationItem("Home", Icons.Default.Home, "home_screen"),
    )
    if (userRole == UserRole.HARVESTER) {
        navItems.add(NavigationItem("Lokasi", Icons.Default.LocationOn, "peta_screen"))
    }
    if (userRole == UserRole.DRIVER) {
        navItems.add(NavigationItem("Laporan", Icons.Default.Description, "laporan_screen"))
    }
    navItems.add(NavigationItem("Pengaturan", Icons.Default.Settings, "pengaturan_screen"))

    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = currentRoute == item.route

                // Warna untuk ikon dan teks
                val selectedColor = MaterialTheme.colorScheme.onPrimary
                val unselectedColor = Color.White
                val iconColor = if (isSelected) Color.Black else unselectedColor
                val textColor = if (isSelected) selectedColor else unselectedColor

                BottomNavItem(
                    icon = item.icon,
                    label = item.label,
                    iconColor = iconColor,
                    textColor = textColor,
                    isSelected = isSelected,
                    selectedIconBackgroundColor = selectedColor,
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
    isSelected: Boolean,
    selectedIconBackgroundColor: Color,
    onClick: () -> Unit
) {
    val iconBackgroundColor = if (isSelected) selectedIconBackgroundColor else Color.Transparent

    Column(
        modifier = Modifier
            .weight(2f)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Box ini yang akan memiliki background lingkaran
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color = iconBackgroundColor, shape = CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(30.dp),
                tint = iconColor
            )
        }
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