package com.app.tourism_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        // Add all fragments, show only the default one
        supportFragmentManager.beginTransaction()
            .add(R.id.main_container, favoritesFragment, "favorites").hide(favoritesFragment)
            .add(R.id.main_container, mapFragment, "map").hide(mapFragment)
            .add(R.id.main_container, profileFragment, "profile").hide(profileFragment)
            .add(R.id.main_container, searchFragment, "search").hide(searchFragment)
            .add(R.id.main_container, homeFragment, "home")
            .commit()

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> switchFragment(homeFragment)
                R.id.nav_search -> switchFragment(searchFragment)
                R.id.nav_profile -> switchFragment(profileFragment)
                R.id.nav_map -> switchFragment(mapFragment)
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