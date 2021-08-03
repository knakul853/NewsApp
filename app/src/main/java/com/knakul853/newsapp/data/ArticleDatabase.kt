package com.knakul853.newsapp.data

import android.content.Context
import androidx.room.*
import com.knakul853.newsapp.data.models.Article
import com.knakul853.newsapp.data.models.Converters

@Database(
    entities = [Article::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class ArticleDatabase :RoomDatabase() {

    abstract fun getArticleDao():ArticleDao

    companion object{

        private var instance:ArticleDatabase? = null

        @Synchronized
        fun getArticleDataBase(context: Context): ArticleDatabase{

            if(instance == null){

                instance = Room.databaseBuilder(context.applicationContext, ArticleDatabase::class.java, "article_db")
                    .fallbackToDestructiveMigration()
                    .build()
            }


            return instance!!
        }
    }
}