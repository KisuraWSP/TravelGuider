package com.app.tourism_app.database.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.app.tourism_app.database.dao.ReviewDao
import com.app.tourism_app.database.dao.FavoriteDao
import com.app.tourism_app.database.dao.UserDao
import com.app.tourism_app.database.model.Review
import com.app.tourism_app.database.model.Favorite
import com.app.tourism_app.database.model.User

@Database(
    entities = [Review::class, Favorite::class, User::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reviewDao(): ReviewDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tourism_app_db"
                )
                    .fallbackToDestructiveMigration(false)
                    .build().also { INSTANCE = it }
            }
    }
}
