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
    // Declaración de variables para manejar Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa Firebase para su uso en esta actividad
        FirebaseApp.initializeApp(this)

        // Asocia esta actividad con su archivo XML de diseño
        setContentView(R.layout.activity_segunda)

        // Ajusta automáticamente los márgenes del diseño principal para acomodar las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configura el botón para regresar a la actividad anterior
        val buttonBackToMain = findViewById<Button>(R.id.btn_back_primera)
        buttonBackToMain.setOnClickListener {
            finish() // Cierra esta actividad y vuelve a la anterior
        }

        // Inicializa los servicios de Firebase
        firebaseAuth = FirebaseAuth.getInstance() // Autenticación de usuarios
        firestore = FirebaseFirestore.getInstance() // Firestore (no usado directamente aquí)
        database = Firebase.database.reference // Realtime Database

        // Obtiene referencias de los elementos de la interfaz
        val btnAcceso: Button = findViewById(R.id.loginButton) // Botón para iniciar sesión
        val btnRegistrarse: Button = findViewById(R.id.registerButton) // Botón para registrarse
        val textEmail: EditText = findViewById(R.id.emailInput) // Campo de texto para el correo
        val textPassword: EditText = findViewById(R.id.passwordInput) // Campo de texto para la contraseña

        // Configura el botón de inicio de sesión
        btnAcceso.setOnClickListener {
            val email = textEmail.text.toString() // Obtiene el correo ingresado
            val password = textPassword.text.toString() // Obtiene la contraseña ingresada

            // Verifica si los campos no están vacíos
            if (email.isNotEmpty() && password.isNotEmpty()) {
                signIn(email, password, textPassword) // Llama a la función para iniciar sesión
            } else {
                // Muestra un mensaje de error si faltan datos
                Toast.makeText(
                    baseContext,
                    "Por favor, ingresa correo y contraseña",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Configura el botón para mostrar el diálogo de registro
        btnRegistrarse.setOnClickListener {
            showRegistrationDialog() // Abre el diálogo para registrarse
        }
    }

    // Muestra un diálogo para que el usuario se registre
    private fun showRegistrationDialog() {
        val builder = AlertDialog.Builder(this) // Crea un constructor de diálogos
        val inflater = layoutInflater // Infla el diseño del diálogo desde XML
        val dialogLayout = inflater.inflate(R.layout.registrarse, null)
        builder.setView(dialogLayout) // Establece el diseño del diálogo

        val dialog = builder.create() // Crea el diálogo
        dialog.show() // Muestra el diálogo

        // Obtiene referencias de los elementos del diseño del diálogo
        val buttonRegister = dialogLayout.findViewById<Button>(R.id.buttonRegister) // Botón de registro
        val editTextUsername = dialogLayout.findViewById<EditText>(R.id.editTextUsername) // Nombre de usuario
        val editTextEmail = dialogLayout.findViewById<EditText>(R.id.editTextEmail) // Correo
        val editTextPassword = dialogLayout.findViewById<EditText>(R.id.editTextPassword) // Contraseña

        // Configura el botón de registro
        buttonRegister.setOnClickListener {
            val username = editTextUsername.text.toString() // Obtiene el nombre de usuario ingresado
            val email = editTextEmail.text.toString() // Obtiene el correo ingresado
            val password = editTextPassword.text.toString() // Obtiene la contraseña ingresada

            // Verifica si todos los campos están llenos
            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                registerUser(email, password, username, dialog) // Llama a la función para registrar al usuario
            } else {
                // Muestra un mensaje de error si faltan datos
                Toast.makeText(
                    baseContext,
                    "Por favor, completa todos los campos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Registra un nuevo usuario en Firebase
    private fun registerUser(email: String, password: String, username: String, dialog: AlertDialog) {
        firebaseAuth.createUserWithEmailAndPassword(email, password) // Intenta registrar al usuario
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Si el registro es exitoso
                    val user = firebaseAuth.currentUser
                    user?.let {
                        val userId = it.uid // Obtiene el ID del usuario
                        val userMap = hashMapOf(
                            "username" to username,
                            "email" to email
                        )

                        // Guarda los datos del usuario en Realtime Database
                        database.child("users").child(userId).setValue(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(baseContext, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                dialog.dismiss() // Cierra el diálogo
                            }
                            .addOnFailureListener { e ->
                                // Muestra un mensaje si ocurre un error al guardar los datos
                                Toast.makeText(baseContext, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Si el registro falla, muestra un mensaje de error
                    Toast.makeText(baseContext, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Inicia sesión con un usuario existente
    private fun signIn(email: String, password: String, textPassword: EditText) {
        firebaseAuth.signInWithEmailAndPassword(email, password) // Intenta autenticar al usuario
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Si la autenticación es exitosa
                    Toast.makeText(baseContext, "Bienvenido", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, CuartaActivity::class.java) // Redirige a otra actividad
                    startActivity(intent)
                } else {
                    // Si falla la autenticación, muestra un mensaje de error
                    Toast.makeText(baseContext, "Error de Correo o Password", Toast.LENGTH_SHORT)
                        .show()
                }
                textPassword.text.clear() // Limpia el campo de contraseña
            }
            .addOnFailureListener { e ->
                // Muestra el error específico en caso de fallo
                Toast.makeText(baseContext, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT)
                    .show()
                textPassword.text.clear() // Limpia el campo de contraseña
            }
    }
}
