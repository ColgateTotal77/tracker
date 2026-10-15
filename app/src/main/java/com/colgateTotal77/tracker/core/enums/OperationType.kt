package com.colgateTotal77.tracker.core.enums

enum class OperationType {
    EXPENSE,
    INCOME
}

data class OperationId(
    val id: Int,
    val type: OperationType
)