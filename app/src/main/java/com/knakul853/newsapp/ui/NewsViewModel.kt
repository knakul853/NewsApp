package com.knakul853.newsapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knakul853.newsapp.NewsApplication
import com.knakul853.newsapp.data.models.Article
import com.knakul853.newsapp.data.models.NewsResponse
import com.knakul853.newsapp.repository.NewsRepository
import com.knakul853.newsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
    app: Application,
    private val repository: NewsRepository
): AndroidViewModel(app) {

    val breakingNews:MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingPageNumber = 1
    var searchPageNumber = 1

    //pagination
    var latestBreakingNews: NewsResponse?=null
    var latestSavedNews: NewsResponse? = null


    init {
        getBreakingNews("us")
    }

     fun getBreakingNews(countryCode:String) = viewModelScope.launch {
            safeBreakingNewsCall(countryCode)
    }
    fun getSearchNews(searchNewsQuery:String) = viewModelScope.launch {
        safeearchNewsCall(searchNewsQuery)
    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse>{

        if(response.isSuccessful){
            response.body()?.let {
                breakingPageNumber++
                if(latestBreakingNews == null){
                    latestBreakingNews = it
                }
                else{

                    val oldArticle = latestBreakingNews?.articles
                    val newArticle = it.articles
                    oldArticle?.addAll(newArticle)
                }
                return Resource.Success(latestBreakingNews?:it)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse>{

        if(response.isSuccessful){
            response.body()?.let {
                searchPageNumber++
                if(latestSavedNews == null){
                    latestSavedNews = it
                }
                else{

                    val oldArticle = latestSavedNews?.articles
                    val newArticle = it.articles
                    oldArticle?.addAll(newArticle)
                }
                return Resource.Success(latestSavedNews?:it)
            }
        }
        return Resource.Error(response.message())
    }

    fun savedArticle(article: Article){
        repository.insertArticle(article)
    }
    fun getSavedArticle() = repository.getSavedArticle()

    fun deleteArticle(article: Article){
        repository.deleteArticle(article)
    }

    private suspend fun safeBreakingNewsCall(countryCode: String){
        breakingNews.postValue(Resource.Loading())

        try{
            if(hasInternetConnection()){
                val response = repository.getBreakingNews(countryCode, breakingPageNumber)
                breakingNews.postValue(handleBreakingNewsResponse(response))
            }
            else{
                breakingNews.postValue(Resource.Error("No internet connection!"))
            }
        }
        catch (t: Throwable){
            when(t){
                is IOException -> breakingNews.postValue(Resource.Error("Network failure"))
                else -> breakingNews.postValue(Resource.Error("Conversion error"))
            }
        }
    }
    private suspend fun safeearchNewsCall(searchNewsQuery: String){
        searchNews.postValue(Resource.Loading())

        try{
            if(hasInternetConnection()){
                val response = repository.searchNews(searchNewsQuery, searchPageNumber)
                searchNews.postValue(handleSearchNewsResponse(response))
            }
            else{
                searchNews.postValue(Resource.Error("No internet connection!"))
            }
        }
        catch (t: Throwable){
            when(t){
                is IOException -> searchNews.postValue(Resource.Error("Network failure"))
                else -> searchNews.postValue(Resource.Error("Conversion error"))
            }
        }
    }


    private fun hasInternetConnection():Boolean{
        val connectivityManager = getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        )as ConnectivityManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val activeNetwork = connectivityManager.activeNetwork?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)?:return false

            return when{
                capabilities.hasTransport(TRANSPORT_WIFI)-> return true
                capabilities.hasTransport(TRANSPORT_CELLULAR)-> return true
                capabilities.hasTransport(TRANSPORT_ETHERNET)-> return true
                else -> return false
            }
        }


        else{
            connectivityManager.activeNetworkInfo?.run {
                return when(type){
                    TYPE_WIFI -> true
                    TYPE_MOBILE-> true
                    TYPE_ETHERNET -> true
                    else -> false

                }
            }
        }

        return false
    }
}