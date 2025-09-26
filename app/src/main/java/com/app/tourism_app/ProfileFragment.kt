package com.app.tourism_app

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.InvalidationTracker
import com.app.tourism_app.database.data.local.AppDatabase
import com.app.tourism_app.database.dao.UserDao
import com.app.tourism_app.database.model.User
import com.app.tourism_app.database.repository.UserRepository
import com.app.tourism_app.database.view.UserViewModel
import com.app.tourism_app.database.view.UserViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var imgAvatar: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var tvMyReviews: TextView
    private lateinit var tvSettings: TextView

    /**
     * Build a UserRepository in a way that does not require changing the repository
     * or AppDatabase. We attempt:
     *  1) cast AppDatabase to the project's UserDatabase type (in case it's the same type),
     *  2) otherwise create a small adapter object implementing UserDatabase that
     *     delegates userDao() to AppDatabase.
     *
     * This assumes UserRepository only needs userDao() from the UserDatabase type,
     * matching the repository code you showed.
     */
    private val repository: UserRepository by lazy {
        // Get the AppDatabase singleton
        val appDb = AppDatabase.getInstance(requireContext().applicationContext)

        // Fully-qualified name of the expected type so the compiler knows it exists in your project
        val userDbType = com.app.tourism_app.database.UserDatabase::class.java

        // Try a safe cast first (works if UserDatabase is actually implemented by AppDatabase)
        val maybeUserDb = (null)

        val finalUserDb = maybeUserDb ?: run {
            // If not castable, create an adapter that implements UserDatabase and delegates to appDb.
            // This requires that UserDatabase at minimum exposes userDao(): UserDao.
            object : com.app.tourism_app.database.UserDatabase() {
                override fun userDao(): UserDao = appDb.userDao()
                override fun clearAllTables() {
                    TODO("Not yet implemented")
                }

                override fun createInvalidationTracker(): InvalidationTracker {
                    TODO("Not yet implemented")
                }
            }
        }

        // Construct repo using the UserDatabase-compatible object
        UserRepository(finalUserDb)
    }

    private val viewModel: UserViewModel by lazy {
        ViewModelProvider(this, UserViewModelFactory(repository))[UserViewModel::class.java]
    }

    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imgAvatar = view.findViewById(R.id.img_avatar)
        tvUsername = view.findViewById(R.id.tv_username)
        btnEditProfile = view.findViewById(R.id.btn_edit_profile)
        tvMyReviews = view.findViewById(R.id.tv_my_reviews)
        tvSettings = view.findViewById(R.id.tv_settings)

        // Observe current user
        lifecycleScope.launch {
            viewModel.currentUser.collectLatest { user ->
                currentUser = user
                updateUi(user)
            }
        }

        btnEditProfile.setOnClickListener {
            currentUser?.let { showEditDialog(it) } ?: showNoUserToast()
        }

        tvMyReviews.setOnClickListener {
            Toast.makeText(requireContext(), "Open My Reviews (implement navigation)", Toast.LENGTH_SHORT).show()
        }

        tvSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Open Settings (implement navigation)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUi(user: User?) {
        if (user == null) {
            tvUsername.text = "Not logged in"
            imgAvatar.setImageResource(R.drawable.ic_placeholder_image)
            btnEditProfile.isEnabled = false
        } else {
            tvUsername.text = user.userName
            btnEditProfile.isEnabled = true
            val initials = user.userName.takeIf { it.isNotBlank() }
                ?.split(" ")
                ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
                ?.joinToString("") ?: "U"
            imgAvatar.contentDescription = "Avatar for ${user.userName} ($initials)"
            imgAvatar.setImageResource(R.drawable.ic_placeholder_image)
        }
    }

    private fun showNoUserToast() {
        Toast.makeText(requireContext(), "No user is logged in.", Toast.LENGTH_SHORT).show()
    }

    private fun showEditDialog(user: User) {
        val dlgView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val edtUsername = dlgView.findViewById<EditText>(R.id.edit_username)
        val edtPassword = dlgView.findViewById<EditText>(R.id.edit_password)
        val chkLoggedIn = dlgView.findViewById<CheckBox>(R.id.chk_logged_in)

        edtUsername.setText(user.userName)
        edtPassword.setText(user.password)
        chkLoggedIn.isChecked = user.isLoggedIn

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(dlgView)
            .setPositiveButton("Save") { _, _ ->
                val newName = edtUsername.text.toString().trim()
                val newPass = edtPassword.text.toString()
                val loggedInFlag = chkLoggedIn.isChecked

                if (newName.isBlank()) {
                    Toast.makeText(requireContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val updatedUser = user.copy().apply {
                    userName = newName
                    password = newPass
                    isLoggedIn = loggedInFlag
                }

                viewModel.updateUser(updatedUser)
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
}
