package com.teladanprimaagro.tmpp.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.teladanprimaagro.tmpp.ui.theme.BackgroundDarkGrey
import com.teladanprimaagro.tmpp.ui.theme.Black
import com.teladanprimaagro.tmpp.ui.theme.DangerRed
import com.teladanprimaagro.tmpp.ui.theme.Grey
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
fun KelolaPemanenScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    var newPemanenName by remember { mutableStateOf("") }
    var editingPemanen by remember { mutableStateOf<String?>(null) }
    var editedPemanenName by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // State untuk Dialog
    var showAddPemanenDialog by remember { mutableStateOf(false) }
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
                            if (!settingsViewModel.pemanenList.contains(cleanedLine)) {
                                names.add(cleanedLine)
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("File berisi data tidak valid.", withDismissAction = true)
                            }
                            return@forEach // Berhenti memproses jika ada yang tidak valid
                        }
                    }
                }
                inputStream?.close()

                if (names.isNotEmpty()) {
                    importedNames = names
                    showImportConfirmDialog = true
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Tidak ada pemanen baru yang valid untuk diimpor.", withDismissAction = true)
                    }
                }
            } catch (e: Exception) {
                Log.e("KelolaPemanenScreen", "Gagal membaca file", e)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Gagal mengimpor pemanen: ${e.message}", withDismissAction = true)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Kelola Pemanen",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
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
            // Tombol "Tambah Pemanen" sekarang memicu dialog
            Button(
                onClick = { showAddPemanenDialog = true },
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
                Text("Tambah Pemanen", fontWeight = FontWeight.Bold)
            }

            // Tombol "Impor Pemanen"
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { filePickerLauncher.launch("text/plain") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6A6AD6),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = "Impor")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Impor dari File .txt", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Daftar Pemanen
            Text(
                text = "Daftar Pemanen:",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(settingsViewModel.pemanenList, key = { it }) { pemanen ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (editingPemanen == pemanen) {
                            OutlinedTextField(
                                value = editedPemanenName,
                                onValueChange = { editedPemanenName = it },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (editedPemanenName.isNotBlank() && editedPemanenName.trim() != pemanen) {
                                        settingsViewModel.updatePemanen(pemanen, editedPemanenName.trim())
                                    }
                                    editingPemanen = null
                                },
                                enabled = editedPemanenName.isNotBlank() && editedPemanenName.trim() != pemanen
                            ) {
                                Icon(Icons.Default.Done, contentDescription = "Simpan", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { editingPemanen = null }) {
                                Icon(Icons.Default.Clear, contentDescription = "Batal", tint = MaterialTheme.colorScheme.error)
                            }
                        } else {
                            Text(
                                text = pemanen,
                                color = Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                editingPemanen = pemanen
                                editedPemanenName = pemanen
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                            }
                            IconButton(onClick = { settingsViewModel.removePemanen(pemanen) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                }
            }
        }
    }

    if (showAddPemanenDialog) {
        AlertDialog(
            onDismissRequest = { showAddPemanenDialog = false },
            title = { Text("Tambah Pemanen Baru", color = MainColor) },
            text = {
                OutlinedTextField(
                    value = newPemanenName,
                    onValueChange = { newPemanenName = it.uppercase() },
                    label = { Text("Nama Pemanen") },
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
                        val nameToAdd = newPemanenName.trim().uppercase()
                        if (nameToAdd.isNotBlank()) {
                            if (!settingsViewModel.pemanenList.contains(nameToAdd)) {
                                settingsViewModel.addPemanen(nameToAdd)
                                newPemanenName = ""
                                showAddPemanenDialog = false
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Pemanen sudah ada.", withDismissAction = true)
                                }
                            }
                        }
                    }
                ) {
                    Text("Tambah", color = SuccessGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPemanenDialog = false }) {
                    Text("Batal", color = DangerRed)
                }
            },
            containerColor = OldGrey,
        )
    }

    // Dialog untuk menambah pemanen baru
    if (showImportConfirmDialog) {
        AlertDialog(
            modifier = Modifier.padding(16.dp),
            onDismissRequest = { showImportConfirmDialog = false },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false),
            title = {
                Text(
                    text = "Impor Pemanen",
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
                        text = "Apakah Anda yakin ingin menambahkan ${importedNames.size} pemanen ini?",
                        fontSize = 15.sp,
                        color = White,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Tambahkan daftar nama pemanen yang akan diimpor
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
                            settingsViewModel.addPemanen(name)
                        }
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Berhasil mengimpor ${importedNames.size} pemanen.", withDismissAction = true)
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