package com.app.tourism_app.database.repository

import android.util.Log
import com.app.tourism_app.database.dao.FavoriteDao
import com.app.tourism_app.database.dao.ReviewDao
import com.app.tourism_app.database.data.remote.ApiService
import com.app.tourism_app.database.data.remote.Feature
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

    /**
     * Fetch remote locations (Geoapify) once and merge with live reviews from Room.
     * - Uses a stable key (placeId when available) to dedupe.
     * - Maps name/formatted into title/description.
     * - Geoapify doesn't provide images -> imageUrl left empty.
     */
    fun locationsWithReviews(): Flow<List<LocationUi>> {
        val remoteFlow: Flow<List<LocationUi>> = flow {
            val remote = api.getLocations(
                categories = "tourism.sights",
                filter = "rect:79.6617,5.9180,81.9090,9.8341",
                limit = 50,
                apiKey = "ff8eac3934aa4b74bd1229543e598951"
            ).features
            Log.d("Repository", "Fetched ${remote.size} locations")

            val unique: List<Feature> = remote
                .filter { it.properties != null }
                .distinctBy { f ->
                    val p = f.properties!!
                    p.placeId ?: "${p.name}|${p.formatted}"
                }

            val initialList: List<LocationUi> = unique.mapNotNull { f ->
                val p = f.properties ?: return@mapNotNull null

                val title = p.name ?: p.formatted ?: "Unknown"

                val description = buildString {
                    append(p.formatted ?: "")
                    val extra = listOfNotNull(p.city, p.country)
                        .filter { it.isNotBlank() }
                    if (extra.isNotEmpty()) {
                        if (isNotEmpty()) append("\n")
                        append(extra.joinToString(", "))
                    }
                }.trim()

                val id = (p.placeId?.hashCode()?.toLong())
                    ?: title.hashCode().toLong()

                LocationUi(
                    id = id,
                    title = title,
                    description = description,
                    imageUrl = "",       // Geoapify doesn't provide images
                    reviews = emptyList()
                )
            }

            emit(initialList)
        }

        // Merge with live review stream
        return remoteFlow.combine(reviewDao.allReviews()) { locations, reviews ->
            val reviewsByLocation = reviews.groupBy { it.locationId }
            locations.map { loc ->
                loc.copy(reviews = reviewsByLocation[loc.id] ?: emptyList())
            }
        }
    }

    // --- Reviews ---
    suspend fun addReview(review: Review): Long = reviewDao.insert(review)
    fun reviewsForLocation(locationId: Long): Flow<List<Review>> = reviewDao.reviewsForLocation(locationId)

    // --- Favorites ---
    suspend fun addFavorite(fav: Favorite) = favoriteDao.insert(fav)
    suspend fun removeFavorite(fav: Favorite) = favoriteDao.delete(fav)
    fun favoritesFlow(): Flow<List<Favorite>> = favoriteDao.getAllFavorites()
    suspend fun isFavorite(placeId: Long): Boolean = favoriteDao.isFavorite(placeId)
    suspend fun getFavoriteById(placeId: Long): Favorite? = favoriteDao.getFavoriteById(placeId)
}
