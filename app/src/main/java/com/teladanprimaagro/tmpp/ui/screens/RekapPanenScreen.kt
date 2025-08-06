package com.teladanprimaagro.tmpp.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.data.PanenData
import com.teladanprimaagro.tmpp.ui.components.PanenDetailDialog
import com.teladanprimaagro.tmpp.ui.theme.BackgroundLightGray
import com.teladanprimaagro.tmpp.ui.theme.DotGray
import com.teladanprimaagro.tmpp.ui.theme.PrimaryOrange
import com.teladanprimaagro.tmpp.ui.theme.TextGray
import com.teladanprimaagro.tmpp.ui.viewmodels.PanenViewModel
import com.teladanprimaagro.tmpp.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RekapPanenScreen(
    navController: NavController,
    panenViewModel: PanenViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {

    val panenList by panenViewModel.panenList.collectAsState()
    val totalDataMasuk by panenViewModel.totalDataMasuk.collectAsState()
    val totalSemuaBuah by panenViewModel.totalSemuaBuah.collectAsState()

    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedPanenData by remember { mutableStateOf<PanenData?>(null) }

    val sortOptions = listOf("Nama", "Blok")
    val selectedSortBy by panenViewModel.sortBy.collectAsState()
    val sortOrderAscending by panenViewModel.sortOrderAscending.collectAsState()
    var sortDropdownExpanded by remember { mutableStateOf(false) }

    val pemanenFilterOptions = remember {
        mutableStateOf(listOf("Semua") + settingsViewModel.pemanenList.toList())
    }
    val blokFilterOptions = remember {
        mutableStateOf(listOf("Semua") + settingsViewModel.blokList.toList())
    }

    val selectedPemanenFilter by panenViewModel.selectedPemanenFilter.collectAsState()
    val selectedBlokFilter by panenViewModel.selectedBlokFilter.collectAsState()

    var pemanenDropdownExpanded by remember { mutableStateOf(false) }
    var blokDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Rekap Panen",
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
                actions = {
                    IconButton(onClick = { /* TODO: Masih Belum Siap */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan",
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExposedDropdownMenuBox(
                        expanded = sortDropdownExpanded,
                        onExpandedChange = { sortDropdownExpanded = !sortDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        TextField(
                            value = selectedSortBy,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sortir Berdasarkan") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = sortDropdownExpanded,
                            onDismissRequest = { sortDropdownExpanded = false }
                        ) {
                            sortOptions.forEach { sortCriteria ->
                                DropdownMenuItem(
                                    text = { Text(sortCriteria) },
                                    onClick = {
                                        panenViewModel.setSortBy(sortCriteria)
                                        sortDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    OutlinedButton(
                        onClick = { panenViewModel.toggleSortOrder() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            containerColor = Color.Transparent
                        ),
                        border = BorderStroke(1.dp, DotGray),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Icon(
                            imageVector = if (sortOrderAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = if (sortOrderAscending) "Urutkan Menaik" else "Urutkan Menurun",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (sortOrderAscending) "Asc" else "Desc")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ExposedDropdownMenuBox(
                        expanded = pemanenDropdownExpanded,
                        onExpandedChange = { pemanenDropdownExpanded = !pemanenDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        TextField(
                            value = selectedPemanenFilter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filter Pemanen") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pemanenDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = pemanenDropdownExpanded,
                            onDismissRequest = { pemanenDropdownExpanded = false }
                        ) {
                            pemanenFilterOptions.value.forEach { pemanen ->
                                DropdownMenuItem(
                                    text = { Text(pemanen) },
                                    onClick = {
                                        panenViewModel.setPemanenFilter(pemanen)
                                        pemanenDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = blokDropdownExpanded,
                        onExpandedChange = { blokDropdownExpanded = !blokDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        TextField(
                            value = selectedBlokFilter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filter Blok") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = blokDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = blokDropdownExpanded,
                            onDismissRequest = { blokDropdownExpanded = false }
                        ) {
                            blokFilterOptions.value.forEach { blok ->
                                DropdownMenuItem(
                                    text = { Text(blok) },
                                    onClick = {
                                        panenViewModel.setBlokFilter(blok)
                                        blokDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = { panenViewModel.clearFilters() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                        containerColor = Color.Transparent
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear Filter",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Filter")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(PrimaryOrange, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderText(text = "Tanggal", weight = 0.20f)
                TableHeaderText(text = "Nama", weight = 0.25f)
                TableHeaderText(text = "Blok", weight = 0.10f)
                TableHeaderText(text = "Total", weight = 0.10f)
                TableHeaderText(text = "Edit", weight = 0.1f)
                TableHeaderText(text = "Detail", weight = 0.1f)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .background(BackgroundLightGray.copy(alpha = 0.1f), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            ) {
                if (panenList.isEmpty()) {
                    item {
                        Text(
                            text = "Belum ada data panen yang sesuai filter.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center,
                            color = TextGray
                        )
                    }
                } else {
                    items(panenList, key = { it.id }) { data ->
                        TableRow(
                            data = data,
                            onDetailClick = { clickedData ->
                                selectedPanenData = clickedData
                                showDetailDialog = true
                            },
                            onEditClick = { editedData ->
                                navController.navigate("panenInputScreen/${editedData.id}")
                            }
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.5f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryBox(label = "Data Masuk", value = totalDataMasuk.toString())

                OutlinedButton(
                    onClick = { panenViewModel.clearAllPanenData() }, // Panggil fungsi penghapusan
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                        containerColor = Color.Transparent
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(60.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "Hapus Semua Data",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "Hapus Semua",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                SummaryBox(label = "Total Buah", value = totalSemuaBuah.toString())
            }
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "*Data akan di reset setiap pukul 00.00",
                color = TextGray,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            )
        }
    }

    if (showDetailDialog && selectedPanenData != null) {
        PanenDetailDialog(
            panenData = selectedPanenData!!,
            onDismiss = {
                showDetailDialog = false
                selectedPanenData = null
            }
        )
    }
}

@Composable
fun RowScope.TableHeaderText(text: String, weight: Float) {
    Text(
        text = text,
        color = Color.Black,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(weight)
    )
}

@Composable
fun TableRow(data: PanenData, onDetailClick: (PanenData) -> Unit, onEditClick: (PanenData) -> Unit) {
    Log.d("RekapPanenDebug", "Nama Pemanen: ${data.namaPemanen}, Blok: ${data.blok}")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCellText(text = data.tanggalWaktu, weight = 0.20f)
        TableCellText(text = data.namaPemanen, weight = 0.25f)
        TableCellText(text = data.blok, weight = 0.10f)
        TableCellText(text = data.totalBuah.toString(), weight = 0.10f)

        Box(modifier = Modifier.weight(0.1f), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = PrimaryOrange,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onEditClick(data) }
            )
        }
        Box(modifier = Modifier.weight(0.1f), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Detail",
                tint = PrimaryOrange,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onDetailClick(data) }
            )
        }
    }
}

@Composable
fun RowScope.TableCellText(text: String, weight: Float) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(weight)
    )
}

@Composable
fun SummaryBox(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(110.dp)
            .background(PrimaryOrange, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
