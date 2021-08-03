package com.knakul853.newsapp.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.knakul853.newsapp.R
import com.knakul853.newsapp.ui.NewsActivity
import com.knakul853.newsapp.ui.NewsViewModel

class SavedNewsFragment:Fragment(R.layout.fragment_saved_news) {

    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    var rvSavedNews: RecyclerView? = null
    var progressBar: ProgressBar? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvSavedNews = getView()?.findViewById(R.id.rvSavedNews)
        progressBar = getView()?.findViewById(R.id.paginationProgressBar)

        viewModel = (activity as NewsActivity).viewModel
        setupAdapter()
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_savedNewsFragment_to_article, bundle)
        }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP  or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val currentPosition = viewHolder.adapterPosition
               if((direction == ItemTouchHelper.RIGHT || direction == ItemTouchHelper.LEFT) && currentPosition != null){

                   val article = newsAdapter.differ.currentList[currentPosition]
                   viewModel.deleteArticle(article)
                   Snackbar.make(view, "Successfully deleted article", Snackbar.LENGTH_LONG)
                       .apply {
                           setAction("Undo"){
                               viewModel.savedArticle(article)
                           }
                           show()
                       }
               }
            }

        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(rvSavedNews)
        }

        viewModel.getSavedArticle().observe(viewLifecycleOwner, Observer {
            articles->
            newsAdapter.differ.submitList(articles)
        })
    }

    private fun setupAdapter() {
        newsAdapter = NewsAdapter()

        rvSavedNews?.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)

        }

    }
}