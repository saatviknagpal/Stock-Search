package com.example.assignment4

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections
import kotlin.math.abs

class StockAdapter(private val context: Context, private val stockList: MutableList<Stock>) :
    RecyclerView.Adapter<StockAdapter.StockViewHolder>(), ItemTouchHelperAdapter {

    class StockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tickerSymbol: TextView = view.findViewById(R.id.tvStockSymbol)
        val currentPrice: TextView = view.findViewById(R.id.tvStockPrice)
        val stockShares: TextView = view.findViewById(R.id.tvStockShares)
        val stockChangeAmount: TextView = view.findViewById(R.id.tvStockChangeAmount)
        val stockChangeIcon: ImageView = view.findViewById(R.id.imgStockChange)
        val imgStockNext: ImageView = view.findViewById(R.id.imgStockNext)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_stock, parent, false)
        return StockViewHolder(itemView)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(stockList, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemSwiped(position: Int) {

    }


    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        val stock = stockList[position]
        holder.tickerSymbol.text = stock.tickerSymbol
        holder.stockShares.text = context.getString(R.string.shares_format, stock.quantity)

        holder.currentPrice.text = String.format("$%.2f", stock.marketValue)

        val changeInPriceText = String.format("$%.2f (%.2f%%)",
            stock.changeInPrice,
            stock.changeInPricePercentage
        )
        holder.stockChangeAmount.text = changeInPriceText

        Log.d("stockChange", stock.changeInPrice.toString())

        if (stock.changeInPrice > 0) {
            holder.stockChangeIcon.setImageResource(R.drawable.ic_trending_up)
            holder.stockChangeAmount.setTextColor(ContextCompat.getColor(context, R.color.positiveChange))
        } else if(stock.changeInPrice == 0.0) {
            holder.stockChangeAmount.setTextColor(ContextCompat.getColor(context, R.color.black))
        }else
         {
            holder.stockChangeIcon.setImageResource(R.drawable.ic_trending_down)
            holder.stockChangeAmount.setTextColor(ContextCompat.getColor(context, R.color.negativeChange))
        }
        holder.imgStockNext.setOnClickListener {
            val intent = Intent(context, StockDetailActivity::class.java).apply {
                putExtra("STOCK", stock)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = stockList.size
}
