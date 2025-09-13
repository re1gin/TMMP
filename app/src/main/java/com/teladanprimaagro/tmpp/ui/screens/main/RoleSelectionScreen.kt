package com.teladanprimaagro.tmpp.ui.screens.main

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.R
import com.teladanprimaagro.tmpp.data.UserRole
import com.teladanprimaagro.tmpp.ui.components.PasswordDialog
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.White
import com.teladanprimaagro.tmpp.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    var isLoading by remember { mutableStateOf(false) }
    var showPasswordHarvester by remember { mutableStateOf(false) }
    var showPasswordDriver by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Tentukan Peran Anda!",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularRoleButton(
                    iconResId = R.drawable.agriculture,
                    label = "Harvester",
                    onClick = { showPasswordHarvester = true },
                    circleColor = Color.Transparent
                )

                // Tombol untuk Driver
                CircularRoleButton(
                    iconResId = R.drawable.car,
                    label = "Driver",
                    onClick = { showPasswordDriver = true },
                    circleColor = Color.Transparent
                )
            }

            Spacer(modifier = Modifier.height(50.dp))
//            TextButton(
//                onClick = {
//                    settingsViewModel.exitToApp()
//                    navController.navigate("login_screen") {
//                        popUpTo("role_selection_screen") { inclusive = true }
//                    }
//                },
//                modifier = Modifier.wrapContentSize()
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.Center
//                ) {
//                    Text(
//                        text = "Logout",
//                        color = DangerRed,
//                        fontSize = 18.sp, // Ukuran teks logout
//                        fontWeight = FontWeight.SemiBold,
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Icon(
//                        imageVector = Icons.AutoMirrored.Filled.Logout,
//                        contentDescription = "Logout Icon",
//                        tint = DangerRed,
//                        modifier = Modifier.size(20.dp) // Ukuran ikon logout
//                    )
//                }
//            }
        }

        // Dialog Password untuk Harvester
        if (showPasswordHarvester) {
            PasswordDialog(
                onDismissRequest = { showPasswordHarvester = false },
                onConfirm = { password ->
                    if (password == "panen123") {
                        isLoading = true
                        showPasswordHarvester = false
                        settingsViewModel.loginSuccess(UserRole.HARVESTER)
                        navController.navigate("main_screen/${UserRole.HARVESTER.name}") {
                            popUpTo("role_selection_screen") { inclusive = true }
                        }
                    }
                },
                correctPassword = "panen123"
            )
        }

        // Dialog Password untuk Driver
        if (showPasswordDriver) {
            PasswordDialog(
                onDismissRequest = { showPasswordDriver = false },
                onConfirm = { password ->
                    if (password == "supir123") {
                        isLoading = true
                        showPasswordDriver = false
                        settingsViewModel.loginSuccess(UserRole.DRIVER)
                        navController.navigate("main_screen/${UserRole.DRIVER.name}") {
                            popUpTo("role_selection_screen") { inclusive = true }
                        }
                    }
                },
                correctPassword = "supir123"
            )
        }

        // Animasi Loading
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(animationSpec = tween(durationMillis = 500)),
            exit = fadeOut(animationSpec = tween(durationMillis = 500))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = White, modifier = Modifier.size(60.dp))
            }
        }
    }
}

@Composable
fun CircularRoleButton(
    @DrawableRes iconResId: Int,
    label: String,
    onClick: () -> Unit,
    circleColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.width(IntrinsicSize.Min) // Agar lebar kolom menyesuaikan konten
    ) {
        // Lingkaran untuk ikon
        Button(
            onClick = onClick,
            modifier = Modifier
                .size(100.dp) // Ukuran lingkaran
                .border(2.dp, MainColor, CircleShape)
                .background(circleColor, CircleShape),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = MainColor,
                modifier = Modifier.size(50.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = label,
            color = White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
