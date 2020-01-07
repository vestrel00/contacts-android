package com.vestrel00.contacts.sample

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.ProgressBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * A coroutine-scoped activity that defines a [SupervisorJob], which automatically cancels all jobs
 * launched in scope of this activity in [onDestroy].
 */
abstract class BaseActivity : Activity(), CoroutineScope {

    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private var progressDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = SupervisorJob()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
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