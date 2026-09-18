package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Product::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "brand_style_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateInitialStyles(database.productDao())
                    }
                }
            }

            suspend fun populateInitialStyles(productDao: ProductDao) {
                val initialStyles = listOf(
                    Product(
                        title = "New Style Slim Fit T-Shirt",
                        category = "T-Shirt",
                        color = "Navy Blue",
                        createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 2
                    ),
                    Product(
                        title = "Premium Stretch Chino Pant",
                        category = "Pant",
                        color = "Olive Green",
                        createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 5
                    ),
                    Product(
                        title = "Luxury Cotton Designer Panjabi",
                        category = "Panjabi",
                        color = "Royal White",
                        createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 24
                    ),
                    Product(
                        title = "Urban Casual Crewneck T-Shirt",
                        category = "T-Shirt",
                        color = "Crimson Red",
                        createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 36
                    )
                )
                productDao.insertAll(initialStyles)
            }
        }
    }
}
