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
     * Returns a new [BlockedNumbersQuery] instance.
     */
    fun query(): BlockedNumbersQuery

    /**
     * Returns a [BlockedNumbersPrivileges] instance, which provides functions for checking required
     * privileges for blocked number operations.
     */
    val privileges: BlockedNumbersPrivileges

    /**
     * A reference to the [Contacts] instance that constructed this. This is mostly used internally
     * to shorten internal code.
     *
     * Don't worry, [Contacts] does not keep references to instances of this. There are no circular
     * references that could cause leaks =). [Contacts] is just a factory.
     */
    val contactsApi: Contacts
}

/**
 * Creates a new [BlockedNumbers] instance.
 */
@Suppress("FunctionName")
internal fun BlockedNumbers(contacts: Contacts): BlockedNumbers =
    BlockedNumbersImpl(BlockedNumbersPrivileges(contacts.applicationContext), contacts)

private class BlockedNumbersImpl(
    override val privileges: BlockedNumbersPrivileges,
    override val contactsApi: Contacts
) : BlockedNumbers {

    override fun startBlockedNumbersActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val telecomManager = contactsApi
                .applicationContext
                .getSystemService(Context.TELECOM_SERVICE) as TelecomManager

            contactsApi.applicationContext.startActivity(
                telecomManager.createManageBlockedNumbersIntent(),
                null
            )
        }
    }

    override fun query() = BlockedNumbersQuery(contactsApi)
}