package com.app.tourism_app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.tourism_app.activities.PlaceDetailActivity
import com.app.tourism_app.R
import com.app.tourism_app.database.data.local.AppDatabase
import com.app.tourism_app.database.data.remote.NetworkModule
import com.app.tourism_app.database.data.ui.FavoriteAdapter
import com.app.tourism_app.database.repository.Repository
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private lateinit var adapter: FavoriteAdapter
    private lateinit var repository: Repository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rv_favorites)
        val db = AppDatabase.Companion.getInstance(requireContext())
        repository = Repository(NetworkModule.provideApiService(), db.reviewDao(), db.favoriteDao())

        adapter = FavoriteAdapter { fav ->
            // open PlaceDetailActivity for this favorite
            val intent = Intent(requireContext(), PlaceDetailActivity::class.java).apply {
                putExtra("place_id", fav.placeId)
                putExtra("place_title", fav.title)
                putExtra("place_desc", fav.description)
                putExtra("place_image", fav.imageUrl)
            }
            startActivity(intent)
        }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // collect favorites
        viewLifecycleOwner.lifecycleScope.launch {
            repository.favoritesFlow().collect { list ->
                adapter.submitList(list)
            }
        }
    }
}