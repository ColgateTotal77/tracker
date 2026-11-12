package com.colgateTotal77.tracker.core.database.market_tins

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.colgateTotal77.tracker.core.database.market.MarketEntity

@Dao
interface MarketTinDao {
    @Query("SELECT markets.* FROM markets JOIN market_tins ON market_tins.marketId = markets.id WHERE market_tins.tin = :tin")
    suspend fun getMarketByTin(tin: String): MarketEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(marketTin: MarketTinEntity)

    @Query("UPDATE market_tins SET marketId = :toId WHERE marketId = :fromId")
    suspend fun moveTins(fromId: Int, toId: Int)
}