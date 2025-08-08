package com.app.tourism_app.database.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.tourism_app.database.repository.UserRepository

class UserViewModelFactory(private val repository: UserRepository): ViewModelProvider.NewInstanceFactory()  {
    override fun <T : ViewModel> create(modelClass: Class<T>): T{
        return UserViewModel(repository) as T
    }
}