package com.app.tourism_app.database.repository

import android.content.Context
import com.app.tourism_app.database.UserDatabase
import com.app.tourism_app.database.dao.UserDao
import com.app.tourism_app.database.data.local.AppDatabase
import com.app.tourism_app.database.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val db: UserDatabase) {

    private val dao = db.userDao()

    // Basic operations (suspend)
    suspend fun insert(user: User) = dao.insert(user)
    suspend fun delete(user: User) = dao.delete(user)
    suspend fun getUserByName(username: String): User? = dao.getUserByNameSuspend(username)

    // Reactive and suspend access to logged-in user
    suspend fun getLoggedInUser(): User? = dao.getLoggedInUserSuspend()
    fun getLoggedInUserFlow(): Flow<User?> = dao.getLoggedInUserFlow()

    // Reactive all users
    fun allUsersFlow(): Flow<List<User>> = dao.getAllUsersFlow()

    // Login / logout helpers
    // Logs out everyone then marks the provided user id as logged in
    suspend fun loginById(userId: Int) {
        dao.logoutAllUsers()
        dao.updateLoggedIn(userId, true)
    }

    // Pass the full user object to login (useful when you already have it)
    // This will logout others and mark given user as logged-in
    suspend fun loginUser(user: User) {
        dao.logoutAllUsers()
        val id = user.id ?: throw IllegalArgumentException("user id required to login")
        dao.updateLoggedIn(id, true)
    }

    suspend fun logoutCurrentUser() {
        dao.logoutAllUsers()
    }

    suspend fun logoutUser(user: User) {
        val id = user.id ?: return
        dao.updateLoggedIn(id, false)
    }

    // Utility: update user row
    suspend fun updateUser(user: User) = dao.updateUser(user)

}
