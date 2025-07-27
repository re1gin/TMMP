package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.teladanprimaagro.tmpp.ui.theme.TeladanPrimaAgroTheme

@Composable
fun DriverScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2196F3)) // Warna Biru untuk Supir
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Selamat Datang, Supir!",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
        Text(
            text = "Ini adalah halaman khusus untuk Supir.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier.padding(top = 8.dp)
        )
        Button(
            onClick = {
                // Contoh: Kembali ke Login, atau ke Splash Screen
                navController.navigate("login_screen") {
                    popUpTo("splash_screen") { inclusive = false } // Hapus semua kecuali splash
                }
            },
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text("Kembali ke Login")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DriverScreenPreview() {
    TeladanPrimaAgroTheme {
        DriverScreen(rememberNavController())
    }
}