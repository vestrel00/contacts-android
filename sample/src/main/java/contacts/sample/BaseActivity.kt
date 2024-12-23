package contacts.sample

import android.app.Activity
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowInsets
import android.widget.ProgressBar
import android.widget.Toast
import contacts.core.Contacts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 * A coroutine-scoped activity that defines a SupervisorJob, which automatically cancels all jobs
 * launched in scope of this activity in [onDestroy].
 *
 * This also contains a singleton instance of the [Contacts] API.
 *
 * #### Convenience functions
 *
 * - [showProgressDialog] and [dismissProgressDialog]
 * - [showToast]
 *
 * ## Edge-to-edge enforcement starting in API 35
 *
 * This activity adjusts the margins of the content view so that its child views are not laid out
 * and drawn behind system bars such as the tool bar (top app bar).
 *
 * Activities that subclass this MUST set its root view id to [R.id.contentView].
 */
abstract class BaseActivity : Activity(), CoroutineScope by MainScope() {

    protected val contacts: Contacts
        get() = (application as SampleApp).contacts

    protected val preferences: SampleAppPreferences
        get() = (application as SampleApp).preferences

    // Obviously, this is not the way to provide a singleton when using dependency injection
    // frameworks such as dagger or koin. Again, this sample is made to be barebones!

    private var progressDialog: AlertDialog? = null

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        /*
         * Starting with API 35, edge-to-edge is enforced and cannot be changed;
         * https://developer.android.com/about/versions/15/behavior-changes-15#edge-to-edge
         *
         * "Important: If your app is not already edge-to-edge, portions of your app may be obscured and you must handle insets."
         *
         * If not handled, the first few items in the list may not be visible as they are laid
         * out behind the system toolbar (or top app bar).
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            // https://developer.android.com/develop/ui/views/layout/edge-to-edge#system-bars-insets
            // findViewById<View>(android.R.id.content) this does not work
            findViewById<View>(R.id.contentView)
                .setOnApplyWindowInsetsListener { v, windowInsets ->
                    val insets = windowInsets.getInsets(WindowInsets.Type.systemBars())
                    (v.layoutParams as MarginLayoutParams).let {
                        it.topMargin = insets.top
                        it.leftMargin = insets.left
                        it.bottomMargin = insets.bottom
                        it.rightMargin = insets.right
                    }

                    WindowInsets.CONSUMED
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    protected fun showProgressDialog() {
        dismissProgressDialog()

        progressDialog = AlertDialog.Builder(this).setView(ProgressBar(this).apply {
            isIndeterminate = true
        }).setCancelable(false).show()
    }

    protected fun dismissProgressDialog() {
        progressDialog?.dismiss()
    }

    protected fun showToast(stringResId: Int) {
        Toast.makeText(this, stringResId, Toast.LENGTH_SHORT).show()
    }
}
