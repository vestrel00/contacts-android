package contacts.core

import android.accounts.Account
import android.content.ContentProviderOperation
import contacts.core.entities.MutableRawContact
import contacts.core.entities.custom.CustomDataCountRestriction
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.entities.operation.*
import contacts.core.util.applyBatch
import contacts.core.util.isEmpty
import contacts.core.util.nullIfNotInSystem
import contacts.core.util.unsafeLazy

/**
 * Inserts one or more RawContacts and Data.
 *
 * The insertion of a RawContact triggers automatic insertion of a new Contact subject to automatic
 * aggregation by the Contacts Provider.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] and
 * [contacts.core.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION] are assumed to have
 * been granted already in these examples for brevity. All inserts will do nothing if these
 * permissions are not granted.
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
     * becomes available. Local contacts remain local.
     *
     * **Account removal**
     *
     * Removing the Account will delete all of the associated rows in the Contact, RawContact, and
     * Data tables.
     */
    fun forAccount(account: Account?): Insert

    /**
     * Specifies that only the given set of [fields] (data) will be inserted.
     *
     * If no fields are specified, then all fields will be inserted. Otherwise, only the specified
     * fields will be inserted.
     *
     * ## Note
     *
     * The use case for this function is probably rare. You can simply not set a particular
     * data instead of using this function. For example, if you want to create a new RawContact
     * with only name and email data, just set only name and email...
     *
     * There may be some cases where this function may come in handy. For example, if you have a
     * mutable RawContact that has all data filled in but you only want some of those data to be
     * inserted (in the database), then this function is exactly what you need =) This can also come
     * in handy if you are trying to make copies of an existing RawContact but only want some data
     * to be copied.
     */
    fun include(vararg fields: AbstractDataField): Insert

    /**
     * See [Insert.include].
     */
    fun include(fields: Collection<AbstractDataField>): Insert

    /**
     * See [Insert.include].
     */
    fun include(fields: Sequence<AbstractDataField>): Insert

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
     * [contacts.core.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

    /**
     * Inserts the [MutableRawContact]s in the queue (added via [rawContacts]) and returns the
     * [Result].
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.WRITE_PERMISSION] and
     * [contacts.core.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
     *
     * ## Cancellation
     *
     * To cancel at any time, the [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * **Cancelling does not undo insertions. This means that depending on when the cancellation
     * occurs, some if not all of the RawContacts in the insert queue may have already been
     * inserted.**
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    // @JvmOverloads cannot be used in interface methods...
    // fun commit(cancel: () -> Boolean = { false }): Result
    fun commit(cancel: () -> Boolean): Result

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
internal fun Insert(contacts: Contacts): Insert = InsertImpl(contacts)

private class InsertImpl(
    private val contacts: Contacts,

    private var allowBlanks: Boolean = false,
    private var include: Include<AbstractDataField> = allDataFields(contacts.customDataRegistry),
    private var account: Account? = null,
    private val rawContacts: MutableSet<MutableRawContact> = mutableSetOf()
) : Insert {

    override fun toString(): String =
        """
            Insert {
                allowBlanks: $allowBlanks
                include: $include
                account: $account
                rawContacts: $rawContacts
            }
        """.trimIndent()

    override fun allowBlanks(allowBlanks: Boolean): Insert = apply {
        this.allowBlanks = allowBlanks
    }

    override fun forAccount(account: Account?): Insert = apply {
        this.account = account
    }

    override fun include(vararg fields: AbstractDataField) = include(fields.asSequence())

    override fun include(fields: Collection<AbstractDataField>) = include(fields.asSequence())

    override fun include(fields: Sequence<AbstractDataField>): Insert = apply {
        include = if (fields.isEmpty()) {
            allDataFields(contacts.customDataRegistry)
        } else {
            Include(fields + Fields.Required.all.asSequence())
        }
    }

    override fun rawContact(configureRawContact: MutableRawContact.() -> Unit): Insert =
        rawContacts(MutableRawContact().apply(configureRawContact))

    override fun rawContacts(vararg rawContacts: MutableRawContact) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Collection<MutableRawContact>) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Sequence<MutableRawContact>): Insert = apply {
        this.rawContacts.addAll(rawContacts)
    }

    override fun commit(): Insert.Result = commit { false }

    override fun commit(cancel: () -> Boolean): Insert.Result {
        if (rawContacts.isEmpty() || !contacts.permissions.canInsert() || cancel()) {
            return InsertFailed()
        }

        // This ensures that a valid account is used. Otherwise, null is used.
        account = account?.nullIfNotInSystem(contacts.accounts())

        val results = mutableMapOf<MutableRawContact, Long?>()
        for (rawContact in rawContacts) {
            if (cancel()) {
                break
            }

            results[rawContact] = if (!allowBlanks && rawContact.isBlank) {
                null
            } else {
                // No need to propagate the cancel function to within insertRawContactForAccount
                // as that operation should be fast and CPU time should be trivial.
                contacts.insertRawContactForAccount(account, include.fields, rawContact, IS_PROFILE)
            }
        }
        return InsertResult(results)
    }

    private companion object {
        const val IS_PROFILE = false
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
internal fun Contacts.insertRawContactForAccount(
    account: Account?,
    includeFields: Set<AbstractDataField>,
    rawContact: MutableRawContact,
    isProfile: Boolean
): Long? {
    val operations = arrayListOf<ContentProviderOperation>()

    /*
     * Like with the native Android Contacts app, a new RawContact row is created for each new
     * raw contact.
     *
     * This needs to be the first operation in the batch as it will be used by all subsequent
     * Data table insert operations.
     */
    operations.add(RawContactsOperation(isProfile).insert(account))

    operations.addAll(
        AddressOperation(isProfile, Fields.Address.intersect(includeFields)).insert(
            rawContact.addresses
        )
    )

    operations.addAll(
        EmailOperation(isProfile, Fields.Email.intersect(includeFields)).insert(
            rawContact.emails
        )
    )

    if (account != null) {
        // I'm not sure why the native Contacts app hides events from the UI for local raw contacts.
        // The Contacts Provider does support having events for local raw contacts. Anyways, let's
        // follow in the footsteps of the native Contacts app...
        operations.addAll(
            EventOperation(isProfile, Fields.Event.intersect(includeFields)).insert(
                rawContact.events
            )
        )
    }

    if (account != null) {
        // Groups require an Account. Therefore, memberships to groups cannot exist without groups.
        // It should not be possible for consumers to get access to group memberships.
        // The Contacts Provider does support having events for local raw contacts.
        operations.addAll(
            GroupMembershipOperation(
                isProfile,
                Fields.GroupMembership.intersect(includeFields),
                groups()
            ).insert(rawContact.groupMemberships, account)
        )
    }

    operations.addAll(
        ImOperation(isProfile, Fields.Im.intersect(includeFields)).insert(rawContact.ims)
    )

    rawContact.name?.let {
        NameOperation(isProfile, Fields.Name.intersect(includeFields)).insert(it)
            ?.let(operations::add)
    }

    rawContact.nickname?.let {
        NicknameOperation(isProfile, Fields.Nickname.intersect(includeFields)).insert(it)
            ?.let(operations::add)
    }

    rawContact.note?.let {
        NoteOperation(isProfile, Fields.Note.intersect(includeFields)).insert(it)
            ?.let(operations::add)
    }

    rawContact.organization?.let {
        OrganizationOperation(isProfile, Fields.Organization.intersect(includeFields)).insert(it)
            ?.let(operations::add)
    }

    operations.addAll(
        PhoneOperation(isProfile, Fields.Phone.intersect(includeFields)).insert(
            rawContact.phones
        )
    )

    // Photo is intentionally excluded here. Use the ContactPhoto and RawContactPhoto extensions
    // to set full-sized and thumbnail photos.

    if (account != null) {
        // I'm not sure why the native Contacts app hides relations from the UI for local raw
        // contacts. The Contacts Provider does support having events for local raw contacts.
        // Anyways, let's follow in the footsteps of the native Contacts app...
        operations.addAll(
            RelationOperation(
                isProfile, Fields.Relation.intersect(includeFields)
            ).insert(rawContact.relations)
        )
    }

    rawContact.sipAddress?.let {
        SipAddressOperation(isProfile, Fields.SipAddress.intersect(includeFields)).insert(it)
            ?.let(operations::add)
    }

    operations.addAll(
        WebsiteOperation(isProfile, Fields.Website.intersect(includeFields)).insert(
            rawContact.websites
        )
    )

    // Process custom data
    operations.addAll(rawContact.customDataInsertOperations(includeFields, customDataRegistry))

    /*
     * Atomically create the RawContact row and all of the associated Data rows. All of the
     * above operations will either succeed or fail.
     */
    val results = applicationContext.contentResolver.applyBatch(operations)

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
        val rawContactId = rawContactUri?.lastPathSegment?.toLongOrNull()
        rawContactId
    }
}

private fun MutableRawContact.customDataInsertOperations(
    includeFields: Set<AbstractDataField>, customDataRegistry: CustomDataRegistry
): List<ContentProviderOperation> = mutableListOf<ContentProviderOperation>().apply {
    for ((mimeTypeValue, customDataEntityHolder) in customDataEntities) {
        val customDataEntry = customDataRegistry.entryOf(mimeTypeValue)

        val countRestriction = customDataEntry.countRestriction
        val customDataOperation = customDataEntry.operationFactory.create(
            isProfile,
            customDataEntry.fieldSet.intersect(includeFields)
        )

        when (countRestriction) {
            CustomDataCountRestriction.AT_MOST_ONE -> {
                customDataEntityHolder.entities.firstOrNull()?.let {
                    customDataOperation.insert(it)?.let(::add)
                }
            }
            CustomDataCountRestriction.NO_LIMIT -> {
                customDataOperation.insert(customDataEntityHolder.entities).let(::addAll)
            }
        }
    }
}

private class InsertResult(private val rawContactMap: Map<MutableRawContact, Long?>) :
    Insert.Result {

    override val rawContactIds: List<Long> by unsafeLazy {
        rawContactMap.values.asSequence()
            .filterNotNull()
            .toList()
    }

    override val isSuccessful: Boolean by unsafeLazy { rawContactMap.all { it.value != null } }

    override fun isSuccessful(rawContact: MutableRawContact): Boolean =
        rawContactId(rawContact) != null

    override fun rawContactId(rawContact: MutableRawContact): Long? =
        rawContactMap.getOrElse(rawContact) { null }
}

private class InsertFailed : Insert.Result {

    override val rawContactIds: List<Long> = emptyList()

    override val isSuccessful: Boolean = false

    override fun isSuccessful(rawContact: MutableRawContact): Boolean = false

    override fun rawContactId(rawContact: MutableRawContact): Long? = null
}