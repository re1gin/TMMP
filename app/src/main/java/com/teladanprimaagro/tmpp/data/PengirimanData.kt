package com.teladanprimaagro.tmpp.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude

@Entity(tableName = "pengiriman_entries", indices = [Index(value = ["waktuPengiriman"])])
data class PengirimanData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val waktuPengiriman: String,
    val spbNumber: String,
    val namaSupir: String,
    val noPolisi: String,
    val totalBuah: Int,
    val uniqueNo: String,
    val tanggalNfc: String,
    val blok: String,
    val detailScannedItemsJson: String,
    val mandorLoading: String,
    val isUploaded: Boolean = false,
    val isFinalized: Boolean = false,
    val workerId: String? = null,
    @Exclude
    val isSelected: Boolean = false
)

