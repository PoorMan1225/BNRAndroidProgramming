package com.rjhwork.mycompany.sampletextsplit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ViewPagerAdapter(private val data: MutableList<String>,
                       private val context: Context
) : RecyclerView.Adapter<ViewPagerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pager_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    inner class ViewHolder(private val itemView : View) : RecyclerView.ViewHolder(itemView) {
        private var textView : TextView = itemView.findViewById(R.id.textView)

        fun bind(data : String) {
            textView.text = data
        }
    }
}