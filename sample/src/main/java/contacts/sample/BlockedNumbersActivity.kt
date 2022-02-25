package contacts.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import contacts.async.blockednumbers.findWithContext
import contacts.ui.util.isDefaultDialerApp
import contacts.ui.util.onRequestToBeDefaultDialerAppResult
import contacts.ui.util.requestToBeTheDefaultDialerApp
import contacts.ui.view.BlockedNumbersView
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Shows a brief explanation of how blocked phone numbers work in Android 7.0+ (N/API 24). Several functions
 * are provided;
 *
 * 1. Launch the default blocked numbers activity, which is also used by the AOSP Contacts app and
 *    Google Contacts app.
 * 2. Request to make this the default dialer app, which is one of the ways for the sample app to
 *    be able to read and write blocked numbers. If it is already the default dialer app or request
 *    is granted, then the blocked number list is populated with options to add or delete.
 *
 * ## Note
 *
 * This is a very rudimentary activity that is not styled or made to look good. It may not follow
 * any good practices and may even implement bad practices. This is for demonstration purposes only!
 *
 * This does not support state retention (e.g. device rotation). The OSS community may contribute to
 * this by implementing it.
 */
class BlockedNumbersActivity : BaseActivity() {

    private lateinit var blockedNumbersView: BlockedNumbersView

    private var queryJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked_numbers)
        blockedNumbersView = findViewById(R.id.blocked_numbers_list)
    }

    override fun onResume() {
        super.onResume()

        setupDefaultBlockedNumbersActivityButton()
        setupSetAsDefaultDialerButton()
        refreshBlockedNumbersView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onRequestToBeDefaultDialerAppResult(requestCode, resultCode, ::onBeingDefaultDialerApp)
    }

    private fun setupDefaultBlockedNumbersActivityButton() {
        val button = findViewById<Button>(R.id.blocked_numbers_default_activity)
        button.setOnClickListener {
            contacts.blockedNumbers().startBlockedNumbersActivity()
        }
        button.isEnabled = contacts.blockedNumbers().privileges.isCurrentApiVersionSupported()
    }

    private fun setupSetAsDefaultDialerButton() {
        val button = findViewById<Button>(R.id.blocked_numbers_set_as_default_dialer)
        if (isDefaultDialerApp()) {
            button.setText(R.string.blocked_numbers_already_default_dialer)
            button.isEnabled = false
        } else {
            button.setOnClickListener {
                requestToBeTheDefaultDialerApp()
            }
            button.setText(R.string.blocked_numbers_set_as_default_dialer)
            button.isEnabled = contacts.blockedNumbers().privileges.isCurrentApiVersionSupported()
        }
    }

    private fun onBeingDefaultDialerApp() {
        if (contacts.blockedNumbers().privileges.canReadAndWrite()) {
            setupSetAsDefaultDialerButton()
            refreshBlockedNumbersView()
        } else {
            // If this is already the default dialer app, then it could be that the current user
            // that is in a multi-user environment is not allowed to read/write blocked numbers.
            // Typically, blocking numbers is only supported for one user at a time.
            onReadWriteBlockedNumbersRestricted()
        }
    }

    private fun refreshBlockedNumbersView() {
        queryJob?.cancel()
        queryJob = launch {
            val blockedNumbers = if (contacts.blockedNumbers().privileges.canReadAndWrite()) {
                contacts.blockedNumbers().query().findWithContext()
            } else {
                null
            }

            blockedNumbersView.set(blockedNumbers)
        }
    }

    private fun onReadWriteBlockedNumbersRestricted() {
        Toast.makeText(this, R.string.blocked_numbers_read_write_restricted, Toast.LENGTH_LONG)
            .show()
    }

    companion object {
        fun showBlockedNumbers(activity: Activity) {
            activity.startActivity(Intent(activity, BlockedNumbersActivity::class.java))
        }
    }
}