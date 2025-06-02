package com.example.assignment4

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class SuggestionAdapter(context: Context, private var suggestions: List<StockSuggestion>)
    : ArrayAdapter<StockSuggestion>(context, android.R.layout.simple_dropdown_item_1line, suggestions) {

    fun updateSuggestions(newSuggestions: List<StockSuggestion>) {
        suggestions = newSuggestions
        notifyDataSetChanged()
    }

    override fun getCount(): Int = suggestions.size

    override fun getItem(position: Int): StockSuggestion? = suggestions[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
        val suggestion = getItem(position)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = "${suggestion?.displaySymbol} | ${suggestion?.description}"
        return view
    }
}


