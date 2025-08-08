package com.app.tourism_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.app.tourism_app.database.UserDatabase
import com.app.tourism_app.database.model.User
import com.app.tourism_app.database.repository.UserRepository
import com.app.tourism_app.database.view.UserViewModel
import com.app.tourism_app.database.view.UserViewModelFactory

class CreateUserActivity : AppCompatActivity() {
    private lateinit var user_name_field : EditText
    private lateinit var password_field : EditText
    private lateinit var reenter_password_field : EditText
    private lateinit var create_user : Button
    private lateinit var user_repository : UserRepository
    private lateinit var user_view_model : UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        val userDatabase = UserDatabase.invoke(applicationContext)
        user_repository = UserRepository(userDatabase)
        user_view_model = ViewModelProvider(this, UserViewModelFactory(user_repository)).get(UserViewModel::class.java)


        user_name_field = findViewById(R.id.enterUserNameField)
        password_field = findViewById(R.id.enterPasswordField)
        reenter_password_field = findViewById(R.id.reEnterPasswordField)
        create_user = findViewById(R.id.createUserBtn)

        create_user.setOnClickListener {
            val username = user_name_field.text.toString().trim()
            val password = password_field.text.toString().trim()
            val reEnterPassword = reenter_password_field.text.toString().trim()

            if (password == reEnterPassword) {
                val user = User(username, password)
                user_view_model.insert(user)
                Toast.makeText(this, "User Successfully Created!!!", Toast.LENGTH_SHORT).show()

                val main_intent = Intent(this, MainActivity::class.java)
                startActivity(main_intent)
            } else {
                Toast.makeText(this, "Passwords Don't Match!!!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}