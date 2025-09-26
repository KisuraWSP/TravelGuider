package com.app.tourism_app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val homeFragment = HomeFragment()
    private val searchFragment = SearchFragment()
    private val profileFragment = ProfileFragment()
    private val mapFragment = MapFragment()
    private val favoritesFragment = FavoritesFragment()

    private var activeFragment: Fragment = homeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val container: View = findViewById(R.id.main_container)
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav)
        val appBar: View? = findViewById(R.id.appbar)

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
            .add(R.id.main_container, searchFragment, "search").hide(searchFragment)
            .add(R.id.main_container, homeFragment, "home")
            .commit()

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home      -> switchFragment(homeFragment)
                R.id.nav_search    -> switchFragment(searchFragment)
                R.id.nav_profile   -> switchFragment(profileFragment)
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
