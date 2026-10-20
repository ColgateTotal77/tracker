package com.colgateTotal77.tracker.core.database.exchange

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.colgateTotal77.tracker.core.enums.Currency
import com.colgateTotal77.tracker.core.enums.IncomeType

@Entity(
    tableName = "exchanges",
    indices = [Index("date")]
)
data class ExchangeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fromAmountMinor: Long,
    val fromCurrency: Currency,
    val toAmountMinor: Long,
    val toCurrency: Currency,
    val note: String?,
    val date: Long,
    val createdAt: Long,
    val updatedAt: Long
)