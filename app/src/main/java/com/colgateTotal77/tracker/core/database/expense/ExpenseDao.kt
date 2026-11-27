package com.colgateTotal77.tracker.core.database.expense

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy
import com.colgateTotal77.tracker.core.database.operation.CurrencyBalance
import com.colgateTotal77.tracker.core.ui.vico.DailySum
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
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
    suspend fun bumpAmountMinorById(id: Int, additionalAmountMinor: Long, updatedAt: Long)

    @Query("DELETE FROM `expenses` WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("""
        SELECT currency, SUM(amountMinor) AS amountMinor                                                                                              
        FROM expenses
        WHERE (:startTime IS NULL OR date >= :startTime)
        GROUP BY currency
    """)
    fun getTotalSpendingFlow(startTime: Long?): Flow<List<CurrencyBalance>>

    @Query("UPDATE expenses SET marketId = :toId WHERE marketId = :fromId")
    suspend fun moveMarket(fromId: Int, toId: Int)

    @Query("""
        SELECT 
            date(date / 1000, 'unixepoch', 'localtime') AS day,
            SUM(amountMinor * currencyChangeRate) as amount
        FROM expenses
        GROUP BY day
    """)
    fun getDailySumsFlow(): Flow<List<DailySum>>
}