package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.theme.BackgroundDarkGrey
import com.teladanprimaagro.tmpp.ui.theme.SuccessGreen
import com.teladanprimaagro.tmpp.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelolaPemanenScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    var newPemanenName by remember { mutableStateOf("") }
    var editingPemanen by remember { mutableStateOf<String?>(null) }
    var editedPemanenName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kelola Pemanen",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    color = MaterialTheme.colorScheme.background,
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        color = BackgroundDarkGrey,
                    )
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Input Field untuk Pemanen Baru
                OutlinedTextField(
                    value = newPemanenName,
                    onValueChange = { newPemanenName = it.uppercase() },
                    label = { Text("Nama Pemanen Baru", color = Color.White) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.onPrimary,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.Transparent,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Tombol Tambah Pemanen
                Button(
                    onClick = {
                        if (newPemanenName.isNotBlank()) {
                            settingsViewModel.addPemanen(newPemanenName.trim())
                            newPemanenName = ""
                        }
                    },
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
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Daftar Pemanen:",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(8.dp))

                // Daftar Pemanen
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
                                // Input Field untuk Edit Pemanen
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
    }
}
