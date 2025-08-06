package com.teladanprimaagro.tmpp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScannedItemDao {
    @Insert
    suspend fun insertScannedItem(item: ScannedItemEntity)

    @Query("SELECT * FROM scanned_items_temp ORDER BY id ASC")
    fun getAllScannedItems(): Flow<List<ScannedItemEntity>>

    @Query("DELETE FROM scanned_items_temp")
    suspend fun deleteAllScannedItems()
}