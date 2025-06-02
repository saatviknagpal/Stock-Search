package com.example.assignment4

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.assignment4.R
import com.example.assignment4.NewsAdapter
import com.example.assignment4.NewsArticle
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Locale

class NewsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NewsAdapter
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.newsRecyclerView)
        layoutManager = LinearLayoutManager(view.context)
        recyclerView.layoutManager = layoutManager
        val tickerSymbol = arguments?.getString("TICKER_SYMBOL") ?: ""
        fetchNewsData(tickerSymbol)
    }

    private fun formatDate(timestamp: Long): String {
        val formatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return formatter.format(timestamp * 1000)
    }

    private fun fetchNewsData(ticker: String) {
        if (!isAdded) return
        val url = "https://backend-dot-webtechassignment3-418820.wl.r.appspot.com/api/stockNews/$ticker"
        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                val newsArticles = mutableListOf<NewsArticle>()
                for (i in 0 until response.length()) {
                    response.getJSONObject(i).also { jsonArticle ->
                        val imageUrl = jsonArticle.getString("image")
                        if (imageUrl.isNotEmpty()) {
                            val article = NewsArticle(
                                headline = jsonArticle.getString("headline"),
                                source = jsonArticle.getString("source"),
                                timeElapsed = jsonArticle.getLong("datetime"),
                                imageUrl = imageUrl,
                                articleUrl = jsonArticle.getString("url"),
                                summary = jsonArticle.getString("summary"),
                                publishedDate = formatDate(jsonArticle.getLong("datetime")) // Format the date here

                            )
                            newsArticles.add(article)
                        }
                    }
                }
                Log.d("newsArticles", newsArticles.toString())
                adapter = NewsAdapter(newsArticles.filter { it.imageUrl.isNotEmpty() }.take(20), parentFragmentManager)
                recyclerView.adapter = adapter
            },
            { error ->
                Log.e("Error", error.toString())
            }
        )
        Volley.newRequestQueue(requireContext()).add(jsonArrayRequest)
    }


    companion object {
        fun newInstance(tickerSymbol: String): NewsFragment {
            return NewsFragment().apply {
                arguments = Bundle().apply {
                    putString("TICKER_SYMBOL", tickerSymbol)
                }
            }
        }
    }
}
