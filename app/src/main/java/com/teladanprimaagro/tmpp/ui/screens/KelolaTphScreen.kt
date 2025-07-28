package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear // Ikon untuk batal edit
import androidx.compose.material.icons.filled.Done // Ikon untuk simpan edit
import androidx.compose.material.icons.filled.Delete // Ikon untuk hapus
import androidx.compose.material.icons.filled.Edit // Ikon untuk edit
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
import com.teladanprimaagro.tmpp.ui.theme.TextGray // Pastikan ini diimpor jika digunakan
import com.teladanprimaagro.tmpp.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelolaTphScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    var newTphName by remember { mutableStateOf("") }
    // State untuk melacak TPH yang sedang diedit
    var editingTph by remember { mutableStateOf<String?>(null) }
    // State untuk menyimpan teks edit sementara
    var editedTphName by remember { mutableStateOf("") }

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
                text = "Kelola No. TPH",
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

            // Input untuk nama TPH baru
            OutlinedTextField(
                value = newTphName,
                onValueChange = { newTphName = it },
                label = { Text("No. TPH Baru") },
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
                    if (newTphName.isNotBlank()) {
                        settingsViewModel.addTph(newTphName.trim()) // Gunakan trim() untuk membersihkan spasi
                        newTphName = "" // Bersihkan input setelah ditambahkan
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
                Text("Tambah TPH", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Daftar No. TPH:",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            HorizontalDivider(thickness = 1.dp, color = DotGray)
            Spacer(modifier = Modifier.height(8.dp))

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
                            // Tampilan saat mode edit
                            OutlinedTextField(
                                value = editedTphName,
                                onValueChange = { editedTphName = it },
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
                                    if (editedTphName.isNotBlank() && editedTphName.trim() != tph) {
                                        settingsViewModel.updateTph(tph, editedTphName.trim())
                                    }
                                    editingTph = null // Keluar dari mode edit
                                },
                                enabled = editedTphName.isNotBlank() // Aktifkan hanya jika ada teks
                            ) {
                                Icon(Icons.Default.Done, contentDescription = "Simpan", tint = Color.Green)
                            }
                            // Tombol Batal
                            IconButton(onClick = { editingTph = null }) {
                                Icon(Icons.Default.Clear, contentDescription = "Batal", tint = Color.Red)
                            }
                        } else {
                            // Tampilan normal
                            Text(
                                text = tph,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Tombol Edit
                            IconButton(onClick = {
                                editingTph = tph // Masuk mode edit untuk item ini
                                editedTphName = tph // Inisialisasi teks edit dengan nama TPH saat ini
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextGray)
                            }
                            // Tombol Hapus
                            IconButton(onClick = { settingsViewModel.removeTph(tph) }) {
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