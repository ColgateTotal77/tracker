package com.colgateTotal77.tracker.core.database.transaction_product

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Insert
import kotlinx.coroutines.flow.Flow
import androidx.room.OnConflictStrategy

@Dao
interface TransactionProductDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) //need to use if we add new product manually to transaction
    suspend fun insert(transactionProduct: TransactionProductEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactionProducts: List<TransactionProductEntity>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(transactionProduct: TransactionProductEntity)

    @Query("DELETE FROM `transaction_products` WHERE id = :id")
    suspend fun deleteById(id: Int)
}