package com.teladanprimaagro.tmpp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pengiriman_data") // Nama tabel disederhanakan menjadi "pengiriman_data"
data class PengirimanData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val waktuPengiriman: String,
    val spbNumber: String,
    val namaSupir: String,
    val noPolisi: String,
    val totalBuah: Int,
    val uniqueNo: String, // Dari NFC
    val tanggalNfc: String, // Dari NFC
    val blok: String, // Dari NFC
    val detailScannedItemsJson: String, // Menyimpan daftar ScannedItem dalam format JSON
    val mandorLoading: String, // Kolom baru: Mandor Loading
    val isUploaded: Boolean = false, // Status upload
    val isFinalized: Boolean = false // Kolom baru untuk statistik finalisasi
)

