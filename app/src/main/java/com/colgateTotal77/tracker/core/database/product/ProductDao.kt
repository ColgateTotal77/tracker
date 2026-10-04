package com.colgateTotal77.tracker.core.database.product

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Transaction
import com.colgateTotal77.tracker.core.enums.ProductSort
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("""
        SELECT * FROM (
            SELECT 
                p.id, 
                p.normalizedName, 
                p.alias, 
                p.isArchived, 
                p.createdAt, 
                p.updatedAt,
                COALESCE(SUM(tp.quantity), 0) AS purchaseCount,
                COALESCE(SUM((tp.quantity * tp.unitPriceMinor) / 1000), 0) AS spentMinor,
                COALESCE((
                    SELECT tp2.unitPriceMinor 
                    FROM transaction_products tp2 
                    JOIN transactions t2 ON t2.id = tp2.transactionId 
                    WHERE tp2.productId = p.id 
                        AND (:startTime IS NULL OR t2.date >= :startTime)
                        AND (:endTime IS NULL OR t2.date <= :endTime)
                    ORDER BY t2.date DESC 
                    LIMIT 1
                ), 0) AS lastPrice
            FROM products p
            JOIN transaction_products tp ON tp.productId = p.id 
            JOIN transactions t ON t.id = tp.transactionId 
            WHERE p.isArchived = 0 
                AND (:nameQuery IS NULL OR p.normalizedName LIKE '%' || :nameQuery || '%')
                AND (:startTime IS NULL OR t.date >= :startTime)
                AND (:endTime IS NULL OR t.date <= :endTime)
                AND (:marketIds IS NULL OR t.marketId IN (:marketIds))
            GROUP BY p.id
        ) filtered
        WHERE (:minPrice IS NULL OR lastPrice >= :minPrice)
            AND (:maxPrice IS NULL OR lastPrice <= :maxPrice)
        ORDER BY
            CASE WHEN :sortBy = 'LAST_PRICE_ASC' THEN lastPrice END ASC,
            CASE WHEN :sortBy = 'LAST_PRICE_DESC' THEN lastPrice END DESC,
            CASE WHEN :sortBy = 'PURCHASE_COUNT_ASC' THEN purchaseCount END ASC,
            CASE WHEN :sortBy = 'PURCHASE_COUNT_DESC' THEN purchaseCount END DESC,
            CASE WHEN :sortBy = 'SPENT_COUNT_ASC' THEN spentMinor END ASC,
            CASE WHEN :sortBy = 'SPENT_COUNT_DESC' THEN spentMinor END DESC,
            purchaseCount DESC
    """)
    fun query(
        nameQuery: String?,
        sortBy: ProductSort,
        startTime: Long?,
        endTime: Long?,
        minPrice: Int? = null,
        maxPrice: Int? = null,
        marketIds: List<Int>? = null,
    ): PagingSource<Int, ProductFromQuery>
    @Query("SELECT * FROM products WHERE isArchived = 0 ORDER BY purchaseCount DESC")
    fun getAllActive(): Flow<List<ProductEntity>>

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

        val existingNormalizedNames = results.mapIndexedNotNull { index, resultId ->
            products[index].normalizedName.takeIf { resultId == -1L }
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

    @Query("""
        SELECT tp.unitPriceMinor, t.date, tp.quantity
        FROM transaction_products tp
        JOIN transactions t ON t.id = tp.transactionId  
        WHERE productId = :id 
        ORDER BY t.date ASC
    """)
    fun getHistoryById(id: Int): Flow<List<PricePoint>>
}

data class PricePoint(
    val unitPriceMinor: Int,
    val quantity: Int,
    val date: Long
)