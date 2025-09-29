package com.app.tourism_app.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.app.tourism_app.R
import com.app.tourism_app.fragments.FavoritesFragment
import com.app.tourism_app.fragments.GuestInfoFragment
import com.app.tourism_app.fragments.HomeFragment
import com.app.tourism_app.fragments.MapFragment
import com.app.tourism_app.fragments.ProfileFragment
import com.app.tourism_app.fragments.SearchFragment
import com.app.tourism_app.database.data.remote.NetworkMonitor
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val homeFragment = HomeFragment()
    private val searchFragment = SearchFragment()
    private val profileFragment = ProfileFragment()
    private val mapFragment = MapFragment()
    private val favoritesFragment = FavoritesFragment()
    private val guestInfoFragment = GuestInfoFragment()

    private var activeFragment: Fragment = homeFragment
    private var offlineBar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // We’ll handle system bars ourselves
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val root: View = findViewById(R.id.main_root)
        val container: View = findViewById(R.id.main_container)   // stays 651dp (from XML)
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav)
        val appBar: View? = findViewById(R.id.appbar)

        // Start network monitoring once per process
        NetworkMonitor.start(applicationContext)

        val isGuest = intent.getBooleanExtra("isGuest", false)

        // Status bar -> add top padding to AppBar
        appBar?.let { bar ->
            ViewCompat.setOnApplyWindowInsetsListener(bar) { v, insets ->
                val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                v.updatePadding(top = top)
                insets
            }
        }

        // Nav bar -> add bottom padding to BottomNav itself
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { v, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            v.updatePadding(bottom = sb)
            insets
        }

        // Keep 651dp container visually above BottomNav; lift for keyboard when visible
        ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val bnHeight = bottomNav.height

            // When keyboard is open → use IME height; otherwise use BottomNav height.
            val bottomPad = if (imeVisible && imeInsets > 0) imeInsets else bnHeight

            // If you ever remove the AppBar, also apply status bar top padding to container here.
            v.updatePadding(bottom = bottomPad)
            insets
        }

        // Fragment setup (show/hide pattern)
        supportFragmentManager.beginTransaction()
            .add(R.id.main_container, favoritesFragment, "favorites").hide(favoritesFragment)
            .add(R.id.main_container, mapFragment, "map").hide(mapFragment)
            .add(R.id.main_container, profileFragment, "profile").hide(profileFragment)
            .add(R.id.main_container, guestInfoFragment, "guestinfo").hide(guestInfoFragment)
            .add(R.id.main_container, searchFragment, "search").hide(searchFragment)
            .add(R.id.main_container, homeFragment, "home")
            .commit()
        activeFragment = homeFragment

        // Badge profile when guest; clicking Profile shows GuestInfo
        if (isGuest) {
            bottomNav.getOrCreateBadge(R.id.nav_profile).apply {
                isVisible = true
                number = 0
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home      -> switchFragment(homeFragment)
                R.id.nav_search    -> switchFragment(searchFragment)
                R.id.nav_profile   -> if (isGuest) switchFragment(guestInfoFragment) else switchFragment(profileFragment)
                R.id.nav_map       -> switchFragment(mapFragment)
                R.id.nav_favorites -> switchFragment(favoritesFragment)
                else -> return@setOnItemSelectedListener false
            }
            true
        }

        // Offline banner with shortcut to Settings
        lifecycleScope.launch {
            NetworkMonitor.isOnline.collectLatest { online ->
                if (!online) {
                    if (offlineBar?.isShown == true) return@collectLatest
                    offlineBar = Snackbar.make(root, "No internet — some features are disabled", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Settings") {
                            startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                        }
                    offlineBar?.show()
                } else {
                    offlineBar?.dismiss()
                }
            }
        }
    }

    private fun switchFragment(target: Fragment): Boolean {
        if (activeFragment === target) return true
        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .show(target)
            .commit()
        activeFragment = target
        return true
    }
}
