package com.colgateTotal77.tracker.screens.dashboard.Camera.fiscal

data class FiscalCheck(
    val fiscalNumber: String? = null,
    val tin: String? = null,
    val receiptNumber: String? = null,
    val date: Long = System.currentTimeMillis(),
    val amountMinor: Int? = null,
    val items: List<FiscalItem> = emptyList(),
)

data class FiscalItem(
    val position: Int? = null,
    val name: String? = null,
    val barcode: String? = null,
    val quantity: Int = 1000,
    val unitPriceMinor: Int,
    val totalMinor: Int? = null,
    val taxGroup: String? = null,
)
