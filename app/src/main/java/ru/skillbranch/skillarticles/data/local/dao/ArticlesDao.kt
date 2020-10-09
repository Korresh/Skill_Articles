package ru.skillbranch.skillarticles.data.local.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import ru.skillbranch.skillarticles.data.local.entities.Article
import ru.skillbranch.skillarticles.data.local.entities.ArticleFull
import ru.skillbranch.skillarticles.data.local.entities.ArticleItem

@Dao
interface ArticlesDao : BaseDao<Article> {
    @Transaction
    suspend fun upsert(list: List<Article>){
        insert(list)
            .mapIndexed{index, recordResult -> if(recordResult == -1L) list[index] else null}
            .filterNotNull()
            .also{ if (it.isNotEmpty()) update(it) }
    }

    @Query("""
         SELECT * FROM Articles
    """)
    fun findArticles(): LiveData<List<Article>>

    @Query("""
         SELECT * FROM Articles
         WHERE id = :id
    """)
    fun findArticleById(id: String): LiveData<Article>

    @Query("""
        SELECT * FROM ArticleItem
    """)
    fun findArticleItems(): LiveData<List<ArticleItem>>


    @Query("""
         SELECT * FROM ArticleItem
         WHERE category_id IN (:categoryIds)
    """)
    fun findArticleItemsByCategoryIds(categoryIds: List<String>): LiveData<List<ArticleItem>>

    @Query("""
         SELECT * FROM ArticleItem
         INNER JOIN article_tag_x_ref AS refs ON refs.a_id = id
         WHERE refs.t_id = :tag
    """)
    fun findArticlesByTagId(tag: String): LiveData<List<ArticleItem>>

    @RawQuery(observedEntities = [ArticleItem::class])
    fun findArticlesByRaw(simpleSQLiteQuery: SimpleSQLiteQuery): DataSource.Factory<Int, ArticleItem>

    @Query("""
        SELECT * FROM Articlefull
        WHERE id = :articleId
    """)
    fun findFullArticle(articleId: String): LiveData<ArticleFull>

    @Query("""
        SELECT id FROM articles ORDER BY date DESC LIMIT 1
    """)
    suspend fun findLastArticleId(): String?
    // тестовая функция
    @Query("SELECT * FROM articles")
    suspend fun findArticlesTest(): List<Article>
}