package ru.skillbranch.skillarticles.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.skillbranch.skillarticles.data.local.entities.ArticleContent

@Dao
interface ArticleContentsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert (obj:ArticleContent): Long

    @Query("delete from article_contents where article_id = :articleId")
    suspend fun delete(articleId: String)

    // тестовая функция
    @Query("SELECT * FROM article_contents")
    suspend fun findArticlesContentsTest(): List<ArticleContent>

}