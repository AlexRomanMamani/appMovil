package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TerceraActivity : AppCompatActivity() {
    private val librosCollectionRef = Firebase.firestore.collection("Libros")

    // Inicializar las vistas después de setContentView
    private lateinit var boton: Button
    private lateinit var botonMostrar: Button
    private lateinit var editText_nombre: EditText
    private lateinit var editText_autor: EditText
    private lateinit var tvLibros: TextView
    private lateinit var buttonBackToSecond:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tercera)

        // Vinculación de vistas
        boton = findViewById(R.id.btn_guardar)
        botonMostrar = findViewById(R.id.btn_mostrar)
        editText_nombre = findViewById(R.id.etNombre)
        editText_autor = findViewById(R.id.etAutor)
        tvLibros = findViewById(R.id.tv_Libros)

        buttonBackToSecond = findViewById(R.id.btn_back_segunda)

        val toFourth = findViewById<Button>(R.id.btn_cuarta)
        toFourth.setOnClickListener {
            val intent = Intent(this, CuartaActivity::class.java)
            startActivity(intent)
        }


// Acción del botón para cerrar la actividad actual y volver a la anterior
        buttonBackToSecond.setOnClickListener {
            finish()
        }

        boton.setOnClickListener {
            val nombre = editText_nombre.text.toString()
            val autor = editText_autor.text.toString()
            val libro = Libro(nombre, autor)
            saveLibro(libro)
        }

        botonMostrar.setOnClickListener {
            retrieveLibros()

        }
    }

    private fun saveLibro(libro: Libro) = CoroutineScope(Dispatchers.IO).launch {
        try {
            // Corrección de la referencia a la colección
            librosCollectionRef.add(libro).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@TerceraActivity, "Datos grabados.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                // Mejora en el manejo de errores
                Toast.makeText(this@TerceraActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun retrieveLibros() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val querySnapshot = librosCollectionRef.get().await()
            val sb = StringBuilder()

            for(document in querySnapshot.documents) {
                val libro = document.toObject<Libro>()
                if(!libro?.nombre.isNullOrBlank() && !libro?.autor.isNullOrBlank()){
                    sb.append("${libro?.nombre} - ${libro?.autor}\n")
                    sb.append("_\n") //separación de filas
                }

            }
            withContext(Dispatchers.Main) {
                tvLibros.text = sb.toString()
            }
        } catch(e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@TerceraActivity, e.message, Toast.LENGTH_LONG).show()
            }
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
                        val userData = dataSnapshot.value as? Map<*, *>
                        if (userData != null) {
                            val username = userData["username"] as? String
                            val email = userData["email"] as? String
                            val displayText = "Username: $username\nEmail: $email"
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@TerceraActivity, "Error al obtener datos: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
            }
        }
}