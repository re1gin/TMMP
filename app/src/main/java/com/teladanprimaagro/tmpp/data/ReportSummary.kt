package com.teladanprimaagro.tmpp.data

data class BlokSummary(
    val blok: String,
    val totalBuah: Int
)

data class SupirSummary(
    val namaSupir: String,
    val totalBuah: Int
)

data class MainStats(
    val totalBuah: Int,
    val totalScannedData: Int,
    val finalizedData: Int
)
