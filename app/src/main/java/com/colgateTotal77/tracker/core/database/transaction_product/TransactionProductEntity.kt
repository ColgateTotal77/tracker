package com.colgateTotal77.tracker.core.database.transaction_product

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.colgateTotal77.tracker.core.database.transaction.TransactionEntity
import com.colgateTotal77.tracker.core.database.product.ProductEntity

@Entity(
    tableName = "transaction_products",
    foreignKeys = [
        ForeignKey(entity = TransactionEntity::class,
            parentColumns = ["id"], childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
    ],
    indices = [Index("transactionId"), Index("productId")]
)
data class TransactionProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val transactionId: Int,
    val productId: Int,
    val quantity: Int,
    val unitPriceMinor: Int,
    val totalMinor: Int,
    val taxGroup: String?,
    val position: Int,

    @ColumnInfo(defaultValue = "0")
    val isManuallyCreated: Boolean
)

data class TransactionProductWithProduct(
    @Embedded
    val transactionProduct: TransactionProductEntity,

    @Relation(
        parentColumn = "productId",
        entityColumn = "id"
    )
    val product: ProductEntity
)

data class TransactionProductDraft(
    val transactionId: Int,
    val position: Int,
    val name: String,
    val quantity: Int,
    val unitPriceMinor: Int,
    val isManuallyCreated: Boolean = true
)