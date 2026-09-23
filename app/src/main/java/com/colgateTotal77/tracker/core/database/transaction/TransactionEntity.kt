package com.colgateTotal77.tracker.core.database.transaction

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ForeignKey
import androidx.room.Relation
import com.colgateTotal77.tracker.core.database.market.MarketChoice
import com.colgateTotal77.tracker.core.database.market.MarketEntity
import com.colgateTotal77.tracker.core.database.transaction_product.TransactionProductEntity
import com.colgateTotal77.tracker.core.database.transaction_product.TransactionProductWithProduct
import com.colgateTotal77.tracker.core.enums.TransactionStatus
import com.colgateTotal77.tracker.core.enums.TransactionSource
import com.colgateTotal77.tracker.core.enums.Currency
import com.colgateTotal77.tracker.screens.dashboard.Camera.fiscal.FiscalCheck
import com.colgateTotal77.tracker.screens.dashboard.Camera.fiscal.FiscalItem

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

data class TransactionWithProducts(
    @Embedded
    val transaction: TransactionEntity,

    @Relation(
        entity = TransactionProductEntity::class,
        parentColumn = "id",
        entityColumn = "transactionId"
    )
    val items: List<TransactionProductWithProduct>,

    @Relation(
        entity = MarketEntity::class,
        parentColumn = "marketId",
        entityColumn = "id"
    )
    val market: MarketEntity?
)

data class TransactionDraft(
    val amountMinor: Int,
    val currency: Currency = Currency.UAH,
    val date: Long = System.currentTimeMillis(),
    val source: TransactionSource,
    val market: MarketChoice = MarketChoice.None,
    val note: String? = "",
    val items: List<FiscalItem> = emptyList(),
    val fiscalId: String? = null,
    val rawFiscalPayload: String? = null,
)

fun FiscalCheck.toDraft(): TransactionDraft = TransactionDraft(
    amountMinor = amountMinor
        ?: items.sumOf { it.unitPriceMinor * it.quantity },
    currency = Currency.UAH,
    date = date,
    source = TransactionSource.QR_CODE,
    market = tin?.let { MarketChoice.ByTin(it) } ?: MarketChoice.None,
    items = items,
    fiscalId = listOfNotNull(fiscalNumber, tin, receiptNumber)
        .joinToString("-").ifEmpty { null },
    rawFiscalPayload = toString(),
)
