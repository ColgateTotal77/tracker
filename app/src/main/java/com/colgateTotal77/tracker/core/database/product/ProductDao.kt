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
                p.lastPrice,
                p.isArchived, 
                p.createdAt, 
                p.updatedAt,
                COALESCE(SUM(ep.quantity), 0) AS purchaseCount,
                COALESCE(SUM((ep.quantity * ep.unitPriceMinor) / 1000), 0) AS spentMinor
            FROM products p
            JOIN expense_products ep ON ep.productId = p.id 
            JOIN expenses e ON e.id = ep.expenseId 
            WHERE p.isArchived = 0 
                AND (:query = '' OR p.normalizedName LIKE '%' || :query || '%')
                AND (:startTime IS NULL OR e.date >= :startTime)
                AND (:endTime IS NULL OR e.date <= :endTime)
                AND (:marketIds IS NULL OR e.marketId IN (:marketIds))
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
        query: String,
        sortBy: ProductSort,
        startTime: Long?,
        endTime: Long?,
        minPrice: Long? = null,
        maxPrice: Long? = null,
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

    data class PricePoint(
        val unitPriceMinor: Long,
        val quantity: Int,
        val date: Long
    )

    @Query("""
        SELECT ep.unitPriceMinor, e.date, ep.quantity
        FROM expense_products ep
        JOIN expenses e ON e.id = ep.expenseId  
        WHERE productId = :id 
        ORDER BY e.date ASC
    """)
    fun getHistoryById(id: Int): Flow<List<PricePoint>>
}