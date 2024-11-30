import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class LecturasAdapter(private val lecturas: List<Map<String, Any>>) :
    RecyclerView.Adapter<LecturasAdapter.LecturaViewHolder>() {

    class LecturaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val lecturaTexto: TextView = view.findViewById(R.id.lecturaTexto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LecturaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lectura, parent, false)
        return LecturaViewHolder(view)
    }

    override fun onBindViewHolder(holder: LecturaViewHolder, position: Int) {
        val lectura = lecturas[position]
        val fecha = lectura["fecha"] as? String ?: "Sin fecha"
        val valorLuz = (lectura["valorLuz"] as? Number)?.toInt() ?: 0
        val origen = (lectura["origen"] as? String ?: "")
        holder.lecturaTexto.text = "$fecha - $valorLuz lux - $origen"
    }

    override fun getItemCount() = lecturas.size
}
