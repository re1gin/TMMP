package com.teladanprimaagro.tmpp.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.theme.DangerRed
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.OldGrey
import com.teladanprimaagro.tmpp.ui.theme.SuccessGreen
import com.teladanprimaagro.tmpp.ui.theme.White
import com.teladanprimaagro.tmpp.viewmodels.SettingsViewModel
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelolaSupirScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    var newSupirName by remember { mutableStateOf("") }
    var editingSupir by remember { mutableStateOf<String?>(null) }
    var editedSupirName by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // State untuk Dialog
    var showAddSupirDialog by remember { mutableStateOf(false) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var importedNames by remember { mutableStateOf<List<String>>(emptyList()) }

    // Launcher untuk penyeleksi file .txt
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val names = mutableListOf<String>()

                reader.useLines { lines ->
                    lines.forEach { line ->
                        val cleanedLine = line.trim().uppercase()
                        if (cleanedLine.isNotBlank() && cleanedLine.matches(Regex("^[a-zA-Z0-9\\s]+$"))) {
                            if (!settingsViewModel.supirList.contains(cleanedLine)) {
                                names.add(cleanedLine)
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("File berisi data tidak valid.", withDismissAction = true)
                            }
                            return@forEach
                        }
                    }
                }
                inputStream?.close()

                if (names.isNotEmpty()) {
                    importedNames = names
                    showImportConfirmDialog = true
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Tidak ada supir baru yang valid untuk diimpor.", withDismissAction = true)
                    }
                }
            } catch (e: Exception) {
                Log.e("KelolaSupirScreen", "Gagal membaca file", e)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Gagal mengimpor supir: ${e.message}", withDismissAction = true)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Kelola Supir",
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
                    containerColor = MainColor // Menggunakan warna dari skema warna
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackground)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Tombol "Tambah Supir" memicu dialog
            Button(
                onClick = { showAddSupirDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Supir", fontWeight = FontWeight.Bold)
            }

            // Tombol "Impor Supir"
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { filePickerLauncher.launch("text/plain") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6A6AD6),
                    contentColor = White
                )
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = "Impor")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Impor dari File .txt", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Daftar Supir:",
                color = White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(8.dp))

            // Daftar Supir
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(settingsViewModel.supirList, key = { it }) { supir ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (editingSupir == supir) {
                            OutlinedTextField(
                                value = editedSupirName,
                                onValueChange = { editedSupirName = it.uppercase() },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = White,
                                    unfocusedTextColor = White,
                                    focusedBorderColor = MainColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    cursorColor = MainColor,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    val newName = editedSupirName.trim().uppercase()
                                    if (newName.isNotBlank() && newName != supir && !settingsViewModel.supirList.contains(newName)) {
                                        settingsViewModel.updateSupir(supir, newName)
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Nama supir tidak valid atau sudah ada.", withDismissAction = true)
                                        }
                                    }
                                    editingSupir = null
                                }
                            ) {
                                Icon(Icons.Default.Done, contentDescription = "Simpan", tint = MainColor)
                            }
                            IconButton(onClick = { editingSupir = null }) {
                                Icon(Icons.Default.Clear, contentDescription = "Batal", tint = DangerRed)
                            }
                        } else {
                            Text(
                                text = supir,
                                color = White,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                editingSupir = supir
                                editedSupirName = supir
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = White)
                            }
                            IconButton(onClick = { settingsViewModel.removeSupir(supir) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed)
                            }
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                }
            }
        }
    }

    // Dialog untuk menambah supir baru
    if (showAddSupirDialog) {
        AlertDialog(
            onDismissRequest = { showAddSupirDialog = false },
            title = { Text("Tambah Supir Baru", color = MainColor) },
            text = {
                OutlinedTextField(
                    value = newSupirName,
                    onValueChange = { newSupirName = it.uppercase() },
                    label = { Text("Nama Supir") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        cursorColor = White,
                        unfocusedTextColor = White,
                        focusedTextColor = White,
                        focusedLabelColor = White,
                        focusedBorderColor = White
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val nameToAdd = newSupirName.trim().uppercase()
                        if (nameToAdd.isNotBlank()) {
                            if (!settingsViewModel.supirList.contains(nameToAdd)) {
                                settingsViewModel.addSupir(nameToAdd)
                                newSupirName = ""
                                showAddSupirDialog = false
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Nama supir sudah ada.", withDismissAction = true)
                                }
                            }
                        }
                    }
                ) {
                    Text("Tambah", color = SuccessGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddSupirDialog = false
                    newSupirName = ""
                }) {
                    Text("Batal", color = DangerRed)
                }
            },
            containerColor = OldGrey,
        )
    }

    // Dialog konfirmasi impor file
    if (showImportConfirmDialog) {
        AlertDialog(
            modifier = Modifier.padding(16.dp),
            onDismissRequest = { showImportConfirmDialog = false },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false),
            title = {
                Text(
                    text = "Impor Supir",
                    fontSize = 18.sp,
                    color = MainColor,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Apakah Anda yakin ingin menambahkan ${importedNames.size} supir ini?",
                        fontSize = 15.sp,
                        color = White,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (importedNames.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 150.dp)
                        ) {
                            importedNames.forEach { name ->
                                Text(
                                    text = "• $name",
                                    color = White,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        importedNames.forEach { name ->
                            settingsViewModel.addSupir(name)
                        }
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Berhasil mengimpor ${importedNames.size} supir.", withDismissAction = true)
                        }
                        showImportConfirmDialog = false
                    }
                ) {
                    Text("Ya, Impor", color = SuccessGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmDialog = false }) {
                    Text("Batal", color = DangerRed)
                }
            },
            containerColor = OldGrey,
        )
    }
}