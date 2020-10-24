package com.vestrel00.contacts.sample

import android.app.Activity
import android.app.AlertDialog
import android.widget.ProgressBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * A coroutine-scoped activity that defines a [SupervisorJob], which automatically cancels all jobs
 * launched in scope of this activity in [onDestroy].
 */
abstract class BaseActivity : Activity(), CoroutineScope by MainScope() {

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
}