package com.teladanprimaagro.tmpp.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude

@Entity(tableName = "panen_entries", indices = [Index(value = ["tanggalWaktu"])])
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
    val localImageUri: String? = null,
    val firebaseImageUrl: String? = null,
    val isSynced: Boolean = false,
    val workerId: String? = null,
    @Exclude
    val isSelected: Boolean = false
)