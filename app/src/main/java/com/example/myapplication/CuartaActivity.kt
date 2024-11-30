package com.example.myapplication

import LecturasBottomSheet
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CuartaActivity : AppCompatActivity() {

    // Declarar variables para las vistas
    private lateinit var buttonShowData: Button
    private lateinit var logoutButton: Button
    private lateinit var simulateButton: Button
    private lateinit var settingsIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cuarta)

        // Inicializar vistas
        buttonShowData = findViewById(R.id.buttonShowData)
        logoutButton = findViewById(R.id.logoutButton)
        simulateButton = findViewById(R.id.simulate)
        settingsIcon = findViewById(R.id.settingsIcon)

        // Configurar listener para simular lectura
        simulateButton.setOnClickListener {
            simulateLightData() // Llama a la función para simular la lectura
        }

        // Configurar listener para mostrar datos
        buttonShowData.setOnClickListener {
            showDataBottomSheet() // Abre el BottomSheetDialog con los datos
        }

        // Configurar listener para cerrar sesión
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            finish() // Regresa al login
        }

        // Configurar listener para el ícono de configuración
        settingsIcon.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_config, null)

            // Crear el diálogo
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            // Añadir animaciones al diálogo
            dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

            // Referencia al RadioGroup y los RadioButtons
            val intervalGroup = dialogView.findViewById<RadioGroup>(R.id.intervalGroup)
            val interval10s = dialogView.findViewById<RadioButton>(R.id.interval10s)
            val interval30s = dialogView.findViewById<RadioButton>(R.id.interval30s)
            val interval60s = dialogView.findViewById<RadioButton>(R.id.interval60s)

            // Configurar el valor inicial desde SharedPreferences
            val currentInterval = getIntervalPreference()
            when (currentInterval) {
                10 -> interval10s.isChecked = true
                30 -> interval30s.isChecked = true
                60 -> interval60s.isChecked = true
            }

            // Configurar botón de guardar
            dialogView.findViewById<Button>(R.id.saveIntervalButton).setOnClickListener {
                val selectedInterval = when (intervalGroup.checkedRadioButtonId) {
                    R.id.interval10s -> 10
                    R.id.interval30s -> 30
                    R.id.interval60s -> 60
                    else -> 10 // Valor predeterminado
                }

                saveIntervalPreference(selectedInterval) // Guardar en SharedPreferences
                Toast.makeText(this, "Intervalo guardado: $selectedInterval segundos", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            dialog.show()
        }


        // Configurar botón para conectar Bluetooth
        val connectBluetoothButton: Button = findViewById(R.id.connectBluetoothButton)
        connectBluetoothButton.setOnClickListener {
            connectToBluetooth() // Llama al método de conexión
        }
    }

    // Función para simular datos de luz y guardarlos en Firebase
    private fun simulateLightData() {
        val randomLightValue = (100..1000).random() // Generar valor aleatorio
        saveLightData(randomLightValue, "T") // Guardar como dato simulado (Test)
    }

    // Función para guardar datos en Firebase
    private fun saveLightData(lightValue: Int, origen: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("America/Santiago")
        val currentDateTime = dateFormat.format(Date())

        val lightData = mapOf(
            "fecha" to currentDateTime,
            "valorLuz" to lightValue,
            "origen" to origen
        )

        val database = Firebase.database.reference
        val lecturaId = database.child("lecturas").push().key
        lecturaId?.let {
            database.child("lecturas").child(it).setValue(lightData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Dato guardado: $lightValue lux (Origen: $origen)", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar dato: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Función para mostrar un BottomSheetDialog con los datos de Firebase
    private fun showDataBottomSheet() {
        val bottomSheet = LecturasBottomSheet()
        bottomSheet.show(supportFragmentManager, "LecturasBottomSheet")
    }

    // Función para conectar al Bluetooth
    private fun connectToBluetooth() {
        if (!checkBluetoothPermissions()) {
            Toast.makeText(this, "Permisos de Bluetooth requeridos", Toast.LENGTH_SHORT).show()
            return
        }

        val bluetoothAdapter = getBluetoothAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth no está disponible en este dispositivo", Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Por favor, habilita Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        if (pairedDevices?.isNotEmpty() == true) {
            val device = pairedDevices.first()
            connectToDevice(device)
        } else {
            Toast.makeText(this, "No hay dispositivos emparejados", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getBluetoothAdapter(): BluetoothAdapter? {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager
        return bluetoothManager?.adapter
    }

    // Función para conectar a un dispositivo específico
    private fun connectToDevice(device: BluetoothDevice) {
        val uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        try {
            val socket = device.createRfcommSocketToServiceRecord(uuid)
            socket.connect()
            Toast.makeText(this, "Conexión establecida con ${device.name}", Toast.LENGTH_SHORT).show()
            listenToArduino(socket)
        } catch (e: SecurityException) {
            Toast.makeText(this, "Permisos de Bluetooth no otorgados", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al conectar con el dispositivo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para verificar permisos de Bluetooth
    private fun checkBluetoothPermissions(): Boolean {
        if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.BLUETOOTH_SCAN
                ), 1
            )
            return false
        }
        return true
    }

    // Función para escuchar datos desde el Arduino
    private fun listenToArduino(socket: BluetoothSocket) {
        val inputStream = socket.inputStream
        val buffer = ByteArray(1024)
        val handler = Handler(Looper.getMainLooper())

        val interval = getIntervalPreference() * 1000L // Convertir segundos a milisegundos


        Thread {
            while (true) {
                try {
                    val bytes = inputStream.read(buffer)
                    val receivedMessage = String(buffer, 0, bytes).trim()
                    val lightValue = receivedMessage.toIntOrNull() ?: continue

                    handler.post {
                        saveLightData(lightValue, "R") // Guardar como dato real en Firebase
                    }

                    // Pausar por el intervalo configurado
                    Thread.sleep(interval)
                } catch (e: Exception) {
                    handler.post {
                        Toast.makeText(this, "Error al leer datos: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    break
                }
            }
        }.start()
    }

    private fun saveIntervalPreference(interval: Int) {
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("reading_interval", interval)
        editor.apply()
    }

    private fun getIntervalPreference(): Int {
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        return sharedPreferences.getInt("reading_interval", 10) // 10 segundos por defecto
    }

}
