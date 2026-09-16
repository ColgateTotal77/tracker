package com.colgateTotal77.tracker.core.database.market

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketDao {
    @Query("SELECT * FROM `markets` ORDER BY name")
    fun getAll(): Flow<List<MarketEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(market: MarketEntity)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(market: MarketEntity)
}