package com.example.assignment4

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.os.Bundle
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import androidx.core.graphics.toColor
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Stocks : AppCompatActivity() {
    private lateinit var queue: RequestQueue
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StockAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var favoriteRecyclerView: RecyclerView
    private lateinit var favoriteAdapter: FavoriteStockAdapter
    private val favoriteStocksList = mutableListOf<FavoriteStock>()
    private var stocksList = mutableListOf<Stock>()
    private lateinit var suggestionAdapter: SuggestionAdapter
    private lateinit var menuItemFavorite: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progressBar)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.title = "Stocks"

//        toolbar.setTitleTextColor(Color.BLACK)

        val tvPoweredBy = findViewById<TextView>(R.id.tvPoweredBy)
        tvPoweredBy.apply {
            setOnClickListener {
                val url = "https://www.finnhub.io"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            }
        }

        queue = Volley.newRequestQueue(this)
        recyclerView = findViewById(R.id.rvStocks)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = StockAdapter(this, stocksList)
        recyclerView.adapter = adapter

        fetchDataFromBackend()
        fetchStocksData()
        setCurrentDate()

        favoriteRecyclerView = findViewById(R.id.rvFavorites)
        favoriteRecyclerView.layoutManager = LinearLayoutManager(this)
        favoriteAdapter = FavoriteStockAdapter(this, favoriteStocksList, queue)
        favoriteRecyclerView.adapter = favoriteAdapter


        showProgressBar()
        setupRecyclerView()
//        fetchFavoriteStocks()

    }

    private fun setupRecyclerView() {
        val favoriteDragManageAdapter = DragManageAdapter(favoriteAdapter, this, ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT)
        val favoriteItemTouchHelper = ItemTouchHelper(favoriteDragManageAdapter)
        favoriteItemTouchHelper.attachToRecyclerView(favoriteRecyclerView)

        val stockDragManageAdapter = DragManageAdapter(adapter, this, ItemTouchHelper.UP or ItemTouchHelper.DOWN)
        val stockItemTouchHelper = ItemTouchHelper(stockDragManageAdapter)
        stockItemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun refreshData() {
        fetchDataFromBackend()
        fetchStocksData()
        fetchFavoriteStocks()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        Log.d("NewText", "Wassup")

        menuItemFavorite = menu.findItem(R.id.action_favorite)
        menuItemFavorite.isVisible = false

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.action_search)?.actionView as? androidx.appcompat.widget.SearchView)?.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            maxWidth = Integer.MAX_VALUE

            val searchAutoComplete: AutoCompleteTextView = findViewById(androidx.appcompat.R.id.search_src_text)
            val suggestionAdapter = SuggestionAdapter(this@Stocks, emptyList())
            searchAutoComplete.setAdapter(suggestionAdapter)
            searchAutoComplete.setOnItemClickListener { _, _, position, _ ->
                val suggestion = suggestionAdapter.getItem(position)
                searchAutoComplete.setText(suggestion?.displaySymbol)
            }

            setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {

                    val intent = Intent(this@Stocks, StockDetailActivity::class.java).apply {
                        putExtra("STOCK_SYMBOL", query)
                    }
                    menuItemFavorite.isVisible = true
                    startActivity(intent)

                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    Log.d("NewText", newText)
                    if (newText.isNotEmpty()) {
                        fetchStockSuggestions(newText, suggestionAdapter)
                    }
                    return true
                }
            })
        }
        return true
    }


    private fun fetchStockSuggestions(query: String, adapter: SuggestionAdapter) {
        Log.d("Fetching", "hi")
        val url = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/home/$query"
        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null, { response ->
            val suggestions = mutableListOf<StockSuggestion>()
            for (i in 0 until response.length()) {
                val item = response.getJSONObject(i)
                val displaySymbol = item.getString("displaySymbol")
                val description = item.getString("description")
                suggestions.add(StockSuggestion(displaySymbol, description))
            }
            adapter.updateSuggestions(suggestions)
        }, { error ->
            Log.e("Stocks", "Error fetching suggestions: $error")
            error.printStackTrace()
        })
        queue.add(jsonArrayRequest)
    }



    private fun setCurrentDate() {
        val currentDateTextView = findViewById<TextView>(R.id.currentDate)
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val date = dateFormat.format(Date())
        currentDateTextView.text = date
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    private fun fetchDataFromBackend() {
        val url = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/wallet"

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val wallet = response.getDouble("balance")

                updateWalletBalanceUI(wallet)
            },
            { error ->
                error.printStackTrace()
            })

        queue.add(jsonObjectRequest)
    }

    private fun fetchStocksData() {
        val url = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/portfolio"

        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                val newStocksList = parseStocksJson(response)
                stocksList.clear()
                stocksList.addAll(newStocksList)
                recyclerView.adapter?.notifyDataSetChanged()
                newStocksList.forEach { stock ->
                    fetchStockQuoteAndUpdate(stock)
                }

            },
            { error -> error.printStackTrace() }
        )
        queue.add(jsonArrayRequest)
    }


    private fun fetchStockQuoteAndUpdate(stock: Stock) {
        val urlDetails = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/stockQuote/${stock.tickerSymbol}"
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, urlDetails, null,
            { response ->
                Log.d("Stocks", "Received stock quote for: ${stock.tickerSymbol}")
                val currentPrice = response.getDouble("c")
                val marketValue = currentPrice * stock.quantity
                val changeInPrice = (currentPrice - stock.averageCostPerShare) * stock.quantity
                val totalCost = stock.averageCostPerShare * stock.quantity
                val changeInPricePercentage = (changeInPrice / totalCost) * 100

                val index = stocksList.indexOfFirst { it.tickerSymbol == stock.tickerSymbol }
                if (index != -1) {
                    val updatedStock = stocksList[index].apply {
                        this.currentPrice = currentPrice
                        this.marketValue = marketValue
                        this.changeInPrice = changeInPrice
                        this.changeInPricePercentage = changeInPricePercentage
                    }
                    stocksList[index] = updatedStock
                    adapter.notifyItemChanged(index)
                    updateUIWithStocks(updatedStock)
                }
            },
            { error ->
                Log.e("Stocks", "Error fetching stock quote for ${stock.tickerSymbol}: $error")
            }
        )
        queue.add(jsonObjectRequest)
    }

    private fun updateUIWithStocks(stock: Stock) {
        runOnUiThread {
            val index = stocksList.indexOfFirst { it.tickerSymbol == stock.tickerSymbol }
            if (index != -1) {
                stocksList[index] = stock
                adapter.notifyItemChanged(index)
            }
        }
        hideProgressBar()
    }




    private fun updateWalletBalanceUI(wallet: Double) {
        runOnUiThread {
            val tvCashBalance = findViewById<TextView>(R.id.tvCashBalance)
            val tvNetWorth = findViewById<TextView>(R.id.tvNetWorth)
            tvCashBalance.text = "$%.2f".format(wallet)
            tvNetWorth.text = "$%.2f".format(wallet)
        }
    }


    private fun parseStocksJson(jsonArray: JSONArray): List<Stock> {
        val stocksList = mutableListOf<Stock>()

        for (i in 0 until jsonArray.length()) {
            jsonArray.getJSONObject(i).let { jsonObject ->
                val stock = Stock(
                    tickerSymbol = jsonObject.getString("tickerSymbol"),
                    name = jsonObject.getString("name"),
                    quantity = jsonObject.getInt("quantity"),
                    totalCost = jsonObject.getDouble("totalCost"),
                    currentPrice = jsonObject.getDouble("currentPrice"),
                    averageCostPerShare = jsonObject.getDouble("averageCostPerShare")
                )
                stocksList.add(stock)
            }
        }
        return stocksList
    }

    private fun fetchFavoriteStocks() {
        val urlWatchlist = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/watchlist"

        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, urlWatchlist, null,
            { response ->
                Log.d("Stocks", "Clearing favorite stocks list")
                favoriteStocksList.clear()
                Log.d("Stocks", "Adding new items to the list")
                for (i in 0 until response.length()) {
                    val favorite = response.getJSONObject(i)
                    val symbol = favorite.getString("tickerSymbol")
                    val companyName = favorite.getString("name")
                    val id = favorite.getString("_id")
                    fetchStockDetails(symbol, companyName, id)
                }
                favoriteAdapter.notifyDataSetChanged()
                Log.d("Stocks", "List updated with new data")
                hideProgressBar()
            },
            { error ->
                Log.e("Stocks", "Error fetching favorite stocks: ${error.message}")
            })

        queue.add(jsonArrayRequest)
    }


    private fun fetchStockDetails(symbol: String, companyName: String, stockId: String) {
        val urlStockDetails = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/stockQuote/$symbol"

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, urlStockDetails, null,
            { response ->
                val changeInPrice = response.getDouble("d")
                val currentPrice = response.getDouble("c")
                val favoriteStock = FavoriteStock(
                    id= stockId,
                    tickerSymbol = symbol,
                    companyName = companyName,
                    currentPrice = currentPrice,
                    changeInPrice = changeInPrice
                )
                updateUIWithFavoriteStock(favoriteStock)
            },
            { error ->
                error.printStackTrace()
            })

        queue.add(jsonObjectRequest)
    }

    private fun updateUIWithFavoriteStock(favoriteStock: FavoriteStock) {
        hideProgressBar()
        runOnUiThread {
            favoriteStocksList.add(favoriteStock)
            favoriteAdapter.notifyItemInserted(favoriteStocksList.size - 1)
        }
    }



}
