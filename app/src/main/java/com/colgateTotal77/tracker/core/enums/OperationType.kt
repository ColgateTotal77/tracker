package com.colgateTotal77.tracker.core.enums

enum class OperationType {
    EXPENSE,
    INCOME,
    EXCHANGE
}

data class OperationId(
    val id: Int,
    val type: OperationType
)