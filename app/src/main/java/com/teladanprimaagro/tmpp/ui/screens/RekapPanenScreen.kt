package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
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
import com.teladanprimaagro.tmpp.ui.components.PanenTableRow
import com.teladanprimaagro.tmpp.ui.components.PasswordConfirmationDialog
import com.teladanprimaagro.tmpp.ui.components.SummaryBox
import com.teladanprimaagro.tmpp.ui.components.TableHeaderText
import com.teladanprimaagro.tmpp.ui.theme.Black
import com.teladanprimaagro.tmpp.ui.theme.DangerRed
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.OldGrey
import com.teladanprimaagro.tmpp.ui.theme.White
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

    val sortOptions = listOf("Nama", "Blok", "Waktu")
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

    // State untuk dialog konfirmasi sandi
    var showPasswordDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isSelectionMode) "${selectedItems.size} Terpilih" else "Rekap Panen",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = White
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
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackground)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            colors = ExposedDropdownMenuDefaults.textFieldColors(
                                focusedTextColor = White,
                                unfocusedTextColor = White,
                                errorTextColor = MaterialTheme.colorScheme.error,
                                focusedContainerColor = OldGrey,
                                unfocusedContainerColor = OldGrey,
                                disabledContainerColor = OldGrey,
                                errorContainerColor = MaterialTheme.colorScheme.error,
                                cursorColor = MaterialTheme.colorScheme.onPrimary,
                                errorCursorColor = MaterialTheme.colorScheme.error,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = MaterialTheme.colorScheme.error,
                                focusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                disabledLabelColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.38f),
                                errorLabelColor = MaterialTheme.colorScheme.error
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = sortDropdownExpanded,
                            onDismissRequest = { sortDropdownExpanded = false },
                            modifier = Modifier.background(OldGrey.copy(0.5f))
                        ) {
                            sortOptions.forEach { sortCriteria ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = sortCriteria,
                                            color = White,
                                            fontSize = 15.sp
                                        )
                                    },
                                    onClick = {
                                        panenViewModel.setSortBy(sortCriteria)
                                        sortDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedButton(
                        onClick = { panenViewModel.toggleSortOrder() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = MainColor),
                        modifier = Modifier.size(55.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = if (sortOrderAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = if (sortOrderAscending) "Urutkan Menaik" else "Urutkan Menurun",
                            tint = Black
                        )
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
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            colors = ExposedDropdownMenuDefaults.textFieldColors(
                                focusedTextColor = White,
                                unfocusedTextColor = White,
                                errorTextColor = MaterialTheme.colorScheme.error,
                                focusedContainerColor = OldGrey,
                                unfocusedContainerColor = OldGrey,
                                disabledContainerColor = OldGrey,
                                errorContainerColor = MaterialTheme.colorScheme.error,
                                cursorColor = MaterialTheme.colorScheme.onPrimary,
                                errorCursorColor = MaterialTheme.colorScheme.error,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = MaterialTheme.colorScheme.error,
                                focusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                disabledLabelColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.38f),
                                errorLabelColor = MaterialTheme.colorScheme.error
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = pemanenDropdownExpanded,
                            onDismissRequest = { pemanenDropdownExpanded = false },
                            modifier = Modifier.background(OldGrey)
                        ) {
                            pemanenFilterOptions.value.forEach { pemanen ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = pemanen,
                                            color = White,
                                            fontSize = 15.sp
                                        )
                                    },
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
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            colors = ExposedDropdownMenuDefaults.textFieldColors(
                                focusedTextColor = White,
                                unfocusedTextColor = White,
                                errorTextColor = MaterialTheme.colorScheme.error,
                                focusedContainerColor = OldGrey,
                                unfocusedContainerColor = OldGrey,
                                disabledContainerColor = OldGrey,
                                errorContainerColor = MaterialTheme.colorScheme.error,
                                cursorColor = MaterialTheme.colorScheme.onPrimary,
                                errorCursorColor = MaterialTheme.colorScheme.error,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                errorIndicatorColor = MaterialTheme.colorScheme.error,
                                focusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                disabledLabelColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.38f),
                                errorLabelColor = MaterialTheme.colorScheme.error
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = blokDropdownExpanded,
                            onDismissRequest = { blokDropdownExpanded = false },
                            modifier = Modifier.background(OldGrey.copy(0.5f))
                        ) {
                            blokFilterOptions.value.forEach { blok ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = blok,
                                            color = White,
                                            fontSize = 15.sp
                                        )
                                    },
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
                        contentColor = DangerRed.copy(0.7f),
                        containerColor = DangerRed.copy(0.1f)
                    ),
                    border = BorderStroke(1.dp, DangerRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear Filter",
                        modifier = Modifier.size(20.dp),
                        tint = White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Clear Filter", color = White)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MainColor)
                        .padding(vertical = 8.dp),
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
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    TableHeaderText(text = "Jam", weight = 0.10f)
                    TableHeaderText(text = "Nama", weight = 0.20f)
                    TableHeaderText(text = "Blok", weight = 0.15f)
                    TableHeaderText(text = "Total", weight = 0.15f)
                    if (!isSelectionMode) {
                        TableHeaderText(text = "Edit", weight = 0.10f)
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Gray.copy(alpha = 0.1f), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                ) {
                    if (panenList.isEmpty()) {
                        item {
                            Text(
                                text = "Belum ada data panen hari ini.",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = Gray
                            )
                        }
                    } else {
                        items(panenList, key = { it.id }) { data ->
                            val isSelected = selectedItems.contains(data.id)
                            PanenTableRow(
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SummaryBox(label = "Data Masuk", value = totalDataMasuk.toString())
                        SummaryBox(label = "Total Buah", value = totalSemuaBuah.toString())
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                            onClick = { showPasswordDialog = true },
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
            }
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
    if (showPasswordDialog) {
        PasswordConfirmationDialog(
            onDismissRequest = { showPasswordDialog = false },
            onConfirm = { password ->
                val correctPassword = "123"
                if (password == correctPassword) {
                    showDeleteAllDialog = true
                }
                showPasswordDialog = false
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
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Hapus Data Terpilih?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Text(
                        text = "Yakin hapus ${selectedItems.size} data? Aksi ini tidak dapat di batalkan!",
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
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
                                panenViewModel.deleteSelectedPanenData(selectedItems.toList())
                                isSelectionMode = false
                                selectedItems = emptySet()
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