package com.app.tourism_app.database.view

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.tourism_app.database.data.remote.NetworkModule
import com.app.tourism_app.database.model.LocationUi
import com.app.tourism_app.database.data.remote.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val _locations = MutableStateFlow<List<LocationUi>>(emptyList())
    val locations: StateFlow<List<LocationUi>> = _locations

    private val api = NetworkModule.provideApiService()

    init {
        // react to connectivity and (re)load when back online
        viewModelScope.launch {
            NetworkMonitor.isOnline.collectLatest { online ->
                if (!online) return@collectLatest
                // brief debounce so rapid flips don't spam the API
                delay(200)
                safeLoad()
            }
        }

        // initial load as well
        viewModelScope.launch { safeLoad() }
    }

    private suspend fun safeLoad() {
        val ctx = getApplication<Application>()
        if (!NetworkMonitor.isOnlineNow(ctx)) {
            // stay quiet; UI handles offline message
            return
        }

        try {
            val list = withContext(Dispatchers.IO) {
                // TODO: replace with your real endpoint(s)
                val resp = api.getLocations(
                    categories = "tourism.sights",
                    filter = "rect:79.6617,5.9180,81.9090,9.8341",
                    limit = 30,
                    apiKey = "ff8eac3934aa4b74bd1229543e598951"
                )
                resp.features.mapNotNull { f ->
                    val p = f.properties ?: return@mapNotNull null
                    val title = p.name ?: p.formatted ?: "Unknown"
                    val desc = listOfNotNull(p.formatted, p.city, p.country)
                        .filter { it.isNotBlank() }
                        .distinct()
                        .joinToString("\n")
                    val id = p.placeId?.hashCode()?.toLong() ?: title.hashCode().toLong()
                    LocationUi(id = id, title = title, description = desc, imageUrl = "", reviews = emptyList())
                }.distinctBy { it.id }
            }
            _locations.value = list
        } catch (t: Throwable) {
            // swallow & keep last good value; never crash UI
            when (t) {
                is UnknownHostException,
                is SocketTimeoutException,
                is HttpException -> Unit
                else -> Unit
            }
        }
    }
}
