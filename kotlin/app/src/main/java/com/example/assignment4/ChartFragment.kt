package com.example.assignment4

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ChartFragment1 : Fragment() {

    private lateinit var webView: WebView
    private lateinit var requestQueue: RequestQueue

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chart1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = view.findViewById(R.id.chartWebView)
        webView.settings.javaScriptEnabled = true

        requestQueue = Volley.newRequestQueue(context)

        val tickerSymbol = arguments?.getString("TICKER_SYMBOL") ?: "AAPL"
        fetchStockQuotesAndHourlyData(tickerSymbol)
    }

    private fun fetchStockQuotesAndHourlyData(ticker: String) {
        val url = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/stockQuote/$ticker"
        Log.d("bro", url);
        val stockQuoteRequest = JsonObjectRequest(Request.Method.GET, url, null, { response ->
            if (response.has("t")) {
                val timestamp = response.getLong("t")
                val change = response.getDouble("d")
                fetchHourlyData(ticker, timestamp, change)
            }
        }, { error ->
            error.printStackTrace()
        })
        requestQueue.add(stockQuoteRequest)
    }

    private fun fetchHourlyData(ticker: String, timestamp: Long, change: Double) {
        val url = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/hourlyData/$ticker?timestamp=$timestamp"
        Log.d("inhour", url)
        val hourlyDataRequest = JsonObjectRequest(Request.Method.GET, url, null, { response ->
            val stockPriceData = parseChartData(response)
            loadChartIntoWebView(stockPriceData, ticker, change)
        }, { error ->
            error.printStackTrace()
        })
        requestQueue.add(hourlyDataRequest)
    }

    private fun parseChartData(response: JSONObject): List<Pair<Long, Double>> {
        val stockPriceData = mutableListOf<Pair<Long, Double>>()

        if (!response.has("results")) {
            Log.e("StockDetailActivity", "No 'results' key found in the JSON response")
            return stockPriceData
        }

        val results = response.getJSONArray("results")

        for (i in 0 until results.length()) {
            val item = results.getJSONObject(i)
            val time = item.getLong("t")
            val price = item.getDouble("c")
            stockPriceData.add(Pair(time, price))
        }
        return stockPriceData
    }

    private fun loadChartIntoWebView(stockPriceData: List<Pair<Long, Double>>, symbol: String, change: Double) {
        Log.d("chart","chart")
        val chartDataString = stockPriceData.joinToString(", ") { "[${it.first}, ${it.second}]" }
        val color = if (change >= 0) "#198754" else "#FF0000"

        val htmlData = """
        <!DOCTYPE html>
        <html>
        <head>
            <script src="https://code.highcharts.com/highcharts.js"></script>
        </head>
        <body>
            <div id="container" style="width:100%; height:100%;"></div>
            <script>
                Highcharts.chart('container', {
                    chart: {
                        type: 'line',
                        zoomType: 'x',
                        reflow: true
                    },
                    title: {
                        text: '${symbol} Hourly Price Variation'
                    },
                    xAxis: {
                        type: 'datetime',
                        dateTimeLabelFormats: {
                            hour: '%H:%M',
                            day: '%e. %b'
                        },
                        labels: {
                            format: '{value:%H:%M}'
                        }
                    },
                    yAxis: [{
                        title: {
                            text: ''
                        },
                        opposite: true,
                        tickAmount: 5
                    }],
                    responsive: {
                        rules: [{
                            condition: {
                                maxWidth: 600
                            }
                        }]
                    },
                    legend: {
                        enabled: false
                    },
                    series: [{
                        name: '',
                        data: [$chartDataString],
                        lineWidth: 2,
                        color: '$color',
                        marker: {
                            enabled: false
                        }
                    }],
                    navigation: {
                        menuItemStyle: {
                            fontSize: '10px'
                        }
                    }
                });
            </script>
        </body>
        </html>
    """

        webView.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null)
    }

    companion object {
        fun newInstance(tickerSymbol: String): ChartFragment1 {
            return ChartFragment1().apply {
                arguments = Bundle().apply {
                    putString("TICKER_SYMBOL", tickerSymbol)
                }
            }
        }
    }
}
