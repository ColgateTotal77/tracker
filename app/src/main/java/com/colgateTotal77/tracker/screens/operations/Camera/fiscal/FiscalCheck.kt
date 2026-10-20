package com.colgateTotal77.tracker.screens.operations.Camera.fiscal

data class FiscalCheck(
    val fiscalNumber: String? = null,
    val tin: String? = null,
    val receiptNumber: String? = null,
    val date: Long = System.currentTimeMillis(),
    val amountMinor: Long? = null,
    val items: List<FiscalItem> = emptyList(),
)

data class FiscalItem(
    val position: Int? = null,
    val name: String? = null,
    val barcode: String? = null,
    val quantity: Int = 1000,
    val unitPriceMinor: Long,
    val totalMinor: Long? = null,
)
