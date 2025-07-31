// com.teladanprimaagro.tmpp.data/PengirimanDao.kt
package com.teladanprimaagro.tmpp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PengirimanDao {
    @Insert
    suspend fun insertPengiriman(pengiriman: PengirimanData)

    @Query("SELECT * FROM pengiriman_data ORDER BY id DESC") // Pastikan nama tabel di sini "pengiriman_data"
    fun getAllPengiriman(): Flow<List<PengirimanData>>

    @Query("SELECT * FROM pengiriman_data WHERE id = :id") // Pastikan nama tabel di sini "pengiriman_data"
    suspend fun getPengirimanById(id: Int): PengirimanData?

    @Update
    suspend fun updatePengiriman(pengiriman: PengirimanData)

    @Query("DELETE FROM pengiriman_data") // Pastikan nama tabel di sini "pengiriman_data"
    suspend fun clearAllPengiriman()

    @Query("DELETE FROM pengiriman_data WHERE id = :id") // Pastikan nama tabel di sini "pengiriman_data"
    suspend fun deletePengirimanById(id: Int)
}