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
        SELECT id AS expenseId, NULL AS incomeId, "EXPENSE" AS type, date                                                                               
        FROM expenses                                                                                                                
                                                                                                                                    
        UNION ALL                                                                                                                    
                                                                                                                                    
        SELECT NULL AS expenseId, id AS incomeId, "INCOME" AS type, date                                                                               
        FROM income                                                                                                                      
        WHERE type != 'BALANCE_ADJUSTMENT'

        ORDER BY date DESC, expenseId DESC, incomeId DESC  
    """)
    fun query(): PagingSource<Int, OperationWithDetails>

    @Transaction
    @Query("""                                                                                                                       
        SELECT id AS expenseId, NULL AS incomeId, "EXPENSE" AS type, date                                                                               
        FROM expenses                                                                                                                
                                                                                                                                    
        UNION ALL                                                                                                                    
                                                                                                                                    
        SELECT NULL AS expenseId, id AS incomeId, "INCOME" AS type, date                                                                               
        FROM income
        WHERE type != 'BALANCE_ADJUSTMENT'

        ORDER BY date DESC, expenseId DESC, incomeId DESC                                                                                                  
        LIMIT 3                                                                                                                      
    """)
    fun getRecent(): Flow<List<OperationForOverview>>

    @Query("""                                                                                                                       
        WITH operations AS (                                                                                                         
            SELECT id AS expenseId, NULL AS incomeId, date                                                                           
            FROM expenses                                                                                                            
                                                                                                                                    
            UNION ALL                                                                                                                
                                                                                                                                    
            SELECT NULL AS expenseId, id AS incomeId, date                                                                           
            FROM income
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
        SELECT COALESCE(SUM(amount), 0)                                                                                              
        FROM (                                                                                                                       
            SELECT -CAST(amountMinor AS INTEGER) AS amount                                                                           
            FROM expenses                                                                                                            
                                                                                                                                    
            UNION ALL                                                                                                                
                                                                                                                                    
            SELECT amountMinor AS amount                                                                                             
            FROM income                                                                                                              
        )
    """)
    fun getBalance(): Flow<Long>
}