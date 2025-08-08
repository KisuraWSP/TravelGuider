package com.app.tourism_app.database.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.app.tourism_app.database.model.User
import com.app.tourism_app.database.repository.UserRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {
    @OptIn(DelicateCoroutinesApi::class)
    fun insert(user : User) = GlobalScope.launch {
        repository.insert(user)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun delete(user : User) = GlobalScope.launch {
        repository.delete(user)
    }

    fun allUsers(): LiveData<List<User>> = repository.allUsers()

    fun getUserByName(userName: String): LiveData<User?> {
        return repository.getUserByName(userName)
    }
}