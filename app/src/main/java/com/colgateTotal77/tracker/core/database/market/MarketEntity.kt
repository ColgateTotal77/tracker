package com.colgateTotal77.tracker.core.database.market

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "markets")
data class MarketEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String?,
)

sealed interface MarketChoice {
    data class Existing(val market: MarketEntity) : MarketChoice
    data class New(val name: String) : MarketChoice
    data class ByTin(val tin: String) : MarketChoice
    data object None : MarketChoice
}
