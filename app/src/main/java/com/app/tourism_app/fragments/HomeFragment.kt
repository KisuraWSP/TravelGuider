package com.app.tourism_app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.tourism_app.R
import com.app.tourism_app.activities.PlaceDetailActivity
import com.app.tourism_app.database.data.ui.LocationsAdapter
import com.app.tourism_app.database.view.HomeViewModel
import com.app.tourism_app.database.view.HomeViewModelFactory
import com.app.tourism_app.database.data.remote.NetworkMonitor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: LocationsAdapter

    private lateinit var rv: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var emptyState: View
    private lateinit var emptyText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- views ---
        rv = view.findViewById(R.id.rv_recommendations)
        progress = view.findViewById(R.id.progress_home)
        emptyState = view.findViewById(R.id.empty_state_home)
        emptyText = emptyState.findViewById(R.id.empty_text)

        // --- adapter ---
        adapter = LocationsAdapter { loc ->
            val ctx = context ?: return@LocationsAdapter
            startActivity(
                Intent(ctx, PlaceDetailActivity::class.java).apply {
                    putExtra("place_title", loc.title)
                    putExtra("place_desc", loc.description)
                    putExtra("place_image", loc.imageUrl)
                    putExtra("place_id",   loc.id)
                }
            )
        }
        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = adapter

        // --- viewmodel (needs Application) ---
        val app = requireActivity().application
        val factory = HomeViewModelFactory(app)
        viewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        // Show spinner until we get a first emission (online or not)
        showLoading(true)
        showEmpty(false)

        // 1) Live connectivity: if we go offline, surface it and clear list (no crash)
        viewLifecycleOwner.lifecycleScope.launch {
            NetworkMonitor.isOnline.collectLatest { online ->
                if (!online) {
                    val ctx = context ?: return@collectLatest
                    Toast.makeText(ctx, "You’re offline. Connect to the internet to load home content.", Toast.LENGTH_SHORT).show()
                    adapter.submitList(emptyList())
                    emptyText.text = "You’re offline. Connect to the internet to load home content."
                    showLoading(false)
                    showEmpty(true)
                } else {
                    if (adapter.itemCount > 0) showEmpty(false)
                }
            }
        }

        // 2) Collect the locations flow safely (view lifecycle)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.locations.collect { list ->
                    showLoading(false)
                    adapter.submitList(list)
                    showEmpty(list.isEmpty())
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progress.visibility = if (show) View.VISIBLE else View.GONE
        rv.alpha = if (show) 0.5f else 1f
    }

    private fun showEmpty(show: Boolean) {
        emptyState.visibility = if (show) View.VISIBLE else View.GONE
        rv.visibility = if (show) View.INVISIBLE else View.VISIBLE
    }
}
