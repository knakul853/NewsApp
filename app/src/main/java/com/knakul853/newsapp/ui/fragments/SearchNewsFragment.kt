package com.knakul853.newsapp.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.knakul853.newsapp.R
import com.knakul853.newsapp.ui.NewsActivity
import com.knakul853.newsapp.ui.NewsViewModel
import com.knakul853.newsapp.util.Constant
import com.knakul853.newsapp.util.Constant.Companion.SEARCH_NEWS_TIME_DELAY
import com.knakul853.newsapp.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchNewsFragment: Fragment(R.layout.fragment_search_news) {

    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    var SearchNews: RecyclerView? = null
    var progressBar: ProgressBar? = null
    var etSearch:EditText? = null
    private val TAG = "SearchNewsFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SearchNews = getView()?.findViewById(R.id.rvSearchNews)
        progressBar = getView()?.findViewById(R.id.paginationProgressBar)
        etSearch = getView()?.findViewById(R.id.etSearch)

        setupAdapter()

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_searchNewsFragment_to_article, bundle)
        }

        viewModel = (activity as NewsActivity).viewModel

        var job:Job? = null

        etSearch?.addTextChangedListener { editabale ->

            job?.cancel()

            job = MainScope().launch {

                delay(SEARCH_NEWS_TIME_DELAY)
                if(editabale.toString().isNotEmpty()){
                    viewModel.getSearchNews(editabale.toString())

                }
            }

        }
        viewModel.searchNews.observe(viewLifecycleOwner, Observer {
                response ->
            when(response){
                is Resource.Success ->{
                    hideProgressBar()
                    response.data?.let {
                            newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles)
                        val totalPages = newsResponse.totalResults / Constant.QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.breakingPageNumber == totalPages
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {
                        Toast.makeText(requireActivity(), "An error occurred: $it", Toast.LENGTH_LONG).show()

                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    val scrollListner = object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManger = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManger.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManger.childCount
            val totalItemCount = layoutManger.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >=0
            val isTotalMoreThanVisible = totalItemCount >= Constant.QUERY_PAGE_SIZE

            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling
            if(shouldPaginate){
                viewModel.getSearchNews(etSearch?.text.toString())
                isScrolling = false
            }
        }
    }

    private fun hideProgressBar() {
        progressBar?.visibility = View.GONE
    }
    private fun showProgressBar(){
        progressBar?.visibility =View.VISIBLE
    }

    private fun setupAdapter() {
        newsAdapter = NewsAdapter()

       SearchNews?.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
           addOnScrollListener(scrollListner)

        }

    }
}