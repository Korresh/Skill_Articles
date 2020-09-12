package ru.skillbranch.skillarticles.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ru.skillbranch.skillarticles.data.local.entities.ArticlePersonalInfo

@Dao
interface ArticlePersonalInfosDao : BaseDao<ArticlePersonalInfo> {

    @Transaction
    suspend fun upsert(list: List<ArticlePersonalInfo>){
        insert(list)
            .mapIndexed{index, recordResult -> if(recordResult == -1L) list[index] else null}
            .filterNotNull()
            .also{ if (it.isNotEmpty()) update(it) }
    }

    @Query("""
        UPDATE article_personal_info SET is_like = NOT is_like, updated_at = CURRENT_TIMESTAMP
        WHERE article_id = :articleId
    """)
    suspend fun toggleLike(articleId:String):Int

    @Query("""
        UPDATE article_personal_info SET is_bookmark = NOT is_bookmark, updated_at = CURRENT_TIMESTAMP
        WHERE article_id = :articleId
    """)
    suspend fun toggleBookmark(articleId:String):Int

    @Transaction
    suspend fun toggleBookmarkOrInsert(articleId:String) : Boolean{
        if (toggleBookmark(articleId)==0) insert(ArticlePersonalInfo(articleId = articleId, isBookmark = true))
        return isBookmarked(articleId)
    }
    @Query("""SELECT is_bookmark FROM article_personal_info
        WHERE article_id = :articleId
    """)
    suspend fun isBookmarked(articleId: String):Boolean


    @Query("""
        SELECT * 
        FROM article_personal_info
        WHERE article_id = :articleId
    """)

    @Transaction
    suspend fun toggleLikeOrInsert(articleId:String): Boolean{
        if (toggleLike(articleId)==0) {
            insert(ArticlePersonalInfo(articleId = articleId, isLike = true))
            return true
        }
        return false
    }

    @Query("""SELECT * FROM article_personal_info""")
    fun findPersonalInfos(): LiveData<List<ArticlePersonalInfo>>

    @Query("""
        SELECT * 
        FROM article_personal_info
        WHERE article_id = :articleId
    """)
    fun findPersonalInfos(articleId: String): LiveData<ArticlePersonalInfo>

    @Transaction
    suspend fun addBookmark(articleId: String) {
        toggleBookmarkOrInsert(articleId)
        if (!isBookmarked(articleId)) toggleBookmark(articleId)
    }

    @Transaction
    suspend fun removeBookmark(articleId: String) {
        toggleBookmarkOrInsert(articleId)
        if (isBookmarked(articleId)) toggleBookmark(articleId)
    }
    // тестовая функция
    @Query("SELECT * FROM article_personal_info WHERE article_id = :articleId")
    suspend fun findPersonalInfosTest(articleId: String): ArticlePersonalInfo
}