package ru.skillbranch.skillarticles.ui.custom.markdown

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Parcel
import android.os.Parcelable
import android.text.Selection
import android.text.Spannable
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.VisibleForTesting
import androidx.core.view.setPadding
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.*

@SuppressLint("ViewConstructor")
class MarkdownCodeView private constructor(
    context: Context,
    fontSize: Float
) : ViewGroup(context, null, 0), IMarkdownView {
    override var fontSize: Float = fontSize
        set(value){
            tv_codeView.textSize = value * 0.85f
            field = value
        }

    override val spannableContent: Spannable
        get() = tv_codeView.text as Spannable

    var copyListener: ((String) -> Unit)? = null

    private lateinit var codeString: CharSequence

    //views
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val iv_copy: ImageView
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val iv_switch: ImageView
    private val tv_codeView: MarkdownTextView
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val sv_scroll: HorizontalScrollView

    //colors
    @ColorInt
    private val darkSurface: Int = context.attrValue(R.attr.darkSurfaceColor) //darkSurfaceColor
    @ColorInt
    private val darkOnSurface: Int = context.attrValue(R.attr.darkOnSurfaceColor) //darkOnSurfaceColor
    @ColorInt
    private val lightSurface: Int = context.attrValue(R.attr.lightSurfaceColor)//lightSurfaceColor
    @ColorInt
    private val lightOnSurface: Int = context.attrValue(R.attr.lightOnSurfaceColor)//lightOnSurfaceColor

    //sizes
    private val iconSize = context.dpToIntPx(12)//12dp
    private val radius = context.dpToPx(8)//8dp
    private val padding  = context.dpToIntPx(8)//8dp
    private val fadingOffset = context.dpToIntPx(144)//144dp
    private val textExtraPadding = context.dpToIntPx(80)//80dp
    private val scrollBarHeight = context.dpToIntPx(2)//2dp

    //for layout
    private var isSingleLine = false
    private var isDark = false
    private var isManual = false
    private val bgColor
        get() = when {
            !isManual -> context.attrValue(R.attr.colorSurface)
            isDark -> darkSurface
            else -> lightSurface
        }

    private val textColor
        get() = when{
            !isManual -> context.attrValue(R.attr.colorOnSurface)
            isDark -> darkOnSurface
            else -> lightOnSurface
        }

    init {
        tv_codeView = MarkdownTextView(context, fontSize * 0.85f).apply {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            setTextColor(textColor)
            setPaddingOptionally(right = textExtraPadding)
            isFocusable = true
            isFocusableInTouchMode = true
        }

        sv_scroll = object : HorizontalScrollView(context){
            override fun getLeftFadingEdgeStrength(): Float {
                return  0f
            }
        }.apply {
            overScrollMode = View.OVER_SCROLL_NEVER
            isHorizontalFadingEdgeEnabled = true
            scrollBarSize = scrollBarHeight
            setFadingEdgeLength(fadingOffset)
            //add code text to scroll
            addView(tv_codeView)
        }
        addView(sv_scroll)

        iv_copy = ImageView(context).apply {
            setImageResource(R.drawable.ic_content_copy_black_24dp)
            imageTintList = ColorStateList.valueOf(textColor)
            setOnClickListener {
                copyListener?.invoke(codeString.toString())
            }
        }
        addView(iv_copy)

        iv_switch = ImageView(context).apply {
            setImageResource(R.drawable.ic_brightness_medium_black_24dp)
            imageTintList = ColorStateList.valueOf(textColor)
            setOnClickListener { toggleColors() }
        }
        addView( iv_switch)
    }

    constructor(
        context: Context,
        fontSize: Float,
        code: CharSequence
    ) : this(context, fontSize) {
        codeString = code
        isSingleLine = code.lines().size == 1
        tv_codeView.setText(codeString, TextView.BufferType.SPANNABLE)
        setPadding(padding)
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = FloatArray(8).apply { radius }
            color = ColorStateList.valueOf(bgColor)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        measureChild(sv_scroll, widthMeasureSpec, heightMeasureSpec)
        //measureChild(iv_copy, widthMeasureSpec,heightMeasureSpec)
        val usedHeight = sv_scroll.measuredHeight +paddingTop + paddingBottom
        setMeasuredDimension(width, usedHeight)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val  usedHeight = paddingTop
        val bodyWidth  = r - l - paddingLeft -paddingRight
        val left = paddingLeft
        val right = paddingLeft + bodyWidth

        if (isSingleLine){
            val iconHeight = (b - t - iconSize) / 2

            iv_copy.layout(
                right - iconSize,
                iconHeight,
                right,
                iconHeight +iconSize
            )

            iv_switch.layout(
                iv_copy.right - (2.5f*iconSize).toInt(),
                iconHeight,
                iv_copy.right - (1.5f*iconSize).toInt(),
                iconHeight + iconSize
            )

        }else{
            iv_copy.layout(
                right - iconSize,
                usedHeight,
                right,
                usedHeight + iconSize
            )

            iv_switch.layout(
                iv_copy.right - (2.5f*iconSize).toInt(),
                usedHeight,
                iv_copy.right - (1.5f*iconSize).toInt(),
                usedHeight + iconSize
            )
        }

        sv_scroll.layout(
            left,
            usedHeight,
            right,
            usedHeight + sv_scroll.measuredHeight
        )
    }

    override fun renderSearchPosition(searchPosition: Pair<Int, Int>, offset: Int) {

        if ((parent as ViewGroup).hasFocus() && !tv_codeView.hasFocus()) tv_codeView.requestFocus()
        Selection.setSelection(spannableContent,searchPosition.first.minus(offset))
    }

    private fun toggleColors() {
        isManual = true
        isDark = !isDark
        applyColors()
    }

    private fun applyColors() {
        iv_switch.imageTintList = ColorStateList.valueOf(textColor)
        iv_copy.imageTintList = ColorStateList.valueOf(textColor)
        (background as GradientDrawable).color = ColorStateList.valueOf(bgColor)
        tv_codeView.setTextColor(textColor)
    }
    public override fun onSaveInstanceState(): Parcelable {
        val savedState = SavedState(super.onSaveInstanceState()!!)
        savedState.isManual = isManual
        return savedState
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            if(state.isManual) toggleColors()
        } else {
            super.onRestoreInstanceState(state)
        }
    }


    internal class SavedState : BaseSavedState {

        var isManual: Boolean = false

        constructor(source: Parcel) : super(source) {
            isManual = source.readByte().toInt() != 0
        }

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeByte((if (isManual) 1 else 0).toByte())
        }

        @JvmField
        val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {

            override fun createFromParcel(source: Parcel): SavedState {
                return SavedState(source)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}