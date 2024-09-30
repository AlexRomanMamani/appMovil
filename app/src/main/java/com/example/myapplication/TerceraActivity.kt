package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class TerceraActivity : AppCompatActivity() {
    private lateinit var textViewData: TextView
    private lateinit var buttonShowData: Button
    private lateinit var logoutButton: Button
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var statusIcon: ImageView
    private lateinit var settingsIcon: ImageView
    private lateinit var connectionStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tercera)

        // Inicializar vistas
        textViewData = findViewById(R.id.textViewData)
        buttonShowData = findViewById(R.id.buttonShowData)
        logoutButton = findViewById(R.id.logoutButton)
        button1 = findViewById(R.id.button1)
        button2 = findViewById(R.id.button2)
        statusIcon = findViewById(R.id.statusIcon)
        settingsIcon = findViewById(R.id.settingsIcon)
        connectionStatus = findViewById(R.id.connectionStatus)

        // Configurar listener para el botón de mostrar datos
        buttonShowData.setOnClickListener {
            fetchDataFromDatabase()
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

        // Configurar listeners para los iconos
        statusIcon.setOnClickListener {
            Toast.makeText(this, "Estado de conexión", Toast.LENGTH_SHORT).show()
        }

        settingsIcon.setOnClickListener {
            Toast.makeText(this, "Abriendo configuración", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchDataFromDatabase() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val database = Firebase.database
            val myRef = database.getReference("users").child(user.uid)

            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val userData = dataSnapshot.value as? Map<String, Any>
                        if (userData != null) {
                            val username = userData["username"] as? String
                            val email = userData["email"] as? String
                            val displayText = "Username: $username\nEmail: $email"
                            textViewData.text = displayText
                        } else {
                            textViewData.text = "No se encontraron datos del usuario"
                        }
                    } else {
                        textViewData.text = "No se encontraron datos del usuario"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@TerceraActivity, "Error al obtener datos: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            textViewData.text = "Usuario no autenticado"
        }
    }
}