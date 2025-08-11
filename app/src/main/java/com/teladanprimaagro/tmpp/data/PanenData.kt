package com.teladanprimaagro.tmpp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "panen_entries")
data class PanenData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tanggalWaktu: String,
    val uniqueNo: String,
    val locationPart1: String,
    val locationPart2: String,
    val kemandoran: String,
    val namaPemanen: String,
    val blok: String,
    val noTph: String,
    val totalBuah: Int,
    val buahN: Int,
    val buahA: Int,
    val buahOR: Int,
    val buahE: Int,
    val buahAB: Int,
    val buahBL: Int,
    val imageUri: String? = null,
    val isSynced: Boolean = false
)
