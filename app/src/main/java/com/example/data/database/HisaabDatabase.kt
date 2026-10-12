package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.NeedEntity
import com.example.data.model.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        NeedEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HisaabDatabase : RoomDatabase() {

    abstract fun dao(): HisaabDao

    companion object {
        @Volatile
        private var INSTANCE: HisaabDatabase? = null

        fun getInstance(context: Context): HisaabDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HisaabDatabase::class.java,
                    "hisaab_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
