package com.teladanprimaagro.tmpp.ui.screens

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.components.DropdownInputField
import com.teladanprimaagro.tmpp.ui.components.TextInputField
import com.teladanprimaagro.tmpp.viewmodels.PengirimanViewModel
import com.teladanprimaagro.tmpp.viewmodels.SettingsViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
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

    LaunchedEffect(selectedMandorLoading) {
        pengirimanViewModel.generateSpbNumber(selectedMandorLoading)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Pengiriman",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    onOptionSelected = {
                        selectedSupir = it
                    },
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Buah",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = totalBuahCalculated.intValue.toString(),
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            // Warna teks di dalam TextField
                            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            disabledTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                            // Warna border
                            focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            // Warna background container
                            disabledContainerColor = MaterialTheme.colorScheme.secondary,
                            // Warna cursor (tidak terlihat karena readOnly)
                            cursorColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.width(120.dp)
                    )
                }
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableHeaderText(text = "No", color = MaterialTheme.colorScheme.onPrimary)
                    TableHeaderText(text = "No. Unik", color = MaterialTheme.colorScheme.onPrimary)
                    TableHeaderText(text = "Blok", color = MaterialTheme.colorScheme.onPrimary)
                    TableHeaderText(text = "Total Buah", color = MaterialTheme.colorScheme.onPrimary)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                ) {
                    if (scannedItems.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Belum ada item yang discan. Scan tag NFC!",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
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
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
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
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = scannedItems.isNotEmpty() &&
                        selectedSupir.isNotBlank() && selectedSupir != "Pilih Supir" &&
                        selectedVehicle.isNotBlank() && selectedVehicle != "Pilih No Polisi"
            ) {
                Text("Finalisasi Pengiriman", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
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
        maxLines = 1,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
    )
}