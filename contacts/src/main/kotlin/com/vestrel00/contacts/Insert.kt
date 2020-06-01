package com.vestrel00.contacts

import android.accounts.Account
import android.content.ContentProviderOperation
import android.content.Context
import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.operation.*
import com.vestrel00.contacts.entities.table.Table
import com.vestrel00.contacts.util.applyBatch
import com.vestrel00.contacts.util.nullIfNotInSystem

/**
 * Inserts one or more raw contacts into the RawContacts table and all associated attributes to the
 * data table.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION] are assumed to have
 * been granted already in these examples for brevity. All inserts will do nothing if these
 * permissions are not granted.
 *
 * ## Accounts
 *
 * **For Lollipop (API 22) and below**
 *
 * When an Account is added, from a state where no accounts have yet been added to the system, the
 * Contacts Provider automatically sets all of the null `accountName` and `accountType` in the
 * RawContacts table to that Account's name and type.
 *
 * RawContacts inserted without an associated account will automatically get assigned to an account
 * if there are any available. This may take a few seconds, whenever the Contacts Provider decides
 * to do it.
 *
 * **For Marshmallow (API 23) and above**
 *
 * The Contacts Provider no longer associates local contacts to an account when an account is or
 * becomes available.
 *
 * **Account removal**
 *
 * Removing the Account will delete all of the associated rows in the Contact, RawContact, and
 * Data tables.
 *
 * ## Usage
 *
 * To insert a raw contact with the name "john doe" with email "john@doe.com" for the given account;
 *
 * In Kotlin,
 *
 * ```kotlin
 * val result = insert
 *      .forAccount(account)
 *      .rawContact {
 *          name = MutableName().apply {
 *              givenName = "john"
 *              familyName = "doe"
 *          }
 *          emails.add(MutableEmail().apply {
 *              type = Email.Type.HOME
 *              address = "john@doe.com"
 *          })
 *      }
 *      .commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * MutableName name = new MutableName();
 * name.setGivenName("john");
 * name.setFamilyName("doe");
 *
 * MutableEmail email = new MutableEmail();
 * email.setType(Email.Type.HOME);
 * email.setAddress("john@doe.com");
 *
 * List<MutableEmail> emails = new ArrayList<>();
 * emails.add(email);
 *
 * MutableRawContact rawContact = new MutableRawContact();
 * rawContact.setName(name);
 * rawContact.setEmails(emails);
 *
 * Insert.Result result = insert
 *      .forAccount(account)
 *      .rawContacts(rawContact)
 *      .commit();
 * ```
 */
interface Insert {

    /**
     * If [allowBlanks] is set to true, then blank RawContacts ([MutableRawContact.isBlank]) will
     * will be inserted. Otherwise, blanks will not be inserted and will result in a failed
     * operation. This flag is set to false by default.
     *
     * The Contacts Providers allows for RawContacts that have no rows in the Data table (let's call
     * them "blanks") to exist. The native Contacts app does not allow insertion of new RawContacts
     * without at least one data row. It also deletes blanks on update. Despite seemingly not
     * allowing blanks, the native Contacts app shows them.
     */
    fun allowBlanks(allowBlanks: Boolean): Insert

    /**
     * All of the raw contacts that are inserted on [commit] will belong to the given [account].
     *
     * If not provided, or null is provided, or if an incorrect account is provided, the raw
     * contacts inserted here will not be associated with an account. RawContacts inserted without
     * an associated account are considered local or device-only contacts, which are not synced.
     */
    fun forAccount(account: Account?): Insert

    /**
     * Adds a new [MutableRawContact] to the insert queue, which will be inserted on [commit].
     * The new instance is configured by the [configureRawContact] function.
     *
     * Existing RawContacts are allowed to be inserted to facilitate "duplication".
     */
    fun rawContact(configureRawContact: MutableRawContact.() -> Unit): Insert

    /**
     * Adds the given [rawContacts] to the insert queue, which will be inserted on [commit].
     *
     * Existing RawContacts are allowed to be inserted to facilitate "duplication".
     */
    fun rawContacts(vararg rawContacts: MutableRawContact): Insert

    /**
     * See [Insert.rawContacts].
     */
    fun rawContacts(rawContacts: Collection<MutableRawContact>): Insert

    /**
     * See [Insert.rawContacts].
     */
    fun rawContacts(rawContacts: Sequence<MutableRawContact>): Insert

    /**
     * Inserts the [MutableRawContact]s in the queue (added via [rawContacts]) and returns the
     * [Result].
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.WRITE_PERMISSION] and
     * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

    interface Result {

        /**
         * The list of IDs of successfully created RawContacts.
         */
        val rawContactIds: List<Long>

        /**
         * True if all MutableRawContacts have successfully been inserted. False if even one insert
         * failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [rawContact] has been successfully inserted. False otherwise.
         */
        fun isSuccessful(rawContact: MutableRawContact): Boolean

        /**
         * Returns the ID of the newly created RawContact (from the [rawContact] passed to
         * [Insert.rawContacts]). Use the ID to get the newly created RawContact via a query. The
         * manually constructed [MutableRawContact] passed to [Insert.rawContacts] are not
         * automatically updated and will remain to have an invalid ID.
         *
         * Returns null if the insert operation failed.
         */
        fun rawContactId(rawContact: MutableRawContact): Long?
    }
}

@Suppress("FunctionName")
internal fun Insert(context: Context): Insert = InsertImpl(
    context,
    ContactsPermissions(context)
)

private class InsertImpl(
    private val context: Context,
    private val permissions: ContactsPermissions,

    private var allowBlanks: Boolean = false,
    private var account: Account? = null,
    private val rawContacts: MutableSet<MutableRawContact> = mutableSetOf()
) : Insert {

    override fun allowBlanks(allowBlanks: Boolean): Insert = apply {
        this.allowBlanks = allowBlanks
    }

    override fun forAccount(account: Account?): Insert = apply {
        this.account = account
    }

    override fun rawContact(configureRawContact: MutableRawContact.() -> Unit): Insert =
        rawContacts(MutableRawContact().apply(configureRawContact))

    override fun rawContacts(vararg rawContacts: MutableRawContact): Insert =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Collection<MutableRawContact>): Insert =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Sequence<MutableRawContact>): Insert = apply {
        this.rawContacts.addAll(rawContacts)
    }

    override fun commit(): Insert.Result {
        if (rawContacts.isEmpty() || !permissions.canInsertUpdateDelete()) {
            return InsertFailed
        }

        // This ensures that a valid account is used. Otherwise, null is used.
        account = account?.nullIfNotInSystem(context)

        val results = mutableMapOf<MutableRawContact, Long?>()
        for (rawContact in rawContacts) {
            results[rawContact] = if (!allowBlanks && rawContact.isBlank()) {
                null
            } else {
                context.insertRawContactForAccount(account, rawContact)
            }
        }
        return InsertResult(results)
    }
}

/**
 * Inserts a new RawContacts row and any non-null Data rows. A Contacts row is automatically
 * created by the Contacts Provider and is associated with the new RawContacts and Data rows.
 *
 * Contact rows should not be manually created because the Contacts Provider and other sync
 * providers may consolidate multiple RawContacts and associated Data rows to a single Contacts
 * row.
 */
private fun Context.insertRawContactForAccount(
    account: Account?,
    rawContact: MutableRawContact
): Long? {
    val operations = arrayListOf<ContentProviderOperation>()

    /*
     * Like with the native Android Contacts app, a new RawContact row is created for each new
     * raw contact.
     *
     * This needs to be the first operation in the batch as it will be used by all subsequent
     * Data table insert operations.
     */
    operations.add(RawContactOperation(Table.RAW_CONTACTS.uri).insert(account))

    operations.addAll(AddressOperation().insert(rawContact.addresses))

    operations.addAll(EmailOperation().insert(rawContact.emails))

    operations.addAll(EventOperation().insert(rawContact.events))

    // The account can only be null if there are no available accounts. In this case, it should
    // not be possible for consumers to obtain group memberships unless they have saved them
    // as parcelables and then restored them after the accounts have been removed.
    // Still we should have this null check just in case.
    if (account != null) {
        operations.addAll(
            GroupMembershipOperation().insert(rawContact.groupMemberships, account, this)
        )
    }

    operations.addAll(ImOperation().insert(rawContact.ims))

    rawContact.name?.let {
        NameOperation().insert(it)?.let(operations::add)
    }

    rawContact.nickname?.let {
        NicknameOperation().insert(it)?.let(operations::add)
    }

    rawContact.note?.let {
        NoteOperation().insert(it)?.let(operations::add)
    }

    rawContact.organization?.let {
        OrganizationOperation().insert(it)?.let(operations::add)
    }

    operations.addAll(PhoneOperation().insert(rawContact.phones))

    operations.addAll(RelationOperation().insert(rawContact.relations))

    rawContact.sipAddress?.let {
        SipAddressOperation().insert(it)?.let(operations::add)
    }

    operations.addAll(WebsiteOperation().insert(rawContact.websites))

    /*
     * Atomically create the RawContact row and all of the associated Data rows. All of the
     * above operations will either succeed or fail.
     */
    val results = contentResolver.applyBatch(operations)

    /*
     * The ContentProviderResult[0] contains the first result of the batch, which is the
     * RawContactOperation. The uri contains the RawContact._ID as the last path segment.
     *
     * E.G. "content://com.android.contacts/raw_contacts/18"
     * In this case, 18 is the RawContacts._ID.
     *
     * It is formed by the Contacts Provider using
     * Uri.withAppendedPath(ContactsContract.RawContacts.CONTENT_URI, "18")
     */
    return results?.firstOrNull()?.let { result ->
        val rawContactUri = result.uri
        val rawContactId = rawContactUri.lastPathSegment?.toLongOrNull()
        rawContactId
    }
}

private class InsertResult(private val rawContactMap: Map<MutableRawContact, Long?>) :
    Insert.Result {

    override val rawContactIds: List<Long> by lazy {
        rawContactMap.values.asSequence()
            .filterNotNull()
            .toList()
    }

    override val isSuccessful: Boolean by lazy { rawContactMap.all { it.value != null } }

    override fun isSuccessful(rawContact: MutableRawContact): Boolean =
        rawContactId(rawContact) != null

    override fun rawContactId(rawContact: MutableRawContact): Long? =
        rawContactMap.getOrElse(rawContact) { null }
}

private object InsertFailed : Insert.Result {

    override val rawContactIds: List<Long> = emptyList()

    override val isSuccessful: Boolean = false

    override fun isSuccessful(rawContact: MutableRawContact): Boolean = false

    override fun rawContactId(rawContact: MutableRawContact): Long? = null
}