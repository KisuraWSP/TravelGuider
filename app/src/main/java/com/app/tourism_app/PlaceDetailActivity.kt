package com.app.tourism_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.tourism_app.database.UserDatabase
import com.app.tourism_app.database.data.local.AppDatabase
import com.app.tourism_app.database.data.ui.ReviewAdapter
import com.app.tourism_app.database.repository.Repository
import com.app.tourism_app.database.repository.UserRepository
import com.bumptech.glide.Glide
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PlaceDetailActivity : AppCompatActivity() {

    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var repository: Repository
    private lateinit var userRepository: UserRepository
    private lateinit var userViewModel: com.app.tourism_app.database.view.UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.place_detail_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Views
        val tvTitle = findViewById<TextView>(R.id.tv_place_title)
        val tvDesc = findViewById<TextView>(R.id.tv_place_desc)
        val imgPlace = findViewById<ImageView>(R.id.img_place)
        val btnWriteReview = findViewById<Button>(R.id.btn_write_review)
        val rvReviews = findViewById<RecyclerView>(R.id.rv_reviews)

        // Repos & ViewModel
        val reviewDao = AppDatabase.getInstance(this).reviewDao()
        repository = Repository(
            api = com.app.tourism_app.database.data.remote.NetworkModule.provideApiService(),
            reviewDao = reviewDao
        )

        val userDb = UserDatabase.getInstance(this)
        userRepository = UserRepository(userDb)
        val userFactory = com.app.tourism_app.database.view.UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, userFactory).get(com.app.tourism_app.database.view.UserViewModel::class.java)

        // Reviews Recycler
        reviewAdapter = ReviewAdapter()
        rvReviews.adapter = reviewAdapter
        rvReviews.layoutManager = LinearLayoutManager(this)

        // Intent extras
        val placeId = intent.getLongExtra("place_id", 0L)
        val title = intent.getStringExtra("place_title") ?: "Unknown"
        val desc = intent.getStringExtra("place_desc") ?: ""
        val imageUrl = intent.getStringExtra("place_image")

        tvTitle.text = title
        tvDesc.text = desc
        imageUrl?.let { Glide.with(this).load(it).centerCrop().into(imgPlace) }

        // Collect reviews for this place (live)
        lifecycleScope.launch {
            repository.reviewsForLocation(placeId).collect { reviews ->
                reviewAdapter.submitList(reviews)
            }
        }

        btnWriteReview.setOnClickListener {
            // short-lived check: get currently logged-in user from the state flow
            lifecycleScope.launch {
                val currentUser = userViewModel.currentUser.first()
                if (currentUser != null) {
                    startActivity(Intent(this@PlaceDetailActivity, WriteReviewActivity::class.java).apply {
                        putExtra("place_id", placeId)
                        putExtra("place_title", title)
                        putExtra("place_desc", desc)
                        putExtra("place_image", imageUrl)
                    })
                } else {
                    Toast.makeText(this@PlaceDetailActivity, "You must be logged in to write a review", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
