package com.example.assignment4

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PeersAdapter(private val peersList: List<String>, private val context: Context) : RecyclerView.Adapter<PeersAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var peerTextView: TextView = itemView.findViewById(R.id.peerName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.peer_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.peerTextView.text = peersList[position] + ","
        holder.peerTextView.setOnClickListener {
            val intent = Intent(context, StockDetailActivity::class.java).apply {
                putExtra("STOCK_SYMBOL", peersList[position])
            }
            context.startActivity(intent)
        }


    }

    override fun getItemCount(): Int = peersList.size
}

