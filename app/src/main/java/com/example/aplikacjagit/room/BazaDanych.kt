package com.example.aplikacjagit.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Produkt::class, Dodane::class], version = 1, exportSchema = false)
abstract class BazaDanych : RoomDatabase() {
    abstract fun DAO(): DAO

    companion object {
        private var INSTANCE: BazaDanych? = null

        fun getInstance(context: Context): BazaDanych{
            return  INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(context.applicationContext,
                    BazaDanych::class.java,
                    "baza_danych").build()
                INSTANCE = instance
                instance
            }
        }
    }
}