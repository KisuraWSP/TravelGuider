package com.app.tourism_app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.app.tourism_app.database.dao.UserDao
import com.app.tourism_app.database.model.User

@Database(entities = [User::class], version = 2)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var INSTANCE: UserDatabase? = null

        fun getInstance(context: Context): UserDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext, UserDatabase::class.java, "user_database.db")
                .addMigrations(MIGRATION_1_2) // register migration
                .build()

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // If you only added a new table (Favorite), create it here.
                // Replace the column definitions with the exact schema of your Favorite entity.

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `Favorite` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `place_id` INTEGER NOT NULL,
                        `title` TEXT,
                        `imageUrl` TEXT,
                        `userId` TEXT
                    )
                """.trimIndent())

                // If you modified any existing tables, alter them here (SQLite doesn't support ALTER add multiple columns easily).
            }
        }
    }
}