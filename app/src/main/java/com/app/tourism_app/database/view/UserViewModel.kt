package com.app.tourism_app.database.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tourism_app.database.model.User
import com.app.tourism_app.database.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    // Reactive currently logged-in user; updates automatically when DB login flag changes.
    val currentUser: StateFlow<User?> = repository
        .getLoggedInUserFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Reactive list of all users (useful for admin screens / debug)
    val allUsers: StateFlow<List<User>> = repository
        .allUsersFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- Simple suspend wrappers that run on viewModelScope for UI callers ---
    fun insert(user: User) {
        viewModelScope.launch { repository.insert(user) }
    }

    fun delete(user: User) {
        viewModelScope.launch { repository.delete(user) }
    }

    /**
     * Login helpers:
     * - loginById: pass an existing user id
     * - loginUser: pass a User instance (must have non-null id)
     */
    fun loginById(userId: Int) {
        viewModelScope.launch { repository.loginById(userId) }
    }

    fun loginUser(user: User) {
        viewModelScope.launch { repository.loginUser(user) }
    }

    fun logoutCurrentUser() {
        viewModelScope.launch { repository.logoutCurrentUser() }
    }

    fun logoutUser(user: User) {
        viewModelScope.launch { repository.logoutUser(user) }
    }

    fun updateUser(user: User) {
        viewModelScope.launch { repository.updateUser(user) }
    }

    // One-off suspend lookup (call from a coroutine)
    suspend fun getUserByNameSuspend(username: String): User? =
        repository.getUserByName(username)
}