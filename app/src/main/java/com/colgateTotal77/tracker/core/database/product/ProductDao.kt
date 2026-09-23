package com.colgateTotal77.tracker.core.database.product

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Transaction
import com.colgateTotal77.tracker.core.enums.ProductSort

@Dao
interface ProductDao {
    @Query("""
        SELECT * FROM `products` p
        WHERE p.isArchived = 0 AND (:nameQuery IS NULL OR p.normalizedName LIKE '%' || :nameQuery || '%')
        ORDER BY
            CASE WHEN :sortBy = 'AVG_PRICE_ASC' THEN p.averagePrice END ASC,
            CASE WHEN :sortBy = 'AVG_PRICE_DESC' THEN p.averagePrice END DESC,
            CASE WHEN :sortBy = 'LAST_PRICE_ASC' THEN p.lastPrice END ASC,
            CASE WHEN :sortBy = 'LAST_PRICE_DESC' THEN p.lastPrice END DESC,
            CASE WHEN :sortBy = 'PURCHASE_COUNT_ASC' THEN p.purchaseCount END ASC,
            CASE WHEN :sortBy = 'PURCHASE_COUNT_DESC' THEN p.purchaseCount END DESC,
            p.purchaseCount DESC
    """)
    fun query(
        nameQuery: String? = null,
        sortBy: ProductSort = ProductSort.PURCHASE_COUNT_DESC
    ): PagingSource<Int, ProductEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRaw(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllRaw(products: List<ProductEntity>): List<Long>

    @Transaction
    suspend fun insertOrRestore(product: ProductEntity) {
        if (insertRaw(product) != -1L) return
        restoreByNormalizedName(product.normalizedName)
    }

    @Transaction
    suspend fun insertAllOrRestore(products: List<ProductEntity>) {
        val results = insertAllRaw(products)

        val existingNormalizedNames = results.mapIndexed { index, resultId ->
            if(resultId == -1L) products[index].normalizedName
            return
        }
        if (existingNormalizedNames.isEmpty()) return

        restoreByNormalizedNames(existingNormalizedNames)
    }

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(product: ProductEntity)

    @Query("UPDATE products SET isArchived = 1 WHERE id = :id")
    suspend fun archiveById(id: Int)

    @Query("UPDATE products SET isArchived = 0 WHERE id = :id")
    suspend fun restoreById(id: Int)

    @Query("UPDATE products SET isArchived = 0 WHERE normalizedName = :normalizedName")
    suspend fun restoreByNormalizedName(normalizedName: String)

    @Query("UPDATE products SET isArchived = 0 WHERE normalizedName IN (:normalizedNames)")
    suspend fun restoreByNormalizedNames(normalizedNames: List<String>)

    @Query("SELECT * FROM `products` WHERE normalizedName = :normalizedName LIMIT 1")
    suspend fun getByNormalizedName(normalizedName: String): ProductEntity?

    @Query("SELECT * FROM `products` WHERE normalizedName IN (:normalizedNames)")
    suspend fun getByNormalizedNames(normalizedNames: List<String>): List<ProductEntity>

    @Query("UPDATE products SET alias = :alias WHERE id = :id")
    suspend fun updateAlliesById(id: Int, alias: String)
}