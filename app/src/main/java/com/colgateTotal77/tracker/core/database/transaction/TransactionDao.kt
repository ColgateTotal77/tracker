package com.colgateTotal77.tracker.core.database.transaction

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.paging.PagingSource
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Transaction
    @Query("SELECT * FROM `transactions` ORDER BY date DESC")
    fun query(): PagingSource<Int, TransactionWithProducts>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long //for notifying user that this transaction already exists

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(transaction: TransactionEntity)

    @Query("UPDATE `transactions` SET amountMinor = amountMinor + :additionalAmountMinor, updatedAt = :updatedAt WHERE id = :id")
    suspend fun bumpAmountMinorById(id: Int, additionalAmountMinor: Int, updatedAt: Long)

    @Query("DELETE FROM `transactions` WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("""
        SELECT COALESCE(SUM(amountMinor), 0) 
        FROM transactions 
        WHERE (:startTime IS NULL OR date >= :startTime)
        AND (:endTime IS NULL OR date <= :endTime)
    """)
    fun getTotalSpendingFlow(startTime: Long?, endTime: Long?): Flow<Long>
}