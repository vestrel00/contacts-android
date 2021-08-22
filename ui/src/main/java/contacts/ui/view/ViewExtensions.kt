package contacts.ui.view

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.ViewGroup

/**
 * Returns the activity that hosts this view.
 *
 * Taken from https://stackoverflow.com/questions/8276634/how-to-get-hosting-activity-from-a-view
 */
val View.activity: Activity?
    get() {
        var context: Context = context
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }

/**
 * Sets [View.isEnabled] to this view group's children and all of their descendants.
 */
fun ViewGroup.setChildrenAndDescendantsEnabled(isEnabled: Boolean) {
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        child.isEnabled = isEnabled
        if (child is ViewGroup) {
            child.setChildrenAndDescendantsEnabled(isEnabled)
        }
    }
}