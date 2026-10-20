package com.colgateTotal77.tracker.core.database.exchange

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy

@Dao
interface ExchangeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(expense: ExchangeEntity): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(expense: ExchangeEntity)

    @Query("DELETE FROM `exchanges` WHERE id = :id")
    suspend fun deleteById(id: Int)
}