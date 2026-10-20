package com.colgateTotal77.tracker.core.database.operation

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface OperationDao {
    @Transaction
    @Query("""
        SELECT id AS expenseId, NULL AS incomeId, NULL AS exchangeId, 'EXPENSE' AS type, date                                                                                                                                                                                                                                            
        FROM expenses                                                                                                                                                                                                                                                             
                                                                                                                                                                                                                                                                         
        UNION ALL                                                                                                                                                                                                                                                                 
                                                                                                                                                                                                                                                                         
        SELECT NULL AS expenseId, id AS incomeId, NULL AS exchangeId, 'INCOME' AS type, date                                                                                                                                                                                                                                             
        FROM incomes                                                                                                                                                                                                                                                              
        WHERE type != 'BALANCE_ADJUSTMENT'                                                                                                                                                                                                                                        
                                                                                                                                                                                                                                                                         
        UNION ALL                                                                                                                                                                                                                                                                 
                                                                                                                                                                                                                                                                         
        SELECT NULL AS expenseId, NULL AS incomeId, id AS exchangeId, 'EXCHANGE' AS type, date                                                                                                                                                                                                                                           
        FROM exchanges                                                                                                                                                                                                                                                            
                                                                                                                                                                                                                                                                         
        ORDER BY date DESC, expenseId DESC, incomeId DESC, exchangeId DESC 
    """)
    fun query(): PagingSource<Int, OperationWithDetails>

    @Transaction
    @Query("""                                                                                                                       
        SELECT id AS expenseId, NULL AS incomeId, NULL AS exchangeId, 'EXPENSE' AS type, date                                                                                                                                                                                                                                            
        FROM expenses                                                                                                                                                                                                                                                             
                                                                                                                                                                                                                                                                         
        UNION ALL                                                                                                                                                                                                                                                                 
                                                                                                                                                                                                                                                                         
        SELECT NULL AS expenseId, id AS incomeId, NULL AS exchangeId, 'INCOME' AS type, date                                                                                                                                                                                                                                             
        FROM incomes                                                                                                                                                                                                                                                              
        WHERE type != 'BALANCE_ADJUSTMENT'                                                                                                                                                                                                                                        
                                                                                                                                                                                                                                                                         
        UNION ALL                                                                                                                                                                                                                                                                 
                                                                                                                                                                                                                                                                         
        SELECT NULL AS expenseId, NULL AS incomeId, id AS exchangeId, 'EXCHANGE' AS type, date                                                                                                                                                                                                                                           
        FROM exchanges                                                                                                                                                                                                                                                            
                                                                                                                                                                                                                                                                         
        ORDER BY date DESC, expenseId DESC, incomeId DESC, exchangeId DESC                                                                                               
        LIMIT 3                                                                                                                      
    """)
    fun getRecent(): Flow<List<OperationForOverview>>

    @Query("""                                                                                                                       
        WITH operations AS (                                                                                                         
            SELECT id AS expenseId, NULL AS incomeId, date                                                                           
            FROM expenses                                                                                                            
                                                                                                                                    
            UNION ALL                                                                                                                
                                                                                                                                    
            SELECT NULL AS expenseId, id AS incomeId, date                                                                           
            FROM incomes
            WHERE type != 'BALANCE_ADJUSTMENT'
        ),                                                                                                                           
        target AS (                                                                                                                  
           SELECT *                                                                                                                 
           FROM operations                                                                                                          
           WHERE expenseId = :expenseId OR incomeId = :incomeId                                                                     
        )  
                                                                                                                                  
        SELECT (                                                                                                                     
           SELECT COUNT(*)                                                                                                          
           FROM operations AS o                                                                                                     
           WHERE o.date > t.date                                                                                                    
              OR (                                                                                                                  
                  o.date = t.date AND (
                    (
                        t.expenseId IS NOT NULL 
                        AND o.expenseId > t.expenseId 
                    )                    
                    OR (                                                                                                                             
                        t.incomeId IS NOT NULL                                                                                                       
                        AND (                                                                                                                        
                            o.expenseId IS NOT NULL                                                                                                  
                            OR o.incomeId > t.incomeId                                                                                               
                        )                                                                                                                            
                    )   
                  )                                                                                                                 
              )                                                                                                                     
        )                                                                                                                            
       FROM target AS t                                                                   
    """)
    suspend fun getPosition(
        expenseId: Int?,
        incomeId: Int?,
    ): Int

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