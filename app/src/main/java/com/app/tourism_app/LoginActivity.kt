package com.app.tourism_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.app.tourism_app.database.UserDatabase
import com.app.tourism_app.database.repository.UserRepository
import com.app.tourism_app.database.view.UserViewModel
import com.app.tourism_app.database.view.UserViewModelFactory

class LoginActivity : AppCompatActivity() {
    private lateinit var login_btn : Button
    private lateinit var create_user_btn : Button
    private lateinit var username_field : EditText
    private lateinit var password_field : EditText
    private lateinit var user_repo : UserRepository
    private lateinit var user_model : UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        val userDB  = UserDatabase.invoke(applicationContext)
        user_repo = UserRepository(userDB)
        user_model = ViewModelProvider(this, UserViewModelFactory(user_repo)).get(UserViewModel::class.java)

        // initialize this otherwise this will crash because of lateinit
        login_btn = findViewById(R.id.login_btn)
        create_user_btn = findViewById(R.id.create_user_btn)
        username_field = findViewById(R.id.userNameText)
        password_field = findViewById(R.id.passwordText)

        login_btn.setOnClickListener {
            login()
        }

        create_user_btn.setOnClickListener {
            val create_user_intent = Intent(this, CreateUserActivity::class.java)
            startActivity(create_user_intent)
        }
    }

    private fun login() {
        val user_name = username_field.text.toString()
        val password = password_field.text.toString()

        if(user_name.isNotEmpty() && password.isNotEmpty()){
            user_model.getUserByName(user_name).observe(this) { user ->
                if (user != null && user.password == password) {
                    Toast.makeText(this, "Login Successful!!!", Toast.LENGTH_SHORT).show()
                    val main_intent = Intent(this, MainActivity::class.java)
                    startActivity(main_intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid Credentials!!!", Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            Toast.makeText(this,"Empty UserName or Password!!!",Toast.LENGTH_SHORT).show()
        }
    }
}