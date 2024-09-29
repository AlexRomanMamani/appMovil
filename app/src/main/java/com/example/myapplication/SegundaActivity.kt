package com.example.myapplication

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity;

class SegundaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_segunda)

        val buttonToThird = findViewById<Button>(R.id.loginButton)
        buttonToThird.setOnClickListener {
            val intent = Intent(this, TerceraActivity::class.java)
            startActivity(intent)
        }

        val buttonBackToMain = findViewById<Button>(R.id.cancelButton)
        buttonBackToMain.setOnClickListener {
            finish()
        }
    }
}