package com.colgateTotal77.tracker.core.database.product

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "products",
    indices = [Index(value = ["normalizedName"], unique = true)]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val normalizedName: String,
    val alias: String,
    val lastPrice: Int,
    val averagePrice: Int = lastPrice,
    val barcode: String? = null,
    val purchaseCount: Int = 0,
    val isArchived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

sealed interface ProductChoice {
    data class Existing(val product: ProductEntity) : ProductChoice
    data class New(val alias: String) : ProductChoice
}