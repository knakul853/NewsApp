package com.knakul853.newsapp.repository

import androidx.lifecycle.LiveData
import com.knakul853.newsapp.api.RetrofitInstance
import com.knakul853.newsapp.data.ArticleDao
import com.knakul853.newsapp.data.ArticleDatabase
import com.knakul853.newsapp.data.models.Article
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewsRepository(
    private val db:ArticleDatabase
) {
    var dao:ArticleDao

    init {
       dao = db.getArticleDao()
    }

    suspend fun getBreakingNews(countryCode:String, pageNumber:Int) = RetrofitInstance.api.getBreakingNews(countryCode, pageNumber )

    suspend fun searchNews(searchQuery: String, pageNumber: Int) = RetrofitInstance.api.searchForNews(searchQuery, pageNumber)

    fun getSavedArticle(): LiveData<List<Article>>{
        return dao.getAllArticle()
    }

    fun deleteArticle(article: Article){
        CoroutineScope(Dispatchers.Default).launch{
            dao.deleteArticle(article)
        }
    }
    fun insertArticle(article: Article){
        CoroutineScope(Dispatchers.Default).launch {
            dao.insertArticle(article)
        }
    }

}