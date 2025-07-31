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
import com.teladanprimaagro.tmpp.ui.theme.BackgroundLightGray
import com.teladanprimaagro.tmpp.ui.theme.DotGray
import com.teladanprimaagro.tmpp.ui.theme.TextGray
import com.teladanprimaagro.tmpp.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelolaKemandoranScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    var newMandorName by remember { mutableStateOf("") }
    // State untuk melacak mandor yang sedang diedit
    var editingMandor by remember { mutableStateOf<String?>(null) }
    // State untuk menyimpan teks edit sementara
    var editedMandorName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(
                text = "Kelola Kemandoran",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp)) // Untuk menyeimbangkan header
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Input untuk nama mandor baru
            OutlinedTextField(
                value = newMandorName,
                onValueChange = { newMandorName = it.uppercase()},
                label = { Text("Nama Mandor Baru") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = DotGray,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = BackgroundLightGray,
                    unfocusedContainerColor = BackgroundLightGray,
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Tambah
            Button(
                onClick = {
                    if (newMandorName.isNotBlank()) {
                        settingsViewModel.addMandor(newMandorName.trim())
                        newMandorName = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFADFF2F),
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Mandor", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Daftar Mandor:",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            HorizontalDivider(thickness = 1.dp, color = DotGray)
            Spacer(modifier = Modifier.height(8.dp))

            // Daftar mandor yang sudah ada
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(settingsViewModel.mandorList, key = { it }) { mandor ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (editingMandor == mandor) {
                            // Tampilan saat mode edit
                            OutlinedTextField(
                                value = editedMandorName,
                                onValueChange = { editedMandorName = it },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = DotGray,
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                    focusedContainerColor = BackgroundLightGray,
                                    unfocusedContainerColor = BackgroundLightGray,
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Tombol Simpan
                            IconButton(
                                onClick = {
                                    if (editedMandorName.isNotBlank() && editedMandorName.trim() != mandor) {
                                        settingsViewModel.updateMandor(mandor, editedMandorName.trim())
                                    }
                                    editingMandor = null // Keluar dari mode edit
                                },
                                enabled = editedMandorName.isNotBlank() // Aktifkan hanya jika ada teks
                            ) {
                                Icon(Icons.Default.Done, contentDescription = "Simpan", tint = Color.Green)
                            }
                            // Tombol Batal
                            IconButton(onClick = { editingMandor = null }) {
                                Icon(Icons.Default.Clear, contentDescription = "Batal", tint = Color.Red)
                            }
                        } else {
                            // Tampilan normal
                            Text(
                                text = mandor,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Tombol Edit
                            IconButton(onClick = {
                                editingMandor = mandor
                                editedMandorName = mandor
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextGray)
                            }
                            // Tombol Hapus
                            IconButton(onClick = { settingsViewModel.removeMandor(mandor) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}