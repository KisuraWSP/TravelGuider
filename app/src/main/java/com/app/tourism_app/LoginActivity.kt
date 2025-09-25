package com.app.tourism_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.app.tourism_app.database.UserDatabase
import com.app.tourism_app.database.repository.UserRepository
import com.app.tourism_app.database.view.UserViewModel
import com.app.tourism_app.database.view.UserViewModelFactory
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var loginBtn: Button
    private lateinit var createUserBtn: Button
    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText

    private lateinit var userViewModel: UserViewModel
    private lateinit var userRepo: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // UI
        loginBtn = findViewById(R.id.login_btn)
        createUserBtn = findViewById(R.id.create_user_btn)
        usernameField = findViewById(R.id.userNameText)
        passwordField = findViewById(R.id.passwordText)

        // DI: DB -> Repo -> ViewModel
        val userDb = UserDatabase.getInstance(applicationContext)
        userRepo = UserRepository(userDb)
        val factory = UserViewModelFactory(userRepo)
        userViewModel = ViewModelProvider(this, factory).get(UserViewModel::class.java)

        loginBtn.setOnClickListener { login() }
        createUserBtn.setOnClickListener {
            startActivity(Intent(this, CreateUserActivity::class.java))
        }
    }

    private fun login() {
        val username = usernameField.text.toString().trim()
        val password = passwordField.text.toString()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Empty UserName or Password!!!", Toast.LENGTH_SHORT).show()
            return
        }

        // Use suspend lookup to avoid LiveData/observe mixing
        lifecycleScope.launch {
            val candidate = userRepo.getUserByName(username) // suspend DAO call inside repo
            if (candidate != null && candidate.password == password) {
                // mark as logged in
                userViewModel.loginUser(candidate) // runs on viewModelScope
                Toast.makeText(this@LoginActivity, "Login Successful!!!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this@LoginActivity, "Invalid Credentials!!!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
