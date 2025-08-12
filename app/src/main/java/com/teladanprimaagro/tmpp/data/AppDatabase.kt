package com.teladanprimaagro.tmpp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PanenData::class,
        PengirimanData::class,
        ScannedItemEntity::class,
        FinalizedUniqueNoEntity::class
    ],
    version = 15,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun panenDao(): PanenDao
    abstract fun pengirimanDao(): PengirimanDao
    abstract fun scannedItemDao(): ScannedItemDao // Ini sudah benar

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}