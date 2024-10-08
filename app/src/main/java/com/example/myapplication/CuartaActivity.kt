package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class CuartaActivity : AppCompatActivity() {
    private lateinit var textViewData: TextView
    private lateinit var buttonShowData: Button
    private lateinit var logoutButton: Button
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var settingsIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cuarta)

        // Inicializar vistas
        textViewData = findViewById(R.id.textViewData)
        buttonShowData = findViewById(R.id.buttonShowData)
        logoutButton = findViewById(R.id.logoutButton)
        button1 = findViewById(R.id.button1)
        button2 = findViewById(R.id.button2)
        settingsIcon = findViewById(R.id.settingsIcon)

        val buttonBackToThird  = findViewById<Button>(R.id.btn_back_tercera)
        buttonBackToThird .setOnClickListener {
            finish()  // Esto cerrará la actividad actual y volverá a la anterior
        }

        // Configurar listener para el botón de cerrar sesión
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            finish()
        }

        // Configurar listeners para los otros botones
        button1.setOnClickListener {
            Toast.makeText(this, "Prueba de alarma iniciada", Toast.LENGTH_SHORT).show()
        }

        button2.setOnClickListener {
            Toast.makeText(this, "Mostrando últimas alarmas", Toast.LENGTH_SHORT).show()
        }


        settingsIcon.setOnClickListener {
            Toast.makeText(this, "Abriendo configuración", Toast.LENGTH_SHORT).show()
            }
        }
}
