package contacts.core

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.telephony.TelephonyManager
import contacts.core.accounts.Accounts
import contacts.core.accounts.AccountsPermissions
import contacts.core.aggregationexceptions.AggregationExceptions
import contacts.core.blockednumbers.BlockedNumbers
import contacts.core.data.Data
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.groups.Groups
import contacts.core.log.EmptyLogger
import contacts.core.log.Logger
import contacts.core.log.LoggerRegistry
import contacts.core.profile.Profile
import contacts.core.sim.SimContacts

/**
 * Provides new [Query], [RawContactsQuery], [BroadQuery], [PhoneLookupQuery], [Insert], [Update],
 * [Delete], [AggregationExceptions], [Data], [Groups], [Profile], [Accounts], [BlockedNumbers],
 * and [SimContacts] instances.
 *
 * ## Permissions
 *
 * - Add the "android.permission.READ_CONTACTS" to the AndroidManifest in order to [query],
 *   [rawContactsQuery], [phoneLookupQuery], and [broadQuery].
 * - Add the "android.permission.WRITE_CONTACTS" to the AndroidManifest in order to [insert],
 *   [update], and [delete].
 *
 * Use [permissions] convenience functions to check for required permissions. The same permissions
 * apply to [AggregationExceptions], [Data], [Groups], [Profile], and [SimContacts].
 *
 * Use [accountsPermissions] convenience functions to check for required permissions to use the
 * [Accounts] API.
 */
interface Contacts {

    /**
     * Returns a new [Query] instance.
     */
    fun query(): Query

    /**
     * Returns a new [RawContactsQuery] instance for non-profile operations.
     */
    fun rawContactsQuery(): RawContactsQuery

    /**
     * Returns a new [BroadQuery] instance.
     */
    fun broadQuery(): BroadQuery

    /**
     * Returns a new [PhoneLookupQuery] instance.
     */
    fun phoneLookupQuery(): PhoneLookupQuery

    /**
     * Returns a new [Insert] instance.
     */
    fun insert(): Insert

    /**
     * Returns a new [Update] instance.
     */
    fun update(): Update

    /**
     * Returns a new [Delete] instance.
     */
    fun delete(): Delete

    /**
     * Returns a new [AggregationExceptions] instance.
     */
    fun aggregationExceptions(): AggregationExceptions

    /**
     * Returns a new [Data] instance for non-Profile data operations.
     */
    fun data(): Data

    /**
     * Returns a new [Groups] instance.
     */
    fun groups(): Groups

    /**
     * Returns a new [Profile] instance.
     */
    fun profile(): Profile

    /**
     * Returns a new [Accounts] instance for non-profile operations.
     */
    fun accounts(): Accounts

    /**
     * Returns a new [BlockedNumbers] instance.
     */
    fun blockedNumbers(): BlockedNumbers

    /**
     * Returns a new [SimContacts] instance.
     */
    fun sim(): SimContacts

    /**
     * Returns a [ContactsPermissions] instance, which provides functions for checking required
     * permissions for Contacts Provider operations.
     */
    val permissions: ContactsPermissions

    /**
     * Returns a [AccountsPermissions] instance, which provides functions for checking required
     * permissions for Account operations.
     */
    val accountsPermissions: AccountsPermissions

    /**
     * Reference to the Application's Context for use in extension functions and external library
     * modules. This is safe to hold on to. Not meant for consumer use.
     *
     * ## Developer notes
     *
     * It's safe to save a hard reference to the Application context as it is alive for as long as
     * the app is alive. No need to make this a weak reference and make our lives more difficult
     * for no reason. Other libraries do the same; e.g. coil.
     *
     * Don't believe me? Then read the official Android documentation about this posted back in
     * 2009; https://android-developers.googleblog.com/2009/01/avoiding-memory-leaks.html
     *
     * Obviously, we should not save a reference to any Activity context.
     *
     * Consumers of this should still use [Context.getApplicationContext] for redundancy, which
     * provides further protection.
     */
    val applicationContext: Context

    /**
     * Registry for [Logger].
     */
    val loggerRegistry: LoggerRegistry

    /**
     * Registry of custom data components, enabling queries, inserts, updates, and deletes for
     * custom data.
     */
    val customDataRegistry: CustomDataRegistry

    /**
     * Registry for all [CrudApi.Listener]s.
     */
    val apiListenerRegistry: CrudApiListenerRegistry

    /**
     * Sets the value of [android.provider.ContactsContract.CALLER_IS_SYNCADAPTER] for all CRUD APIs
     * provided by this instance of [Contacts] that use [android.provider.ContactsContract] URIs.
     *
     * ## For sync adapter use only!
     *
     * Applications should NOT set this value to true!
     *
     * As mentioned in the official docs, setting the value for this property at the time of
     * insertion or updating its value afterwards is typically only done in the context of sync
     * adapters. This is not for general app use!
     *
     * If you are using this API in your sync adapter implementation, then you should set this value
     * to true. It will allow you to perform certain insert, update, and delete operations that
     * may otherwise fail. For example, attempting to update read-only rows
     * ([contacts.core.entities.NewDataEntity.isReadOnly]) in the Data table
     * (e.g. name, email, phone) will result in the actual value of the read-only data to remain
     * unchanged (even if the API result indicates success). If this is set to true, updating
     * read-only data should result in actual changes to take effect. Updating read-only data is
     * just one of the many different behaviors/side-effects that this value affects.
     *
     * This library is not responsible for documenting all of the different behaviors/side-effects
     * caused by setting this to true.
     *
     * Do NOT mess with this unless you know exactly what you are doing. Otherwise, it MAY cause
     * issues with syncing with respect to the Account's sync adapter and remote servers/databases.
     */
    val callerIsSyncAdapter: Boolean
}

/**
 * Creates a new [Contacts] instance.
 */
@JvmOverloads
@Suppress("FunctionName")
fun Contacts(
    context: Context,
    callerIsSyncAdapter: Boolean = false,
    customDataRegistry: CustomDataRegistry = CustomDataRegistry(),
    logger: Logger = EmptyLogger(),
): Contacts {
    val apiListenerRegistry = CrudApiListenerRegistry()
    val loggerRegistry = LoggerRegistry(logger)
    return ContactsImpl(
        context.applicationContext,
        ContactsPermissions(context.applicationContext),
        AccountsPermissions(context.applicationContext),
        loggerRegistry,
        customDataRegistry,
        apiListenerRegistry.register(loggerRegistry.apiListener),
        callerIsSyncAdapter
    )
}

/**
 * Creates a new [Contacts] instance.
 *
 * This is mainly exist for traditional Java conventions. Kotlin users should use the [Contacts]
 * function instead.
 */
object ContactsFactory {

    @JvmStatic
    @JvmOverloads
    fun create(
        context: Context,
        callerIsSyncAdapter: Boolean = false,
        customDataRegistry: CustomDataRegistry = CustomDataRegistry(),
        logger: Logger = EmptyLogger()
    ): Contacts = Contacts(context, callerIsSyncAdapter, customDataRegistry, logger)
}

private class ContactsImpl(
    override val applicationContext: Context,
    override val permissions: ContactsPermissions,
    override val accountsPermissions: AccountsPermissions,
    override val loggerRegistry: LoggerRegistry,
    override val customDataRegistry: CustomDataRegistry,
    override val apiListenerRegistry: CrudApiListenerRegistry,
    override val callerIsSyncAdapter: Boolean
) : Contacts {

    override fun query() = Query(this)

    override fun rawContactsQuery() = RawContactsQuery(this, false)

    override fun broadQuery() = BroadQuery(this)

    override fun phoneLookupQuery() = PhoneLookupQuery(this)

    override fun insert() = Insert(this)

    override fun update() = Update(this)

    override fun delete() = Delete(this)

    override fun aggregationExceptions() = AggregationExceptions(this)

    override fun data() = Data(this, false)

    override fun groups() = Groups(this)

    override fun profile() = Profile(this)

    override fun accounts() = Accounts(this)

    override fun blockedNumbers() = BlockedNumbers(this)

    override fun sim() = SimContacts(this)
}

// region Shortcuts

internal val Contacts.contentResolver: ContentResolver
    get() = applicationContext.contentResolver

internal val Contacts.resources: Resources
    get() = applicationContext.resources

internal val Contacts.telephonyManager: TelephonyManager
    get() = applicationContext.getSystemService(Activity.TELEPHONY_SERVICE) as TelephonyManager

internal val Contacts.isSimCardReady: Boolean
    get() = telephonyManager.simState == TelephonyManager.SIM_STATE_READY

// endregion