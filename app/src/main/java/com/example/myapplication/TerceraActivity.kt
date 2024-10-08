package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var editTextNombre: EditText
    private lateinit var editTextAutor: EditText
    private lateinit var tvLibros: TextView
    private lateinit var buttonBackToSecond:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tercera)

        // Vinculación de vistas
        boton = findViewById(R.id.btn_guardar)
        botonMostrar = findViewById(R.id.btn_mostrar)
        editTextNombre = findViewById(R.id.etNombre)
        editTextAutor = findViewById(R.id.etAutor)
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
            val nombre = editTextNombre.text.toString()
            val autor = editTextAutor.text.toString()
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


}