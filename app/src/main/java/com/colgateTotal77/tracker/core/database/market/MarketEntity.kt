package com.colgateTotal77.tracker.core.database.market

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.colgateTotal77.tracker.core.ui.NamedItem

@Entity(tableName = "markets", indices = [Index("tin", unique = true)])
data class MarketEntity (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tin: String?,
    override val name: String?
): NamedItem

sealed interface MarketChoice {
    data class Existing(val marketId: Int) : MarketChoice
    data class New(val name: String) : MarketChoice
    data class ByTin(val tin: String) : MarketChoice
    data object None : MarketChoice
}
