package com.teladanprimaagro.tmpp.ui.data

data class PanenData(
    val tanggalWaktu: String,
    val uniqueNo: String, // No. Unik
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
    val buahBL: Int,
    val imageUri: String? = null
) {
    fun toNfcWriteableData(): PanenData {
        return this.copy(
            imageUri = null,
            locationPart1 = "",
            locationPart2 = "",
            kemandoran = "",
            namaPemanen = "",
            noTph = ""
        )
    }
}