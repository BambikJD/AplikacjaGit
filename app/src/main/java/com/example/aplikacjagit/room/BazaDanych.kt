package com.example.aplikacjagit.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Produkt::class,
        Dodane::class,
        Lodowka::class,
        Przepis::class,
        PrzepisProdukt::class,
        // Dodane encje treningowe:
        Cwiczenie::class,
        Wykonane::class,
        Plan::class,
        PlanCwiczenie::class
    ],
    version = 1, // Zmieniono na 2, aby baza się przeładowała
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class BazaDanych : RoomDatabase() {
    abstract fun DAO(): DAO

    companion object {
        @Volatile private var INSTANCE: BazaDanych? = null

        fun getInstance(context: Context): BazaDanych {
            return INSTANCE ?: synchronized(this) {
                val inst = Room.databaseBuilder(
                    context.applicationContext,
                    BazaDanych::class.java,
                    "baza_danych"
                )
                    // Czyści bazę przy zmianie wersji (brak potrzeby ręcznych migracji)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = inst
                inst
            }
        }
    }
}