package com.colgateTotal77.tracker.core.database.market

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "markets", indices = [Index("tin", unique = true)])
data class MarketEntity (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tin: String?,
    val name: String?
)