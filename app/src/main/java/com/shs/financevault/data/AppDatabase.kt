package com.shs.financevault.data

import android.content.Context
import androidx.room.*

class Converters {
    @TypeConverter fun fromType(v: TransactionType) = v.name
    @TypeConverter fun toType(v: String) = TransactionType.valueOf(v)
    @TypeConverter fun fromCat(v: Category) = v.name
    @TypeConverter fun toCat(v: String) = Category.valueOf(v)
}

@Database(
    entities = [Transaction::class, Budget::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(ctx: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(ctx.applicationContext, AppDatabase::class.java, "finance_vault.db")
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
        }
    }
}
