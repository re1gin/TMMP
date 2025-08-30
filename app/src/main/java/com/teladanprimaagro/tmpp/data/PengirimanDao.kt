package com.teladanprimaagro.tmpp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PengirimanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPengiriman(pengiriman: PengirimanData)

    @Query("SELECT * FROM pengiriman_entries ORDER BY id DESC")
    fun getAllPengiriman(): Flow<List<PengirimanData>>

    @Query("SELECT * FROM pengiriman_entries WHERE isUploaded = 0 ORDER BY id DESC")
    fun getUnuploadedPengirimanDataFlow(): Flow<List<PengirimanData>>

    @Query("SELECT * FROM pengiriman_entries WHERE id = :id")
    suspend fun getPengirimanById(id: Int): PengirimanData?

    // --- FUNGSI BARU: Mengambil beberapa entri pengiriman berdasarkan daftar ID ---
    @Query("SELECT * FROM pengiriman_entries WHERE id IN (:ids)")
    suspend fun getPengirimanByIds(ids: List<Int>): List<PengirimanData>

    @Update
    suspend fun updatePengiriman(pengiriman: PengirimanData)

    @Query("DELETE FROM pengiriman_entries")
    suspend fun clearAllPengiriman()

    @Query("DELETE FROM pengiriman_entries WHERE id = :id")
    suspend fun deletePengirimanById(id: Int)

    @Query("DELETE FROM pengiriman_entries WHERE id IN (:ids)")
    suspend fun deleteMultiplePengiriman(ids: List<Int>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFinalizedUniqueNo(uniqueNoEntity: FinalizedUniqueNoEntity)

    @Query("SELECT * FROM finalized_unique_nos")
    fun getAllFinalizedUniqueNos(): Flow<List<FinalizedUniqueNoEntity>>

    @Query("SELECT * FROM finalized_unique_nos WHERE isUploaded = 0")
    fun getUnuploadedFinalizedUniqueNosFlow(): Flow<List<FinalizedUniqueNoEntity>>

    @Update
    suspend fun updateFinalizedUniqueNo(finalizedUniqueNoEntity: FinalizedUniqueNoEntity)

    @Query("SELECT COUNT(*) FROM finalized_unique_nos")
    fun getTotalScanCount(): Flow<Int>

    @Query("DELETE FROM finalized_unique_nos")
    suspend fun clearAllFinalizedUniqueNos()

    @Query("SELECT * FROM pengiriman_entries WHERE isUploaded = 0")
    suspend fun getUnuploadedPengirimanData(): List<PengirimanData>

    @Query("SELECT * FROM finalized_unique_nos WHERE isUploaded = 0")
    suspend fun getUnuploadedFinalizedUniqueNos(): List<FinalizedUniqueNoEntity>
}