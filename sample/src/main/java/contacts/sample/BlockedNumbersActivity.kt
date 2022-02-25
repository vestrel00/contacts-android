package contacts.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import contacts.debug.logBlockedNumbersTable
import contacts.ui.util.onRequestToBeDefaultDialerAppResult
import contacts.ui.util.requestToBeTheDefaultDialerAppIfNeeded


/**
 * Shows a brief explanation of how blocked phone numbers work in Android 7.0+ (N/API 24). Several functions
 * are provided;
 *
 * 1. Launch the default blocked numbers activity, which is also used by AOSP Contacts and Google
 *    Contacts.
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!contacts.blockedNumbers().privileges.isCurrentApiVersionSupported()) {
            onBlockedNumbersUnSupported()
        } else {
            onBlockedNumbersSupported()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onRequestToBeDefaultDialerAppResult(requestCode, resultCode, ::onBeingDefaultDialerApp)
    }

    private fun onBlockedNumbersSupported() {
        requestToBeTheDefaultDialerAppIfNeeded(::onBeingDefaultDialerApp)
    }

    private fun onBlockedNumbersUnSupported() {
        // TODO
    }

    private fun onBeingDefaultDialerApp() {
        if (contacts.blockedNumbers().privileges.canReadAndWrite()) {
            // TODO Update activity
            logBlockedNumbersTable()
        } else {
            onBlockedNumbersUnSupported()
        }
    }

    companion object {
        fun showBlockedNumbers(activity: Activity) {
            activity.startActivity(Intent(activity, BlockedNumbersActivity::class.java))
        }
    }
}