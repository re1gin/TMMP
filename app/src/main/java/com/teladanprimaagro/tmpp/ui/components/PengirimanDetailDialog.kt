@file:Suppress("DEPRECATION")

package com.teladanprimaagro.tmpp.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.teladanprimaagro.tmpp.data.PengirimanData
import com.teladanprimaagro.tmpp.viewmodels.ScannedItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PengirimanDetailDialog(
    pengirimanEntry: PengirimanData,
    onDismiss: () -> Unit,
    onSendPrintClick: (PengirimanData) -> Unit
) {
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
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer) // Menggunakan warna dari tema
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Data Lengkap",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Detail Pengiriman Utama
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DetailItem(label = "Nomor SPB", value = pengirimanEntry.spbNumber)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                    DetailItem(label = "Tanggal", value = formattedDate)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                    DetailItem(label = "Nama Supir", value = pengirimanEntry.namaSupir)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                    DetailItem(label = "Nomor Polisi", value = pengirimanEntry.noPolisi)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                    DetailItem(label = "Mandor Loading", value = pengirimanEntry.mandorLoading)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Header untuk detail item yang discan (BLOK dan Total Buah)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BLOK",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Total Buah",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

                // Daftar Item yang Discan (menggunakan aggregatedScannedItems)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 150.dp) // Menggunakan heightIn untuk fleksibilitas
                        .padding(horizontal = 16.dp)
                ) {
                    if (aggregatedScannedItems.isEmpty()) {
                        item {
                            Text(
                                text = "Tidak ada detail item yang discan.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.blok,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = item.totalBuah.toString(),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )
                            }
                            if (index < aggregatedScannedItems.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 0.5.dp)
                            }
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(16.dp))

                DetailItem(label = "Total Buah", value = "${pengirimanEntry.totalBuah} Janjang", isBoldValue = true)

                Spacer(modifier = Modifier.height(24.dp))

                // Tombol Kirim & Cetak
                Button(
                    onClick = { onSendPrintClick(pengirimanEntry) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimary.copy(0.7f)) // Menggunakan warna primary dari tema
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Kirim/Cetak Data",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Kirim & Cetak Data",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, isBoldValue: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontWeight = if (isBoldValue) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}