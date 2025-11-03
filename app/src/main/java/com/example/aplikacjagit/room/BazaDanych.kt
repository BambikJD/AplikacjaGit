package com.example.aplikacjagit.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Produkt::class, Dodane::class /*,...*/], version = 2)
@TypeConverters(Converters::class)
abstract class BazaDanych : RoomDatabase() {
    abstract fun DAO(): DAO

    companion object {
        @Volatile private var INSTANCE: BazaDanych? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Tutaj SQL dodający nową kolumnę `data` typu INTEGER (Room przechowuje Date jako Long)
                database.execSQL("ALTER TABLE Dodane ADD COLUMN data INTEGER")
            }
        }


        fun getInstance(context: Context): BazaDanych {
            return INSTANCE ?: synchronized(this) {
                val inst = Room.databaseBuilder(
                    context.applicationContext,
                    BazaDanych::class.java,
                    "baza_danych"
                )
                    .addMigrations(MIGRATION_1_2) // <-- usuwa starą DB gdy brak migracji
                    .build()
                INSTANCE = inst
                inst
            }
        }
    }
}
