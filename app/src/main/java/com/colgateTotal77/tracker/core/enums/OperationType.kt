package com.colgateTotal77.tracker.core.enums

import com.colgateTotal77.tracker.R

enum class OperationType(val labelRes: Int) {
    EXPENSE(R.string.expense),
    INCOME(R.string.income),
    EXCHANGE(R.string.exchange)
}

data class OperationId(
    val id: Int,
    val type: OperationType
)