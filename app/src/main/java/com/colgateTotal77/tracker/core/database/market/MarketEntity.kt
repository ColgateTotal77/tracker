package com.colgateTotal77.tracker.core.database.market

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "markets")
data class MarketEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String? = "",
    @ColumnInfo(defaultValue = "-1")
    var lastVisited: Long = -1,
    @ColumnInfo(defaultValue = "0")
    val createdAt: Long,
    @ColumnInfo(defaultValue = "0")
    val updatedAt: Long
)

sealed interface MarketChoice {
    data class Existing(val market: MarketEntity) : MarketChoice
    data class New(val name: String) : MarketChoice
    data class ByTin(val tin: String) : MarketChoice
    data object None : MarketChoice
}
