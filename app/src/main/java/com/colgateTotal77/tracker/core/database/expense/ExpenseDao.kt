package com.colgateTotal77.tracker.core.database.expense

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val currency: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)