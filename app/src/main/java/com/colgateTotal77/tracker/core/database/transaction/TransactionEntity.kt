package com.colgateTotal77.tracker.core.database.transaction

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ForeignKey
import com.colgateTotal77.tracker.core.database.market.MarketEntity
import com.colgateTotal77.tracker.core.enums.TransactionStatus
import com.colgateTotal77.tracker.core.enums.TransactionSource
import com.colgateTotal77.tracker.core.enums.Currency

@Entity(
    tableName = "transactions",
    foreignKeys = [ForeignKey(
        entity = MarketEntity::class,
        parentColumns = ["id"], childColumns = ["marketId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("marketId"), Index("date"), Index("fiscalId", unique = true)]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amountMinor: Int,
    val currency: Currency,
    val note: String?,
    val status: TransactionStatus,
    val source: TransactionSource,
    val rawFiscalPayload: String?,
    val fiscalId: String?,
    val marketId: Int?,
    val date: Long,
    val createdAt: Long,
    val updatedAt: Long
)