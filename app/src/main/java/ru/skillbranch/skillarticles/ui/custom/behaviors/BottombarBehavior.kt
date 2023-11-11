package ru.skillbranch.skillarticles.ui.custom.behaviors


import android.util.Log
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout
import ru.skillbranch.skillarticles.ui.custom.Bottombar


class BottombarBehavior : CoordinatorLayout.Behavior<Bottombar>(){
    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: Bottombar,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL

    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: Bottombar,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        //if dy<0 scroll down
        //if dy>0 scroll up
        child.translationY = MathUtils.clamp(child.translationY + dy, 0f, child.minHeight.toFloat())
        Log.e("BottomBer behavior", "dy : $dy  translationY : ${child.translationY} ")
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
    }
}


//class BottombarBehavior <V : View>(context: Context, attrs: AttributeSet) :
//    CoordinatorLayout.Behavior<V>(context, attrs) {
//
//    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
//        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
//    }
//
//    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int
//    ) {
//        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
//        child.translationY = max(0f, min(child.height.toFloat(), child.translationY + dy))
//    }
//}