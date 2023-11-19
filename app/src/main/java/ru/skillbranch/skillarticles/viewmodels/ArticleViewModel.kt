package ru.skillbranch.skillarticles.viewmodels

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import ru.skillbranch.skillarticles.data.ArticleData
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.extensions.asMap
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.data.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.format
import ru.skillbranch.skillarticles.extensions.indexesOf


class ArticleViewModel(private val articleId: String, savedStateHandle: SavedStateHandle) :
    BaseViewModel<ArticleState>(ArticleState(),savedStateHandle),IArticleViewModel {
    private val repository = ArticleRepository

    init{
        //set custom saved provider for non serializable or custom states
        savedStateHandle.setSavedStateProvider("state"){
            currentState.toBundle()
        }
        //subscribe on mutable data
        subscribeOnDataSource(getArticleData()){article,state ->
           article ?: return@subscribeOnDataSource null
           state.copy(
                   shareLink = article.shareLink,
                   title = article.title,
                   category = article.category,
                   author = article.author,
                   categoryIcon = article.categoryIcon,
                   date = article.date.format()
           )
       }
        subscribeOnDataSource(getArticleContent()){ content, state->
            content ?: return@subscribeOnDataSource null
            state.copy(
                    isLoadingContent = false,
                    content = content
            )
        }

        subscribeOnDataSource(getArticlePersonalInfo()){ info, state->
            info ?: return@subscribeOnDataSource null
            state.copy(
                    isBookmark = info.isBookmark,
                    isLike = info.isLike
            )
        }
        subscribeOnDataSource(repository.getAppSettings()){ settings, state ->
            state.copy(
                    isDarkMode = settings.isDarkMode,
                    isBigText = settings.isBigText
            )
        }

    }

    //load text from network
    override fun getArticleContent(): LiveData<List<String>?> {
        return repository.loadArticleContent(articleId)
    }

    //load data from db
    override fun getArticleData():LiveData<ArticleData?>{
        return repository.getArticle(articleId)
    }

    //load data from db
    override fun getArticlePersonalInfo(): LiveData<ArticlePersonalInfo?>{
        return repository.loadArticlePersonalInfo(articleId)
    }

    //app settings
    override fun handleNightMode(){
        val settings = currentState.toAppSettings()
        repository.updateSettings(settings.copy(isDarkMode = !settings.isDarkMode))
    }

    override fun handleUpText(){
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = true))
    }

    override fun handleDownText(){
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = false))
    }

    //personal article info
    override fun handleBookmark(){
        val toggleBookmark = {
            val info = currentState.toArticlePersonalInfo()
            repository.updateArticlePersonalInfo(info.copy(isBookmark = !info.isBookmark))
        }
        toggleBookmark()

        val msg = if (currentState.isBookmark) Notify.TextMessage("Add to bookmarks")
        else {
            Notify.ActionMessage(
                    "Remove from bookmarks", //message
                    "Bookmark be added!",//action label on snackbar
                    toggleBookmark //handler function, if press "No, still like it" on snackbar, then toggle again
            )
        }
        notify(msg)
    }

    override fun handleLike(){

        val toggleLike = {
            val info = currentState.toArticlePersonalInfo()
            repository.updateArticlePersonalInfo(info.copy(isLike = !info.isLike))
        }
        toggleLike()

        val msg = if (currentState.isLike) Notify.TextMessage("Mark is liked")
        else {
            Notify.ActionMessage(
                    "Don`t like it anymore", //message
                    "No, still like it",//action label on snackbar
                    toggleLike //handler function, if press "No, still like it" on snackbar, then toggle again
            )
        }
        notify(msg)
    }

    //not implemented
    override fun handleShare(){
        val msg = "Share is not implemented"
        notify(Notify.ErrorMessage(msg, "OK", null))
    }

    //session state
    override fun handleToggleMenu(){
        updateState { it.copy(isShowMenu = !it.isShowMenu) }
    }

    override fun handleSearchMode(isSearch: Boolean){
        updateState { it.copy(isSearch = isSearch, isShowMenu = false, searchPosition = 0) }
    }

    override fun handleSearch(query: String?){
        query ?: return
        val result = currentState.content.firstOrNull().indexesOf(query)
            .map{it to it+query.length}
        updateState { it.copy(searchQuery = query, searchResults = result) }
    }

    override fun handleUpResult() {
        updateState { it.copy(searchPosition = it.searchPosition.dec()) }
    }

    override fun handleDownResult() {
        updateState { it.copy(searchPosition = it.searchPosition.inc()) }
    }
}

data class ArticleState(
    val isAuth: Boolean = false, //пользователь авторизован
    val isLoadingContent: Boolean = true,//контент загружается
    val isLoadingReviews: Boolean = true,//отзывы загружаются
    val isLike: Boolean = false,//отмечено как Like
    val isBookmark: Boolean = false,//в закладках
    val isShowMenu:Boolean =false,//отображается меню
    val isBigText: Boolean = false,//шрифт увеличен
    val isDarkMode: Boolean = false,//темный режим
    val isSearch: Boolean = false,//режим поиска
    val searchQuery: String? = null,//поисковый запрос
    val searchResults: List<Pair<Int,Int>> = emptyList(),//результаты поиска (стартовая и конечная позиции)
    val searchPosition: Int = 0,//текущая позиция найденного результата
    val shareLink: String? = null,//ссылка Share
    val title: String? = null,//заголовок статьи
    val category: String? = null,//категория
    val categoryIcon: Any? = null,//иконка категории
    val date: String? = null,//дата публикации
    val author: Any? = null,//автор статьи
    val poster: String? = null,//обложка статьи
    val content: List<String> = emptyList(),//контент
    val reviews: List<Any> = emptyList() //комментарии
): VMState{
    override fun toBundle(): Bundle {
        val map = copy(content = emptyList(), isLoadingContent = true)
            .asMap()
            .toList()
            .toTypedArray()
        return bundleOf(*map)
    }

    override fun fromBundle(bundle: Bundle): ArticleState {
        val map = bundle.keySet().associateWith { bundle[it] }
        return copy(
            isAuth = map["isAuth"] as Boolean,
            isLoadingContent = map["isLoadingContent"] as Boolean,
            isLoadingReviews = map["isLoadingReviews"] as Boolean,
            isLike = map["isLike"] as Boolean,
            isBookmark = map["isBookmark"] as Boolean,
            isShowMenu = map["isShowMenu"] as Boolean,
            isBigText = map["isBigText"] as Boolean,
            isDarkMode = map["isDarkMode"] as Boolean,
            isSearch = map["isSearch"] as Boolean,
            searchQuery = map["searchQuery"] as String?,
            searchResults = map["searchResults"] as List<Pair<Int,Int>>,
            searchPosition = map["searchPosition"] as Int,
            shareLink = map["shareLink"] as String?,
            title = map["title"] as String?,
            category = map["category"] as String?,
            categoryIcon = map["categoryIcon"] as Any?,
            date = map["date"] as String?,
            author = map["author"] as Any?,
            poster = map["poster"] as String?,
            content = map["content"] as List<String>,
            reviews = map["reviews"] as List<Any>
        )
    }
}

data class BottombarData(
    val isLike: Boolean = false, //отмечена как Like
    val isBookmark: Boolean = false, //в закладках
    val isShowMenu: Boolean = false, //отображается меню
    val isSearch: Boolean = false, //режим поиска
    val resultsCount: Int = 0, //количество найденных вхождений
    val searchPosition: Int = 0 //текущая позиция поиска
)

data class SubmenuData(
    val isShowMenu: Boolean = false, //отображается меню
    val isBigText: Boolean = false, //шрифт увеличен
    val isDarkMode: Boolean = false //темный режим
)

fun ArticleState.toBottombarData() = BottombarData(isLike,isBookmark,isShowMenu,isSearch,searchResults.size,searchPosition)
fun ArticleState.toSubmenuData() = SubmenuData(isShowMenu,isBigText,isDarkMode)