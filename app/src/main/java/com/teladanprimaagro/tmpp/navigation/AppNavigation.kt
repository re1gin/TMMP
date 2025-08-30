package com.teladanprimaagro.tmpp.navigation

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teladanprimaagro.tmpp.data.UserRole
import com.teladanprimaagro.tmpp.ui.screens.PanenInputScreen
import com.teladanprimaagro.tmpp.ui.screens.DataTerkirimScreen
import com.teladanprimaagro.tmpp.ui.screens.KelolaBlokScreen
import com.teladanprimaagro.tmpp.ui.screens.KelolaKemandoranScreen
import com.teladanprimaagro.tmpp.ui.screens.KelolaKendaraanScreen
import com.teladanprimaagro.tmpp.ui.screens.KelolaPemanenScreen
import com.teladanprimaagro.tmpp.ui.screens.KelolaSupirScreen
import com.teladanprimaagro.tmpp.ui.screens.KelolaTphScreen
import com.teladanprimaagro.tmpp.ui.screens.LaporanScreen
import com.teladanprimaagro.tmpp.ui.screens.LoginScreen
import com.teladanprimaagro.tmpp.ui.screens.MainScreen
import com.teladanprimaagro.tmpp.ui.screens.PengirimanInputScreen
import com.teladanprimaagro.tmpp.ui.screens.PetaScreen
import com.teladanprimaagro.tmpp.ui.screens.RekapPanenScreen
import com.teladanprimaagro.tmpp.ui.screens.RekapPengirimanScreen
import com.teladanprimaagro.tmpp.ui.screens.ScanInputScreen
import com.teladanprimaagro.tmpp.ui.screens.SendPrintDataScreen
import com.teladanprimaagro.tmpp.ui.screens.SpbSettingsScreen
import com.teladanprimaagro.tmpp.ui.screens.SplashScreen
import com.teladanprimaagro.tmpp.ui.screens.StatistikPanenScreen
import com.teladanprimaagro.tmpp.ui.screens.StatusPengirimanScreen
import com.teladanprimaagro.tmpp.ui.screens.UbahFormatUniqueNoScreen
import com.teladanprimaagro.tmpp.viewmodels.MapViewModel
import com.teladanprimaagro.tmpp.viewmodels.PanenViewModel
import com.teladanprimaagro.tmpp.viewmodels.PengirimanViewModel
import com.teladanprimaagro.tmpp.viewmodels.SettingsViewModel
import com.teladanprimaagro.tmpp.viewmodels.SharedNfcViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    nfcIntent: State<Intent?>,
    pengirimanViewModel: PengirimanViewModel,
    sharedNfcViewModel: SharedNfcViewModel
) {
    val navController = rememberNavController()

    val panenViewModel: PanenViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel() // This is the instance you need to pass
    val mapViewModel: MapViewModel = viewModel()

    NavHost(navController = navController, startDestination = "splash_screen") {
        composable("splash_screen") {
            SplashScreen(
                navController = navController,
                settingsViewModel = settingsViewModel,
            )
        }
        composable("login_screen") {
            LoginScreen(navController = navController, settingsViewModel = settingsViewModel)
        }

        composable(
            route = "main_screen/{userRole}",
            arguments = listOf(navArgument("userRole") { type = NavType.StringType })
        ) { backStackEntry ->
            val userRoleString = backStackEntry.arguments?.getString("userRole")
            val userRole = userRoleString?.let { UserRole.valueOf(it) } ?: UserRole.HARVESTER

            MainScreen(
                mainNavController = navController,
                userRole = userRole,
                settingsViewModel = settingsViewModel
            )
        }

        composable(
            route = "send_print_data/{pengirimanId}",
            arguments = listOf(navArgument("pengirimanId") { type = NavType.IntType })
        ) { backStackEntry ->
            val pengirimanId = backStackEntry.arguments?.getInt("pengirimanId") ?: -1
            SendPrintDataScreen(navController, pengirimanId)
        }

        composable(
            route = "panenInputScreen/{panenId}",
            arguments = listOf(
                navArgument("panenId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val panenId = backStackEntry.arguments?.getInt("panenId") ?: -1

            val panenDataToEdit by panenViewModel.panenDataToEdit.collectAsState()

            LaunchedEffect(panenId) {
                if (panenId != -1) {
                    panenViewModel.loadPanenDataById(panenId)
                } else {
                    panenViewModel.clearPanenDataToEdit()
                }
            }

            PanenInputScreen(
                navController = navController,
                panenViewModel = panenViewModel,
                settingsViewModel = settingsViewModel,
                nfcIntentFromActivity = nfcIntent,
                panenDataToEdit = if (panenId != -1) panenDataToEdit else null
            )
        }

        composable("rekap_panen_screen") {
            RekapPanenScreen(
                navController = navController,
                panenViewModel = panenViewModel
            )
        }

        composable("statistik_panen_screen") {
            StatistikPanenScreen(
                navController = navController,
                panenViewModel = panenViewModel
            )
        }

        composable("data_terkirim_screen") {
            DataTerkirimScreen(
                navController = navController,
            )
        }

        composable("ubah_format_unique_no_screen") {
            UbahFormatUniqueNoScreen(navController, settingsViewModel
            )
        }

        composable("kelola_kemandoran_screen") {
            KelolaKemandoranScreen(
                navController, settingsViewModel
            )
        }
        composable("kelola_pemanen_screen") {
            KelolaPemanenScreen(
                navController, settingsViewModel
            )
        }

        composable("kelola_blok_screen") {
            KelolaBlokScreen(
                navController, settingsViewModel
            )
        }
        composable("kelola_tph_screen") {
            KelolaTphScreen(
                navController, settingsViewModel
            )
        }

        composable("scan_input_screen") {
            ScanInputScreen(
                navController = navController,
                pengirimanViewModel = pengirimanViewModel,
                sharedNfcViewModel = sharedNfcViewModel,
                nfcIntentFromActivity = nfcIntent
            )
        }
        composable("pengiriman_input_screen") {
            PengirimanInputScreen(
                navController = navController,
                pengirimanViewModel = pengirimanViewModel
            )
        }
        composable("rekap_pengiriman_screen") {
            RekapPengirimanScreen(
                navController = navController,
                pengirimanViewModel = pengirimanViewModel
            )
        }

        composable("data_pengiriman_screen") {
            StatusPengirimanScreen(
                navController = navController,
            )
        }

        composable("spb_settings_screen") {
            SpbSettingsScreen(
                navController = navController,
                settingsViewModel = settingsViewModel
            )
        }


        composable("kelola_supir_screen") {
            KelolaSupirScreen(
                navController = navController,
                settingsViewModel = settingsViewModel
            )
        }
        composable("kelola_kendaraan_screen") {
            KelolaKendaraanScreen(
                navController = navController,
                settingsViewModel = settingsViewModel
            )
        }
        composable("peta_screen") {
            PetaScreen(
                navController = navController,
                mapViewModel = mapViewModel
            )
        }
        composable("laporan_screen") {
            LaporanScreen()
        }
    }
}
