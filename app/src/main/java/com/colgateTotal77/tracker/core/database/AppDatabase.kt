package com.colgateTotal77.tracker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.colgateTotal77.tracker.core.database.market.MarketDao
import com.colgateTotal77.tracker.core.database.market.MarketEntity
import com.colgateTotal77.tracker.core.database.product.ProductDao
import com.colgateTotal77.tracker.core.database.product.ProductEntity
import com.colgateTotal77.tracker.core.database.transaction.TransactionDao
import com.colgateTotal77.tracker.core.database.transaction.TransactionEntity
import com.colgateTotal77.tracker.core.database.transaction_product.TransactionProductDao
import com.colgateTotal77.tracker.core.database.transaction_product.TransactionProductEntity

@Database(
    entities = [
        TransactionEntity::class,
        MarketEntity::class,
        ProductEntity::class,
        TransactionProductEntity::class,
    ],
    version = 1,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun marketDao(): MarketDao
    abstract fun productDao(): ProductDao
    abstract fun transactionProductDao(): TransactionProductDao

    companion object {
        val CALLBACK =  object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                db.execSQL("""                                                                                                                   
                    CREATE TRIGGER IF NOT EXISTS update_product_after_transaction_products_creation                                              
                    AFTER INSERT ON transaction_products                                                                                         
                    BEGIN                                                                                                                        
                        UPDATE products                                                                                                         
                        SET                                                                                                                      
                            lastPrice = NEW.unitPriceMinor,                                                                                      
                            purchaseCount = purchaseCount + NEW.quantity,
                            averagePrice = (                                                                                                     
                                SELECT COALESCE(CAST(AVG(unitPriceMinor) AS INTEGER), 0)                                                         
                                FROM transaction_products                                                                                        
                                WHERE productId = NEW.productId                                                                                  
                            )                                                                                                                    
                        WHERE id = NEW.productId;                                                                                                
                    END;                                                                                                                         
                """.trimIndent())

                db.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS update_product_after_transaction_products_update
                    AFTER UPDATE OF unitPriceMinor, productId, quantity ON transaction_products
                    WHEN OLD.unitPriceMinor != NEW.unitPriceMinor OR OLD.productId != NEW.productId OR OLD.quantity != NEW.quantity
                    BEGIN
                        UPDATE products SET
                            averagePrice = (SELECT COALESCE(CAST(AVG(unitPriceMinor) AS INTEGER), 0)
                                            FROM transaction_products WHERE productId IN (OLD.productId, NEW.productId)),
                            lastPrice = (SELECT unitPriceMinor FROM transaction_products tp
                                         JOIN transactions t ON t.id = tp.transactionId
                                         WHERE tp.productId IN (OLD.productId, NEW.productId)
                                         ORDER BY t.date DESC LIMIT 1),
                            purchaseCount = purchaseCount
                                          + (CASE WHEN id = NEW.productId THEN NEW.quantity ELSE 0 END)
                                          - (CASE WHEN id = OLD.productId THEN OLD.quantity ELSE 0 END)
                        WHERE id IN (OLD.productId, NEW.productId);
                    END;
                """.trimIndent())

                db.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS update_product_after_transaction_products_delete
                    AFTER DELETE ON transaction_products
                    BEGIN
                        UPDATE products SET
                            averagePrice = (SELECT COALESCE(CAST(AVG(unitPriceMinor) AS INTEGER), 0)
                                            FROM transaction_products WHERE productId = OLD.productId),
                            lastPrice = (SELECT unitPriceMinor FROM transaction_products tp
                                         JOIN transactions t ON t.id = tp.transactionId
                                         WHERE tp.productId = OLD.productId
                                         ORDER BY t.date DESC LIMIT 1),
                            purchaseCount = purchaseCount - OLD.quantity
                        WHERE id = OLD.productId;
                    END;
                """.trimIndent())
            }
        }
    }
}