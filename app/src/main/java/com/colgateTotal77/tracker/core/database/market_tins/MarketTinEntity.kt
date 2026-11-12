package com.colgateTotal77.tracker.core.database.market_tins

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ForeignKey
import com.colgateTotal77.tracker.core.database.market.MarketEntity

@Entity(
    tableName = "market_tins",
    foreignKeys = [ForeignKey(MarketEntity::class, ["id"], ["marketId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("marketId")],
)
data class MarketTinEntity(
    @PrimaryKey val tin: String,
    val marketId: Int,
)