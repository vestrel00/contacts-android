package contacts.sample

import android.app.Activity
import android.app.AlertDialog
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
 */
abstract class BaseActivity : Activity(), CoroutineScope by MainScope() {

    protected val contacts: Contacts
        get() = (application as SampleApp).contacts

    // Obviously, this is not the way to provide a singleton when using dependency injection
    // frameworks such as dagger or koin. Again, this sample is made to be barebones!

    private var progressDialog: AlertDialog? = null

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    protected fun showProgressDialog() {
        dismissProgressDialog()

        progressDialog = AlertDialog.Builder(this)
            .setView(ProgressBar(this).apply {
                isIndeterminate = true
            })
            .setCancelable(false)
            .show()
    }

    protected fun dismissProgressDialog() {
        progressDialog?.dismiss()
    }

    protected fun showToast(stringResId: Int) {
        Toast.makeText(this, stringResId, Toast.LENGTH_SHORT).show()
    }
}
