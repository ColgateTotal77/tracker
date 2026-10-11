package com.colgateTotal77.tracker.core.database.expense_product

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Insert
import kotlinx.coroutines.flow.Flow
import androidx.room.OnConflictStrategy

@Dao
interface ExpenseProductDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) //need to use if we add new product manually to expense
    suspend fun insert(expenseProduct: ExpenseProductEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(expenseProducts: List<ExpenseProductEntity>)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(expenseProduct: ExpenseProductEntity)

    @Query("DELETE FROM `expense_products` WHERE id = :id")
    suspend fun deleteById(id: Int)
}