package com.teladanprimaagro.tmpp.ui.screens

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.teladanprimaagro.tmpp.data.PengirimanData
import com.teladanprimaagro.tmpp.ui.theme.Black
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.White
import com.teladanprimaagro.tmpp.viewmodels.PengirimanViewModel
import com.teladanprimaagro.tmpp.viewmodels.ScannedItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.lang.StringBuilder
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendPrintDataScreen(
    navController: NavController,
    pengirimanId: Int,
    pengirimanViewModel: PengirimanViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var pengirimanData by remember { mutableStateOf<PengirimanData?>(null) }
    var showDeviceListDialog by remember { mutableStateOf(false) }

    LaunchedEffect(pengirimanId) {
        if (pengirimanId != -1) {
            val fetchedData = pengirimanViewModel.getPengirimanById(pengirimanId)
            pengirimanData = fetchedData
            Log.d("SendPrintDataScreen", "Data fetched for ID $pengirimanId: ${fetchedData?.spbNumber}")
        } else {
            Log.e("SendPrintDataScreen", "Invalid pengirimanId received: $pengirimanId")
        }
    }

    // --- Permissions Launcher ---
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            showDeviceListDialog = true
        } else {
            Toast.makeText(context, "Izin Bluetooth diperlukan untuk mencetak.", Toast.LENGTH_SHORT).show()
        }
    }

    // Mengatur warna latar belakang Scaffold
    Scaffold(
        modifier = Modifier.background(MainBackground),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Kirim & Cetak Data",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = White,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MainBackground
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
                    color = White
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = "Ikon Printer",
                    modifier = Modifier.size(120.dp),
                    tint = MainColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Data Pengiriman SPB: ${pengirimanData!!.spbNumber}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.BLUETOOTH_SCAN,
                                        Manifest.permission.BLUETOOTH_CONNECT
                                    )
                                )
                            } else {
                                showDeviceListDialog = true
                            }
                        } else {
                            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                                permissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH))
                            } else {
                                showDeviceListDialog = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MainColor)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cetak Data", fontSize = 18.sp, color = Black)
                    }
                }
            }
        }
    }
    if (showDeviceListDialog && pengirimanData != null) {
        val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

        val pairedDevices = if (bluetoothAdapter?.isEnabled == true) {
            if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                bluetoothAdapter.bondedDevices.toList()
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }

        AlertDialog(
            onDismissRequest = { showDeviceListDialog = false },
            title = { Text("Pilih Perangkat Bluetooth") },
            text = {
                Column {
                    if (pairedDevices.isEmpty()) {
                        Text("Tidak ada perangkat yang terhubung.")
                    } else {
                        pairedDevices.forEach { device ->
                            TextButton(onClick = {
                                showDeviceListDialog = false
                                coroutineScope.launch {
                                    printData(context, device, pengirimanData!!)
                                }
                            }) {
                                Text(device.name ?: "Unknown Device")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDeviceListDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

private suspend fun printData(context: Context, device: BluetoothDevice, data: PengirimanData) {
    withContext(Dispatchers.IO) {
        if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BluetoothPrint", "Izin BLUETOOTH_CONNECT tidak diberikan.")
            return@withContext
        }

        var socket: BluetoothSocket? = null
        var outputStream: OutputStream? = null

        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket.connect()
            outputStream = socket.outputStream

            val formattedData = ThermalPrinter(data)
            outputStream.write(formattedData.toByteArray())
            outputStream.flush()

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Data berhasil dikirim ke printer.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("BluetoothPrint", "Gagal mengirim data ke printer: ${e.message}", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Gagal mencetak: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } finally {
            try {
                // Pastikan untuk menutup stream dan socket di blok 'finally'
                outputStream?.close()
                socket?.close()
            } catch (closeException: Exception) {
                Log.e("BluetoothPrint", "Gagal menutup socket/stream: ${closeException.message}", closeException)
            }
        }
    }
}

private val gson = Gson()
private fun PengirimanData.getDetailScannedItems(): List<ScannedItem> {
    val type = object : TypeToken<List<ScannedItem>>() {}.type
    return gson.fromJson(this.detailScannedItemsJson, type) ?: emptyList()
}

private fun ThermalPrinter(data: PengirimanData): String {
    val ESC = 0x1B.toChar()
    val GS = 0x1D.toChar()
    val LF = 0x0A.toChar()
    val FF = 0x0C.toChar()

    val output = StringBuilder()

    // Header
    output.append("${ESC}a\u0001") // Centered alignment
    output.append("================================\n")
    output.append("${GS}!\u0000") // Reset font size to normal
    output.append("TELADAN PRIMA AGRO\n")
    output.append("================================\n")

    // Detail Pengiriman
    output.append("${ESC}a\u0000") // Left alignment
    output.append("SPB No: ${data.spbNumber}\n")
    output.append("Tgl/Jam: ${data.waktuPengiriman}\n")
    output.append("Mandor: ${data.mandorLoading}\n")
    output.append("Supir: ${data.namaSupir}\n")
    output.append("Plat No: ${data.noPolisi}\n")

    // Ringkasan
    output.append("--------------------------------\n")
    output.append("${ESC}a\u0001") // Centered alignment
    output.append("Total Buah: ${data.totalBuah}\n")
    output.append("${ESC}a\u0000") // Left alignment
    output.append("--------------------------------\n")

    // --- Bagian Detail Scan dengan Agregasi ---
    output.append("Detail Scan (Per Blok):\n")

    val rawScannedItems = data.getDetailScannedItems()

    val aggregatedScannedItems = rawScannedItems
        .groupBy { it.blok }
        .map { (blok, itemsInBlock) ->
            val totalBuahAggregated = itemsInBlock.sumOf { it.totalBuah }
            ScannedItem(
                uniqueNo = "",
                tanggal = "",
                blok = blok,
                totalBuah = totalBuahAggregated
            )
        }
        .sortedBy { it.blok }

    aggregatedScannedItems.forEachIndexed { _, item ->
        output.append("   Blok: ${item.blok}, Buah: ${item.totalBuah}\n")
    }

    output.append("--------------------------------\n")

    // Footer
    output.append("${ESC}a\u0001") // Centered alignment
    output.append("================================\n")
    output.append("TERIMA KASIH!\n")
    output.append("&\n")
    output.append("UTAMAKAN KESELAMATAN BANG!\n")
    output.append("================================\n")

    output.append(LF)
    output.append(LF)
    output.append(LF)
    output.append(FF)
    output.append(LF)

    return output.toString()
}