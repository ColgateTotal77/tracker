package com.colgateTotal77.tracker.core.database.operation

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.colgateTotal77.tracker.core.enums.OperationType
import kotlinx.coroutines.flow.Flow

@Dao
interface OperationDao {
    @Transaction
    @Query("""
        SELECT id AS expenseId, NULL AS incomeId, NULL AS exchangeId, 'EXPENSE' AS type, date                                                                                                                                                                                                                                            
        FROM expenses                                                                                                                                                                                                                                                
        WHERE :searchQuery == '' 
            OR note LIKE '%' || :searchQuery || '%'
            OR EXISTS (
                SELECT 1
                FROM markets AS m
                WHERE m.id = expenses.marketId AND m.name LIKE '%' || :searchQuery || '%'
            )
            OR EXISTS (
                SELECT 1
                FROM expense_products AS ep
                JOIN products AS p ON p.id = ep.productId
                WHERE ep.expenseId = expenses.id AND p.alias LIKE '%' || :searchQuery || '%'
            )

        UNION ALL                                                                                                                                                                                                                                                                 
                                                                                                                                                                                                                                                                         
        SELECT NULL AS expenseId, id AS incomeId, NULL AS exchangeId, 'INCOME' AS type, date                                                                                                                                                                                                                                             
        FROM incomes                                                                                                                                                                                                                                                              
        WHERE incomes.type != 'BALANCE_ADJUSTMENT' AND (:searchQuery = '' OR note LIKE '%' || :searchQuery || '%')                                                                                                                                                                                                                                       
                                                                                                                                                                                                                                                                         
        UNION ALL                                                                                                                                                                                                                                                                 
                                                                                                                                                                                                                                                                         
        SELECT NULL AS expenseId, NULL AS incomeId, id AS exchangeId, 'EXCHANGE' AS type, date                                                                                                                                                                                                                                           
        FROM exchanges                                                                                                                                                                                                                                                            
        WHERE :searchQuery = '' OR note LIKE '%' || :searchQuery || '%'
                                                                                                                                                                                                                                                                       
        ORDER BY date DESC, expenseId DESC, incomeId DESC, exchangeId DESC 
    """)
    fun query(
        searchQuery: String,
    ): PagingSource<Int, OperationWithDetails>

    @Transaction
    @Query("""                                                                                                                       
        SELECT id AS expenseId, NULL AS incomeId, NULL AS exchangeId, 'EXPENSE' AS type, date                                                                                                                                                                                                                                            
        FROM expenses                                                                                                                                                                                                                                                             
                                                                                                                                                                                                                                                                         
        UNION ALL                                                                                                                                                                                                                                                                 
                                                                                                                                                                                                                                                                         
        SELECT NULL AS expenseId, id AS incomeId, NULL AS exchangeId, 'INCOME' AS type, date                                                                                                                                                                                                                                             
        FROM incomes                                                                                                                                                                                                                                                              
        WHERE incomes.type != 'BALANCE_ADJUSTMENT'                                                                                                                                                                                                                                        
                                                                                                                                                                                                                                                                         
        UNION ALL                                                                                                                                                                                                                                                                 
                                                                                                                                                                                                                                                                         
        SELECT NULL AS expenseId, NULL AS incomeId, id AS exchangeId, 'EXCHANGE' AS type, date                                                                                                                                                                                                                                           
        FROM exchanges                                                                                                                                                                                                                                                            
                                                                                                                                                                                                                                                                         
        ORDER BY date DESC, expenseId DESC, incomeId DESC, exchangeId DESC                                                                                               
        LIMIT 3                                                                                                                      
    """)
    fun getRecent(): Flow<List<OperationForOverview>>

    @Query("""                                                                                                                       
        WITH operations AS (                                                                                                         
        SELECT id, 'EXPENSE' AS type, 3 AS priority, date                                                                        
        FROM expenses                                                                                                            
        WHERE :searchQuery == '' 
            OR note LIKE '%' || :searchQuery || '%'
            OR EXISTS (
                SELECT 1
                FROM markets AS m
                WHERE m.id = expenses.marketId AND m.name LIKE '%' || :searchQuery || '%'
            )
            OR EXISTS (
                SELECT 1
                FROM expense_products AS ep
                JOIN products AS p ON p.id = ep.productId
                WHERE ep.expenseId = expenses.id AND p.alias LIKE '%' || :searchQuery || '%'
            )
                                                                                                                    
        UNION ALL                                                                                                                
                                                                                                                                
        SELECT id, 'INCOME' AS type, 2 AS priority, date                                                                         
        FROM incomes                                                                                                             
        WHERE incomes.type != 'BALANCE_ADJUSTMENT' AND (:searchQuery = '' OR note LIKE '%' || :searchQuery || '%')                                                                                        
                                                                                                                                
        UNION ALL                                                                                                                
                                                                                                                                
        SELECT id, 'EXCHANGE' AS type, 1 AS priority, date                                                                       
        FROM exchanges                                                                                                           
        WHERE :searchQuery = '' OR note LIKE '%' || :searchQuery || '%'
       
        )                                                                                                                            
        SELECT (                                                                                                                     
            SELECT COUNT(*)                                                                                                          
            FROM operations AS o                                                                                                     
            WHERE o.date > target.date                                                                                               
            OR (                                                                                                                 
                o.date = target.date                                                                                             
                AND o.priority > target.priority                                                                                 
            )                                                                                                                    
            OR (                                                                                                                 
                o.date = target.date                                                                                             
                AND o.priority = target.priority                                                                                 
                AND o.id > target.id                                                                                             
            )                                                                                                                    
        )                                                                                                                            
       FROM operations AS target                                                                                                    
       WHERE target.id = :id AND target.type = :type                                                                                
    """)
    suspend fun getPosition(
        id: Int,
        type: OperationType,
        searchQuery: String,
    ): Int?

    @Query("""
        SELECT currency, SUM(amount) AS amountMinor                                                                                              
        FROM (                                                                                                                       
            SELECT currency, -CAST(amountMinor AS INTEGER) AS amount                                                                           
            FROM expenses                                                                                                            
                                                                                                                                    
            UNION ALL                                                                                                                
                                                                                                                                    
            SELECT currency, amountMinor AS amount                                                                                             
            FROM incomes
            
            UNION ALL
            
            SELECT fromCurrency AS currency, -CAST(fromAmountMinor AS INTEGER) AS amount                                                                                              
            FROM exchanges
            
            UNION ALL
            
            SELECT toCurrency AS currency, toAmountMinor AS amount                                                                                              
            FROM exchanges
        )
        GROUP BY currency
    """)
    fun getBalance(): Flow<List<CurrencyBalance>>
}