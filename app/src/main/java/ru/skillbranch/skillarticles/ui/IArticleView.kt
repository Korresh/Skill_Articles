package ru.skillbranch.skillarticles.ui

import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.BottombarData
import ru.skillbranch.skillarticles.viewmodels.SubmenuData

interface IArticleView {
    fun setupSubmenu()
    fun setupBottombar()
    fun renderSubMenu(data: SubmenuData)
    fun renderBottombar(data: BottombarData)
    fun renderUi(data: ArticleState)
    fun setupToolbar()
    fun renderSearchResult(searchResult: List<Pair<Int,Int>>)
    fun renderSearchPosition(searchPosition: Int)
     fun clearSearchResult()
     fun showSearchBar(resultsCount: Int, searchPosition: Int)
     fun hideSearchBar()

}