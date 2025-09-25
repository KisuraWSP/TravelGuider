package com.app.tourism_app

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.tourism_app.database.data.ui.home.HomeViewModel
import com.app.tourism_app.database.data.ui.home.HomeViewModelFactory
import com.app.tourism_app.database.data.ui.home.LocationsAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: LocationsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize adapter
        adapter = LocationsAdapter { loc ->
            Log.d("HomeFragment", "Clicked: ${loc.title}")
        }

        // Find RecyclerView and attach adapter + layout manager
        val rv = view.findViewById<RecyclerView>(R.id.rv_recommendations)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // Initialize ViewModel
        val factory = HomeViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        // Collect locations safely with repeatOnLifecycle
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.locations.collect { list ->
                    Log.d("HomeFragment", "Got ${list.size} locations")
                    adapter.submitList(list)
                }
            }
        }
    }
}