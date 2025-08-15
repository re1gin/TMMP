package com.teladanprimaagro.tmpp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.data.PengirimanData
import com.teladanprimaagro.tmpp.ui.components.PengirimanDetailDialog
import com.teladanprimaagro.tmpp.ui.components.SummaryBox
import com.teladanprimaagro.tmpp.viewmodels.PengirimanViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RekapPengirimanScreen(
    navController: NavController,
    pengirimanViewModel: PengirimanViewModel = viewModel()
) {

    val pengirimanList by pengirimanViewModel.pengirimanList.collectAsState()
    val totalDataMasuk by pengirimanViewModel.totalDataMasuk.collectAsState()
    val totalSemuaBuah by pengirimanViewModel.totalSemuaBuah.collectAsState()

    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedPengirimanData by remember { mutableStateOf<PengirimanData?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf(setOf<Int>()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isSelectionMode) "${selectedItems.size} Terpilih" else "Rekap Evakuasi",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSelectionMode) {
                            isSelectionMode = false
                            selectedItems = emptySet()
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            imageVector = if (isSelectionMode) Icons.Default.Clear else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isSelectionMode) "Batal" else "Kembali",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    if (!isSelectionMode) {
                        IconButton(onClick = { showDeleteAllDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = "Hapus Semua",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Checkbox(
                            checked = pengirimanList.isNotEmpty() && selectedItems.size == pengirimanList.size,
                            onCheckedChange = { isChecked ->
                                selectedItems = if (isChecked) {
                                    pengirimanList.map { it.id }.toSet()
                                } else {
                                    emptySet()
                                }
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(MaterialTheme.colorScheme.onPrimary, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    Spacer(modifier = Modifier.size(24.dp))
                } else {
                    Spacer(modifier = Modifier.size(24.dp))
                }
                TableHeaderText(text = "Tanggal", weight = 0.20f)
                TableHeaderText(text = "No. SPB", weight = 0.35f)
                TableHeaderText(text = "Total", weight = 0.20f)
                if (!isSelectionMode) {
                    TableHeaderText(text = "Detail", weight = 0.15f)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            ) {
                if (pengirimanList.isEmpty()) {
                    item {
                        Text(
                            text = "Belum ada data pengiriman.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(pengirimanList, key = { it.id }) { data ->
                        val isSelected = selectedItems.contains(data.id)
                        PengirimanTableRow(
                            data = data,
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected,
                            onToggleSelection = { itemId ->
                                selectedItems = if (selectedItems.contains(itemId)) {
                                    selectedItems - itemId
                                } else {
                                    selectedItems + itemId
                                }
                            },
                            onLongPress = {
                                isSelectionMode = true
                                selectedItems = selectedItems + it.id
                            },
                            onDetailClick = { clickedData: PengirimanData ->
                                selectedPengirimanData = clickedData
                                showDetailDialog = true
                            },
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (!isSelectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SummaryBox(label = "Data Masuk", value = totalDataMasuk.toString())
                    SummaryBox(label = "Total Buah", value = totalSemuaBuah.toString())
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(
                        onClick = {
                            isSelectionMode = false
                            selectedItems = emptySet()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Batal", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            pengirimanViewModel.deleteSelectedPengirimanData(selectedItems.toList())
                            isSelectionMode = false
                            selectedItems = emptySet()
                        },
                        enabled = selectedItems.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus (${selectedItems.size})", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "*Data akan di reset setiap pukul 00.00",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
    }

    if (showDetailDialog && selectedPengirimanData != null) {
        PengirimanDetailDialog(
            pengirimanEntry = selectedPengirimanData!!,
            onDismiss = {
                showDetailDialog = false
                selectedPengirimanData = null
            },
            onSendPrintClick = { pengirimanDataToPrint ->
                navController.navigate("send_print_data/${pengirimanDataToPrint.id}")
                showDetailDialog = false
                selectedPengirimanData = null
            }
        )
    }

    if (showDeleteAllDialog) {
        Dialog(onDismissRequest = { showDeleteAllDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Hapus Semua Data?",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Text(
                        text = "Yakin hapus semua data? Aksi ini tidak dapat di batalkan!",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        Button(
                            onClick = { showDeleteAllDialog = false },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Tidak", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                pengirimanViewModel.clearAllPengirimanData()
                                showDeleteAllDialog = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Ya", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PengirimanTableRow(
    data: PengirimanData,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelection: (Int) -> Unit,
    onLongPress: (PengirimanData) -> Unit,
    onDetailClick: (PengirimanData) -> Unit,
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onToggleSelection(data.id)
                    } else {
                        onDetailClick(data)
                    }
                },
                onLongClick = { onLongPress(data) }
            )
            .background(backgroundColor)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection(data.id) },
                modifier = Modifier.padding(end = 8.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(24.dp))
        }

        TableCellText(text = data.waktuPengiriman, weight = 0.20f)
        TableCellText(text = data.spbNumber, weight = 0.35f)
        TableCellText(text = data.totalBuah.toString(), weight = 0.20f)

        if (!isSelectionMode) {
            Box(modifier = Modifier.weight(0.15f), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Detail",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onDetailClick(data) }
                )
            }
        }
    }
}
