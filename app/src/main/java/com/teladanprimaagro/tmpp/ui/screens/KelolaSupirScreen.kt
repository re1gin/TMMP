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
fun KelolaSupirScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    var newSupirName by remember { mutableStateOf("") }
    // State untuk melacak supir yang sedang diedit
    var editingSupir by remember { mutableStateOf<String?>(null) }
    // State untuk menyimpan teks edit sementara
    var editedSupirName by remember { mutableStateOf("") }

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
                text = "Kelola Supir",
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

            // Input untuk nama supir baru
            OutlinedTextField(
                value = newSupirName,
                onValueChange = { newSupirName = it.uppercase() },
                label = { Text("Nama Supir Baru") },
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
                    if (newSupirName.isNotBlank()) {
                        settingsViewModel.addSupir(newSupirName.trim()) // Gunakan trim() untuk membersihkan spasi
                        newSupirName = "" // Bersihkan input setelah ditambahkan
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFADFF2F), // Warna hijau terang
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Supir", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Daftar Supir:",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            HorizontalDivider(thickness = 1.dp, color = DotGray)
            Spacer(modifier = Modifier.height(8.dp))

            // Daftar supir yang sudah ada
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Gunakan key untuk performa dan stabilitas UI saat daftar berubah
                items(settingsViewModel.supirList, key = { it }) { supir ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (editingSupir == supir) {
                            // Tampilan saat mode edit
                            OutlinedTextField(
                                value = editedSupirName,
                                onValueChange = { editedSupirName = it.uppercase() },
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
                                    // Validasi: Pastikan nama baru tidak kosong dan tidak sama dengan nama lama (setelah trim)
                                    if (editedSupirName.isNotBlank() && editedSupirName.trim() != supir) {
                                        settingsViewModel.updateSupir(supir, editedSupirName.trim())
                                    }
                                    editingSupir = null // Keluar dari mode edit setelah simpan
                                },
                                // Tombol aktif hanya jika ada teks dan teksnya berbeda dari yang lama
                                enabled = editedSupirName.isNotBlank() && editedSupirName.trim() != supir
                            ) {
                                Icon(Icons.Default.Done, contentDescription = "Simpan", tint = Color.Green)
                            }
                            // Tombol Batal
                            IconButton(onClick = { editingSupir = null }) {
                                Icon(Icons.Default.Clear, contentDescription = "Batal", tint = Color.Red)
                            }
                        } else {
                            // Tampilan normal
                            Text(
                                text = supir,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Tombol Edit
                            IconButton(onClick = {
                                editingSupir = supir // Masuk mode edit untuk item ini
                                editedSupirName = supir // Inisialisasi teks edit dengan nama supir saat ini
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextGray)
                            }
                            // Tombol Hapus
                            IconButton(onClick = { settingsViewModel.removeSupir(supir) }) {
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