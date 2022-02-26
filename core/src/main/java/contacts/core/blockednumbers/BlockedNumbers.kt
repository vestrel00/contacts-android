package contacts.core.blockednumbers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import contacts.core.Contacts

/**
 * Provides new [BlockedNumbersQuery], [BlockedNumbersInsert], and [BlockedNumbersDelete] instances.
 *
 * Note that updating an existing blocked number is not supported by the Blocked Number Provider.
 * Use [delete] and [insert] operations instead.
 */
interface BlockedNumbers {

    /**
     * Launches the builtin system Blocked numbers activity, which provides a fully functional UI
     * allowing users to see, add, and remove blocked numbers.
     *
     * This is useful for apps that do not have the privilege to read/write directly to the
     * blocked number provider. See [BlockedNumbersPrivileges] for more info.
     *
     * This is the same activity used by the native (AOSP) Contacts app and Google Contacts app
     * when accessing the "Blocked numbers".
     *
     * If the [activity] is null, the builtin blocked numbers activity will be launched as a new
     * task, separate from the current application instance. If it is provided, then the activity
     * will be part of the current application's stack/history.
     *
     * ## API Version
     *
     * Blocked numbers have been introduced in Android 7.0 (N) (API 24). Therefore, this will do
     * nothing for versions prior to API 24.
     */
    // [ANDROID X] @RequiresApi (not using annotation to avoid dependency on androidx.annotation)
    fun startBlockedNumbersActivity(activity: Activity?)

    /**
     * Returns a new [BlockedNumbersQuery] instance.
     */
    fun query(): BlockedNumbersQuery

    /**
     * Returns a new [BlockedNumbersInsert] instance.
     */
    fun insert(): BlockedNumbersInsert

    // As per official documentation, updates are not supported. Use Delete, and Insert instead.

    /**
     * Returns a new [BlockedNumbersDelete] instance.
     */
    fun delete(): BlockedNumbersDelete

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

    override fun startBlockedNumbersActivity(activity: Activity?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val telecomManager = contactsApi
                .applicationContext
                .getSystemService(Context.TELECOM_SERVICE) as TelecomManager

            (activity ?: contactsApi.applicationContext).startActivity(
                telecomManager.createManageBlockedNumbersIntent().apply {
                    if (activity == null) {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                },
                null
            )
        }
    }

    override fun query() = BlockedNumbersQuery(contactsApi)

    override fun insert() = BlockedNumbersInsert(contactsApi)

    override fun delete() = BlockedNumbersDelete(contactsApi)
}