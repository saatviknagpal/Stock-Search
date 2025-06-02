package com.example.assignment4

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class NewsArticleDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_news_article_dialog, null)

        val headline = arguments?.getString("headline")
        val source = arguments?.getString("source")
        val publishedDate = arguments?.getString("publishedDate")
        val summary = arguments?.getString("summary")
        val articleUrl = arguments?.getString("articleUrl")

        view.findViewById<TextView>(R.id.article_headline).text = headline
        view.findViewById<TextView>(R.id.article_source).text = source
        view.findViewById<TextView>(R.id.article_published_date).text = publishedDate
        view.findViewById<TextView>(R.id.article_summary).text = summary

        val chromeButton = view.findViewById<ImageView>(R.id.button_open_in_chrome)
        val twitterButton = view.findViewById<ImageView>(R.id.button_share_twitter)
        val facebookButton = view.findViewById<ImageView>(R.id.button_share_facebook)

        chromeButton.setOnClickListener {
            openUrl(articleUrl)
        }

        twitterButton.setOnClickListener {
            val string = "Check out this Link:"
            val twitterUrl = "https://twitter.com/intent/tweet?text=$string&url=$articleUrl"
            openUrl(twitterUrl)
        }

        facebookButton.setOnClickListener {
            val facebookUrl = "https://www.facebook.com/sharer/sharer.php?u=$articleUrl"
            openUrl(facebookUrl)
        }


        val dialog = AlertDialog.Builder(requireActivity())
            .setView(view)
            .setPositiveButton(android.R.string.ok, null)
            .create()

        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_roundable_bg)
        return dialog

    }

    private fun openUrl(url: String?) {
        url?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            startActivity(intent)
        }
    }

    companion object {
        fun newInstance(article: NewsArticle): NewsArticleDialogFragment {
            return NewsArticleDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("headline", article.headline)
                    putString("source", article.source)
                    putString("publishedDate", article.publishedDate)
                    putString("summary", article.summary)
                    putString("articleUrl", article.articleUrl)
                }
            }
        }
    }
}
