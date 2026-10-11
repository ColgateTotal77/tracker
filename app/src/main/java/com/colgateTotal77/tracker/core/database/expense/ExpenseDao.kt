package com.colgateTotal77.tracker.core.database.expense

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.paging.PagingSource
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Transaction
    @Query("SELECT * FROM `expenses` ORDER BY date DESC")
    fun query(): PagingSource<Int, ExpenseWithProducts>

    @Query("""                                                                                                                       
       SELECT * FROM expenses                                                                                                   
       ORDER BY date DESC, id DESC                                                                                                  
       LIMIT 3                                                                                                                      
   """)
    fun getRecent(): Flow<List<ExpenseWithMarket>>

    @Query("""
        SELECT * FROM expenses 
        WHERE fiscalId = :fiscalId
    """)
    suspend fun getTransactionByFiscalId(fiscalId: String): ExpenseEntity

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(expense: ExpenseEntity): Long //for notifying user that this expense already exists

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(expense: ExpenseEntity)

    @Query("UPDATE `expenses` SET amountMinor = amountMinor + :additionalAmountMinor, updatedAt = :updatedAt WHERE id = :id")
    suspend fun bumpAmountMinorById(id: Int, additionalAmountMinor: Int, updatedAt: Long)

    @Query("DELETE FROM `expenses` WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("""
        SELECT COALESCE(SUM(amountMinor), 0) 
        FROM expenses 
        WHERE (:startTime IS NULL OR date >= :startTime)
        AND (:endTime IS NULL OR date <= :endTime)
    """)
    fun getTotalSpendingFlow(startTime: Long?, endTime: Long?): Flow<Long>

    @Query("""                                                                                                                       
       SELECT COUNT(*)                                                                                                              
       FROM expenses                                                                                                                
       WHERE date > (SELECT date FROM expenses WHERE id = :id)                                                                       
   """)
    suspend fun getPosition(id: Int): Int
}