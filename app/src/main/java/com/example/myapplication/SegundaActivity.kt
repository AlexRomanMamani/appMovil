package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AlertDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.DatabaseReference

class SegundaActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)

        setContentView(R.layout.activity_segunda)

        // Ajustar los márgenes del layout principal para las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val buttonBackToMain = findViewById<Button>(R.id.btn_back_primera)
        buttonBackToMain.setOnClickListener {
            finish()  // Esto cerrará la actividad actual y volverá a la anterior
        }

        // Inicializar FirebaseAuth, Firestore y Realtime Database
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        database = Firebase.database.reference

        // Obtener las referencias de los elementos de la interfaz
        val btnAcceso: Button = findViewById(R.id.loginButton)
        val btnRegistrarse: Button = findViewById(R.id.registerButton)
        val textEmail: EditText = findViewById(R.id.emailInput)
        val textPassword: EditText = findViewById(R.id.passwordInput)

        // Configurar el listener del botón para iniciar sesión
        btnAcceso.setOnClickListener {
            val email = textEmail.text.toString()
            val password = textPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signIn(email, password, textEmail, textPassword)
            } else {
                Toast.makeText(
                    baseContext,
                    "Por favor, ingresa correo y contraseña",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Configurar el listener del botón para abrir el diálogo de registro
        btnRegistrarse.setOnClickListener {
            showRegistrationDialog()
        }
    }

    private fun showRegistrationDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.registrarse, null)
        builder.setView(dialogLayout)

        val dialog = builder.create()
        dialog.show()

        val buttonRegister = dialogLayout.findViewById<Button>(R.id.buttonRegister)
        val editTextUsername = dialogLayout.findViewById<EditText>(R.id.editTextUsername)
        val editTextEmail = dialogLayout.findViewById<EditText>(R.id.editTextEmail)
        val editTextPassword = dialogLayout.findViewById<EditText>(R.id.editTextPassword)

        buttonRegister.setOnClickListener {
            val username = editTextUsername.text.toString()
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                registerUser(email, password, username, dialog)
            } else {
                Toast.makeText(
                    baseContext,
                    "Por favor, completa todos los campos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun registerUser(email: String, password: String, username: String, dialog: AlertDialog) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registro exitoso
                    val user = firebaseAuth.currentUser
                    user?.let {
                        val userId = it.uid
                        val userMap = hashMapOf(
                            "username" to username,
                            "email" to email
                        )

                        // Guardar en Realtime Database
                        database.child("users").child(userId).setValue(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(baseContext, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(baseContext, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Si falla el registro
                    Toast.makeText(baseContext, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signIn(email: String, password: String, textEmail: EditText, textPassword: EditText) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    Toast.makeText(baseContext, "Bienvenido", Toast.LENGTH_SHORT).show()
                    // Aquí puedes saltar a otra pantalla si la autenticación es exitosa
                    val intent = Intent(this, TerceraActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(baseContext, "Error de Correo o Password", Toast.LENGTH_SHORT)
                        .show()
                }
                // Limpiar los campos de email y contraseña después del intento
                textPassword.text.clear()
            }
            .addOnFailureListener { e ->
                // Muestra el error exacto en caso de fallo
                Toast.makeText(baseContext, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT)
                    .show()
                // Limpiar los campos de email y contraseña después del intento fallido
                textPassword.text.clear()
            }
    }
}