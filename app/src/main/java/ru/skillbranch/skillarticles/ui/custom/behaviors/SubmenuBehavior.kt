package ru.skillbranch.skillarticles.ui.custom.behaviors


import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.marginRight
import ru.skillbranch.skillarticles.extensions.dpToPx
import ru.skillbranch.skillarticles.ui.custom.ArticleSubmenu
import ru.skillbranch.skillarticles.ui.custom.Bottombar
import kotlin.math.max
import kotlin.math.min


class SubmenuBehavior : CoordinatorLayout.Behavior<ArticleSubmenu>(){
    //set view as dependent BottomBar
    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: ArticleSubmenu,
        dependency: View
    ): Boolean {
        return dependency is Bottombar
    }
    // will be called if dependent view has been changed
    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: ArticleSubmenu,
        dependency: View
    ): Boolean {
        return if (dependency is Bottombar && dependency.translationY >=0 ){
            animate(child,dependency)
            true
        }else false
    }

    private fun animate(child: ArticleSubmenu, dependency: Bottombar) {
        val fraction = dependency.translationY/dependency.minHeight
        child.translationX = (child.width + child.marginRight) * fraction
    }
}

//class SubmenuBehavior <V : View>(context: Context, attrs: AttributeSet) :
//    CoordinatorLayout.Behavior<V>(context, attrs) {
//    private var border: Float = context.dpToPx(8)
//    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
//        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
//    }
//
//    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int
//    ) {
//        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
//        child.translationY = max(0f, min(child.height.toFloat()+ border, child.translationY + dy))
//        child.translationX = max(0f, min(child.width.toFloat()+ border, child.translationX + dy))
//    }
//}