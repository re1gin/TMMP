package com.teladanprimaagro.tmpp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PanenDao {
    @Query("SELECT * FROM panen_entries ORDER BY id DESC")
    fun getAllPanen(): Flow<List<PanenData>>

    @Query("SELECT * FROM panen_entries WHERE id = :panenId")
    suspend fun getPanenById(panenId: Int): PanenData?

    @Insert
    suspend fun insertPanen(panen: PanenData)

    @Query("DELETE FROM panen_entries")
    suspend fun clearAllPanen()

    @Update
    suspend fun updatePanen(panen: PanenData)

    @Query("DELETE FROM panen_entries WHERE id = :panenId")
    suspend fun deletePanenById(panenId: Int)

    @Query("DELETE FROM panen_entries WHERE id IN (:ids)")
    suspend fun deleteMultiplePanen(ids: List<Int>)
}