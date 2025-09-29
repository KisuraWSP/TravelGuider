package com.app.tourism_app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.tourism_app.R

class CrashHandlerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash_handler)

        findViewById<TextView>(R.id.tvTitle).text = "Something went wrong"
        findViewById<TextView>(R.id.tvMsg).text = intent.getStringExtra("throwable") ?: "Unexpected error"
        findViewById<TextView>(R.id.tvTrace).text = intent.getStringExtra("stacktrace") ?: "(no stacktrace)"

        findViewById<Button>(R.id.btnRestart).setOnClickListener {
            val restart = Intent(this, MainActivity::class.java)
            restart.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(restart)
            finish()
        }
    }
}
