package ru.skillbranch.skillarticles.viewmodels.article

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.skillbranch.skillarticles.data.remote.res.CommentRes
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.data.repositories.CommentsDataFactory
import ru.skillbranch.skillarticles.data.repositories.MarkdownElement
import ru.skillbranch.skillarticles.data.repositories.clearContent
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.indexesOf
import ru.skillbranch.skillarticles.extensions.shortFormat
import ru.skillbranch.skillarticles.viewmodels.base.*
import java.util.concurrent.Executors

class ArticleViewModel(
    handle: SavedStateHandle,
    private val articleId: String
) : BaseViewModel<ArticleState>(handle, ArticleState()), IArticleViewModel {
    private val repository = ArticleRepository
    private var clearContent: String? = null
    val listConfig by lazy {
        PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPageSize(5)
            .build()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val listData : LiveData<PagedList<CommentRes>> =
        Transformations.switchMap(repository.findArticleCommentCount(articleId)){
            buildPagedList(repository.loadAllComments(articleId, it, :: commentLoadErrorHandler))
    }

    init{
        //subscribe on mutable data
       subscribeOnDataSource(repository.findArticle(articleId)){article,state ->
           if (article.content == null) fetchContent()
           state.copy(
                   shareLink = article.shareLink,
                   title = article.title,
                   category = article.category.title,
                   categoryIcon = article.category.icon,
                   date = article.date.shortFormat(),
                   author = article.author,
                   isBookmark = article.isBookmark,
                   isLike = article.isLike,
                   content = article.content ?: emptyList(),
                   isLoadingContent = article.content == null,
                   tags = article.tags,
                   source = article.source
           )
       }

        subscribeOnDataSource(repository.getAppSettings()){ settings, state ->
            state.copy(
                    isDarkMode = settings.isDarkMode,
                    isBigText = settings.isBigText
            )
        }

        subscribeOnDataSource(repository.isAuth()){ auth, state ->
            state.copy(isAuth = auth)
        }
    }
    fun refresh(){
        launchSafety {
            launch { repository.fetchArticleContent(articleId) }
            launch { repository.refreshCommentsCount(articleId) }
        }
    }

    private fun commentLoadErrorHandler(throwable: Throwable) {
        // TODO handle network errors this
    }

    private fun fetchContent() {
        launchSafety{
            repository.fetchArticleContent(articleId)
        }
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
        launchSafety {
            val isBookmarked = repository.toggleBookmark(articleId)
            if (isBookmarked) repository.addBookmark(articleId)

            val msg = if (isBookmarked) "Add to bookmarks" else "Remove from bookmarks"
            notify(Notify.TextMessage(msg))
        }

    }
    override fun handleLike(){
        launchSafety {
            val isLiked = repository.toggleLike(articleId)

            if (isLiked) repository.incrementLike(articleId)
            else repository.decrementLike(articleId)

            val msg = if(isLiked)Notify.TextMessage("Articles marked as liked")
            else{
                Notify.ActionMessage(
                    "Don t like anymore",//
                    "No, still like it", //action label on snackbar
                {handleLike()}//handle function, if press "No, still like it" on snackbar, then toggle again
                )
            }
            notify(msg)
        }
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

    override fun handleUpResult() {
        updateState { it.copy(searchPosition = it.searchPosition.dec()) }
    }
    override fun handleDownResult() {
        updateState { it.copy(searchPosition = it.searchPosition.inc()) }
    }

    override fun handleCopyCode() {
        notify(Notify.TextMessage("Code copy to clipboard"))
    }


    override fun handleSendComment(comment: String?) {
        if (comment == null) {
            notify(Notify.TextMessage("Comment must not be empty"))
            return
        }
        updateState { it.copy(commentText = comment) }
        if (!currentState.isAuth){
            navigate(NavigationCommand.StartLogin())
        }else{
            launchSafety (null){
                updateState {
                    it.copy(
                        answerTo = null,
                        answerToMessageId = null,
                        commentText = null
                    )
                }
            }
            viewModelScope.launch(Dispatchers.IO){
                repository.sendMessage(
                    articleId,
                    currentState.commentText!!,
                    currentState.answerToMessageId
                )

            }
        }
    }

    fun observeList(
        owner: LifecycleOwner,
        onChanged: (list:PagedList<CommentRes>) -> Unit
    ){
        listData.observe(owner, Observer{onChanged(it)})
    }

    private fun buildPagedList(
        dataFactory: CommentsDataFactory
    ): LiveData<PagedList<CommentRes>>{
        return LivePagedListBuilder<String, CommentRes>(
            dataFactory,
            listConfig
        )
            .setFetchExecutor(Executors.newSingleThreadExecutor())
            .build()
    }

    fun handleCommentFocus(hasFocus: Boolean) {
        updateState { it.copy(showBottomBar = !hasFocus) }
    }

    fun handleClearComment() {
        updateState { it.copy(answerTo = null, answerToMessageId = null) }
    }

    fun handleReplyTo(messageId:String, name:String){
        updateState { it.copy(answerToMessageId = messageId, answerTo = "Reply to ${name}") }
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
    val commentsCount: Int = 0,
    val answerTo:String? = null,
    val answerToMessageId:String? = null,
    val showBottomBar:Boolean = true,
    val commentText: String? = null,
    val tags: List<String> = emptyList(),
    val source: String? = null
):IViewModelState{
    override fun save(outState: SavedStateHandle) {
        outState.set("isSearch" , isSearch)
        outState.set("searchQuery" , searchQuery)
        outState.set("searchResults" , searchResults)
        outState.set("commentText", commentText)
        outState.set("answerTo", answerTo)
        outState.set("answerToSlug", answerToMessageId)
    }
    @Suppress("UNCHECKED_CAST")
    override fun restore(savedState: SavedStateHandle): ArticleState {
        return copy(
            isSearch = savedState["isSearch"] ?: false,
            searchQuery = savedState["searchQuery"] ,
            searchResults = savedState["searchResults"] ?: emptyList(),
            searchPosition = savedState["searchPosition"] ?:0,
            commentText = savedState["commentText"],
            answerTo = savedState["answerTo"],
            answerToMessageId = savedState["answerToSlug"]
        )
    }
}