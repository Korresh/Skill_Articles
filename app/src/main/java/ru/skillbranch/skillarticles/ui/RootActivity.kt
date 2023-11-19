package ru.skillbranch.skillarticles.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.text.getSpans
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.ActivityRootBinding
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.setMarginOptionally
import ru.skillbranch.skillarticles.ui.custom.SearchFocusSpan
import ru.skillbranch.skillarticles.ui.custom.SearchSpan
import ru.skillbranch.skillarticles.ui.delegate.AttrValue
import ru.skillbranch.skillarticles.ui.delegate.viewBinding
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.BottombarData
import ru.skillbranch.skillarticles.viewmodels.Notify
import ru.skillbranch.skillarticles.viewmodels.SubmenuData
import ru.skillbranch.skillarticles.viewmodels.ViewModelFactory
import ru.skillbranch.skillarticles.viewmodels.toBottombarData
import ru.skillbranch.skillarticles.viewmodels.toSubmenuData

class RootActivity : AppCompatActivity() ,IArticleView {
    var viewModelFactory : ViewModelProvider.Factory = ViewModelFactory(this,"0")
    private val viewModel: ArticleViewModel by viewModels { viewModelFactory }
    //private val viewModel: ArticleViewModel by viewModels { ViewModelFactory(this,"0") }
    private val vb: ActivityRootBinding by viewBinding(ActivityRootBinding::inflate)
    private val vbBottombar
        get() = vb.bottombar.binding
    private val vbSubmenu
        get() = vb.submenu.binding
    private lateinit var searchView: SearchView
    private val  bgColor by AttrValue(R.attr.colorSecondary)
    private val  fgColor by AttrValue(R.attr.colorOnSecondary)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar()
        setupBottombar()
        setupSubmenu()

        viewModel.observeState(this, ::renderUi)
        viewModel.observeSubState(this,ArticleState::toBottombarData, ::renderBottombar)
        viewModel.observeSubState(this,ArticleState::toSubmenuData, ::renderSubMenu)
        viewModel.observeNotifications(this){
            renderNotification(it)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search,menu)
        val searchItem = menu?.findItem(R.id.action_search)
        searchView = searchItem?.actionView as SearchView
        searchView.queryHint = "Search"
        if (viewModel.currentState.isSearch){
            searchItem.expandActionView()
            searchView.setQuery(viewModel.currentState.searchQuery, false)
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.handleSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.handleSearch(newText)
                return true
            }
        })
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {


            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                viewModel.handleSearchMode(true)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                viewModel.handleSearchMode(false)
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    private fun renderNotification(notify: Notify) {
        val snackbar = Snackbar.make(vb.coordinatorContainer, notify.message, Snackbar.LENGTH_LONG)
                .setAnchorView(vb.bottombar)


        when (notify){
            is Notify.TextMessage -> {/*nothing*/}

            is Notify.ActionMessage -> {
                snackbar.setActionTextColor(getColor(R.color.color_accent_dark))
                snackbar.setAction(notify.actionLabel){
                    notify.actionHandler.invoke()
                }
            }

            is Notify.ErrorMessage -> {
                with(snackbar){
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(android.R.color.white))
                    setActionTextColor(getColor(android.R.color.white))
                    setAction(notify.errLabel){
                        notify.errHandler?.invoke()
                    }
                }
            }
        }
        snackbar.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.saveState()
        super.onSaveInstanceState(outState)
    }

    override fun setupSubmenu() {
        with(vbSubmenu){
            btnTextUp.setOnClickListener { viewModel.handleUpText() }
            btnTextDown.setOnClickListener { viewModel.handleDownText() }
            switchMode.setOnClickListener { viewModel.handleNightMode() }
        }
    }

    override fun setupBottombar() {
        with(vbBottombar){
            btnLike.setOnClickListener { viewModel.handleLike() }
            btnBookmark.setOnClickListener { viewModel.handleBookmark() }
            btnShare.setOnClickListener { viewModel.handleShare() }
            btnSettings.setOnClickListener { viewModel.handleToggleMenu() }

            btnResultUp.setOnClickListener{
                searchView.clearFocus()
                viewModel.handleUpResult()
            }

            btnResultDown.setOnClickListener{
                searchView.clearFocus()
                viewModel.handleDownResult()
            }

            btnSearchClose.setOnClickListener{
                viewModel.handleSearchMode(false)
                invalidateOptionsMenu()
            }
        }
    }

    override fun renderSubMenu(data: SubmenuData) {
        Log.e("RootActivity","RenderSubmenu $data")
        with(vbSubmenu){
            switchMode.isChecked = data.isDarkMode
            btnTextDown.isChecked = !data.isBigText
            btnTextUp.isChecked = data.isBigText
        }
        if (data.isShowMenu) vb.submenu.open() else vb.submenu.close()
    }

    override fun renderBottombar(data: BottombarData) {
        Log.e("RootActivity","RenderBottombar $data")
        with(vbBottombar){
            btnSettings.isChecked = data.isShowMenu
            btnLike.isChecked = data.isLike
            btnBookmark.isChecked = data.isBookmark
        }
        if (data.isSearch) showSearchBar(data.resultsCount,data.searchPosition)
        else hideSearchBar()
    }

    override fun renderUi(data: ArticleState) {
        Log.e("RootActivity","RenderUI")
            delegate.localNightMode =
                    if (data.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            with(vb.tvTextContent){
                textSize = if (data.isBigText) 18f else 14f
                movementMethod = ScrollingMovementMethod()
                val content = if (data.isLoadingContent) "loading" else data.content.first()
                if (text.toString() == content) return@with
                    setText(content, TextView.BufferType.SPANNABLE)
            }
            //bind toolbar
            with(vb.toolbar){
                title = data.title ?: "loading"
                subtitle = data.category ?: "loading"
                if (data.categoryIcon!=null) logo = getDrawable(data.categoryIcon as Int)
            }

            if (data.isLoadingContent) return

            if (data.isSearch){
                renderSearchResult(data.searchResults)
                renderSearchPosition(data.searchPosition)
            }else clearSearchResult()
    }

    override fun setupToolbar (){
        setSupportActionBar(vb.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val logo = vb.toolbar.children.find { it is AppCompatImageView } as? ImageView
        logo?.scaleType = ImageView.ScaleType.CENTER_CROP
        val lp = logo?.layoutParams as? Toolbar.LayoutParams
        lp?.let {
            it.width = this.dpToIntPx(40)
            it.height = this.dpToIntPx(40)
            it.marginEnd = this.dpToIntPx(16)
            logo.layoutParams = it
        }
    }

    override fun renderSearchResult(searchResult: List<Pair<Int, Int>>) {
        val content = vb.tvTextContent.text as Spannable
        clearSearchResult()
        searchResult.forEach{(start,end) ->
            content.setSpan(
                SearchSpan(bgColor,fgColor),
                start,
                end,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun renderSearchPosition(searchPosition: Int) {
        val content = vb.tvTextContent.text as Spannable
        val spans = content.getSpans<SearchSpan>()
        //remove old search focus span
        content.getSpans<SearchFocusSpan>()
            .forEach { content.removeSpan(it) }
        if (spans.isNotEmpty()){
            //find position span
            val result = spans[searchPosition]
            //move to selection
            Selection.setSelection(content,content.getSpanStart(result))
            //set new search focus span
            content.setSpan(
                SearchFocusSpan( bgColor,fgColor),
                content.getSpanStart(result),
                content.getSpanEnd(result),
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun clearSearchResult() {
        val content = vb.tvTextContent.text as Spannable
        content.getSpans<SearchSpan>()
            .forEach { content.removeSpan(it) }
    }

    override fun showSearchBar(resultsCount: Int, searchPosition: Int) {
        with(vb.bottombar){
            setSearchState(true)
            setSearchInfo(resultsCount,searchPosition)
        }
        vb.scroll.setMarginOptionally(bottom = dpToIntPx(56))
    }

    override fun hideSearchBar() {
        with(vb.bottombar){
            setSearchState(false)
        }
        vb.scroll.setMarginOptionally(bottom = dpToIntPx(0))
    }
}
