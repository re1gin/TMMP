package com.teladanprimaagro.tmpp.ui.screens.setting

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
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
fun KelolaTphScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    var newTphName by remember { mutableStateOf("") }
    var editingTph by remember { mutableStateOf<String?>(null) }
    var editedTphName by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // State untuk Dialog
    var showAddTphDialog by remember { mutableStateOf(false) }
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
                            if (!settingsViewModel.tphList.contains(cleanedLine)) {
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
                        snackbarHostState.showSnackbar("Tidak ada No. TPH baru yang valid untuk diimpor.", withDismissAction = true)
                    }
                }
            } catch (e: Exception) {
                Log.e("KelolaTphScreen", "Gagal membaca file", e)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Gagal mengimpor No. TPH: ${e.message}", withDismissAction = true)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Kelola No. TPH",
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
                    containerColor = MainColor
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
            // Tombol "Tambah TPH" memicu dialog
            Button(
                onClick = { showAddTphDialog = true },
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
                Text("Tambah TPH", fontWeight = FontWeight.Bold)
            }

            // Tombol "Impor TPH"
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
                text = "Daftar No. TPH:",
                color = White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(8.dp))

            // Daftar TPH
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(settingsViewModel.tphList, key = { it }) { tph ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (editingTph == tph) {
                            OutlinedTextField(
                                value = editedTphName,
                                onValueChange = { editedTphName = it.uppercase() },
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
                                    selectionColors = TextSelectionColors(
                                        handleColor = MainColor,
                                        backgroundColor = MainColor.copy(alpha = 0.4f)
                                    )
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    val newName = editedTphName.trim().uppercase()
                                    if (newName.isNotBlank() && newName != tph && !settingsViewModel.tphList.contains(newName)) {
                                        settingsViewModel.updateTph(tph, newName)
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("No. TPH tidak valid atau sudah ada.", withDismissAction = true)
                                        }
                                    }
                                    editingTph = null
                                }
                            ) {
                                Icon(Icons.Default.Done, contentDescription = "Simpan", tint = MainColor)
                            }
                            IconButton(onClick = { editingTph = null }) {
                                Icon(Icons.Default.Clear, contentDescription = "Batal", tint = DangerRed)
                            }
                        } else {
                            Text(
                                text = tph,
                                color = White,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                editingTph = tph
                                editedTphName = tph
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = White)
                            }
                            IconButton(onClick = { settingsViewModel.removeTph(tph) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = DangerRed)
                            }
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                }
            }
        }
    }

    // Dialog untuk menambah TPH baru
    if (showAddTphDialog) {
        AlertDialog(
            onDismissRequest = { showAddTphDialog = false },
            title = { Text("Tambah TPH Baru", color = MainColor) },
            text = {
                OutlinedTextField(
                    value = newTphName,
                    onValueChange = { newTphName = it.uppercase() },
                    label = { Text("No. TPH") },
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
                        val nameToAdd = newTphName.trim().uppercase()
                        if (nameToAdd.isNotBlank()) {
                            if (!settingsViewModel.tphList.contains(nameToAdd)) {
                                settingsViewModel.addTph(nameToAdd)
                                newTphName = ""
                                showAddTphDialog = false
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("No. TPH sudah ada.", withDismissAction = true)
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
                    showAddTphDialog = false
                    newTphName = ""
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
                    text = "Impor No. TPH",
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
                        text = "Apakah Anda yakin ingin menambahkan ${importedNames.size} No. TPH ini?",
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
                            settingsViewModel.addTph(name)
                        }
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Berhasil mengimpor ${importedNames.size} No. TPH.", withDismissAction = true)
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