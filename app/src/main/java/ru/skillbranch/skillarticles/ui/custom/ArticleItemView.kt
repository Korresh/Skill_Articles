package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.format
import kotlin.math.max

class ArticleItemView constructor(
    context: Context
) : ViewGroup(context, null, 0) {

    private val poster: ImageView
    private val category: ImageView
    private val likes: ImageView
    private val comments: ImageView
    private val bookmark: CheckableImageView
    private val date: TextView
    private val author: TextView
    private val title: TextView
    private val description: TextView
    private val likesCount: TextView
    private val commentsCount: TextView
    private val readDuration: TextView

    private val posterSize = context.dpToIntPx(64)
    private val iconSize = context.dpToIntPx(16)
    private val defaultPadding = context.dpToIntPx(16)
    private val defaultSpace = context.dpToIntPx(8)
    private val cornerRadius = context.dpToIntPx(8)
    private val categorySize = context.dpToIntPx(40)


    init {
        setPadding(defaultPadding)
        date = TextView(context).apply {
            id = R.id.tv_date
            setTextColor(context.getColor(R.color.color_gray))
            textSize = 12f
        }
        addView(date)

        author = TextView(context).apply {
            id = R.id.tv_author
            setTextColor(R.attr.colorPrimary)
            textSize = 12f
        }
        addView(author)

        title = TextView(context).apply {
            id = R.id.tv_title
            setTextColor(context.attrValue(R.attr.colorPrimary))
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
        }
        addView(title)

        description = TextView(context).apply {
            id = R.id.tv_description
            setTextColor(context.getColor(R.color.color_gray))
            textSize = 14f
        }
        addView(description)

        poster = ImageView(context).apply {
            id = R.id.iv_poster
            layoutParams = LayoutParams(posterSize, posterSize)
        }
        addView(poster)

        category = ImageView(context).apply {
            id = R.id.iv_category
            layoutParams = LayoutParams(categorySize, categorySize)
        }
        addView(category)



        likes = ImageView(context).apply {
            id = R.id.iv_likes
            layoutParams = LayoutParams(iconSize, iconSize)
            imageTintList = ColorStateList.valueOf(context.getColor(R.color.color_gray))
            setImageResource(R.drawable.ic_favorite_black_24dp)
        }
        addView(likes)

        likesCount = TextView(context).apply {
            id = R.id.tv_likes_count
            setTextColor(context.getColor(R.color.color_gray))
            textSize = 12f
        }
        addView(likesCount)


        comments = ImageView(context).apply {
            id = R.id.iv_comments
            imageTintList = ColorStateList.valueOf(context.getColor(R.color.color_gray))
            setImageResource(R.drawable.ic_insert_comment_black_24dp)
        }
        addView(comments)

        commentsCount = TextView(context).apply {
            id = R.id.tv_comments_count
            setTextColor(context.getColor(R.color.color_gray))
            textSize = 12f
        }
        addView(commentsCount)

        readDuration = TextView(context).apply {
            id = R.id.tv_read_duration
            setTextColor(context.getColor(R.color.color_gray))
            textSize = 12f
        }
        addView(readDuration)

        bookmark = CheckableImageView(context).apply {
            id = R.id.iv_bookmark
            imageTintList = ColorStateList.valueOf(context.getColor(R.color.color_gray))
            setImageResource(R.drawable.bookmark_states)
        }
        addView(bookmark)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var usedHeight = paddingTop
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)

        measureChild(date, widthMeasureSpec, heightMeasureSpec)
        author.maxWidth = width - (date.measuredWidth + 3 * defaultPadding)
        measureChild(author, widthMeasureSpec, heightMeasureSpec)
        usedHeight += author.measuredHeight

        //title
        val rh = posterSize + categorySize / 2
        title.maxWidth = width - (rh + 2 * paddingLeft + defaultSpace)
        measureChild(title, widthMeasureSpec, heightMeasureSpec)
        usedHeight += max(title.measuredHeight, rh) + 2 * defaultSpace

        //description
        measureChild(description, widthMeasureSpec, heightMeasureSpec)
        usedHeight += description.measuredHeight + defaultSpace

        //icon
        measureChild(likesCount, widthMeasureSpec, heightMeasureSpec)
        measureChild(commentsCount, widthMeasureSpec, heightMeasureSpec)
        measureChild(readDuration, widthMeasureSpec, heightMeasureSpec)

        usedHeight += iconSize + paddingBottom
        setMeasuredDimension(width, usedHeight)
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var usedHeight = paddingTop
        val bodyWidth = right - left - paddingLeft - paddingRight
        var left = paddingLeft

        date.layout(
            left,
            usedHeight,
            left + date.measuredWidth,
            usedHeight + date.measuredHeight
        )
        left = date.right + defaultPadding
        author.layout(
            left,
            usedHeight,
            left + author.measuredWidth,
            usedHeight + author.measuredHeight
        )
        usedHeight += author.measuredHeight + defaultSpace
        left = paddingLeft

        val rh = posterSize + categorySize / 2
        val leftTop = if (rh > title.measuredHeight) (rh - title.measuredHeight) / 2 else 0
        val rightTop = if (rh < title.measuredHeight) (title.measuredHeight - rh) / 2 else 0

        title.layout(
            left,
            usedHeight + leftTop,
            left + title.measuredWidth,
            usedHeight + leftTop + title.measuredHeight
        )
        poster.layout(
            left + bodyWidth - posterSize,
            usedHeight + rightTop,
            left + bodyWidth,
            usedHeight + rightTop + posterSize
        )
        category.layout(
            poster.left - categorySize / 2,
            poster.bottom - categorySize / 2,
            poster.left + categorySize / 2,
            poster.bottom + categorySize / 2
        )
        usedHeight += if (rh > title.measuredHeight) rh else title.measuredHeight
        usedHeight += defaultSpace

        description.layout(
            left,
            usedHeight,
            left + bodyWidth,
            usedHeight + description.measuredHeight
        )
        usedHeight += description.measuredHeight + defaultSpace

        val fontDiff = iconSize - likesCount.measuredHeight
        likes.layout(
            left,
            usedHeight - fontDiff,
            left + iconSize,
            usedHeight + iconSize - fontDiff
        )

        left = likes.right + defaultSpace
        likesCount.layout(
            left,
            usedHeight,
            left + likesCount.measuredWidth,
            usedHeight + likesCount.measuredHeight
        )
        left = likesCount.right + defaultPadding

        comments.layout(
            left,
            usedHeight - fontDiff,
            left + iconSize,
            usedHeight + iconSize - fontDiff
        )
        left = comments.right + defaultSpace
        commentsCount.layout(
            left,
            usedHeight,
            left + commentsCount.measuredWidth,
            usedHeight + commentsCount.measuredHeight
        )
        left = commentsCount.right + defaultPadding
        readDuration.layout(
            left,
            usedHeight,
            left + readDuration.measuredWidth,
            usedHeight + readDuration.measuredHeight
        )

        left = defaultPadding
        bookmark.layout(
            left + bodyWidth - iconSize,
            usedHeight - fontDiff,
            left + bodyWidth,
            usedHeight + iconSize - fontDiff
        )
    }

    fun bind(item: ArticleItemData) {
        date.text = item.date.format()
        author.text = item.author
        title.text = item.title
        description.text = item.description
        likesCount.text = "${item.likeCount}"
        commentsCount.text = "${item.commentCount}"
        val rd = "${item.readDuration} min read"
        readDuration.text = rd

        Glide.with(context)
            .load(item.poster)
            .transform(CenterCrop(), RoundedCorners(cornerRadius))
            .override(posterSize)
            .into(poster)

        Glide.with(context)
            .load(item.categoryIcon)
            .transform(CenterCrop(), RoundedCorners(cornerRadius))
            .override(categorySize)
            .into(category)

    }
}