package com.app.tourism_app.database.repository

import androidx.lifecycle.LiveData
import com.app.tourism_app.database.UserDatabase
import com.app.tourism_app.database.model.User

class UserRepository(private val db: UserDatabase) {
    suspend fun insert(user : User) = db.getUserDao().insert(user)
    suspend fun delete(user : User) = db.getUserDao().delete(user)

    fun allUsers() = db.getUserDao().getAllUsers()
    fun getUserByName(userName: String): LiveData<User?> {
        return db.getUserDao().getUserByName(userName)
    }
}