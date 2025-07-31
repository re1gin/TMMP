package com.teladanprimaagro.tmpp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "panen_entries")
data class PanenData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Tambahkan ID unik sebagai Primary Key
    val tanggalWaktu: String,
    val uniqueNo: String,
    val locationPart1: String, // Lokasi Bagian 1
    val locationPart2: String, // Lokasi Bagian 2
    val kemandoran: String, // Kemandoran
    val namaPemanen: String, // Nama Pemanen
    val blok: String, // Blok
    val noTph: String, // No. TPH
    val totalBuah: Int, // Total Buah
    val buahN: Int, // Buah N
    val buahA: Int, // Buah A
    val buahOR: Int, // Buah OR
    val buahE: Int, // Buah E
    val buahAB: Int, // Buah AB
    val buahBL: Int, //Berondolan Lepas
    val imageUri: String? = null
) {
    fun toNfcWriteableData(): PanenData {
        return this.copy(
        )
    }
}
