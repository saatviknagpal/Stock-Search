package com.example.assignment4

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray

class EPSFragment : Fragment() {

    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_eps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = view.findViewById(R.id.epsWebView)
        webView.settings.javaScriptEnabled = true

        val tickerSymbol = arguments?.getString("TICKER_SYMBOL") ?: "AAPL"
        fetchEPSData(tickerSymbol)
    }

    private fun fetchEPSData(ticker: String) {
        val url = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/earnings/$ticker"
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                generateChartScript(response, ticker)
            },
            { error ->
                Log.e("EPSFragment", "Error fetching EPS data", error)
            }
        )
        Volley.newRequestQueue(context).add(jsonArrayRequest)
    }

    private fun generateChartScript(jsonArray: JSONArray, symbol: String) {
        val categories = mutableListOf<String>()
        val actualData = mutableListOf<Double>()
        val estimateData = mutableListOf<Double>()

        for (i in 0 until jsonArray.length()) {
            jsonArray.getJSONObject(i).apply {
                categories.add("\"${getString("period")} Surprise: ${getDouble("surprise")}\"") // Ensure each string is properly quoted
                actualData.add(getDouble("actual"))
                estimateData.add(getDouble("estimate"))
            }
        }

        val categoriesJs = categories.joinToString(separator = ",", prefix = "[", postfix = "]")
        val actualDataJs = actualData.joinToString(separator = ",", prefix = "[", postfix = "]")
        val estimateDataJs = estimateData.joinToString(separator = ",", prefix = "[", postfix = "]")


        Log.d("epsC", categoriesJs)
        Log.d("epsA", actualDataJs)
        Log.d("epsE", estimateDataJs)

        val script = """
        <html>
        <head>
            <script src="https://code.highcharts.com/highcharts.js"></script>
        </head>
        <body>
            <div id="container" style="width: 100%; height: 100%;"></div>
            <script>
                Highcharts.chart('container', {
                    chart: {
                        type: 'spline',
                    },
                    title: {
                        text: 'Historical EPS Surprises',
                        align: 'center'
                    },
                    xAxis: {
                        categories: $categoriesJs
                    },
                    yAxis: {
                        title: {
                            text: 'Quarterly EPS'
                        }
                    },
                    tooltip: {
                        headerFormat: '<b>{series.name}</b><br/>',
                        pointFormat: '{series.name}: <b>{point.y}</b><br/>{point.category}'
                    },
                    series: [{
                        name: 'Actual',
                        data: $actualDataJs
                    }, {
                        name: 'Estimate',
                        data: $estimateDataJs
                    }]
                });
            </script>
        </body>
        </html>
    """.trimIndent()

        webView.loadDataWithBaseURL(null, script, "text/html", "UTF-8", null)
    }

    companion object {
        fun newInstance(tickerSymbol: String): EPSFragment {
            return EPSFragment().apply {
                arguments = Bundle().apply {
                    putString("TICKER_SYMBOL", tickerSymbol)
                }
            }
        }
    }
}
