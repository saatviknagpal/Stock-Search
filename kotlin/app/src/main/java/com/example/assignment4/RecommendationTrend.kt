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
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class RecommendationTrendFragment : Fragment() {

    private lateinit var webView: WebView
    private lateinit var requestQueue: RequestQueue

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recommendation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = view.findViewById(R.id.chartWebView3)
        webView.settings.javaScriptEnabled = true

        requestQueue = Volley.newRequestQueue(requireContext())

        val tickerSymbol = arguments?.getString("TICKER_SYMBOL") ?: "AAPL"
        fetchTrendData(tickerSymbol)
    }

    private fun fetchTrendData(ticker: String) {
        val url = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/recommendation/$ticker"
        Log.d("urlBoy", url)
        val jsonObjectRequest = JsonArrayRequest(Request.Method.GET, url, null, { response ->
            val trendData = parseTrendData(response)
            loadTrendChartIntoWebView(trendData, ticker)
        }, { error ->
            Log.e("RecommendationTrend", "Error fetching data", error)
        })
        requestQueue.add(jsonObjectRequest)
    }

    private fun parseTrendData(jsonArray: JSONArray): List<List<Any>> {
        val seriesList = List(6) { mutableListOf<Any>() }

        for (i in 0 until jsonArray.length()) {
            jsonArray.getJSONObject(i).apply {
                seriesList[0].add(getInt("strongBuy"))
                seriesList[1].add(getInt("buy"))
                seriesList[2].add(getInt("hold"))
                seriesList[3].add(getInt("sell"))
                seriesList[4].add(getInt("strongSell"))
                seriesList[5].add(getString("period"))
            }
        }
        Log.d("series", seriesList.toString())
        return seriesList
    }

    private fun loadTrendChartIntoWebView(trendData: List<List<Any>>, symbol: String) {
        val script = generateChartScript(trendData, symbol)
        webView.loadDataWithBaseURL(null, script, "text/html", "UTF-8", null)
    }

    private fun generateChartScript(trendData: List<List<Any>>, symbol: String): String {
        val categories = trendData.last().joinToString(",") { "\"$it\"" }
        Log.d("cat", categories)
        val seriesData = trendData.dropLast(1).mapIndexed { index, data ->
            val name = listOf("Strong Buy", "Buy", "Hold", "Sell", "Strong Sell")[index]
            val color = listOf("#195F32", "#23AF50", "#AF7D28", "#F05050", "#732828")[index]
            """{
            name: "$name",
            data: [${data.joinToString(",")}],
            color: "$color",
        }"""
        }.joinToString(",")
        Log.d("seriesD", seriesData)
        return """
    <!DOCTYPE html>
    <html>
    <head>
        <script src="https://code.highcharts.com/highcharts.js"></script>
    </head>
    <body>
        <div id="container" style="width: 100%; height: 100%;"></div>
        <script>
            Highcharts.chart('container', {
                chart: {
                    type: 'column',
                },
                title: {
                    text: 'Recommendation Trends',
                    align: 'center'
                },
                xAxis: {
                    categories: [$categories]
                },
                yAxis: {
                    min: 0,
                    title: {
                        text: '#Analysis'
                    },
                    stackLabels: {
                        enabled: true
                    }
                },
                tooltip: {
                    headerFormat: '<b>{point.x}</b><br/>',
                    pointFormat: '{series.name}: {point.y}<br/>Total: {point.stackTotal}'
                },
                plotOptions: {
                    column: {
                        stacking: 'normal',
                        dataLabels: {
                            enabled: true
                        }
                    }
                },
                series: [$seriesData]
            });
        </script>
    </body>
    </html>
    """.trimIndent()
    }


    companion object {
        fun newInstance(tickerSymbol: String): RecommendationTrendFragment {
            return RecommendationTrendFragment().apply {
                arguments = Bundle().apply {
                    putString("TICKER_SYMBOL", tickerSymbol)
                }
            }
        }
    }
}
