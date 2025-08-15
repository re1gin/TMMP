package com.teladanprimaagro.tmpp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teladanprimaagro.tmpp.ui.theme.TeladanPrimaAgroTheme
import com.teladanprimaagro.tmpp.viewmodels.SettingsViewModel

/**
 * AppWrapper membungkus seluruh konten aplikasi dan bertanggung jawab untuk mengelola tema.
 *
 * Fungsi ini mengamati status tema dari SettingsViewModel dan meneruskan nilainya
 * ke TeladanPrimaAgroTheme, sehingga perubahan tema dapat diterapkan secara dinamis ke seluruh UI.
 *
 * @param content Composable yang berisi navigasi atau UI utama aplikasi.
 */
@Composable
fun AppWrapper(content: @Composable () -> Unit) {
    val settingsViewModel: SettingsViewModel = viewModel()

    val isYellowNeonTheme = settingsViewModel.isYellowNeonTheme.collectAsState()

    TeladanPrimaAgroTheme(isYellowNeonTheme = isYellowNeonTheme.value) {
        content()
    }
}
