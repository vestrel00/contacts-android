package contacts.core.blockednumbers

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.provider.BlockedNumberContract
import android.provider.Telephony
import android.telecom.TelecomManager

/**
 * Provides functions for checking for blocked number privileges.
 *
 * ## Developer notes
 *
 * The word used here is "privilege" instead of "permission" because in order to use blocked number
 * APIs directly, an app must meet one of the following requirements;
 *
 * - be a system app
 * - be the default dialer/phone app
 * - be the default SMS/messaging app
 *
 * Additionally, in a multi-user environment, the
 * [android.provider.BlockedNumberContract.canCurrentUserBlockNumbers] function must also return
 * true.
 *
 * It's not about "permissions". It's about rights/privileges.
 */
interface BlockedNumbersPrivileges {

    /**
     * Returns true if the device's current API version supports blocked numbers, which have been
     * introduced in Android 7.0 (N) (API 24).
     */
    // [ANDROID X] @ChecksSdkIntAtLeast (not using annotation to avoid dependency on androidx.annotation)
    fun isCurrentApiVersionSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

    /**
     * Returns true if queries, inserts, and deletes are allowed.
     *
     * This will return true if one of the following requirements are met;
     *
     * - be a system app
     * - be the default dialer/phone app
     * - be the default SMS/messaging app
     *
     * Additionally, in a multi-user environment, the
     * [android.provider.BlockedNumberContract.canCurrentUserBlockNumbers] function must also return
     * true.
     *
     * ## Manifest
     *
     * Starting with Android 11 (API 30), you must include the following to your manifest in order
     * to successfully use this function.
     *
     * ```
     * <queries>
     *     <intent>
     *         <action android:name="android.provider.Telephony.SMS_DELIVER" />
     *     </intent>
     * </queries>
     * ```
     */
    fun canReadAndWrite(): Boolean
}

@Suppress("FunctionName")
internal fun BlockedNumberPrivileges(context: Context): BlockedNumbersPrivileges =
    BlockedNumbersPrivilegesImpl(context.applicationContext)

private class BlockedNumbersPrivilegesImpl(
    private val applicationContext: Context
) : BlockedNumbersPrivileges {

    // @TargetApi would not be necessary if isCurrentApiVersionSupported is annotated with
    // @ChecksSdkIntAtLeast(Build.VERSION_CODES.N)
    @TargetApi(Build.VERSION_CODES.N)
    override fun canReadAndWrite(): Boolean {
        if (!isCurrentApiVersionSupported()) {
            return false
        }

        val telecomManager =
            applicationContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val defaultDialerPackage = telecomManager.defaultDialerPackage
        val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(applicationContext)

        val isDefaultDialer = applicationContext.packageName == defaultDialerPackage
        val isDefaultSms = applicationContext.packageName == defaultSmsPackage

        val canCurrentUserBlockNumbers =
            BlockedNumberContract.canCurrentUserBlockNumbers(applicationContext)

        // Assume that this is not a system app. If you are reading this and you want to use this
        // library in your system app, please raise an issue in GitHub and/or contact me =)
        return canCurrentUserBlockNumbers && (isDefaultDialer || isDefaultSms)
    }
}