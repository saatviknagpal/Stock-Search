package com.example.assignment4

import android.os.Bundle
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

class ChartFragment2 : Fragment() {

    private lateinit var webView: WebView
    private lateinit var requestQueue: RequestQueue

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chart2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = view.findViewById(R.id.chartWebView2)
        webView.settings.javaScriptEnabled = true

        requestQueue = Volley.newRequestQueue(context)

        val tickerSymbol = arguments?.getString("TICKER_SYMBOL") ?: "AAPL"
        fetchChartData(tickerSymbol)
    }

    private fun fetchChartData(ticker: String) {
        val url = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/charts/$ticker"

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null, { response ->
            val stockPriceData = parseChartData(response)
            loadChartIntoWebView(stockPriceData, ticker)
        }, { error ->
            error.printStackTrace()
        })

        requestQueue.add(jsonObjectRequest)
    }

    private fun parseChartData(jsonResponse: JSONObject): Pair<List<List<Any>>, List<List<Any>>> {
        val results = jsonResponse.optJSONArray("results") ?: return Pair(emptyList(), emptyList())

        val stockPriceData = mutableListOf<List<Any>>()
        val volumeData = mutableListOf<List<Any>>()

        for (i in 0 until results.length()) {
            val item = results.optJSONObject(i) ?: continue
            val time = item.optLong("t")
            val open = item.optDouble("o")
            val high = item.optDouble("h")
            val low = item.optDouble("l")
            val close = item.optDouble("c")
            val volume = item.optDouble("v")

            stockPriceData.add(listOf(time, open, high, low, close))
            volumeData.add(listOf(time, volume))
        }

        return Pair(stockPriceData, volumeData)
    }


    private fun loadChartIntoWebView(stockPriceData: Pair<List<List<Any>>, List<List<Any>>>, symbol: String) {
        val stockPriceDataString = stockPriceData.first.joinToString(",") { it.joinToString(prefix = "[", postfix = "]") }
        val volumeDataString = stockPriceData.second.joinToString(",") { it.joinToString(prefix = "[", postfix = "]") }

        val htmlData = """
    <!DOCTYPE html>
    <html>
    <head>
        <script src="https://code.highcharts.com/stock/highstock.js"></script>
        <script src="https://code.highcharts.com/stock/modules/exporting.js"></script>
        <script src="https://code.highcharts.com/stock/modules/data.js"></script>
        <script src="https://code.highcharts.com/stock/indicators/indicators-all.js"></script>
        <script src="https://code.highcharts.com/stock/indicators/volume-by-price.js"></script>
    </head>
    <body>
        <div id="container" style="height: 100%; min-width: 310px"></div>
        <script>
            Highcharts.stockChart('container', {
                rangeSelector: {
                    selected: 2
                },
                title: {
                    text: '$symbol Historical'
                },
                subtitle: {
                    text: 'With SMA and Volume by Price technical indicators'
                },
                yAxis: [{
                    startOnTick: false,
                    endOnTick: false,
                    labels: {
                        align: 'right',
                        x: -3
                    },
                    title: {
                        text: 'OHLC'
                    },
                    height: '60%',
                    lineWidth: 2,
                    resize: {
                        enabled: true
                    }
                }, {
                    labels: {
                        align: 'right',
                        x: -3
                    },
                    title: {
                        text: 'Volume'
                    },
                    top: '65%',
                    height: '35%',
                    offset: 0,
                    lineWidth: 2
                }],
                tooltip: {
                    split: true
                },
                series: [{
                    type: 'candlestick',
                    name: '$symbol',
                    id: 'aapl',
                    zIndex: 2,
                    data: [$stockPriceDataString]
                }, {
                    type: 'column',
                    name: 'Volume',
                    id: 'volume',
                    data: [$volumeDataString],
                    yAxis: 1
                }, {
                    type: 'vbp',
                    linkedTo: 'aapl',
                    params: {
                        volumeSeriesID: 'volume'
                    },
                    dataLabels: {
                        enabled: false
                    },
                    zoneLines: {
                        enabled: false
                    }
                }, {
                    type: 'sma',
                    linkedTo: 'aapl',
                    zIndex: 1,
                    marker: {
                        enabled: false
                    }
                }]
            });
        </script>
    </body>
    </html>
    """

        webView.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null)
    }



    companion object {
        fun newInstance(tickerSymbol: String): ChartFragment2 {
            return ChartFragment2().apply {
                arguments = Bundle().apply {
                    putString("TICKER_SYMBOL", tickerSymbol)
                }
            }
        }
    }
}
