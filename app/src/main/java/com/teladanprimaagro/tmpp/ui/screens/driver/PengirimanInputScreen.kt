package com.teladanprimaagro.tmpp.ui.screens.driver

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.components.DropdownInputField
import com.teladanprimaagro.tmpp.ui.components.TextInputField
import com.teladanprimaagro.tmpp.ui.components.TotalBuahDisplay
import com.teladanprimaagro.tmpp.ui.theme.Black
import com.teladanprimaagro.tmpp.ui.theme.LightGrey
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.OldGrey
import com.teladanprimaagro.tmpp.ui.theme.White
import com.teladanprimaagro.tmpp.viewmodels.PengirimanViewModel
import com.teladanprimaagro.tmpp.viewmodels.SettingsViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PengirimanInputScreen(
    navController: NavController,
    pengirimanViewModel: PengirimanViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val scannedItems by pengirimanViewModel.scannedItems.collectAsState()
    val scrollState = rememberScrollState()

    val supirOptions = settingsViewModel.supirList
    val noPolisiOptions = settingsViewModel.kendaraanList

    val dateTimeDisplay = pengirimanViewModel.dateTimeDisplay
    val totalBuahCalculated = pengirimanViewModel.totalBuahCalculated
    val spbNumber = pengirimanViewModel.spbNumber

    var selectedSupir by remember(supirOptions) { mutableStateOf(supirOptions.firstOrNull() ?: "") }
    var supirExpanded by remember { mutableStateOf(false) }

    var selectedVehicle by remember(noPolisiOptions) { mutableStateOf(noPolisiOptions.firstOrNull() ?: "") }
    var vehicleExpanded by remember { mutableStateOf(false) }

    val selectedMandorLoading by settingsViewModel.selectedMandorLoading.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (!pengirimanViewModel.isSessionActive.value) {
            pengirimanViewModel.generateSpbNumber(selectedMandorLoading)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Pengiriman",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = White
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackground)
                .padding(innerPadding)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                TextInputField(
                    label = "No. SPB",
                    value = spbNumber.value,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                TextInputField(
                    label = "Tanggal/Jam",
                    value = dateTimeDisplay.value,
                    onValueChange = {},
                    readOnly = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                DropdownInputField(
                    label = "Nama Supir",
                    options = supirOptions.toList(),
                    selectedOption = selectedSupir,
                    onOptionSelected = { selectedSupir = it },
                    expanded = supirExpanded,
                    onExpandedChange = { supirExpanded = it }
                )
                Spacer(modifier = Modifier.height(12.dp))

                DropdownInputField(
                    label = "No Polisi",
                    options = noPolisiOptions.toList(),
                    selectedOption = selectedVehicle,
                    onOptionSelected = { selectedVehicle = it },
                    expanded = vehicleExpanded,
                    onExpandedChange = { vehicleExpanded = it }
                )
                Spacer(modifier = Modifier.height(12.dp))

                TotalBuahDisplay(value = totalBuahCalculated.intValue)
                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    thickness = 1.dp,
                    color = White
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MainColor)
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableHeaderText(text = "No", color = Black)
                    TableHeaderText(text = "No. Unik", color = Black)
                    TableHeaderText(text = "Blok", color = Black)
                    TableHeaderText(text = "Total Buah", color = Black)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(OldGrey)
                ) {
                    if (scannedItems.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(50.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "No data icon",
                                    tint = LightGrey,
                                    modifier = Modifier.size(50.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Belum ada data panen hari ini.",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    color = LightGrey
                                )
                            }
                        }
                    } else {
                        itemsIndexed(scannedItems) { index, item ->
                            TableRow(
                                no = index + 1,
                                noUnik = item.uniqueNo,
                                blok = item.blok,
                                totalBuah = item.totalBuah
                            )
                            HorizontalDivider(thickness = 0.5.dp, color = White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        pengirimanViewModel.finalizeScannedItemsAsPengiriman(
                            namaSupir = selectedSupir,
                            noPolisi = selectedVehicle
                        )
                        selectedSupir = supirOptions.firstOrNull() ?: ""
                        selectedVehicle = noPolisiOptions.firstOrNull() ?: ""

                        navController.navigate("rekap_pengiriman_screen") {
                            popUpTo("pengiriman_input_screen") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MainColor,
                    disabledContainerColor = MainColor.copy(0.4f)
                ),
                enabled = scannedItems.isNotEmpty() &&
                        selectedSupir.isNotBlank() && selectedSupir != "Pilih Supir" &&
                        selectedVehicle.isNotBlank() && selectedVehicle != "Pilih No Polisi"
            ) {
                val textColor = if (scannedItems.isNotEmpty() &&
                    selectedSupir.isNotBlank() && selectedSupir != "Pilih Supir" &&
                    selectedVehicle.isNotBlank() && selectedVehicle != "Pilih No Polisi") {
                    Color.Black
                } else {
                    Color.White
                }
                Text("Finalisasi Pengiriman", color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun RowScope.TableHeaderText(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.weight(1f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
    )
}

@Composable
fun TableRow(no: Int, noUnik: String, blok: String, totalBuah: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableItemText(text = no.toString(), modifier = Modifier.weight(1f))
        TableItemText(text = noUnik, modifier = Modifier.weight(1f))
        TableItemText(text = blok, modifier = Modifier.weight(1f))
        TableItemText(text = totalBuah.toString(), modifier = Modifier.weight(1f))
    }
}

@Composable
fun TableItemText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
        fontSize = 14.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
    )
}