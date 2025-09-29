package com.app.tourism_app.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.tourism_app.R
import com.app.tourism_app.activities.PlaceDetailActivity
import com.app.tourism_app.database.data.remote.NetworkModule
import com.app.tourism_app.database.data.ui.LocationsAdapter
import com.app.tourism_app.database.model.LocationUi
import com.app.tourism_app.database.data.remote.NetworkMonitor
// If you've added NetGuards.kt, uncomment these imports and remove the inlined fallbacks below.
import com.app.tourism_app.database.data.remote.requireOnlineOrNull
import com.app.tourism_app.database.data.remote.friendlyNetError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var adapter: LocationsAdapter
    private val api = NetworkModule.provideApiService()
    private var searchJob: Job? = null

    // --- BEGIN: inline fallbacks (delete if using NetGuards.kt) ---
    private fun requireOnlineOrNull(): String? {
        val ctx = context ?: return "You’re offline. Connect to the internet."
        return if (!NetworkMonitor.isOnlineNow(ctx)) "You’re offline. Connect to the internet." else null
    }
    private fun friendlyNetError(t: Throwable): String = when (t) {
        is java.net.UnknownHostException -> "No internet connection."
        is java.net.SocketTimeoutException -> "Network timeout. Try again."
        is retrofit2.HttpException -> "Server error: ${t.code()}"
        else -> t.message ?: "Network error."
    }
    // --- END: inline fallbacks ---

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sv = view.findViewById<SearchView>(R.id.search_view)
        val rv = view.findViewById<RecyclerView>(R.id.rv_search_results)
        val progress = view.findViewById<ProgressBar>(R.id.progress)
        val emptyState = view.findViewById<View>(R.id.empty_state_search)
        val emptyText = view.findViewById<TextView>(R.id.empty_text)

        adapter = LocationsAdapter { loc ->
            val ctx = context ?: return@LocationsAdapter
            val intent = Intent(ctx, PlaceDetailActivity::class.java).apply {
                putExtra("place_id", loc.id)
                putExtra("place_title", loc.title)
                putExtra("place_desc", loc.description ?: "")
                putExtra("place_image", loc.imageUrl)
            }
            startActivity(intent)
        }

        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = adapter

        // Live connectivity: enable/disable search and show a friendly empty state when offline
        viewLifecycleOwner.lifecycleScope.launch {
            NetworkMonitor.isOnline.collectLatest { online ->
                sv.isEnabled = online
                if (!online) {
                    toggleUi(
                        showLoading = false,
                        showEmpty = true,
                        emptyMessage = "You’re offline. Connect to the internet to search.",
                        progress = progress,
                        emptyState = emptyState
                    )
                    adapter.submitList(emptyList())
                } else {
                    // clear the offline message once back online
                    toggleUi(showLoading = false, showEmpty = false, progress = progress, emptyState = emptyState)
                }
            }
        }

        // Debounce search input
        sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val q = query?.trim().orEmpty()
                if (q.isEmpty()) {
                    adapter.submitList(emptyList())
                    toggleUi(showLoading = false, showEmpty = false, progress = progress, emptyState = emptyState)
                    sv.clearFocus()
                    return true
                }
                requireOnlineOrNull()?.let { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    toggleUi(showLoading = false, showEmpty = true, emptyMessage = msg, progress = progress, emptyState = emptyState)
                    return true
                }
                doSearch(q, progress, emptyState)
                sv.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchJob?.cancel()
                val q = newText?.trim().orEmpty()
                if (q.isEmpty()) {
                    adapter.submitList(emptyList())
                    toggleUi(showLoading = false, showEmpty = false, progress = progress, emptyState = emptyState)
                    return true
                }
                requireOnlineOrNull()?.let { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    toggleUi(showLoading = false, showEmpty = true, emptyMessage = msg, progress = progress, emptyState = emptyState)
                    return true
                }
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(300)
                    if (!isActive) return@launch
                    doSearch(q, progress, emptyState)
                }
                return true
            }
        })
    }

    private fun doSearch(
        query: String,
        progress: ProgressBar,
        emptyState: View
    ) {
        val ctx = context ?: return

        requireOnlineOrNull()?.let { msg ->
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
            toggleUi(showLoading = false, showEmpty = true, emptyMessage = msg, progress = progress, emptyState = emptyState)
            return
        }

        toggleUi(showLoading = true, showEmpty = false, progress = progress, emptyState = emptyState)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val list: List<LocationUi> = withContext(Dispatchers.IO) {
                    val filter = "rect:79.6617,5.9180,81.9090,9.8341"
                    val limit = 50
                    val resp = api.getLocations(
                        categories = "tourism.sights",
                        filter = filter,
                        limit = limit,
                        apiKey = "ff8eac3934aa4b74bd1229543e598951",
                        text = query
                    )
                    resp.features.mapNotNull { f ->
                        val p = f.properties ?: return@mapNotNull null
                        val title = p.name ?: p.formatted ?: "Unknown"
                        val desc = buildString {
                            append(p.formatted ?: "")
                            val extra = listOfNotNull(p.city, p.country).filter { it.isNotBlank() }
                            if (extra.isNotEmpty()) {
                                if (isNotEmpty()) append("\n")
                                append(extra.joinToString(", "))
                            }
                        }.trim()
                        val stableId = p.placeId?.hashCode()?.toLong() ?: title.hashCode().toLong()
                        LocationUi(
                            id = stableId,
                            title = title,
                            description = desc,
                            imageUrl = "",
                            reviews = emptyList()
                        )
                    }.distinctBy { it.id }
                }

                withContext(Dispatchers.Main) {
                    if (!isAdded || view == null) return@withContext
                    adapter.submitList(list)
                    toggleUi(
                        showLoading = false,
                        showEmpty = list.isEmpty(),
                        emptyMessage = if (list.isEmpty()) "No results found" else null,
                        progress = progress,
                        emptyState = emptyState
                    )
                }
            } catch (t: Throwable) {
                val msg = friendlyNetError(t)
                Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
                withContext(Dispatchers.Main) {
                    if (!isAdded || view == null) return@withContext
                    adapter.submitList(emptyList())
                    toggleUi(showLoading = false, showEmpty = true, emptyMessage = msg, progress = progress, emptyState = emptyState)
                }
            }
        }
    }

    private fun toggleUi(
        showLoading: Boolean,
        showEmpty: Boolean,
        emptyMessage: String? = null,
        progress: ProgressBar,
        emptyState: View
    ) {
        progress.visibility = if (showLoading) View.VISIBLE else View.GONE
        emptyState.visibility = if (showEmpty) View.VISIBLE else View.GONE
        emptyMessage?.let {
            emptyState.findViewById<TextView>(R.id.empty_text)?.text = it
        }
    }
}
