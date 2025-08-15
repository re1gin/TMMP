package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.data.PanenData
import com.teladanprimaagro.tmpp.ui.components.PanenDetailDialog
import com.teladanprimaagro.tmpp.ui.components.SummaryBox
import com.teladanprimaagro.tmpp.viewmodels.PanenViewModel
import com.teladanprimaagro.tmpp.viewmodels.SettingsViewModel

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
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    // State untuk multi-selection
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf(setOf<Int>()) }

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
                        text = if (isSelectionMode) "${selectedItems.size} Terpilih" else "Rekap Panen",
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
                                tint = Color.Red
                            )
                        }
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
                                .fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.textFieldColors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                errorTextColor = MaterialTheme.colorScheme.error,

                                // Warna container
                                focusedContainerColor = MaterialTheme.colorScheme.secondary,
                                unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                                disabledContainerColor = MaterialTheme.colorScheme.secondary,
                                errorContainerColor = MaterialTheme.colorScheme.error,

                                // Warna cursor
                                cursorColor = MaterialTheme.colorScheme.onPrimary,
                                errorCursorColor = MaterialTheme.colorScheme.error,

                                // Hilangkan garis bawah
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = MaterialTheme.colorScheme.error,

                                // Warna label
                                focusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                disabledLabelColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.38f),
                                errorLabelColor = MaterialTheme.colorScheme.error,
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = sortDropdownExpanded,
                            onDismissRequest = { sortDropdownExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.secondary)
                        ) {
                            sortOptions.forEach { sortCriteria ->
                                DropdownMenuItem(
                                    text = { Text(sortCriteria, color = MaterialTheme.colorScheme.onSecondary)},
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
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Icon(
                            imageVector = if (sortOrderAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = if (sortOrderAscending) "Urutkan Menaik" else "Urutkan Menurun",
                            tint = MaterialTheme.colorScheme.onSecondary
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
                                .fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.textFieldColors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                errorTextColor = MaterialTheme.colorScheme.error,

                                // Warna container
                                focusedContainerColor = MaterialTheme.colorScheme.secondary,
                                unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                                disabledContainerColor = MaterialTheme.colorScheme.secondary,
                                errorContainerColor = MaterialTheme.colorScheme.error,

                                // Warna cursor
                                cursorColor = MaterialTheme.colorScheme.onPrimary,
                                errorCursorColor = MaterialTheme.colorScheme.error,

                                // Hilangkan garis bawah
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = MaterialTheme.colorScheme.error,

                                // Warna label
                                focusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                disabledLabelColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.38f),
                                errorLabelColor = MaterialTheme.colorScheme.error,
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = pemanenDropdownExpanded,
                            onDismissRequest = { pemanenDropdownExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.secondary)
                        ) {
                            pemanenFilterOptions.value.forEach { pemanen ->
                                DropdownMenuItem(
                                    text = { Text(pemanen, color = MaterialTheme.colorScheme.onSecondary) },
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
                                .fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.textFieldColors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                errorTextColor = MaterialTheme.colorScheme.error,

                                // Warna container
                                focusedContainerColor = MaterialTheme.colorScheme.secondary,
                                unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                                disabledContainerColor = MaterialTheme.colorScheme.secondary,
                                errorContainerColor = MaterialTheme.colorScheme.error,

                                // Warna cursor
                                cursorColor = MaterialTheme.colorScheme.onPrimary,
                                errorCursorColor = MaterialTheme.colorScheme.error,

                                // Hilangkan garis bawah
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = MaterialTheme.colorScheme.error,

                                // Warna label
                                focusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                disabledLabelColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.38f),
                                errorLabelColor = MaterialTheme.colorScheme.error,
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = blokDropdownExpanded,
                            onDismissRequest = { blokDropdownExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.secondary)
                        ) {
                            blokFilterOptions.value.forEach { blok ->
                                DropdownMenuItem(
                                    text = { Text(blok, color = MaterialTheme.colorScheme.onSecondary) },
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
                    .background(MaterialTheme.colorScheme.onPrimary, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = panenList.isNotEmpty() && selectedItems.size == panenList.size,
                        onCheckedChange = { isChecked ->
                            selectedItems = if (isChecked) {
                                panenList.map { it.id }.toSet()
                            } else {
                                emptySet()
                            }
                        }
                    )
                } else {
                    Spacer(modifier = Modifier.size(24.dp))
                }
                TableHeaderText(text = "Tanggal", weight = 0.20f)
                TableHeaderText(text = "Nama", weight = 0.25f)
                TableHeaderText(text = "Blok", weight = 0.10f)
                TableHeaderText(text = "Total", weight = 0.10f)
                if (!isSelectionMode) {
                    TableHeaderText(text = "Edit", weight = 0.1f)
                    TableHeaderText(text = "Detail", weight = 0.1f)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .background(Gray.copy(alpha = 0.1f), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            ) {
                if (panenList.isEmpty()) {
                    item {
                        Text(
                            text = "Belum ada data panen yang sesuai filter.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center,
                            color = Gray
                        )
                    }
                } else {
                    items(panenList, key = { it.id }) { data ->
                        val isSelected = selectedItems.contains(data.id)
                        TableRow(
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
                            onDetailClick = { clickedData ->
                                selectedPanenData = clickedData
                                showDetailDialog = true
                            },
                            onEditClick = { editedData ->
                                navController.navigate("panenInputScreen/${editedData.id}")
                            }
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = Gray.copy(alpha = 0.5f))
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
                            panenViewModel.deleteSelectedPanenData(selectedItems.toList())
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
                color = Gray,
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

    if (showDeleteAllDialog) {
        Dialog(onDismissRequest = { showDeleteAllDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Hapus Semua Data?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Text(
                        text = "Yakin hapus semua data? Aksi ini tidak dapat di batalkan!",
                        color = Color.White.copy(alpha = 0.7f),
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
                                containerColor = Color(0xFFFF0600),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Tidak", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                panenViewModel.clearAllPanenData()
                                showDeleteAllDialog = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF89FF00),
                                contentColor = Color.Black
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
fun TableRow(
    data: PanenData,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelection: (Int) -> Unit,
    onLongPress: (PanenData) -> Unit,
    onDetailClick: (PanenData) -> Unit,
    onEditClick: (PanenData) -> Unit
) {
    val backgroundColor = if (isSelected) Gray.copy(alpha = 0.5f) else Color.Transparent

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
            .padding(vertical = 8.dp, horizontal = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection(data.id) },
                modifier = Modifier.padding(end = 1.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(10.dp))
        }

        TableCellText(text = data.tanggalWaktu, weight = 0.25f)
        TableCellText(text = data.namaPemanen, weight = 0.25f)
        TableCellText(text = data.blok, weight = 0.10f)
        TableCellText(text = data.totalBuah.toString(), weight = 0.10f)

        if (!isSelectionMode) {
            Box(modifier = Modifier.weight(0.1f), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onEditClick(data) }
                )
            }
            Box(modifier = Modifier.weight(0.1f), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Detail",
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onDetailClick(data) }
                )
            }
        }
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
fun RowScope.TableCellText(text: String, weight: Float) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(weight)
    )
}

