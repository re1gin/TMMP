package com.teladanprimaagro.tmpp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PanenData::class,
        PengirimanData::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun panenDao(): PanenDao
    abstract fun pengirimanDao(): PengirimanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database" // Pastikan nama database ini konsisten di seluruh aplikasi Anda
                )
                    .fallbackToDestructiveMigration() // <--- Hapus '(false)' di sini
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}