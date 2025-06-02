package com.example.assignment4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment4.R
import com.example.assignment4.NewsArticle
import com.squareup.picasso.Picasso

class NewsAdapter(private val newsList: List<NewsArticle>, private val fragmentManager: FragmentManager) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {


    class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.news_image)
        val headlineView: TextView = view.findViewById(R.id.news_title)
        val source: TextView = view.findViewById(R.id.news_source)
        val elapsedTime: TextView = view.findViewById(R.id.news_elapsed_time)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val layoutId = when (viewType) {
            VIEW_TYPE_TOP_NEWS -> R.layout.top_news
            VIEW_TYPE_STANDARD_NEWS -> R.layout.news_item
            else -> throw IllegalStateException("Invalid view type")
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)

        return NewsViewHolder(view)

    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_TOP_NEWS else VIEW_TYPE_STANDARD_NEWS
    }

    companion object {
        private const val VIEW_TYPE_TOP_NEWS = 0
        private const val VIEW_TYPE_STANDARD_NEWS = 1
    }


    fun getElapsedTime(timestamp: Long): String {
        val seconds = (System.currentTimeMillis() / 1000) - timestamp
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "$days days ago"
            hours > 0 -> "$hours hours ago"
            minutes > 0 -> "$minutes minutes ago"
            else -> "$seconds seconds ago"
        }
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = newsList[position]
        holder.headlineView.text = article.headline
        holder.source.text = article.source
        holder.elapsedTime.text = getElapsedTime(article.timeElapsed)
        Picasso.get()
            .load(article.imageUrl)
            .resize(500, 500)
            .centerCrop()
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            val dialogFragment = NewsArticleDialogFragment.newInstance(article)
            dialogFragment.show(fragmentManager, "newsArticle")
        }
    }



    override fun getItemCount(): Int = newsList.size
}
