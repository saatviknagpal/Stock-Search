package com.example.assignment4

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.json.JSONArray
import org.json.JSONObject

class StockDetailActivity : AppCompatActivity() {

    private var isFavorite = false
    private lateinit var menuItemFavorite: MenuItem
    private lateinit var tvStockDetailSymbol: TextView
    private lateinit var tvStockDetailCompany: TextView
    private lateinit var tvStockDetailCurrentPrice: TextView
    private lateinit var tvStockDetailChange: TextView
    private lateinit var queue: RequestQueue
    private var stock: Stock? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var contentLayout: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_detail)

        val toolbar: Toolbar = findViewById(R.id.stock_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        progressBar = findViewById(R.id.progressBarDetail)
        contentLayout = findViewById(R.id.mainContentLayout)
//        loadData()

        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        val tabs: TabLayout = findViewById(R.id.tabs)

        tvStockDetailSymbol = findViewById(R.id.tvStockDetailSymbol)
        tvStockDetailCompany = findViewById(R.id.tvStockDetailCompany)
        tvStockDetailCurrentPrice = findViewById(R.id.tvStockDetailCurrent)
        tvStockDetailChange = findViewById(R.id.tvStockDetailChange)

        queue = Volley.newRequestQueue(this)
        val searchStock = intent.getStringExtra("STOCK_SYMBOL")
        stock = intent.getParcelableExtra("STOCK")
        val favoriteStock = intent.getParcelableExtra("FAVORITE_STOCK") as? FavoriteStock
        if (searchStock != null) {
            supportActionBar?.title = searchStock
            tvStockDetailSymbol.text = searchStock
        }
        if (stock == null && favoriteStock != null) {
            stock = convertFavoriteToStock(favoriteStock)
        }

        if (stock == null && searchStock != null) {
            fetchStockDetails(searchStock) { fetchedStock ->
                stock = fetchedStock
                updateUIWithStockDetails(stock!!)
                setupViewPagerAndTabs(viewPager, tabs)
            }
        } else {
            setupViewPagerAndTabs(viewPager, tabs)
            stock?.let { updateUIWithStockDetails(it) }
        }

        findViewById<Button>(R.id.btnTrade).setOnClickListener {
            stock?.let { showTradeDialog(it) } ?: Toast.makeText(this, "Stock data is not loaded yet.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadData() {
        showProgressBar(true)
        Handler(Looper.getMainLooper()).postDelayed({
            showProgressBar(false)
        }, 3000)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupViewPagerAndTabs(viewPager: ViewPager2, tabs: TabLayout) {
        stock?.let {
            viewPager.adapter = ChartPagerAdapter(this, it.tickerSymbol)
            TabLayoutMediator(tabs, viewPager) { tab, position ->
                tab.icon = when (position) {
                    0 -> ContextCompat.getDrawable(this, R.drawable.chart_hour)
                    1 -> ContextCompat.getDrawable(this, R.drawable.chart_historical)
                    else -> null
                }
            }.attach()
        }
    }

    private fun convertFavoriteToStock(favorite: FavoriteStock): Stock = Stock(
        tickerSymbol = favorite.tickerSymbol,
        name = favorite.companyName,
        currentPrice = favorite.currentPrice,
        changeInPrice = favorite.changeInPrice
    )

    private fun showTradeDialog(stock: Stock) {
        fetchWalletBalance { balance ->
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_trade, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_roundable_bg)


            val etShareAmount = dialogView.findViewById<EditText>(R.id.etShareQuantity)
            val tvCalculatedPrice = dialogView.findViewById<TextView>(R.id.tvCalculation)
            val tvAvailableBalance = dialogView.findViewById<TextView>(R.id.tvAvailableMoney)
            val btnBuy = dialogView.findViewById<Button>(R.id.btnBuy)
            val btnSell = dialogView.findViewById<Button>(R.id.btnSell)
            val tvShareName = dialogView.findViewById<TextView>(R.id.tvShareName)

            tvAvailableBalance.text = String.format("$%.2f available to trade", balance)
            tvCalculatedPrice.text = "0 * ${stock.currentPrice}/share = $0.00"
            tvShareName.text = "Trade ${stock.name} Shares"

            etShareAmount.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val numShares = s.toString().toIntOrNull() ?: 0
                    val totalCost = (numShares * stock.currentPrice)
                    tvCalculatedPrice.text = "$numShares * ${String.format("%.2f", stock.currentPrice)}/share = ${String.format("$%.2f", totalCost)}"
                }
            })

            btnBuy.setOnClickListener {
                val numShares = etShareAmount.text.toString().toIntOrNull()
                if (numShares == null || numShares == 0) {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                }
                else if(numShares < 0) {
                    Toast.makeText(this, "Cannot buy non-positive shares", Toast.LENGTH_SHORT).show()

                }
                else if (numShares * stock.currentPrice > balance) {
                    Toast.makeText(this, "Not enough money to buy", Toast.LENGTH_SHORT).show()
                }
                else {
                    buyStock(stock, numShares)
                    dialog.dismiss()
                }
            }

            btnSell.setOnClickListener {
                val numShares = etShareAmount.text.toString().toIntOrNull()
                if (numShares == null || numShares == 0) {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                } else if(numShares < 0) {
                    Toast.makeText(this, "Cannot sell non-positive shares", Toast.LENGTH_SHORT).show()
                }else if (numShares > stock.quantity) {
                    Toast.makeText(this, "Not enough shares to sell", Toast.LENGTH_SHORT).show()
                }else {
                    sellStock(stock, numShares)
                    dialog.dismiss()
                }
            }

            dialog.show()
        }
    }

    private fun buyStock(stock: Stock, quantity: Int) {
        val url = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/portfolio/buy/${stock.tickerSymbol}"
        val params = JSONObject().apply {
            put("quantity", quantity)
            put("price", stock.currentPrice)
            put("name", stock.name)
        }

        val newQuantity = stock.quantity + quantity
        val newTotalCost = stock.totalCost + (quantity * stock.currentPrice)
        val newAvgCostPerShare = newTotalCost / newQuantity

        stock.quantity = newQuantity
        stock.totalCost = newTotalCost
        stock.averageCostPerShare = newAvgCostPerShare
        stock.marketValue = newQuantity * stock.currentPrice

        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, params,
            { response ->
                fetchPortfolioDetails(stock.tickerSymbol, stock.currentPrice)
                showSuccessDialog("You have successfully bought $quantity shares of ${stock.tickerSymbol}.")

            },
            { error ->
                Toast.makeText(this, "Failed to buy shares: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(jsonObjectRequest)
    }

    private fun showSuccessDialog(message: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_succesful, null)
        val successDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        successDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_roundable_bg)


        val tvSuccessMessage = dialogView.findViewById<TextView>(R.id.tvSuccessMessage)
        tvSuccessMessage.text = message

        val btnDone = dialogView.findViewById<Button>(R.id.btnBuy)
        btnDone.setOnClickListener {
            successDialog.dismiss()
            updatePortfolioUI(stock!!)
        }

        successDialog.show()
    }


    private fun sellStock(stock: Stock, quantity: Int) {
        if(quantity > stock.quantity) {
            Toast.makeText(this, "Not enough shares to sell", Toast.LENGTH_LONG).show()
            return
        }

        val url = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/portfolio/sell/${stock.tickerSymbol}"
        val params = JSONObject().apply {
            put("quantity", quantity)
            put("price", stock.currentPrice)
            put("name", stock.name)
        }

        val newQuantity = stock.quantity - quantity
        val newTotalCost = stock.totalCost - (quantity * stock.averageCostPerShare)
        val newChange = stock.currentPrice - stock.averageCostPerShare
        val newAvgCostPerShare = if (newQuantity > 0) newTotalCost / newQuantity else 0.0

        stock.quantity = newQuantity
        stock.totalCost = newTotalCost
        stock.averageCostPerShare = newAvgCostPerShare
        stock.marketValue = newQuantity * stock.currentPrice

        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, params,
            { response ->
                fetchPortfolioDetails(stock.tickerSymbol, stock.currentPrice)
                showSuccessDialog("You have successfully sold $quantity shares of ${stock.tickerSymbol}.")
            },
            { error ->
                Toast.makeText(this, "Failed to sell shares: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(jsonObjectRequest)

    }


    private fun updatePortfolioUI(stock: Stock) {
        findViewById<TextView>(R.id.tvSharesOwnedValue).text = stock.quantity.toString()
        findViewById<TextView>(R.id.tvAvgCostValue).text = String.format("$%.2f", stock.averageCostPerShare)
        findViewById<TextView>(R.id.tvTotalCostValue).text = String.format("$%.2f", stock.totalCost)
        if(stock.averageCostPerShare == 0.0){
            findViewById<TextView>(R.id.tvChangeValue).text = String.format("$%.2f", 0.0)
        } else {
            findViewById<TextView>(R.id.tvChangeValue).text =
                String.format("$%.2f", stock.currentPrice - stock.averageCostPerShare)
        }
        findViewById<TextView>(R.id.tvMarketValueValue).text = String.format("$%.2f", stock.marketValue)
    }

    private fun fetchWalletBalance(onResult: (Double) -> Unit) {
        val url = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/wallet"
        val walletRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val balance = response.optDouble("balance", 0.0)
                onResult(balance)
            },
            { error ->
                Log.e("WalletFetchError", "Error fetching wallet balance", error)
                onResult(0.0)
            }
        )
        queue.add(walletRequest)
    }




    inner class ChartPagerAdapter(fa: FragmentActivity, private val tickerSymbol: String) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ChartFragment1.newInstance(tickerSymbol)
                1 -> ChartFragment2.newInstance(tickerSymbol)
                else -> throw IllegalArgumentException("Position $position is invalid for this view pager")
            }
        }
    }

    private fun showProgressBar(show: Boolean) {
        if (show) {
            progressBar.visibility = View.VISIBLE
            contentLayout.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            contentLayout.visibility = View.VISIBLE
        }
    }

    private fun fetchStockDetails(tickerSymbol: String, onResult: (Stock) -> Unit) {
        showProgressBar(true)
        val detailUrl = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/stockDetails/$tickerSymbol"
        val detailRequest = JsonObjectRequest(Request.Method.GET, detailUrl, null,
            { response ->
                val fetchedStock = parseStockDetails(response)
                onResult(fetchedStock)
                showProgressBar(false)
            },
            { error ->
                Log.e("StockDetailError", "Error fetching stock details", error)
                showProgressBar(false)
                Toast.makeText(this, "Failed to load stock details.", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(detailRequest)
    }

    private fun updateAboutSection(jsonObject: JSONObject){
        findViewById<TextView>(R.id.tvIPOStartDate).text = jsonObject.getString("ipo")
        findViewById<TextView>(R.id.tvIndustry).text = jsonObject.getString("finnhubIndustry")
        findViewById<TextView>(R.id.tvWebpage).text = jsonObject.getString("weburl")
    }

    private fun fetchStockQuote(stock: Stock) {
        val quoteUrl = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/stockQuote/${stock.tickerSymbol}"
        val quoteRequest = JsonObjectRequest(Request.Method.GET, quoteUrl, null,
            { response ->
                updateStockWithQuote(stock, response)
                updateUIWithQuoteDetails(response)
            },
            { error ->
                Log.e("StockQuoteError", "Error fetching stock quote", error)
            }
        )
        queue.add(quoteRequest)
    }

    private fun updateUIWithQuoteDetails(jsonObject: JSONObject) {
        val openPrice = jsonObject.optDouble("o", 0.0)
        val highPrice = jsonObject.optDouble("h", 0.0)
        val lowPrice = jsonObject.optDouble("l", 0.0)
        val prevClose = jsonObject.optDouble("pc", 0.0)
        val currentPrice = jsonObject.optDouble("c")
        val changeInPrice = jsonObject.optDouble("d")
        val changeInPricePercentage = jsonObject.optDouble("dp")

        stock?.currentPrice = currentPrice
        stock?.changeInPrice = changeInPrice
        stock?.changeInPricePercentage = changeInPricePercentage

        tvStockDetailCurrentPrice.text = String.format("$%.2f", currentPrice)
        findViewById<TextView>(R.id.tvOpenPriceValue).text = String.format("$%.2f", openPrice)
        findViewById<TextView>(R.id.tvHighPriceValue).text = String.format("$%.2f", highPrice)
        findViewById<TextView>(R.id.tvLowPriceValue).text = String.format("$%.2f", lowPrice)
        findViewById<TextView>(R.id.tvPrevCloseValue).text = String.format("$%.2f", prevClose)
        val changeInPriceText = String.format("$%.2f (%.2f%%)", changeInPrice, changeInPricePercentage)
        tvStockDetailChange.text = changeInPriceText
        if (changeInPrice > 0.0) {
            tvStockDetailChange.setTextColor(resources.getColor(R.color.positiveChange))
            findViewById<ImageView>(R.id.up_down_detail).setImageResource(R.drawable.ic_trending_up)

        } else{
            findViewById<ImageView>(R.id.up_down_detail).setImageResource(R.drawable.ic_trending_down)
            tvStockDetailChange.setTextColor(resources.getColor(R.color.negativeChange))
        }

    }


    private fun parseStockDetails(jsonObject: JSONObject): Stock {
        return Stock(
            tickerSymbol = jsonObject.getString("ticker"),
            name = jsonObject.getString("name"),
            quantity = 0,
            totalCost = 0.0,
            averageCostPerShare = 0.0,
            currentPrice = 0.0,
            marketValue = 0.0,
            changeInPrice = 0.0,
            changeInPricePercentage = 0.0
        )
    }

    private fun updateStockWithQuote(stock: Stock, jsonObject: JSONObject) {
        stock.currentPrice = jsonObject.getDouble("c")
        stock.changeInPrice = jsonObject.getDouble("d")
        stock.changeInPricePercentage = jsonObject.getDouble("dp")
        stock.marketValue = stock.quantity * stock.currentPrice
        updateUIWithStockDetails(stock)
    }


    private fun addRecommendationTrendFragment(tickerSymbol: String) {
        val fragment = RecommendationTrendFragment.newInstance(tickerSymbol)
        supportFragmentManager.beginTransaction()
            .replace(R.id.recommendation_trend_fragment_container, fragment)
            .commit()
    }

    private fun addEPSFragment(tickerSymbol: String) {
        val fragment = EPSFragment.newInstance(tickerSymbol)
        supportFragmentManager.beginTransaction()
            .replace(R.id.eps_fragment, fragment)
            .commit()
    }

    private fun addNewsFragment(tickerSymbol: String) {
        val fragment = NewsFragment.newInstance(tickerSymbol)
        supportFragmentManager.beginTransaction()
            .replace(R.id.news_fragment, fragment)
            .commit()
    }

    private fun fetchQuotes(tickerSymbol: String) {
        val quoteUrl = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/stockQuote/${tickerSymbol}"
        val quoteRequest = JsonObjectRequest(Request.Method.GET, quoteUrl, null,
            { response ->
                updateUIWithQuoteDetails(response)
            },
            { error ->
                Log.e("StockQuoteError", "Error fetching stock quote", error)
            }
        )
        queue.add(quoteRequest)
    }


    private fun updateUIWithStockDetails(stock: Stock) {

        showProgressBar(true)
        fetchPortfolioDetails(stock.tickerSymbol, stock.currentPrice)
        addRecommendationTrendFragment(stock.tickerSymbol)
        addEPSFragment(stock.tickerSymbol)
        addNewsFragment(stock.tickerSymbol)
        fetchQuotes(stock.tickerSymbol)
        updateChangeColor(stock.changeInPrice)
        fetchDetails(stock.tickerSymbol)

        supportActionBar?.title = stock.tickerSymbol
        tvStockDetailSymbol.text = stock.tickerSymbol
        tvStockDetailCompany.text = stock.name
        showProgressBar(false)
    }

    private fun fetchPortfolioDetails(tickerSymbol: String, currentPrice: Double) {
        val url = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/portfolio/$tickerSymbol"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                    updatePortfolioUI(response, currentPrice)
            },
            { error ->
                resetPortfolioUI()
                Log.e("PortfolioFetchError", "Error fetching portfolio data", error)
            }
        )
        queue.add(jsonObjectRequest)
    }


    private fun resetPortfolioUI() {
        findViewById<TextView>(R.id.tvSharesOwnedValue).text = "0"
        findViewById<TextView>(R.id.tvAvgCostValue).text = "$0.00"
        findViewById<TextView>(R.id.tvTotalCostValue).text = "$0.00"
        findViewById<TextView>(R.id.tvChangeValue).text = "$0.00"
        findViewById<TextView>(R.id.tvMarketValueValue).text = "$0.00"
        findViewById<TextView>(R.id.tvChangeValue).setTextColor(resources.getColor(R.color.black))
        findViewById<TextView>(R.id.tvMarketValueValue).setTextColor(resources.getColor(R.color.black))
    }


    private fun updatePortfolioUI(jsonObject: JSONObject, currentPrice: Double) {
        val sharesOwned = jsonObject.optInt("quantity", 0)
        val avgCostPerShare = jsonObject.optDouble("averageCostPerShare", 0.0)
        val totalCost = jsonObject.optDouble("totalCost", 0.0)
        val change =  currentPrice - avgCostPerShare

        findViewById<TextView>(R.id.tvSharesOwnedValue).text = sharesOwned.toString()
        findViewById<TextView>(R.id.tvAvgCostValue).text = String.format("$%.2f", avgCostPerShare)
        findViewById<TextView>(R.id.tvTotalCostValue).text = String.format("$%.2f", totalCost)
        findViewById<TextView>(R.id.tvChangeValue).text = String.format("$%.2f", change)
        findViewById<TextView>(R.id.tvMarketValueValue).text = String.format("$%.2f", sharesOwned * currentPrice)

        if(change >= 0.0){
            findViewById<TextView>(R.id.tvChangeValue).setTextColor(resources.getColor(R.color.positiveChange))
            findViewById<TextView>(R.id.tvMarketValueValue).setTextColor(resources.getColor(R.color.positiveChange))
        } else if (change == 0.0) {
            findViewById<TextView>(R.id.tvChangeValue).setTextColor(resources.getColor(R.color.black))
            findViewById<TextView>(R.id.tvMarketValueValue).setTextColor(resources.getColor(R.color.black))
        } else{
            findViewById<TextView>(R.id.tvChangeValue).setTextColor(resources.getColor(R.color.negativeChange))
            findViewById<TextView>(R.id.tvMarketValueValue).setTextColor(resources.getColor(R.color.negativeChange))

        }
    }


    private fun updateUIWithFavoriteStockDetails(favoriteStock: FavoriteStock) {
        supportActionBar?.title = favoriteStock.tickerSymbol
        tvStockDetailSymbol.text = favoriteStock.tickerSymbol
        tvStockDetailCompany.text = favoriteStock.companyName
        tvStockDetailCurrentPrice.text = String.format("$%.2f", favoriteStock.currentPrice)
        val changeInPriceText = String.format("$%.2f", favoriteStock.changeInPrice)
        tvStockDetailChange.text = changeInPriceText
        fetchPortfolioDetails(favoriteStock.tickerSymbol, favoriteStock.currentPrice)
        addRecommendationTrendFragment(favoriteStock.tickerSymbol)
        addEPSFragment(favoriteStock.tickerSymbol)
        addNewsFragment(favoriteStock.tickerSymbol)
        updateChangeColor(favoriteStock.changeInPrice)
        fetchQuotes(favoriteStock.tickerSymbol)
        fetchDetails(favoriteStock.tickerSymbol)

    }

    private fun updateChangeColor(change: Double) {
        Log.d("change", change.toString())
        if (change == 0.0) {
            tvStockDetailChange.setTextColor(resources.getColor(R.color.positiveChange))
            findViewById<ImageView>(R.id.up_down_detail).setImageResource(R.drawable.ic_trending_up)

        } else{
            findViewById<ImageView>(R.id.up_down_detail).setImageResource(R.drawable.ic_trending_down)
            tvStockDetailChange.setTextColor(resources.getColor(R.color.negativeChange))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        menuItemFavorite = menu.findItem(R.id.action_favorite)
        updateFavoriteIcon()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_favorite -> toggleFavorite()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun toggleFavorite() {
        isFavorite = !isFavorite
        updateFavoriteIcon()

        if (isFavorite) {
            addStockToFavorites()
        }
    }

    private fun addStockToFavorites() {
        val url = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/watchList"
        val params = JSONObject()
        params.put("tickerSymbol", stock?.tickerSymbol)
        params.put("name", stock?.name)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, params,
            { response ->
                Toast.makeText(this, "${stock?.tickerSymbol} is added to favorites", Toast.LENGTH_SHORT).show()
            },
            { error ->
                Toast.makeText(this, "Error adding to favorites: ${error.message}", Toast.LENGTH_LONG).show()
                isFavorite = false
                updateFavoriteIcon()
            }
        )

        queue.add(jsonObjectRequest)
    }

    private fun updateFavoriteIcon() {
        if (isFavorite) {
            menuItemFavorite.icon = ContextCompat.getDrawable(this, R.drawable.ic_fullstar)
        } else {
            menuItemFavorite.icon = ContextCompat.getDrawable(this, R.drawable.ic_star)
        }
    }
    private fun fetchDetails(tickerSymbol: String) {
        val detailUrl = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/stockDetails/$tickerSymbol"

        val detailRequest = JsonObjectRequest(Request.Method.GET, detailUrl, null,
            { response ->
                updateAboutSection(response)
                fetchCompanyPeers(tickerSymbol)
                fetchInsiderSentiments(tickerSymbol)
            },
            { error ->
                Log.e("StockDetailError", "Error fetching stock details", error)
            }
        )
        queue.add(detailRequest)
    }

    private fun fetchCompanyPeers(tickerSymbol: String) {
        val companyUrl = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/companyPeers/$tickerSymbol"

        val companyRequest = JsonArrayRequest(Request.Method.GET, companyUrl, null,
            { response ->
                val peersList = ArrayList<String>()
                for (i in 0 until response.length()) {
                    peersList.add(response.getString(i))
                }
                val recyclerView = findViewById<RecyclerView>(R.id.rvCompanyPeers)
                val adapter = PeersAdapter(peersList, this)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            },
            { error ->
                Log.e("StockDetailError", "Error fetching company peers", error)
            }
        )
        queue.add(companyRequest)
    }

    private fun fetchInsiderSentiments(tickerSymbol: String) {
        val sentimentUrl = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/insiderSentiment/$tickerSymbol"
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, sentimentUrl, null,
            { response ->
                val sentiments = response.getJSONArray("data")
                updateSentimentUI(sentiments)
            },
            { error ->
                Log.e("SentimentError", "Error fetching insider sentiments", error)
            }
        )
        queue.add(jsonObjectRequest)
    }

    private fun updateSentimentUI(sentiments: JSONArray) {
        var totalMSPR = 0.0
        var totalChange = 0
        var positiveMSPR = 0.0
        var positiveChange = 0
        var negativeMSPR = 0.0
        var negativeChange = 0

        for (i in 0 until sentiments.length()) {
            val sentiment = sentiments.getJSONObject(i)
            val mspr = sentiment.getDouble("mspr")
            val change = sentiment.getInt("change")

            totalMSPR += mspr
            totalChange += change

            if (mspr > 0) {
                positiveMSPR += mspr
                if (change > 0) positiveChange += change
            }
            if (mspr < 0) {
                negativeMSPR += mspr
                if (change < 0) negativeChange += change
            }
        }

        findViewById<TextView>(R.id.tvTotalMsrp).text = String.format("%.2f", totalMSPR)
        findViewById<TextView>(R.id.tvCompanyName).text = findViewById<TextView>(R.id.tvStockDetailCompany).text
        findViewById<TextView>(R.id.tvTotalChange).text = totalChange.toString()
        findViewById<TextView>(R.id.tvPositiveMsrp).text = String.format("%.2f", positiveMSPR)
        findViewById<TextView>(R.id.tvPositiveChange).text = positiveChange.toString()
        findViewById<TextView>(R.id.tvNegativeMsrp).text = String.format("%.2f", negativeMSPR)
        findViewById<TextView>(R.id.tvNegativeChange).text = negativeChange.toString()
    }




}
