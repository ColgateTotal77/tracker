package com.colgateTotal77.tracker.core.database.expense

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ForeignKey
import androidx.room.Relation
import com.colgateTotal77.tracker.core.database.market.MarketChoice
import com.colgateTotal77.tracker.core.database.market.MarketEntity
import com.colgateTotal77.tracker.core.database.expense_product.ExpenseProductEntity
import com.colgateTotal77.tracker.core.database.expense_product.ExpenseProductWithProduct
import com.colgateTotal77.tracker.core.enums.ExpenseStatus
import com.colgateTotal77.tracker.core.enums.ExpenseSource
import com.colgateTotal77.tracker.core.enums.Currency
import com.colgateTotal77.tracker.screens.operations.Camera.fiscal.FiscalCheck
import com.colgateTotal77.tracker.screens.operations.Camera.fiscal.FiscalItem

@Entity(
    tableName = "expenses",
    foreignKeys = [ForeignKey(
        entity = MarketEntity::class,
        parentColumns = ["id"], childColumns = ["marketId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("marketId"), Index("date"), Index("fiscalId", unique = true)]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amountMinor: Int,
    val currency: Currency,
    val note: String?,
    val status: ExpenseStatus,
    val source: ExpenseSource,
    val rawFiscalPayload: String?,
    val fiscalId: String?,
    val marketId: Int?,
    val date: Long,
    val createdAt: Long,
    val updatedAt: Long
)

data class ExpenseWithProducts(
    @Embedded
    val expense: ExpenseEntity,

    @Relation(
        entity = ExpenseProductEntity::class,
        parentColumn = "id",
        entityColumn = "expenseId"
    )
    val items: List<ExpenseProductWithProduct>,

    @Relation(
        entity = MarketEntity::class,
        parentColumn = "marketId",
        entityColumn = "id"
    )
    val market: MarketEntity?
)

data class ExpenseWithMarket(
    @Embedded
    val expense: ExpenseEntity,

    @Relation(
        entity = MarketEntity::class,
        parentColumn = "marketId",
        entityColumn = "id"
    )
    val market: MarketEntity?
)

data class ExpenseDraft(
    val amountMinor: Int,
    val currency: Currency = Currency.UAH,
    val date: Long = System.currentTimeMillis(),
    val source: ExpenseSource,
    val market: MarketChoice = MarketChoice.None,
    val note: String? = "",
    val items: List<FiscalItem> = emptyList(),
    val fiscalId: String? = null,
    val rawFiscalPayload: String? = null,
)

fun FiscalCheck.toDraft(rawFiscalPayload: String? = null): ExpenseDraft = ExpenseDraft(
    amountMinor = amountMinor
        ?: items.sumOf { it.unitPriceMinor * it.quantity },
    currency = Currency.UAH,
    date = date,
    source = ExpenseSource.QR_CODE,
    market = tin?.let { MarketChoice.ByTin(it) } ?: MarketChoice.None,
    items = items,
    fiscalId = listOfNotNull(fiscalNumber, tin, receiptNumber)
        .joinToString("-").ifEmpty { null },
    rawFiscalPayload = rawFiscalPayload,
)
