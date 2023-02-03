package contacts.core

import android.content.ContentProviderOperation
import contacts.core.entities.NewRawContact
import contacts.core.entities.custom.CustomDataCountRestriction
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.entities.operation.*
import contacts.core.util.*

/**
 * Inserts one or more RawContacts and Data.
 *
 * The insertion of a RawContact triggers automatic insertion of a new Contact subject to automatic
 * aggregation by the Contacts Provider.
 *
 * As per documentation in [android.provider.ContactsContract.Contacts],
 *
 * > A Contact cannot be created explicitly. When a raw contact is inserted, the provider will first
 * > try to find a Contact representing the same person. If one is found, the raw contact's
 * > RawContacts#CONTACT_ID column gets the _ID of the aggregate Contact. If no match is found,
 * > the provider automatically inserts a new Contact and puts its _ID into the
 * > RawContacts#CONTACT_ID column of the newly inserted raw contact.
 *
 * ## Blank data are ignored
 *
 * Blank data will be ignored. For example, if all properties of an email are all null, empty, or
 * blank, then the email is not inserted. This is the same behavior as the AOSP Contacts app. This
 * behavior cannot be modified.
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
 *      .rawContact {
 *          this.account = account
 *          name = NewName(
 *              givenName = "john"
 *              familyName = "doe"
 *          )
 *          emails.add(NewEmail(
 *              type = EmailEntity.Type.HOME
 *              address = "john@doe.com"
 *          ))
 *      }
 *      .commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * NewName name = new NewName();
 * name.setGivenName("john");
 * name.setFamilyName("doe");
 *
 * NewEmail email = new NewEmail();
 * email.setType(EmailEntity.Type.HOME);
 * email.setAddress("john@doe.com");
 *
 * List<NewEmail> emails = new ArrayList<>();
 * emails.add(email);
 *
 * NewRawContact rawContact = new NewRawContact();
 * rawContact.setAccount(account);
 * rawContact.setName(name);
 * rawContact.setEmails(emails);
 *
 * Insert.Result result = insert
 *      .rawContacts(rawContact)
 *      .commit();
 * ```
 */
interface Insert : CrudApi {

    /**
     * If [allowBlanks] is set to true, then blank RawContacts ([NewRawContact.isBlank]) will
     * will be inserted. Otherwise, blanks will not be inserted and will result in a failed
     * operation. This flag is set to false by default.
     *
     * The Contacts Providers allows for RawContacts that have no rows in the Data table (let's call
     * them "blanks") to exist. The AOSP Contacts app does not allow insertion of new RawContacts
     * without at least one data row. It also deletes blanks on update. Despite seemingly not
     * allowing blanks, the AOSP Contacts app shows them.
     *
     * Note that blank data are ignored. For example, if all properties of an email are all null,
     * empty, or blank, then the email is not inserted. This is the same behavior as the AOSP
     * Contacts app. This is the same behavior as the AOSP Contacts app. This behavior cannot be
     * modified.
     */
    fun allowBlanks(allowBlanks: Boolean): Insert

    /**
     * Specifies that only the given set of [fields] (data) will be inserted.
     *
     * If no fields are specified, then all fields will be inserted. Otherwise, only the specified
     * fields will be inserted.
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
     * See [Insert.include].
     */
    fun include(fields: Fields.() -> Collection<AbstractDataField>): Insert

    /**
     * Similar to [include] except this is really only used to specify
     * [contacts.core.entities.RawContact.options] fields. All other RawContact table fields
     * and the corresponding properties are immutable (exception for options).
     *
     * If no fields are specified, then all RawContacts fields ([RawContactsFields.all]) are
     * included. Otherwise, only the specified fields will be included in addition to required API
     * fields [RawContactsFields.Required].
     */
    fun includeRawContactsFields(vararg fields: RawContactsField): Insert

    /**
     * See [Insert.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: Collection<RawContactsField>): Insert

    /**
     * See [Insert.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: Sequence<RawContactsField>): Insert

    /**
     * See [Insert.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: RawContactsFields.() -> Collection<RawContactsField>): Insert

    /**
     * Adds a new [NewRawContact] to the insert queue, which will be inserted on [commit].
     * The new instance is configured by the [configureRawContact] function.
     */
    fun rawContact(configureRawContact: NewRawContact.() -> Unit): Insert

    /**
     * Adds the given [rawContacts] to the insert queue, which will be inserted on [commit].
     */
    fun rawContacts(vararg rawContacts: NewRawContact): Insert

    /**
     * See [Insert.rawContacts].
     */
    fun rawContacts(rawContacts: Collection<NewRawContact>): Insert

    /**
     * See [Insert.rawContacts].
     */
    fun rawContacts(rawContacts: Sequence<NewRawContact>): Insert

    /**
     * Inserts the [NewRawContact]s in the queue (added via [rawContacts]) and returns the
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
     * Inserts the [NewRawContact]s in the queue (added via [rawContacts]) and returns the
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

    /**
     * Returns a redacted instance where all private user data are redacted.
     *
     * ## Redacted instances may produce invalid results!
     *
     * Redacted instance may have critical information redacted, which is required to make
     * the operation work properly.
     *
     * **Redacted operations should typically only be used for logging in production!**
     */
    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): Insert

    interface Result : CrudApi.Result {

        /**
         * The list of IDs of successfully created RawContacts.
         */
        val rawContactIds: List<Long>

        /**
         * True if all NewRawContacts have successfully been inserted. False if even one insert
         * failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [rawContact] has been successfully inserted. False otherwise.
         */
        fun isSuccessful(rawContact: NewRawContact): Boolean

        /**
         * Returns the ID of the newly created RawContact (from the [rawContact] passed to
         * [Insert.rawContacts]). Use the ID to get the newly created RawContact via a query.
         *
         * Returns null if the insert operation failed.
         */
        fun rawContactId(rawContact: NewRawContact): Long?

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun Insert(contacts: Contacts): Insert = InsertImpl(contacts)

private class InsertImpl(
    override val contactsApi: Contacts,

    private var allowBlanks: Boolean = false,
    private var include: Include<AbstractDataField> = contactsApi.includeAllFields(),
    private var includeRawContactsFields: Include<RawContactsField> = DEFAULT_INCLUDE_RAW_CONTACTS_FIELDS,
    private val rawContacts: MutableSet<NewRawContact> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : Insert {

    override fun toString(): String =
        """
            Insert {
                allowBlanks: $allowBlanks
                include: $include
                includeRawContactsFields: $includeRawContactsFields
                rawContacts: $rawContacts
                hasPermission: ${permissions.canInsert()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): Insert = InsertImpl(
        contactsApi,

        allowBlanks,
        include,
        includeRawContactsFields,
        // Redact contact data.
        rawContacts.asSequence().redactedCopies().toMutableSet(),

        isRedacted = true
    )

    override fun allowBlanks(allowBlanks: Boolean): Insert = apply {
        this.allowBlanks = allowBlanks
    }

    override fun include(vararg fields: AbstractDataField) = include(fields.asSequence())

    override fun include(fields: Collection<AbstractDataField>) = include(fields.asSequence())

    override fun include(fields: Sequence<AbstractDataField>): Insert = apply {
        include = if (fields.isEmpty()) {
            contactsApi.includeAllFields()
        } else {
            Include(fields + Fields.Required.all.asSequence())
        }
    }

    override fun include(fields: Fields.() -> Collection<AbstractDataField>) =
        include(fields(Fields))

    override fun includeRawContactsFields(vararg fields: RawContactsField) =
        includeRawContactsFields(fields.asSequence())

    override fun includeRawContactsFields(fields: Collection<RawContactsField>) =
        includeRawContactsFields(fields.asSequence())

    override fun includeRawContactsFields(fields: Sequence<RawContactsField>): Insert = apply {
        includeRawContactsFields = if (fields.isEmpty()) {
            DEFAULT_INCLUDE_RAW_CONTACTS_FIELDS
        } else {
            Include(fields + REQUIRED_INCLUDE_RAW_CONTACTS_FIELDS)
        }
    }

    override fun includeRawContactsFields(
        fields: RawContactsFields.() -> Collection<RawContactsField>
    ) = includeRawContactsFields(fields(RawContactsFields))

    override fun rawContact(configureRawContact: NewRawContact.() -> Unit): Insert =
        rawContacts(NewRawContact().apply(configureRawContact))

    override fun rawContacts(vararg rawContacts: NewRawContact) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Collection<NewRawContact>) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Sequence<NewRawContact>): Insert = apply {
        this.rawContacts.addAll(rawContacts.redactedCopiesOrThis(isRedacted))
    }

    override fun commit(): Insert.Result = commit { false }

    override fun commit(cancel: () -> Boolean): Insert.Result {
        onPreExecute()

        return if (rawContacts.isEmpty() || !permissions.canInsert() || cancel()) {
            InsertFailed()
        } else {

            val results = mutableMapOf<NewRawContact, Long?>()
            for (rawContact in rawContacts) {
                if (cancel()) {
                    break
                }

                results[rawContact] = if (!allowBlanks && rawContact.isBlank) {
                    null
                } else {
                    // No need to propagate the cancel function to within insertRawContact as that
                    // operation should be fast and CPU time should be trivial.
                    contactsApi.insertRawContact(
                        include.fields, includeRawContactsFields.fields,
                        rawContact,
                        IS_PROFILE
                    )
                }
            }
            InsertResult(results)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }

    private companion object {
        const val IS_PROFILE = false

        val DEFAULT_INCLUDE_RAW_CONTACTS_FIELDS by unsafeLazy { Include(RawContactsFields.all) }
        val REQUIRED_INCLUDE_RAW_CONTACTS_FIELDS by unsafeLazy {
            RawContactsFields.Required.all.asSequence()
        }
    }
}

/**
 * Inserts a new RawContacts row and any non-null Data rows. A Contacts row is automatically
 * created by the Contacts Provider and is associated with the new RawContacts and Data rows.
 *
 * Contact rows should not be manually created because the Contacts Provider and other sync
 * providers may consolidate multiple RawContacts and associated Data rows to a single Contacts
 * row.
 *
 * Returns the inserted RawContact's ID. Or null, if insert failed.
 */
internal fun Contacts.insertRawContact(
    includeFields: Set<AbstractDataField>,
    includeRawContactsFields: Set<RawContactsField>,
    rawContact: NewRawContact,
    isProfile: Boolean
): Long? {
    // This ensures that a valid account is used. Otherwise, null is used. For Samsung devices, the
    // "account" will later be set to a local non-null account that is not returned by the system
    // AccountManager.
    val account = rawContact.account.nullIfNotInSystem(accounts())

    val operations = arrayListOf<ContentProviderOperation>()

    /*
     * Like with the AOSP Android Contacts app, a new RawContact row is created for each new
     * raw contact.
     *
     * This needs to be the first operation in the batch as it will be used by all subsequent
     * Data table insert operations.
     */
    operations.add(RawContactsOperation(isProfile).insert(account))

    operations.addAll(
        AddressOperation(isProfile, Fields.Address.intersect(includeFields)).insertForNewRawContact(
            rawContact.addresses
        )
    )

    operations.addAll(
        EmailOperation(isProfile, Fields.Email.intersect(includeFields)).insertForNewRawContact(
            rawContact.emails
        )
    )

    operations.addAll(
        EventOperation(isProfile, Fields.Event.intersect(includeFields)).insertForNewRawContact(
            rawContact.events
        )
    )

    operations.addAll(
        GroupMembershipOperation(
            isProfile,
            Fields.GroupMembership.intersect(includeFields),
            groups()
        ).insertForNewRawContact(rawContact.groupMemberships, account)
    )

    // Apply the options operations after the group memberships operation.
    // Any add membership operation to the favorites group will be overshadowed by the value of
    // Options.starred. If starred is true, the Contacts Provider will automatically add a group
    // membership to the favorites group (if exist). If starred is false, then the favorites group
    // membership will be removed.
    OptionsOperation().updateNewRawContactOptions(
        rawContact.options,
        RawContactsFields.Options.all.intersect(includeRawContactsFields),
        isProfile
    )?.let(operations::add)

    operations.addAll(
        ImOperation(
            isProfile,
            Fields.Im.intersect(includeFields)
        ).insertForNewRawContact(rawContact.ims)
    )

    rawContact.name?.let {
        NameOperation(isProfile, Fields.Name.intersect(includeFields)).insertForNewRawContact(it)
            ?.let(operations::add)
    }

    rawContact.nickname?.let {
        NicknameOperation(
            isProfile,
            Fields.Nickname.intersect(includeFields)
        ).insertForNewRawContact(it)
            ?.let(operations::add)
    }

    rawContact.note?.let {
        NoteOperation(isProfile, Fields.Note.intersect(includeFields)).insertForNewRawContact(it)
            ?.let(operations::add)
    }

    rawContact.organization?.let {
        OrganizationOperation(
            isProfile,
            Fields.Organization.intersect(includeFields)
        ).insertForNewRawContact(it)
            ?.let(operations::add)
    }

    operations.addAll(
        PhoneOperation(isProfile, Fields.Phone.intersect(includeFields)).insertForNewRawContact(
            rawContact.phones
        )
    )

    // Photo is intentionally excluded here. Use the ContactPhoto and RawContactPhoto extensions
    // to set full-sized and thumbnail photos.

    operations.addAll(
        RelationOperation(
            isProfile,
            Fields.Relation.intersect(includeFields)
        ).insertForNewRawContact(rawContact.relations)
    )

    rawContact.sipAddress?.let {
        SipAddressOperation(
            isProfile,
            Fields.SipAddress.intersect(includeFields)
        ).insertForNewRawContact(it)
            ?.let(operations::add)
    }

    operations.addAll(
        WebsiteOperation(isProfile, Fields.Website.intersect(includeFields)).insertForNewRawContact(
            rawContact.websites
        )
    )

    // Process custom data
    operations.addAll(
        rawContact.customDataInsertOperations(includeFields, customDataRegistry, isProfile)
    )

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
        result.uri?.lastPathSegment?.toLongOrNull()
    }?.also { rawContactId ->
        // We will attempt to set the photo, ignoring whether it fails or succeeds. Users of this
        // library can submit a request to change this behavior if they want =)
        rawContact.photoDataOperation?.let {
            if (it is PhotoDataOperation.SetPhoto) {
                setRawContactPhotoDirect(rawContactId, it.photoData)
            }
        }
        // Perform the operation only once. Users can set a pending operation again if they'd like.
        rawContact.photoDataOperation = null
    }
}

private fun NewRawContact.customDataInsertOperations(
    includeFields: Set<AbstractDataField>,
    customDataRegistry: CustomDataRegistry,
    isProfile: Boolean
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
                    customDataOperation.insertForNewRawContact(it)?.let(::add)
                }
            }
            CustomDataCountRestriction.NO_LIMIT -> {
                customDataOperation.insertForNewRawContact(customDataEntityHolder.entities)
                    .let(::addAll)
            }
        }
    }
}

private class InsertResult private constructor(
    private val rawContactMap: Map<NewRawContact, Long?>,
    override val isRedacted: Boolean
) : Insert.Result {

    constructor(rawContactMap: Map<NewRawContact, Long?>) : this(rawContactMap, false)

    override fun toString(): String =
        """
            Insert.Result {
                isSuccessful: $isSuccessful
                rawContactIds: $rawContactIds
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): Insert.Result = InsertResult(
        // This is not included in the toString function but we'll redact it anyways just in case in
        // the future. #YAGNI-violation
        rawContactMap.redactedKeys(),
        isRedacted = true
    )

    override val rawContactIds: List<Long> by unsafeLazy {
        rawContactMap.values.asSequence()
            .filterNotNull()
            .toList()
    }

    override val isSuccessful: Boolean by unsafeLazy {
        // By default, all returns true when the collection is empty. So, we override that.
        rawContactMap.run { isNotEmpty() && all { it.value != null } }
    }

    override fun isSuccessful(rawContact: NewRawContact): Boolean =
        rawContactId(rawContact) != null

    override fun rawContactId(rawContact: NewRawContact): Long? =
        rawContactMap.getOrElse(rawContact) { null }
}

private class InsertFailed private constructor(
    override val isRedacted: Boolean
) : Insert.Result {

    constructor() : this(false)

    override fun toString(): String =
        """
            Insert.Result {
                isSuccessful: $isSuccessful
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): Insert.Result = InsertFailed(true)

    override val rawContactIds: List<Long> = emptyList()

    override val isSuccessful: Boolean = false

    override fun isSuccessful(rawContact: NewRawContact): Boolean = false

    override fun rawContactId(rawContact: NewRawContact): Long? = null
}