package com.teladanprimaagro.tmpp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_items_temp")
data class ScannedItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uniqueNo: String,
    val tanggal: String,
    val blok: String,
    val totalBuah: Int,
)

@Entity(tableName = "finalized_unique_nos")
data class FinalizedUniqueNoEntity(
    @PrimaryKey(autoGenerate = false)
    val uniqueNo: String,
    val finalizedAt: Long = System.currentTimeMillis()
)