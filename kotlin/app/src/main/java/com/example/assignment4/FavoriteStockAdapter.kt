package com.example.assignment4

import android.content.Context
import android.content.Intent
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import java.util.Collections

class FavoriteStockAdapter(val context: Context,
                           private val favoriteStockList: MutableList<FavoriteStock>, private val queue: RequestQueue) :
    RecyclerView.Adapter<FavoriteStockAdapter.FavoriteStockViewHolder>(), ItemTouchHelperAdapter {

    class FavoriteStockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tickerSymbol: TextView = view.findViewById(R.id.tvFavoriteStockSymbol)
        val companyName: TextView = view.findViewById(R.id.tvFavoriteCompanyName)
        val currentPrice: TextView = view.findViewById(R.id.tvFavoriteCurrentPrice)
        val changeInPrice: TextView = view.findViewById(R.id.tvFavoriteChangeInPrice)
        val changeIcon: ImageView= view.findViewById(R.id.up_down)
        val favNext: ImageView = view.findViewById(R.id.favImgStockNext)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteStockViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite_stock, parent, false)
        return FavoriteStockViewHolder(itemView)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(favoriteStockList, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemSwiped(position: Int) {
        removeItem(position)
    }

    override fun onBindViewHolder(holder: FavoriteStockViewHolder, position: Int) {
        val favoriteStock = favoriteStockList[position]
        holder.tickerSymbol.text = favoriteStock.tickerSymbol
        holder.companyName.text = favoriteStock.companyName
        holder.currentPrice.text = String.format("$%.2f", favoriteStock.currentPrice)
        holder.changeInPrice.text = String.format("$%.2f", favoriteStock.changeInPrice)

        val changeInPercentage = (favoriteStock.changeInPrice / favoriteStock.currentPrice) * 100

        val color = if (changeInPercentage >= 0) R.color.positiveChange else R.color.negativeChange
        holder.changeInPrice.setTextColor(ContextCompat.getColor(holder.itemView.context, color))

        val changeText = "${holder.changeInPrice.text} (${String.format("%.2f%%", changeInPercentage)})"
        holder.changeInPrice.text = changeText

        holder.changeIcon.setImageResource(if (changeInPercentage >= 0) R.drawable.ic_trending_up else R.drawable.ic_trending_down)
        holder.favNext.setOnClickListener {
            val intent = Intent(context, StockDetailActivity::class.java).apply {
                putExtra("FAVORITE_STOCK", favoriteStock)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = favoriteStockList.size



    fun removeItem(position: Int) {
        val stockId = favoriteStockList[position].id
        favoriteStockList.removeAt(position)
        notifyItemRemoved(position)
        removeStockFromFavorites(stockId)
    }


    private fun removeStockFromFavorites(stockId: String) {
        val url = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/watchList/$stockId"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.DELETE, url, null,
            { response ->
                // Handle the successful deletion
                Log.d("Delete", "Stock removed: $response")
            },
            { error ->
                // Handle error
                Log.e("DeleteError", "Failed to remove stock: $error")
            }
        )
        queue.add(jsonObjectRequest)
    }
}

