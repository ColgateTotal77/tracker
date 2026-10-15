package com.colgateTotal77.tracker.core.database.operation

import androidx.room.Embedded
import androidx.room.Relation
import com.colgateTotal77.tracker.core.database.expense.ExpenseEntity
import com.colgateTotal77.tracker.core.database.expense.ExpenseWithMarket
import com.colgateTotal77.tracker.core.database.expense.ExpenseWithProducts
import com.colgateTotal77.tracker.core.database.income.IncomeEntity
import com.colgateTotal77.tracker.core.enums.OperationType

data class OperationEntity(
    val expenseId: Int?,
    val incomeId: Int?,
    val type: OperationType,
    val date: Long,
)

data class OperationWithDetails(
    @Embedded
    val operation: OperationEntity,

    @Relation(
        entity = ExpenseEntity::class,
        parentColumn = "expenseId",
        entityColumn = "id",
    )
    val expense: ExpenseWithProducts?,

    @Relation(
        parentColumn = "incomeId",
        entityColumn = "id"
    )
    val income: IncomeEntity?,
)

data class OperationForOverview(
    @Embedded
    val operation: OperationEntity,

    @Relation(
        entity = ExpenseEntity::class,
        parentColumn = "expenseId",
        entityColumn = "id",
    )
    val expense: ExpenseWithMarket?,

    @Relation(
        parentColumn = "incomeId",
        entityColumn = "id"
    )
    val income: IncomeEntity?,
)