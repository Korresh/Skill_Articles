package ru.skillbranch.skillarticles.extensions

import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop



fun View.setMarginOptionally(left:Int = marginLeft, top : Int = marginTop, right : Int = marginRight, bottom : Int = marginBottom){
    val lp = layoutParams as? ViewGroup.MarginLayoutParams
        ?: return

    lp.setMargins(
        left ?: lp.leftMargin,
        top ?: lp.topMargin,
        right ?: lp.rightMargin,
        bottom ?: lp.rightMargin
    )

    layoutParams = lp

}

fun View.setPaddingOptionally(
    left: Int = paddingLeft,
    right: Int = paddingRight,
    top: Int = paddingTop,
    bottom: Int = paddingBottom
) {
    setPadding(left, top, right, bottom)
}