package com.example.aplikacjagit.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Produkt::class, Dodane::class], version = 5)
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
                kalorycznosc INT,
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

        val MIGRATION_4_5 = object : Migration(4, 5) { //
            override fun migrate(db: SupportSQLiteDatabase) {
                // 0) utwórz nową tabelę z poprawnymi typami
                db.execSQL("""
            CREATE TABLE IF NOT EXISTS ProduktyDodane_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                idProduktu INTEGER,
                nazwa TEXT,
                ilosc INTEGER,
                data INTEGER,
                poraDnia INTEGER,
                sumaKalorii INTEGER,
                sumaBialek REAL,
                sumaWeglowodanow REAL,
                sumaTluszczy REAL
            )
        """.trimIndent())

                // 1) sprawdź, które kolumny istnieją w starej tabeli
                val existingCols = mutableSetOf<String>()
                db.query("PRAGMA table_info('ProduktyDodane')").use { cursor ->
                    val nameIdx = cursor.getColumnIndex("name")
                    while (cursor.moveToNext()) {
                        existingCols.add(cursor.getString(nameIdx))
                    }
                }

                // 2) przygotuj fragmenty SELECT z CAST lub wartością domyślną
                val sumaKaloriiExpr = if (existingCols.contains("sumaKalorii")) "CAST(sumaKalorii AS REAL)" else "0.0"
                val sumaBialekExpr = if (existingCols.contains("sumaBialek")) "CAST(sumaBialek AS REAL)" else "0.0"
                val sumaTluszczyExpr = if (existingCols.contains("sumaTluszczy")) "CAST(sumaTluszczy AS REAL)" else "0.0"
                val sumaWeglowodanowExpr = if (existingCols.contains("sumaWeglowodanow")) "CAST(sumaWeglowodanow AS REAL)" else "0.0"

                // 3) wykonaj INSERT ... SELECT
                val insertSql = """
            INSERT INTO ProduktyDodane_new (
                id, idProduktu, nazwa, ilosc, data, poraDnia,
                sumaKalorii, sumaBialek, sumaWeglowodanow, sumaTluszczy
            )
            SELECT 
                id, idProduktu, nazwa, ilosc, data, poraDnia,
                $sumaKaloriiExpr, $sumaBialekExpr, $sumaWeglowodanowExpr, $sumaTluszczyExpr
            FROM ProduktyDodane
        """.trimIndent()
                db.execSQL(insertSql)

                // 4) usuń starą tabelę
                db.execSQL("DROP TABLE IF EXISTS ProduktyDodane")

                // 5) zmień nazwę nowej tabeli
                db.execSQL("ALTER TABLE ProduktyDodane_new RENAME TO ProduktyDodane")
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
                    .addMigrations(MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = inst
                inst
            }
        }
    }
}
