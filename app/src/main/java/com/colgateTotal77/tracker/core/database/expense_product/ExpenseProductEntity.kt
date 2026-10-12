package com.colgateTotal77.tracker.core.database.expense_product

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.colgateTotal77.tracker.core.database.expense.ExpenseEntity
import com.colgateTotal77.tracker.core.database.product.ProductEntity

@Entity(
    tableName = "expense_products",
    foreignKeys = [
        ForeignKey(entity = ExpenseEntity::class,
            parentColumns = ["id"], childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
    ],
    indices = [Index("expenseId"), Index("productId")]
)
data class ExpenseProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val expenseId: Int,
    val productId: Int,
    val quantity: Int,
    val unitPriceMinor: Int,
    val totalMinor: Int,
    val position: Int,

    @ColumnInfo(defaultValue = "0")
    val isManuallyCreated: Boolean,
    val barcode: String? = null
)

data class ExpenseProductWithProduct(
    @Embedded
    val expenseProduct: ExpenseProductEntity,

    @Relation(
        parentColumn = "productId",
        entityColumn = "id"
    )
    val product: ProductEntity
)

data class ExpenseProductDraft(
    val expenseId: Int,
    val position: Int,
    val quantity: Int,
    val unitPriceMinor: Int,
    val isManuallyCreated: Boolean = true
)