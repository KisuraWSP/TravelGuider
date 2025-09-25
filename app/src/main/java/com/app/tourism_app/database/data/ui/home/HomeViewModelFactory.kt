package com.app.tourism_app.database.data.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.tourism_app.database.data.local.AppDatabase
import com.app.tourism_app.database.data.remote.NetworkModule
import com.app.tourism_app.database.repository.Repository

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val api = NetworkModule.provideApiService()
        val db = AppDatabase.getInstance(context)
        val repo = Repository(api, db.reviewDao())
        @Suppress("UNCHECKED_CAST")
        return HomeViewModel(repo) as T
    }
}