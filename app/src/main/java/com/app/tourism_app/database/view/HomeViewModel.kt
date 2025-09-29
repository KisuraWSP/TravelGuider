package com.app.tourism_app.database.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tourism_app.database.model.LocationUi
import com.app.tourism_app.database.model.Review
import com.app.tourism_app.database.repository.Repository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: Repository) : ViewModel() {

    // Expose a StateFlow of locations that always has 20 items immediately
    val locations: StateFlow<List<LocationUi>> = repo.locationsWithReviews()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.Lazily,
            initialValue = emptyList() // RecyclerView sees empty list initially
        )

    // Add a review safely
    fun addReview(locationId: Long, userId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            val review = Review(
                locationId = locationId,
                userId = userId,
                rating = rating,
                comment = comment
            )
            repo.addReview(review)
        }
    }
}