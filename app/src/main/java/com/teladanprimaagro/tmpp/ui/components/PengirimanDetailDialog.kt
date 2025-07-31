// com.teladanprimaagro.tmpp.ui.components/PengirimanDetailDialog.kt
@file:Suppress("DEPRECATION")

package com.teladanprimaagro.tmpp.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.teladanprimaagro.tmpp.data.PengirimanData
import com.teladanprimaagro.tmpp.ui.theme.PrimaryOrange
import com.teladanprimaagro.tmpp.ui.theme.TextGray
import com.teladanprimaagro.tmpp.ui.viewmodels.ScannedItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PengirimanDetailDialog(pengirimanEntry: PengirimanData, onDismiss: () -> Unit) {
    val gson = Gson()
    val scannedItemsType = object : TypeToken<List<ScannedItem>>() {}.type
    val rawDetailScannedItems: List<ScannedItem> = gson.fromJson(pengirimanEntry.detailScannedItemsJson, scannedItemsType) ?: emptyList()

    val aggregatedScannedItems = remember(rawDetailScannedItems) {
        rawDetailScannedItems
            .groupBy { it.blok }
            .map { (blok, itemsInBlock) ->
                val totalBuahAggregated = itemsInBlock.sumOf { it.totalBuah }
                ScannedItem(
                    uniqueNo = "",
                    tanggal = "",
                    blok = blok,
                    totalBuah = totalBuahAggregated
                )
            }
            .sortedBy { it.blok }
    }

    val dialogDateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale("id", "ID")) }
    val formattedDate = remember(pengirimanEntry.waktuPengiriman, pengirimanEntry.tanggalNfc) {
        try {
            val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss")
            LocalDateTime.parse(pengirimanEntry.waktuPengiriman, inputFormatter).format(dialogDateFormatter)
        } catch (_: DateTimeParseException) {
            try {
                // If waktuPengiriman fails, try to parse tanggalNfc (assuming it's just "dd/MM/yy")
                DateTimeFormatter.ofPattern("dd/MM/yy", Locale("id", "ID"))
                // Append " 00:00:00" to make it parsable by LocalDateTime
                LocalDateTime.parse(pengirimanEntry.tanggalNfc + " 00:00:00", DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss")).format(dialogDateFormatter)
            } catch (_: DateTimeParseException) {
                "Tanggal Tidak Valid"
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF424242))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Dialog: Data Lengkap dan Tombol Tutup
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Spacer ini dihapus karena IconButton di kanan akan menarik judul ke tengah
                    // Spacer(Modifier.width(24.dp))
                    Text(
                        text = "Data Lengkap",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryOrange,
                        modifier = Modifier.weight(1f), // Agar teks bisa di tengah
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center // Rata tengah
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = PrimaryOrange
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Detail Pengiriman Utama
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DetailItem(label = "Nomor SPB", value = pengirimanEntry.spbNumber, labelColor = Color.White, valueColor = Color.White)
                    HorizontalDivider(color = TextGray.copy(alpha = 0.3f), thickness = 0.5.dp) // Garis pemisah
                    DetailItem(label = "Tanggal", value = formattedDate, labelColor = Color.White, valueColor = Color.White)
                    HorizontalDivider(color = TextGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    DetailItem(label = "Nama Supir", value = pengirimanEntry.namaSupir, labelColor = Color.White, valueColor = Color.White)
                    HorizontalDivider(color = TextGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    DetailItem(label = "Nomor Polisi", value = pengirimanEntry.noPolisi, labelColor = Color.White, valueColor = Color.White)
                    HorizontalDivider(color = TextGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    DetailItem(label = "Mandor Loading", value = pengirimanEntry.mandorLoading, labelColor = Color.White, valueColor = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Header untuk detail item yang discan (BLOK dan Total Buah)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // .background(PrimaryOrange, RoundedCornerShape(8.dp)) // Hapus background, sesuaikan seperti gambar
                        .padding(horizontal = 16.dp, vertical = 8.dp), // Beri padding saja
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BLOK",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PrimaryOrange, // Ubah warna teks header menjadi PrimaryOrange
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Total Buah",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PrimaryOrange, // Ubah warna teks header menjadi PrimaryOrange
                        modifier = Modifier.weight(1f)
                    )
                }
                HorizontalDivider(color = TextGray.copy(alpha = 0.3f), thickness = 0.5.dp) // Garis pemisah setelah header detail

                // Daftar Item yang Discan (menggunakan aggregatedScannedItems)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (aggregatedScannedItems.isNotEmpty()) 100.dp else 50.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    if (aggregatedScannedItems.isEmpty()) {
                        item {
                            Text(
                                text = "Tidak ada detail item yang discan.",
                                color = TextGray,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }
                    } else {
                        itemsIndexed(aggregatedScannedItems) { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.blok,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = item.totalBuah.toString(),
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (index < aggregatedScannedItems.lastIndex) {
                                HorizontalDivider(color = TextGray.copy(alpha = 0.1f), thickness = 0.5.dp) // Garis pemisah antar item
                            }
                        }
                    }
                }
                HorizontalDivider(color = TextGray.copy(alpha = 0.3f), thickness = 0.5.dp) // Garis pemisah setelah daftar detail
                Spacer(modifier = Modifier.height(16.dp))

                // Total Buah (di bagian bawah)
                DetailItem(label = "Total Buah", value = "${pengirimanEntry.totalBuah} Janjang", labelColor = Color.White, valueColor = Color.White, isBoldValue = true)
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, labelColor: Color = MaterialTheme.colorScheme.onSurface, valueColor: Color = MaterialTheme.colorScheme.onSurface, isBoldValue: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp), // Tingkatkan padding vertikal dan horizontal
        verticalAlignment = Alignment.CenterVertically // Rata tengah secara vertikal
    ) {
        // Kolom Label
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = labelColor,
            fontSize = 16.sp,
            modifier = Modifier.weight(0.4f) // Beri bobot untuk label
        )
        // Kolom Value
        Text(
            text = value,
            fontWeight = if (isBoldValue) FontWeight.Bold else FontWeight.Normal,
            color = valueColor,
            fontSize = 16.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.End, // Rata kanan untuk value
            modifier = Modifier.weight(0.6f) // Beri bobot untuk value
        )
    }
}