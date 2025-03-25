package org.geeksforgeeks.demo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Adapter(
    private val list: ArrayList<DataModel>,
    private val context: Context
) :
    RecyclerView.Adapter<Adapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.search_result_rv_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val modal: DataModel = list[position]
        holder.titleTV.text = modal.title
        holder.snippetTV.text = modal.displayedLink
        holder.descTV.text = modal.snippet
        holder.itemView.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.setData(Uri.parse(modal.link))
            context.startActivity(i)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val titleTV: TextView = itemView.findViewById(R.id.idTVTitle)
        val descTV: TextView = itemView.findViewById(R.id.idTVDescription)
        val snippetTV: TextView = itemView.findViewById(R.id.idTVSnippet)
    }
}
