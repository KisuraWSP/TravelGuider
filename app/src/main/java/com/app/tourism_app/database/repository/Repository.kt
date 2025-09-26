package com.app.tourism_app.database.repository

import android.util.Log
import com.app.tourism_app.database.dao.FavoriteDao
import com.app.tourism_app.database.dao.ReviewDao
import com.app.tourism_app.database.data.remote.ApiService
import com.app.tourism_app.database.data.ui.LocationUi
import com.app.tourism_app.database.model.Favorite
import com.app.tourism_app.database.model.Review
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

class Repository(
    private val api: ApiService,
    private val reviewDao: ReviewDao,
    private val favoriteDao: FavoriteDao
) {

    // Fetch remote locations once and merge with reviews dynamically
    fun locationsWithReviews(): Flow<List<LocationUi>> {
        val remoteFlow: Flow<List<LocationUi>> = flow {
            val remoteLocations = api.getLocations(
                categories = "tourism.sights",
                filter = "rect:79.6617,5.9180,81.9090,9.8341", // Sri Lanka bounding box
                limit = 50,
                apiKey = "ff8eac3934aa4b74bd1229543e598951"
            ).features
            Log.d("Repository", "Fetched ${remoteLocations.size} locations")

            // Deduplicate by name
            val uniqueLocations = remoteLocations.distinctBy { it.properties.name }

            // Map DTOs to UI model (reviews empty initially)
            val initialList = uniqueLocations.map { dto ->
                LocationUi(
                    id = dto.properties.hashCode().toLong(),
                    title = dto.properties.name ?: "Unknown",
                    description = dto.properties.description ?: "",
                    imageUrl = dto.properties.imageUrl ?: "",
                    reviews = emptyList()
                )
            }

            emit(initialList)
        }

        // Combine remote locations with live reviews
        return remoteFlow.combine(reviewDao.allReviews()) { locations, reviews ->
            val reviewsByLocation = reviews.groupBy { it.locationId }
            locations.map { loc ->
                loc.copy(
                    reviews = reviewsByLocation[loc.id] ?: emptyList()
                )
            }
        }
    }

    suspend fun addReview(review: Review): Long {
        return reviewDao.insert(review)
    }

    fun reviewsForLocation(locationId: Long): Flow<List<Review>> =
        reviewDao.reviewsForLocation(locationId)

    // Favorites
    suspend fun addFavorite(fav: Favorite) = favoriteDao.insert(fav)
    suspend fun removeFavorite(fav: Favorite) = favoriteDao.delete(fav)
    fun favoritesFlow(): Flow<List<Favorite>> = favoriteDao.getAllFavorites()
    suspend fun isFavorite(placeId: Long): Boolean = favoriteDao.isFavorite(placeId)
    suspend fun getFavoriteById(placeId: Long): Favorite? = favoriteDao.getFavoriteById(placeId)
}
