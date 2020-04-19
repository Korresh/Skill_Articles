package ru.skillbranch.skillarticles.viewmodels.article

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import ru.skillbranch.skillarticles.data.ArticleData
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.data.repositories.MarkdownElement
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.data.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.format
import ru.skillbranch.skillarticles.extensions.indexesOf
import ru.skillbranch.skillarticles.data.repositories.clearContent
import ru.skillbranch.skillarticles.viewmodels.base.*

class ArticleViewModel(
    handle: SavedStateHandle,
    private val articleId: String) : BaseViewModel<ArticleState>(handle,
    ArticleState()
), IArticleViewModel {
    private val repository = ArticleRepository
    private val _searchMode = MutableLiveData<Event<Pair<Boolean, String>>>()
    private var clearContent:String? = null

    init{
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

        subscribeOnDataSource(repository.isAuth()){ isAuth, state ->
            state.copy(isAuth = isAuth)
        }

    }

    //load text from network
    override fun getArticleContent(): LiveData<List<MarkdownElement>?> {
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
                    toggleBookmark //handler function, if press "Bookmark be added" on snackbar, then toggle again
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
        updateState { it.copy(isSearch = isSearch) /*isShowMenu = false, searchPosition = 0)*/ }
    }

    override fun handleSearch(query: String?){
        query ?: return
        if(clearContent == null && currentState.content.isNotEmpty()) clearContent = currentState.content.clearContent()
        val result = clearContent
            .indexesOf(query)
            .map{ it to it + query.length}
        updateState { it.copy(searchQuery = query, searchResults = result, searchPosition = 0) }
    }

    fun handleUpResult() {
        updateState { it.copy(searchPosition = it.searchPosition.dec()) }
    }
    fun handleDownResult() {
        updateState { it.copy(searchPosition = it.searchPosition.inc()) }
    }

    fun handleCopyCode() {
        notify(Notify.TextMessage("Code copy to clipboard"))
    }


    fun handleSetComment() {
        if (!currentState.isAuth) navigate(NavigationCommand.StartLogin())
        //TODO send comment
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
    val searchResults: List<Pair<Int,Int>> = emptyList(),//результаты поиска (стартовая и конечная прозиции)
    val searchPosition: Int = 0,//текущая позиция найденного результата
    val shareLink: String? = null,//ссылка Share
    val title: String? = null,//заголовок статьи
    val category: String? = null,//категория
    val categoryIcon: Any? = null,//иконка категории
    val date: String? = null,//дата публикации
    val author: Any? = null,//автор статьи
    val poster: String? = null,//обложка статьи
    val content: List<MarkdownElement> = emptyList(),//контент
    val reviews: List<Any> = emptyList() //комментарии
):IViewModelState{
    override fun save(outState: SavedStateHandle) {
        outState.set("isSearch" , isSearch)
        outState.set("searchQuery" , searchQuery)
        outState.set("searchResults" , searchResults)
        outState.set("searchPosition", searchPosition)
    }
    @Suppress("UNCHECKED_CAST")
    override fun restore(savedState: SavedStateHandle): ArticleState {
        return copy(
            isSearch = savedState["isSearch"] ?: false,
            searchQuery = savedState["searchQuery"] ,
            searchResults = savedState["searchResults"] ?: emptyList(),
            searchPosition = savedState["searchPosition"] ?:0
        )

    }

}