package com.colgateTotal77.tracker.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.execSQL
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
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ],
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun marketDao(): MarketDao
    abstract fun productDao(): ProductDao
    abstract fun transactionProductDao(): TransactionProductDao

    companion object {
        private val TRIGGERS = listOf(
            "update_product_after_transaction_product_creation" to """
                CREATE TRIGGER update_product_after_transaction_product_creation
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
                        ),
                        updatedAt = (strftime('%s','now') * 1000)
                    WHERE id = NEW.productId;
                END;
            """.trimIndent(),

            "update_product_after_transaction_product_update" to """
                CREATE TRIGGER update_product_after_transaction_product_update
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
                                      - (CASE WHEN id = OLD.productId THEN OLD.quantity ELSE 0 END),
                        updatedAt = (strftime('%s','now') * 1000)
                    WHERE id IN (OLD.productId, NEW.productId);
                END;
            """.trimIndent(),

            "update_product_after_transaction_product_delete" to """
                CREATE TRIGGER update_product_after_transaction_product_delete
                AFTER DELETE ON transaction_products
                BEGIN
                    UPDATE products SET
                        averagePrice = (SELECT COALESCE(CAST(AVG(unitPriceMinor) AS INTEGER), 0)
                                        FROM transaction_products WHERE productId = OLD.productId),
                        lastPrice = (SELECT unitPriceMinor FROM transaction_products tp
                                     JOIN transactions t ON t.id = tp.transactionId
                                     WHERE tp.productId = OLD.productId
                                     ORDER BY t.date DESC LIMIT 1),
                        purchaseCount = purchaseCount - OLD.quantity,
                        updatedAt = (strftime('%s','now') * 1000)
                    WHERE id = OLD.productId;
                END;
            """.trimIndent(),

            "update_transaction_after_transaction_product_update" to """
                CREATE TRIGGER update_transaction_after_transaction_product_update
                AFTER UPDATE OF totalMinor, transactionId ON transaction_products
                BEGIN
                    UPDATE transactions SET
                        amountMinor = amountMinor - OLD.totalMinor,
                        updatedAt = (strftime('%s','now') * 1000)
                    WHERE id = OLD.transactionId;
                    UPDATE transactions SET
                        amountMinor = amountMinor + NEW.totalMinor,
                        updatedAt = (strftime('%s','now') * 1000)
                    WHERE id = NEW.transactionId;
                END;
            """.trimIndent(),

            "update_transaction_after_transaction_product_delete" to """
                CREATE TRIGGER update_transaction_after_transaction_product_delete
                AFTER DELETE ON transaction_products
                BEGIN
                    UPDATE transactions SET
                        amountMinor = amountMinor - OLD.totalMinor,
                        updatedAt = (strftime('%s','now') * 1000)
                    WHERE id = OLD.transactionId;
                END;
            """.trimIndent(),
        )

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("""
                    CREATE TABLE `_new_transaction_products` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `transactionId` INTEGER NOT NULL,
                        `productId` INTEGER NOT NULL,
                        `quantity` INTEGER NOT NULL,
                        `unitPriceMinor` INTEGER NOT NULL,
                        `totalMinor` INTEGER NOT NULL,
                        `taxGroup` TEXT,
                        `position` INTEGER NOT NULL,
                        `isManuallyCreated` INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(`transactionId`) REFERENCES `transactions`(`id`)
                            ON UPDATE CASCADE ON DELETE CASCADE
                    )
                """.trimIndent())
                connection.execSQL("""
                    INSERT INTO `_new_transaction_products`
                        (`id`, `transactionId`, `productId`, `quantity`, `unitPriceMinor`,
                         `totalMinor`, `taxGroup`, `position`, `isManuallyCreated`)
                    SELECT `id`, `transactionId`, `productId`, `quantity`, `unitPriceMinor`,
                           `totalMinor`, `taxGroup`, `position`, `isManuallyCreated`
                    FROM `transaction_products`
                """.trimIndent())
                connection.execSQL("DROP TABLE `transaction_products`")
                connection.execSQL("ALTER TABLE `_new_transaction_products` RENAME TO `transaction_products`")
                connection.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_products_transactionId` ON `transaction_products` (`transactionId`)")
                connection.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_products_productId` ON `transaction_products` (`productId`)")
            }
        }

        val CALLBACK = object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)

                //TODO only for dev, in prod use IF NOT EXISTS
                for ((name, createSql) in TRIGGERS) {
                    db.execSQL("DROP TRIGGER IF EXISTS `$name`")
                    db.execSQL(createSql)
                }
            }
        }
    }
}
