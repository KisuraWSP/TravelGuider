package com.app.tourism_app.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.app.tourism_app.R
import com.app.tourism_app.fragments.FavoritesFragment
import com.app.tourism_app.fragments.GuestInfoFragment
import com.app.tourism_app.fragments.HomeFragment
import com.app.tourism_app.fragments.MapFragment
import com.app.tourism_app.fragments.ProfileFragment
import com.app.tourism_app.fragments.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val homeFragment = HomeFragment()
    private val searchFragment = SearchFragment()
    private val profileFragment = ProfileFragment()
    private val mapFragment = MapFragment()
    private val favoritesFragment = FavoritesFragment()
    private val guestInfoFragment = GuestInfoFragment() // <-- added

    private var activeFragment: Fragment = homeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val container: View = findViewById(R.id.main_container)
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav)
        val appBar: View? = findViewById(R.id.appbar)

        // Read the guest flag (comes from LoginActivity when Guest Login is used)
        val isGuest = intent.getBooleanExtra("isGuest", false)

        // Pad only the appbar for status bar
        appBar?.let { bar ->
            ViewCompat.setOnApplyWindowInsetsListener(bar) { v, insets ->
                val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                v.updatePadding(top = top)
                insets
            }
        }

        // Let the container handle bottom insets (keyboard + nav bar)
        ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())

            val bottomInset = if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                ime.bottom   // keyboard open
            } else {
                0            // no extra padding when keyboard closed
            }

            val topInset = if (appBar == null) sys.top else 0

            v.updatePadding(top = topInset, bottom = bottomInset)
            insets
        }

        // --- fragment setup ---
        supportFragmentManager.beginTransaction()
            .add(R.id.main_container, favoritesFragment, "favorites").hide(favoritesFragment)
            .add(R.id.main_container, mapFragment, "map").hide(mapFragment)
            .add(R.id.main_container, profileFragment, "profile").hide(profileFragment)
            .add(R.id.main_container, guestInfoFragment, "guestinfo").hide(guestInfoFragment) // <-- added
            .add(R.id.main_container, searchFragment, "search").hide(searchFragment)
            .add(R.id.main_container, homeFragment, "home")
            .commit()

        // Optional: hint that profile is limited in guest mode
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
                R.id.nav_profile   -> {
                    if (isGuest) switchFragment(guestInfoFragment)
                    else switchFragment(profileFragment)
                }
                R.id.nav_map       -> switchFragment(mapFragment)
                R.id.nav_favorites -> switchFragment(favoritesFragment)
                else -> return@setOnItemSelectedListener false
            }
            true
        }
    }

    private fun switchFragment(target: Fragment): Boolean {
        if (activeFragment != target) {
            supportFragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit()
            activeFragment = target
        }
        return true
    }
}