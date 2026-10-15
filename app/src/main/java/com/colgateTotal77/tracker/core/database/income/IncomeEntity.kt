package com.colgateTotal77.tracker.core.database.income

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.colgateTotal77.tracker.core.enums.Currency
import com.colgateTotal77.tracker.core.enums.IncomeType

@Entity(
    tableName = "income",
    indices = [Index("type"), Index("date")]
)
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amountMinor: Int,
    val currency: Currency,
    val note: String?,
    val type: IncomeType,
    val date: Long,
    val createdAt: Long,
    val updatedAt: Long
)