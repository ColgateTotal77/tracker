package com.colgateTotal77.tracker.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.execSQL
import com.colgateTotal77.tracker.core.database.market.MarketDao
import com.colgateTotal77.tracker.core.database.market.MarketEntity
import com.colgateTotal77.tracker.core.database.product.ProductDao
import com.colgateTotal77.tracker.core.database.product.ProductEntity
import com.colgateTotal77.tracker.core.database.expense.ExpenseDao
import com.colgateTotal77.tracker.core.database.expense.ExpenseEntity
import com.colgateTotal77.tracker.core.database.expense_product.ExpenseProductDao
import com.colgateTotal77.tracker.core.database.expense_product.ExpenseProductEntity

@Database(
    entities = [
        ExpenseEntity::class,
        MarketEntity::class,
        ProductEntity::class,
        ExpenseProductEntity::class,
    ],
    version = 6,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 4, to = 5, spec = AppDatabase.Migration4To5::class)
    ],
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    @DeleteColumn(tableName = "products", columnName = "averagePrice")
    class Migration4To5 : AutoMigrationSpec

    abstract fun expenseDao(): ExpenseDao
    abstract fun marketDao(): MarketDao
    abstract fun productDao(): ProductDao
    abstract fun expenseProductDao(): ExpenseProductDao

    companion object {
        private val TRIGGERS = listOf(
            "update_product_after_expense_product_creation" to """
                CREATE TRIGGER update_product_after_expense_product_creation
                AFTER INSERT ON expense_products
                BEGIN
                    UPDATE products
                    SET
                        lastPrice = NEW.unitPriceMinor,
                        purchaseCount = purchaseCount + NEW.quantity,
                        updatedAt = (strftime('%s','now') * 1000)
                    WHERE id = NEW.productId;
                END;
            """.trimIndent(),

            "update_product_after_expense_product_update" to """
                CREATE TRIGGER update_product_after_expense_product_update
                AFTER UPDATE OF unitPriceMinor, productId, quantity ON expense_products
                WHEN OLD.unitPriceMinor != NEW.unitPriceMinor OR OLD.productId != NEW.productId OR OLD.quantity != NEW.quantity
                BEGIN
                    UPDATE products SET
                        lastPrice = COALESCE((SELECT tp.unitPriceMinor FROM expense_products tp
                                     JOIN expenses t ON t.id = tp.expenseId
                                     WHERE tp.productId = products.id
                                     ORDER BY t.date DESC LIMIT 1), lastPrice),
                        purchaseCount = purchaseCount
                                      + (CASE WHEN products.id = NEW.productId THEN NEW.quantity ELSE 0 END)
                                      - (CASE WHEN products.id = OLD.productId THEN OLD.quantity ELSE 0 END),
                        updatedAt = (strftime('%s','now') * 1000)
                    WHERE id IN (OLD.productId, NEW.productId);
                END;
            """.trimIndent(),

            "update_product_after_expense_product_delete" to """
                CREATE TRIGGER update_product_after_expense_product_delete
                AFTER DELETE ON expense_products
                BEGIN
                    UPDATE products SET
                        lastPrice = COALESCE((SELECT tp.unitPriceMinor FROM expense_products tp
                                     JOIN expenses t ON t.id = tp.expenseId
                                     WHERE tp.productId = OLD.productId
                                     ORDER BY t.date DESC LIMIT 1), lastPrice),
                        purchaseCount = purchaseCount - OLD.quantity,
                        updatedAt = (strftime('%s','now') * 1000)
                    WHERE id = OLD.productId;
                END;
            """.trimIndent(),

            "update_expense_after_expense_product_update" to """
                CREATE TRIGGER update_expense_after_expense_product_update
                AFTER UPDATE OF totalMinor, expenseId ON expense_products
                BEGIN
                    UPDATE expenses SET
                        amountMinor = amountMinor - OLD.totalMinor,
                        updatedAt = (strftime('%s','now') * 1000)
                    WHERE id = OLD.expenseId;
                    UPDATE expenses SET
                        amountMinor = amountMinor + NEW.totalMinor,
                        updatedAt = (strftime('%s','now') * 1000)
                    WHERE id = NEW.expenseId;
                END;
            """.trimIndent(),

            "update_expense_after_expense_product_delete" to """
                CREATE TRIGGER update_expense_after_expense_product_delete
                AFTER DELETE ON expense_products
                BEGIN
                    UPDATE expenses SET
                        amountMinor = amountMinor - OLD.totalMinor,
                        updatedAt = (strftime('%s','now') * 1000)
                    WHERE id = OLD.expenseId;
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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(connection: SQLiteConnection) {
                for ((name, _) in TRIGGERS) {
                    val oldName = name.replace("expense", "transaction")
                    connection.execSQL("DROP TRIGGER IF EXISTS `$oldName`")
                }

                connection.execSQL("""
                    CREATE TABLE `_new_products` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `normalizedName` TEXT NOT NULL,
                        `alias` TEXT NOT NULL,
                        `lastPrice` INTEGER NOT NULL,
                        `averagePrice` INTEGER NOT NULL,
                        `purchaseCount` INTEGER NOT NULL,
                        `isArchived` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                connection.execSQL("""
                    INSERT INTO `_new_products`
                        (`id`, `normalizedName`, `alias`, `lastPrice`, `averagePrice`,
                         `purchaseCount`, `isArchived`, `createdAt`, `updatedAt`)
                    SELECT `id`, `normalizedName`, `alias`, `lastPrice`, `averagePrice`,
                           `purchaseCount`, `isArchived`, `createdAt`, `updatedAt`
                    FROM `products`
                """.trimIndent())
                connection.execSQL("DROP TABLE `products`")
                connection.execSQL("ALTER TABLE `_new_products` RENAME TO `products`")
                connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_products_normalizedName` ON `products` (`normalizedName`)")

                connection.execSQL("ALTER TABLE `transaction_products` ADD COLUMN `barcode` TEXT")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(connection: SQLiteConnection) {
                val oldTriggers = listOf(
                    "update_product_after_transaction_product_creation",
                    "update_product_after_transaction_product_update",
                    "update_product_after_transaction_product_delete",
                    "update_transaction_after_transaction_product_update",
                    "update_transaction_after_transaction_product_delete",
                )
                for (name in oldTriggers) connection.execSQL("DROP TRIGGER IF EXISTS `$name`")

                connection.execSQL("""
                    CREATE TABLE `expenses` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `amountMinor` INTEGER NOT NULL,
                        `currency` TEXT NOT NULL,
                        `note` TEXT,
                        `status` TEXT NOT NULL,
                        `source` TEXT NOT NULL,
                        `rawFiscalPayload` TEXT,
                        `fiscalId` TEXT,
                        `marketId` INTEGER,
                        `date` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        FOREIGN KEY(`marketId`) REFERENCES `markets`(`id`) ON DELETE SET NULL
                    )
                """.trimIndent())
                connection.execSQL("INSERT INTO `expenses` SELECT * FROM `transactions`")
                connection.execSQL("""
                    CREATE TABLE `expense_products` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `expenseId` INTEGER NOT NULL,
                        `productId` INTEGER NOT NULL,
                        `quantity` INTEGER NOT NULL,
                        `unitPriceMinor` INTEGER NOT NULL,
                        `totalMinor` INTEGER NOT NULL,
                        `taxGroup` TEXT,
                        `position` INTEGER NOT NULL,
                        `isManuallyCreated` INTEGER NOT NULL DEFAULT 0,
                        `barcode` TEXT,
                        FOREIGN KEY(`expenseId`) REFERENCES `expenses`(`id`)
                            ON UPDATE CASCADE ON DELETE CASCADE
                    )
                """.trimIndent())
                connection.execSQL("INSERT INTO `expense_products` SELECT * FROM `transaction_products`")
                connection.execSQL("DROP TABLE `transaction_products`")
                connection.execSQL("DROP TABLE `transactions`")

                for (column in listOf("marketId", "date", "fiscalId")) {
                    val unique = if (column == "fiscalId") "UNIQUE " else ""
                    connection.execSQL("CREATE ${unique}INDEX `index_expenses_$column` ON `expenses` (`$column`)")
                }
                connection.execSQL("CREATE INDEX `index_expense_products_expenseId` ON `expense_products` (`expenseId`)")
                connection.execSQL("CREATE INDEX `index_expense_products_productId` ON `expense_products` (`productId`)")

                for ((_, sql) in TRIGGERS) connection.execSQL(sql)
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
