package com.teladanprimaagro.tmpp.ui.screens

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.viewmodels.PengirimanViewModel
import com.teladanprimaagro.tmpp.ui.theme.PrimaryOrange
import com.teladanprimaagro.tmpp.data.PengirimanData

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendPrintDataScreen(
    navController: NavController,
    pengirimanId: Int,
    pengirimanViewModel: PengirimanViewModel = viewModel()
) {
    val context = LocalContext.current

    // Pastikan ini adalah deklarasi yang benar:
    // `pengirimanData` adalah sebuah State yang akan menampung objek `PengirimanData?`
    var pengirimanData by remember { mutableStateOf<PengirimanData?>(null) }

    LaunchedEffect(pengirimanId) {
        if (pengirimanId != -1) { // Pastikan ID valid
            // Panggil suspend function dari ViewModel
            val fetchedData = pengirimanViewModel.getPengirimanById(pengirimanId)
            // Tetapkan hasil langsung ke state `pengirimanData`
            pengirimanData = fetchedData
            Log.d("SendPrintDataScreen", "Data fetched for ID $pengirimanId: ${fetchedData?.spbNumber}")
        } else {
            Log.e("SendPrintDataScreen", "Invalid pengirimanId received: $pengirimanId")
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Kirim & Cetak Data",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (pengirimanData == null) {
                Text(
                    text = if (pengirimanId == -1) "ID data tidak valid." else "Memuat data pengiriman...",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                // Di sini, `pengirimanData` adalah objek PengirimanData, sehingga `spbNumber` dapat diakses.
                Text(
                    text = "Data Pengiriman SPB: ${pengirimanData!!.spbNumber}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryOrange,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        Toast.makeText(context, "Fungsi Tulis ke NFC untuk SPB ${pengirimanData!!.spbNumber} (BELUM DIIMPLEMENTASI)", Toast.LENGTH_SHORT).show()
                        Log.d("SendPrintScreen", "Tulis ke NFC untuk SPB: ${pengirimanData!!.spbNumber}")
                    },
                    modifier = Modifier.fillMaxWidth(0.8f).height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Nfc, contentDescription = "Tulis NFC", modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tulis ke NFC", fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        Toast.makeText(context, "Fungsi Cetak Data untuk SPB ${pengirimanData!!.spbNumber} (BELUM DIIMPLEMENTASI)", Toast.LENGTH_SHORT).show()
                        Log.d("SendPrintScreen", "Cetak Data untuk SPB: ${pengirimanData!!.spbNumber}")
                    },
                    modifier = Modifier.fillMaxWidth(0.8f).height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Print, contentDescription = "Cetak Data", modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cetak Data", fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Detail akan diambil dari PengirimanData ID: ${pengirimanId}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}