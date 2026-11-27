package com.colgateTotal77.tracker.core.database.market

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.colgateTotal77.tracker.core.ui.vico.DailySum
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketDao {
    @Query("""
        SELECT * FROM `markets` m
        WHERE :query = '' OR m.name LIKE '%' || :query || '%'
        ORDER BY lastVisited DESC
    """)
    fun query(query: String): PagingSource<Int, MarketEntity>

    @Query("SELECT * FROM `markets` ORDER BY lastVisited DESC")
    fun getAll(): Flow<List<MarketEntity>>

    @Query("SELECT * FROM `markets` WHERE id = :id")
    suspend fun getById(id: Int): MarketEntity?

    @Query("SELECT * FROM `markets` WHERE LOWER(TRIM(name)) = :normalizedName LIMIT 1")
    suspend fun getByNormalizedName(normalizedName: String): MarketEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(market: MarketEntity): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(market: MarketEntity)

    @Query("DELETE FROM `markets` WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("""
        SELECT 
            DATE(e.date / 1000, 'unixepoch', 'localtime') AS day,
            SUM(amountMinor * currencyChangeRate) as amount
        FROM expenses e
        WHERE e.marketId = :id
        GROUP BY day
        ORDER BY day ASC
    """)
    fun getHistoryById(id: Int): Flow<List<DailySum>>
}