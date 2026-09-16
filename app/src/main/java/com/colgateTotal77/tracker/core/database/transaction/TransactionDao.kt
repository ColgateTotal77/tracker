package com.colgateTotal77.tracker.core.database.transaction

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.paging.PagingSource
import androidx.room.OnConflictStrategy

@Dao
interface TransactionDao {
    @Query("SELECT * FROM `transactions` ORDER BY date DESC")
    fun query(): PagingSource<Int, TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long //for notifying user that this transaction already exists

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM `transactions` WHERE id = :id")
    suspend fun deleteById(id: Int)
}