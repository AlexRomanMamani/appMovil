import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LecturasBottomSheet : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_lecturas, container, false)

        // Configurar RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewLecturas)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Lista para almacenar lecturas
        val lecturas = mutableListOf<Map<String, Any>>()

        // Adaptador para RecyclerView
        val adapter = LecturasAdapter(lecturas)
        recyclerView.adapter = adapter

        // Obtener datos de Firebase
        val database = Firebase.database.reference.child("lecturas")
        database.limitToLast(10).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                lecturas.clear()
                for (data in snapshot.children) {
                    val lectura = data.value as? Map<String, Any>
                    lectura?.let { lecturas.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores
            }
        })

        return view
    }
}
