package contacts.core.blockednumbers

import android.content.Context
import android.os.Build
import android.telecom.TelecomManager
import contacts.core.Contacts

/**
 * TODO
 */
interface BlockedNumbers {

    /**
     * Launches the system Blocked numbers activity, which provides a fully functional UI allowing
     * users to see, add, and remove blocked numbers.
     *
     * This is useful for apps that do not have the privilege to read/write directly to the
     * blocked number provider. See [BlockedNumbersPrivileges] for more info.
     *
     * This is the same activity used by the native (AOSP) Contacts app and Google Contacts app
     * when accessing the "Blocked numbers".
     *
     * ## API Version
     *
     * Blocked numbers have been introduced in Android 7.0 (N) (API 24). Therefore, this will do
     * nothing for versions prior to API 24.
     */
    // [ANDROID X] @RequiresApi (not using annotation to avoid dependency on androidx.annotation)
    fun startBlockedNumbersActivity()

    /**
     * Returns a [BlockedNumbersPrivileges] instance, which provides functions for checking required
     * privileges for blocked number operations.
     */
    val privileges: BlockedNumbersPrivileges
}

/**
 * Creates a new [BlockedNumbers] instance.
 */
@Suppress("FunctionName")
internal fun BlockedNumbers(contacts: Contacts): BlockedNumbers =
    BlockedNumbersImpl(
        BlockedNumberPrivileges(contacts.applicationContext),
        contacts.applicationContext
    )

private class BlockedNumbersImpl(
    override val privileges: BlockedNumbersPrivileges,
    private val applicationContext: Context
) : BlockedNumbers {

    override fun startBlockedNumbersActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val telecomManager =
                applicationContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

            applicationContext.startActivity(
                telecomManager.createManageBlockedNumbersIntent(),
                null
            )
        }
    }
}