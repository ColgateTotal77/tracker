package com.colgateTotal77.tracker.core.database.transaction_product

import androidx.room.Entity
import androidx.room.Index
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.colgateTotal77.tracker.core.database.transaction.TransactionEntity
import com.colgateTotal77.tracker.core.database.product.ProductEntity

@Entity(
    tableName = "transaction_products",
    foreignKeys = [
        ForeignKey(entity = TransactionEntity::class,
            parentColumns = ["id"], childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE),
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
    val position: Int //in transaction display
)