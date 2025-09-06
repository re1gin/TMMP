package com.teladanprimaagro.tmpp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "finalized_unique_nos")
data class FinalizedUniqueNoEntity(
    @PrimaryKey val uniqueNo: String,
    val isUploaded: Boolean = false
)