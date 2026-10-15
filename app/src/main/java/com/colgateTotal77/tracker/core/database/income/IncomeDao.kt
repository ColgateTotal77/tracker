package com.colgateTotal77.tracker.core.database.income

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(expense: IncomeEntity): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(expense: IncomeEntity)

    @Query("DELETE FROM `income` WHERE id = :id")
    suspend fun deleteById(id: Int)
}