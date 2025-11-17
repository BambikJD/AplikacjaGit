package com.example.aplikacjagit.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Produkt::class, Dodane::class], version = 4)
@TypeConverters(Converters::class)
abstract class BazaDanych : RoomDatabase() {
    abstract fun DAO(): DAO

    companion object {
        @Volatile private var INSTANCE: BazaDanych? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ProduktyDodane ADD COLUMN data INTEGER")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ProduktyDodane ADD COLUMN poraDnia INTEGER")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 0) utwórz nową tabelę z docelowym schematem
                db.execSQL("""
            CREATE TABLE IF NOT EXISTS ListaProduktow_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                nazwa TEXT,
                kalorycznosc INTEGER,
                bialka REAL,
                tluszcze REAL,
                weglowodany REAL,
                kodKreskowy TEXT
            )
        """.trimIndent())

                // 1) ustal, która tabela źródłowa istnieje: ListaProduktow czy ProduktyDodane
                var srcTable: String? = null
                db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='ListaProduktow'").use { c ->
                    if (c.moveToFirst()) srcTable = "ListaProduktow"
                }
                if (srcTable == null) {
                    db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='ProduktyDodane'").use { c ->
                        if (c.moveToFirst()) srcTable = "ProduktyDodane"
                    }
                }

                if (srcTable != null) {
                    // 2) odczytaj kolumny ze źródłowej tabeli
                    val existingCols = mutableSetOf<String>()
                    db.query("PRAGMA table_info('$srcTable')").use { cur ->
                        val nameIdx = cur.getColumnIndex("name")
                        while (cur.moveToNext()) {
                            existingCols.add(cur.getString(nameIdx))
                        }
                    }

                    // 3) przygotuj fragmenty SELECT: jeśli kolumna istnieje -> użyj jej, w przeciwnym razie -> stała domyślna
                    val bialkaExpr = if (existingCols.contains("bialka")) "CAST(bialka AS REAL)" else "0.0"
                    val tluszczeExpr = if (existingCols.contains("tluszcze")) "CAST(tluszcze AS REAL)" else "0.0"
                    val weglowodanyExpr = if (existingCols.contains("weglowodany")) "CAST(weglowodany AS REAL)" else "0.0"

                    val kodExpr = when {
                        existingCols.contains("kodKreskowy") -> "kodKreskowy"
                        existingCols.contains("kodKreskowy") -> "kodKreskowy"
                        else -> "''"
                    }

                    // 4) zbuduj i wykonaj INSERT ... SELECT
                    val insertSql = """
                INSERT INTO ListaProduktow_new (id, nazwa, kalorycznosc, bialka, tluszcze, weglowodany, kodKreskowy)
                SELECT id, nazwa, kalorycznosc, $bialkaExpr, $tluszczeExpr, $weglowodanyExpr, $kodExpr
                FROM $srcTable
            """.trimIndent()

                    db.execSQL(insertSql)

                    // 5) usuń starą tabelę (jeśli istniała)
                    db.execSQL("DROP TABLE IF EXISTS $srcTable")
                } // jeśli nie ma żadnej starej tabeli - nowa zostanie pusta

                // 6) zmień nazwę nowej tabeli na oryginalną
                db.execSQL("ALTER TABLE ListaProduktow_new RENAME TO ListaProduktow")
            }
        }


        fun getInstance(context: Context): BazaDanych {
            return INSTANCE ?: synchronized(this) {
                val inst = Room.databaseBuilder(
                                context.applicationContext,
                                BazaDanych::class.java,
                                "baza_danych"
                            )
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = inst
                inst
            }
        }
    }
}
