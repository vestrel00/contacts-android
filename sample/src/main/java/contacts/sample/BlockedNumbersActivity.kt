package contacts.sample

import android.annotation.TargetApi
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.BlockedNumberContract
import android.provider.Telephony
import android.telecom.TelecomManager
import android.util.Log
import contacts.debug.logBlockedNumbersTable


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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            onBlockedNumbersUnSupported()
        } else {
            onBlockedNumbersSupported()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER && resultCode == RESULT_OK) {
            onBeingDefaultDialerApp()
        }
    }

    private fun onBlockedNumbersSupported() {
        requestToBeTheDefaultDialerAppIfNeeded()
    }

    private fun onBlockedNumbersUnSupported() {
        // TODO
    }

    // [ANDROID X] @RequiresApi (not using annotation to avoid dependency on androidx.annotation)
    @TargetApi(Build.VERSION_CODES.M)
    private fun requestToBeTheDefaultDialerAppIfNeeded() {
        val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager // Requires API 21+
        val isAlreadyDefaultDialer =
            packageName == telecomManager.defaultDialerPackage // Requires API 23+
        if (isAlreadyDefaultDialer) {
            onBeingDefaultDialerApp()
        } else {
            requestToBeTheDefaultDialerApp()
        }
    }

    // TODO move this to the UI package
    // [ANDROID X] @RequiresApi (not using annotation to avoid dependency on androidx.annotation)
    @TargetApi(Build.VERSION_CODES.M)
    private fun requestToBeTheDefaultDialerApp() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
            startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER)
        } else {
            val roleManager = getSystemService(ROLE_SERVICE) as RoleManager
            startActivityForResult(
                roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER),
                REQUEST_CODE_SET_DEFAULT_DIALER
            )
        }
    }

    private fun onBeingDefaultDialerApp() {
        if (canReadWriteBlockedNumbers()) {
            // TODO Update activity
            logBlockedNumbersTable()
        } else {
            onBlockedNumbersUnSupported()
        }
    }

    // [ANDROID X] @RequiresApi (not using annotation to avoid dependency on androidx.annotation)
    @TargetApi(Build.VERSION_CODES.N)
    private fun canReadWriteBlockedNumbers(): Boolean {
        val defaultDialerPackage =
            (getSystemService(Context.TELECOM_SERVICE) as TelecomManager).defaultDialerPackage
        val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this)

        val canCurrentUserBlockNumbers = BlockedNumberContract.canCurrentUserBlockNumbers(this)
        val isDefaultDialer = packageName == defaultDialerPackage
        val isDefaultSms = packageName == defaultSmsPackage

        // A check that is omitted here is if this is a system app. We are just assuming that it is not.
        return canCurrentUserBlockNumbers && (isDefaultDialer || isDefaultSms)
    }

    companion object {
        fun showBlockedNumbers(activity: Activity) {
            activity.startActivity(Intent(activity, BlockedNumbersActivity::class.java))
        }
    }
}

private const val REQUEST_CODE_SET_DEFAULT_DIALER = 123