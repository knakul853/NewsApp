package com.knakul853.newsapp.ui.fragments

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.knakul853.newsapp.R
import com.knakul853.newsapp.ui.NewsActivity
import com.knakul853.newsapp.ui.NewsViewModel

class ArticleFragment: Fragment(R.layout.fragment_article) {

    lateinit var viewModel: NewsViewModel
    var fab : FloatingActionButton? = null
    val args:ArticleFragmentArgs by navArgs()
    var webView :WebView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel
        webView = getView()?.findViewById(R.id.webView)
        fab = getView()?.findViewById(R.id.fab)

        val article = args.article
        webView?.apply {
            webViewClient = WebViewClient()
            loadUrl(article.url)
        }

        fab?.setOnClickListener{
            viewModel.savedArticle(article)
            Snackbar.make(view, "Article saved successfully!", Snackbar.LENGTH_SHORT).show()
        }
    }
}