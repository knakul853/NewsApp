package com.knakul853.newsapp.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.knakul853.newsapp.data.models.Article

@Dao
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: Article):Long

    @Query("select * from article_table")
    fun getAllArticle():LiveData<List<Article>>

    @Delete
    suspend fun deleteArticle(article: Article)
}