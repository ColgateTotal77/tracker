package com.colgateTotal77.tracker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.colgateTotal77.tracker.core.database.expense.ExpenseDao
import com.colgateTotal77.tracker.core.database.expense.ExpenseEntity

@Database(entities = [ExpenseEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}