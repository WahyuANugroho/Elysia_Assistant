package com.example.elysia_assistant.data.local.database // Pastikan package ini sesuai ya, Kapten!

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Pastikan ChatMessageEntity dan ChatMessageDao diimpor jika berbeda package,
// tapi jika dalam package yang sama, impor eksplisit tidak diperlukan.
// import com.example.elysia_assistant.data.local.database.ChatMessageEntity
// import com.example.elysia_assistant.data.local.database.ChatMessageDao

@Database(
    entities = [ChatMessageEntity::class], // Entitas yang akan ada di database ini
    version = 1,                            // Versi database, naikkan jika ada perubahan skema
    exportSchema = false                    // Tidak mengekspor skema ke file (opsional, tapi umum untuk development)
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun chatMessageDao(): ChatMessageDao // Fungsi abstrak untuk mendapatkan DAO

    companion object {
        @Volatile // Memastikan INSTANCE selalu up-to-date dan sama untuk semua thread
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // synchronized block untuk mencegah beberapa thread membuat instance database secara bersamaan
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Gunakan application context
                    AppDatabase::class.java,
                    "elysia_assistant_chat_db" // Nama file database Anda
                )
                    // .fallbackToDestructiveMigration() // Hanya untuk development: Hapus dan buat ulang database jika versi berubah
                    // .addCallback(roomDatabaseCallback) // Jika ada callback yang ingin ditambahkan saat database dibuat/dibuka
                    .build()
                INSTANCE = instance
                // Mengembalikan instance yang baru dibuat atau yang sudah ada
                instance
            }
        }
    }
}
