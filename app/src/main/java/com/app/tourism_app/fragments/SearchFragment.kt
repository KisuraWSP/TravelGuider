package com.app.tourism_app.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.tourism_app.activities.PlaceDetailActivity
import com.app.tourism_app.R
import com.app.tourism_app.database.data.remote.NetworkModule
import com.app.tourism_app.database.model.LocationUi
import com.app.tourism_app.database.data.ui.LocationsAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var adapter: LocationsAdapter
    private val api = NetworkModule.provideApiService()
    private var searchJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sv = view.findViewById<SearchView>(R.id.search_view)
        val rv = view.findViewById<RecyclerView>(R.id.rv_search_results)

        adapter = LocationsAdapter { loc ->
            // open PlaceDetailActivity with extras
            val intent = Intent(requireContext(), PlaceDetailActivity::class.java).apply {
                putExtra("place_id", loc.id)
                putExtra("place_title", loc.title)
                putExtra("place_desc", loc.description ?: "")
                putExtra("place_image", loc.imageUrl)
            }
            startActivity(intent)
        }

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // Optional: prefill or show popular / default results if you want
        // otherwise the list will be blank until the user types

        // Debounce search input:
        sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { doSearch(it.trim()) }
                sv.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // debounce typing: cancel previous job and launch delayed search
                searchJob?.cancel()
                val q = newText?.trim()
                if (q.isNullOrEmpty()) {
                    adapter.submitList(emptyList())
                    return true
                }
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    // small debounce (300ms)
                    delay(300)
                    if (!isActive) return@launch
                    doSearch(q)
                }
                return true
            }
        })
    }

    private fun doSearch(query: String) {
        // run network call safely in coroutine
        viewLifecycleOwner.lifecycleScope.launch {
            // optional: show loading UI (not included here)
            try {
                // Example bounding box for Sri Lanka (your filter earlier) — adjust if needed
                val filter = "rect:79.6617,5.9180,81.9090,9.8341"
                val limit = 50

                val resp = api.getLocations(
                    categories = "tourism.sights",
                    filter = filter,
                    limit = limit,
                    apiKey = "ff8eac3934aa4b74bd1229543e598951",
                    text = query // our new text search param
                )

                val list = resp.features
                    .mapNotNull { f ->
                        val p = f.properties ?: return@mapNotNull null   // <- guard null

                        val title = p.name ?: p.formatted ?: "Unknown"

                        val desc = buildString {
                            append(p.formatted ?: "")
                            val extra = listOfNotNull(p.city, p.country)
                                .filter { it.isNotBlank() }
                            if (extra.isNotEmpty()) {
                                if (isNotEmpty()) append("\n")
                                append(extra.joinToString(", "))
                            }
                        }.trim()

                        val stableId = p.placeId?.hashCode()?.toLong()
                            ?: title.hashCode().toLong()

                        LocationUi(
                            id = stableId,
                            title = title,
                            description = desc,
                            imageUrl = "",          // Geoapify doesn't provide images
                            reviews = emptyList()
                        )
                    }
                    .distinctBy { it.id }


                Log.d("SearchFragment", "Search '$query' -> ${list.size} results")
                adapter.submitList(list)
            } catch (t: Throwable) {
                t.printStackTrace()
                // show simple error (you can show a Snackbar/Toast/UI)
                // Toast.makeText(requireContext(), "Search failed: ${t.message}", Toast.LENGTH_SHORT).show()
                adapter.submitList(emptyList())
            } finally {
                // hide loading UI if you had one
            }
        }
    }
}