package com.colgateTotal77.tracker.screens.dashboard.Camera.fiscal

data class FiscalCheck(
    val fiscalNumber: String? = null,
    val tin: String? = null,
    val registrarSerial: String? = null,
    val receiptNumber: String? = null,
    val date: Long = System.currentTimeMillis(),
    val amountMinor: Int? = null,
    val payments: List<FiscalPayment> = emptyList(),
    val items: List<FiscalItem> = emptyList(),
    val mac: String? = null,
    val macDi: String? = null,
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

data class FiscalPayment(
    val name: String? = null,
    val description: String? = null,
    val cardNumber: String? = null,
    val systemName: String? = null,
    val rrn: String? = null,
    val amountMinor: Int? = null,
)
