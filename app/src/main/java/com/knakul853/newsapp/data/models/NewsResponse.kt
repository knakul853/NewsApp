package com.knakul853.newsapp.data.models

import com.knakul853.newsapp.data.models.Article

data class NewsResponse(
    val articles: MutableList<Article>,
    val status: String,
    val totalResults: Int
)