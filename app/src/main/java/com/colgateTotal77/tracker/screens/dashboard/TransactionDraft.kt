package com.colgateTotal77.tracker.screens.dashboard

import com.colgateTotal77.tracker.screens.dashboard.Camera.fiscal.FiscalCheck
import com.colgateTotal77.tracker.screens.dashboard.Camera.fiscal.FiscalItem
import com.colgateTotal77.tracker.core.enums.Currency
import com.colgateTotal77.tracker.core.enums.TransactionSource

sealed interface MarketChoice {
    data class Existing(val marketId: Int) : MarketChoice
    data class New(val name: String) : MarketChoice
    data class ByTin(val tin: String) : MarketChoice
    data object None : MarketChoice
}

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
