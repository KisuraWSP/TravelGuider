package com.app.tourism_app.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.app.tourism_app.database.model.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Delete
    suspend fun delete(user: User)

    // Reactive list of all users
    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<User>>

    // Suspend single lookup by username
    @Query("SELECT * FROM users WHERE username = :userName LIMIT 1")
    suspend fun getUserByNameSuspend(userName: String): User?

    // Reactive single lookup by username
    @Query("SELECT * FROM users WHERE username = :userName LIMIT 1")
    fun getUserByNameFlow(userName: String): Flow<User?>

    // Suspend lookup for the currently logged-in user (if any)
    @Query("SELECT * FROM users WHERE is_logged_in = 1 LIMIT 1")
    suspend fun getLoggedInUserSuspend(): User?

    // Reactive current logged-in user
    @Query("SELECT * FROM users WHERE is_logged_in = 1 LIMIT 1")
    fun getLoggedInUserFlow(): Flow<User?>

    @Query("UPDATE users SET is_logged_in = 0")
    suspend fun logoutAllUsers()

    @Update
    suspend fun updateUser(user: User)

    // Directly update user's logged-in flag
    @Query("UPDATE users SET is_logged_in = :loggedIn WHERE id = :userId")
    suspend fun updateLoggedIn(userId: Int, loggedIn: Boolean)
}
