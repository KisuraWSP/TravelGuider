package com.app.tourism_app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.app.tourism_app.database.UserDatabase
import com.app.tourism_app.database.data.local.AppDatabase
import com.app.tourism_app.database.model.Review
import com.app.tourism_app.database.repository.Repository
import com.app.tourism_app.database.repository.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WriteReviewActivity : AppCompatActivity() {

    private lateinit var repository: Repository
    private lateinit var userRepository: UserRepository
    private lateinit var userViewModel: com.app.tourism_app.database.view.UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_review)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.write_review_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val ratingBar = findViewById<RatingBar>(R.id.rating_bar)
        val etReview = findViewById<EditText>(R.id.et_review)
        val btnSubmit = findViewById<Button>(R.id.btn_submit_review)
        val placeId = intent.getLongExtra("place_id", 0L)

        // Repo + ViewModel
        val reviewDao = AppDatabase.getInstance(this).reviewDao()
        repository = Repository(
            api = com.app.tourism_app.database.data.remote.NetworkModule.provideApiService(),
            reviewDao = reviewDao
        )

        val userDb = UserDatabase.getInstance(this)
        userRepository = UserRepository(userDb)
        val userFactory = com.app.tourism_app.database.view.UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, userFactory).get(com.app.tourism_app.database.view.UserViewModel::class.java)

        btnSubmit.setOnClickListener {
            val rating = ratingBar.rating.toInt()
            val comment = etReview.text.toString().trim()
            if (comment.isEmpty()) {
                Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                // get current logged-in user from the reactive StateFlow (first() is suspend)
                val currentUser = userViewModel.currentUser.first()
                if (currentUser != null) {
                    val review = Review(
                        locationId = placeId,
                        userId = currentUser.userName, // use the actual logged-in username
                        rating = rating,
                        comment = comment
                    )
                    repository.addReview(review)
                    Toast.makeText(this@WriteReviewActivity, "Review added!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@WriteReviewActivity, "User not logged in", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
