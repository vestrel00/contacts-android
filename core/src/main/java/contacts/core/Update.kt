package contacts.core

import android.content.ContentProviderOperation
import android.content.ContentResolver
import contacts.core.entities.ExistingContactEntity
import contacts.core.entities.ExistingRawContactEntity
import contacts.core.entities.MutableRawContact
import contacts.core.entities.custom.CustomDataCountRestriction
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.entities.operation.*
import contacts.core.util.*

/**
 * Updates one or more contacts in the Contacts Provider database to ensure that it contains the
 * same data as the contacts and raw contacts provided in [contacts] and [rawContacts].
 *
 * This does not support updating user Profile Contact. For Profile updates, use
 * [contacts.core.profile.ProfileUpdate].
 *
 * As per documentation in [android.provider.ContactsContract.Contacts],
 *
 * > Only certain columns of Contact are modifiable: STARRED, CUSTOM_RINGTONE, SEND_TO_VOICEMAIL.
 * > Changing any of these columns on the Contact also changes them on all constituent raw contacts.
 *
 * ## Blank data are deleted
 *
 * Blank data will be deleted, unless the corresponding fields are not provided in [include].
 * For example, if all properties of an email are all null, empty, or blank, then the email is
 * deleted. This is the same behavior as the AOSP Contacts app.
 *
 * Note that in cases where blank data are deleted, existing RawContact instances (in memory) will
 * still have references to the deleted data instance. The RawContact instances (in memory) must be
 * refreshed to get the most up-to-date data sets.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All updates will do nothing if these permissions are not granted.
 *
 * ## Usage
 *
 * To update a raw contact's name to "john doe" and add an email "john@doe.com";
 *
 * In Kotlin,
 *
 * ```kotlin
 * val mutableRawContact = rawContact.mutableCopy {
 *      name = NewName(
 *          givenName = "john"
 *          familyName = "doe"
 *      )
 *      emails.add(NewEmail(
 *          type = EmailEntity.Type.HOME
 *          address = "john@doe.com"
 *      ))
 * }
 *
 * val result = update
 *      .rawContacts(mutableRawContact)
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
 * MutableRawContact mutableRawContact = rawContact.mutableCopy();
 * mutableRawContact.setName(name);
 * mutableRawContact.getEmails().add(email);
 *
 * Update.Result result = update
 *      .rawContacts(mutableRawContact)
 *      .commit();
 * ```
 */
interface Update : CrudApi {

    /**
     * If [deleteBlanks] is set to true, then updating blank RawContacts
     * ([ExistingRawContactEntity.isBlank]) or blank Contacts ([ExistingContactEntity.isBlank]) will
     * result in their deletion. Otherwise, blanks will not be deleted and will result in a failed
     * operation. This flag is set to true by default.
     *
     * The Contacts Providers allows for RawContacts that have no rows in the Data table (let's call
     * them "blanks") to exist. The AOSP Contacts app does not allow insertion of new RawContacts
     * without at least one data row. It also deletes blanks on update. Despite seemingly not
     * allowing blanks, the AOSP Contacts app shows them.
     *
     * Note that this DOES NOT refer to blank data, which are deleted regardless of the value passed
     * to this function.
     */
    fun deleteBlanks(deleteBlanks: Boolean): Update

    /**
     * Specifies that only the given set of [fields] (data) will be updated.
     *
     * If no fields are specified, then all fields will be updated. Otherwise, only the specified
     * fields will be updated in addition to required API fields [Fields.Required] (e.g. IDs),
     * which are always included.
     *
     * Blank data are deleted on update, unless the corresponding fields are NOT included.
     *
     * Note that this may affect performance. It is recommended to only include fields that will be
     * used to save CPU and memory.
     */
    fun include(vararg fields: AbstractDataField): Update

    /**
     * See [Update.include].
     */
    fun include(fields: Collection<AbstractDataField>): Update

    /**
     * See [Update.include].
     */
    fun include(fields: Sequence<AbstractDataField>): Update

    /**
     * See [Update.include].
     */
    fun include(fields: Fields.() -> Collection<AbstractDataField>): Update

    /**
     * Similar to [include] except this is used to specify
     * [contacts.core.entities.RawContact.sourceId] and
     * [contacts.core.entities.RawContact.options] fields. All other RawContact table fields are
     * ignored.
     *
     * If no fields are specified, then all RawContacts fields ([RawContactsFields.all]) are
     * included. Otherwise, only the specified fields will be included in addition to required API
     * fields [RawContactsFields.Required].
     */
    fun includeRawContactsFields(vararg fields: RawContactsField): Update

    /**
     * See [Update.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: Collection<RawContactsField>): Update

    /**
     * See [Update.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: Sequence<RawContactsField>): Update

    /**
     * See [Update.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: RawContactsFields.() -> Collection<RawContactsField>): Update

    /**
     * Adds the given [rawContacts] to the update queue, which will be updated on [commit].
     *
     * Each [ExistingRawContactEntity] will be updated separately.
     */
    fun rawContacts(vararg rawContacts: ExistingRawContactEntity): Update

    /**
     * See [Update.rawContacts].
     */
    fun rawContacts(rawContacts: Collection<ExistingRawContactEntity>): Update

    /**
     * See [Update.rawContacts].
     */
    fun rawContacts(rawContacts: Sequence<ExistingRawContactEntity>): Update

    /**
     * Adds the given [contacts] to the update queue, which will be updated on [commit].
     *
     * Each [ExistingContactEntity] will be updated separately. However, all of the
     * [ExistingContactEntity.rawContacts] of a [ExistingContactEntity] will be updated together
     * in one atomic operation.
     */
    fun contacts(vararg contacts: ExistingContactEntity): Update

    /**
     * See [Update.contacts].
     */
    fun contacts(contacts: Collection<ExistingContactEntity>): Update

    /**
     * See [Update.contacts].
     */
    fun contacts(contacts: Sequence<ExistingContactEntity>): Update

    /**
     * Updates the [ExistingRawContactEntity]s in the queue (added via [rawContacts] and [contacts])
     * and returns the [Result].
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

    /**
     * Updates the [ExistingRawContactEntity]s in the queue (added via [rawContacts] and [contacts])
     * and returns the [Result].
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Cancellation
     *
     * To cancel at any time, the [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * **Cancelling does not undo updates. This means that depending on when the cancellation
     * occurs, some if not all of the RawContacts in the update queue may have already been
     * updated.**
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
    override fun redactedCopy(): Update

    interface Result : CrudApi.Result {

        /**
         * True if all Contacts and RawContacts have successfully been updated. False if even one
         * update failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [rawContact] has been successfully updated. False otherwise.
         *
         * Use this for checking the success status of [ExistingRawContactEntity] passed in
         * [rawContacts].
         */
        fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean

        /**
         * True if the [contact] along with all of its [ExistingContactEntity.rawContacts] have
         * been successfully updated. False otherwise.
         *
         * Use this for checking the success status of [ExistingContactEntity] passed in [contacts].
         */
        fun isSuccessful(contact: ExistingContactEntity): Boolean

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun Update(contacts: Contacts): Update = UpdateImpl(contacts)

private class UpdateImpl(
    override val contactsApi: Contacts,

    private var deleteBlanks: Boolean = true,
    private var include: Include<AbstractDataField> = contactsApi.includeAllFields(),
    private var includeRawContactsFields: Include<RawContactsField> = DEFAULT_INCLUDE_RAW_CONTACTS_FIELDS,
    private val contacts: MutableSet<ExistingContactEntity> = mutableSetOf(),
    private val rawContacts: MutableSet<ExistingRawContactEntity> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : Update {

    override fun toString(): String =
        """
            Update {
                deleteBlanks: $deleteBlanks
                include: $include
                includeRawContactsFields: $includeRawContactsFields
                contacts: $contacts
                rawContacts: $rawContacts
                hasPermission: ${permissions.canUpdateDelete()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): Update = UpdateImpl(
        contactsApi,

        deleteBlanks,
        include,
        includeRawContactsFields,
        // Redact contact data.
        contacts.asSequence().redactedCopies().toMutableSet(),
        rawContacts.asSequence().redactedCopies().toMutableSet(),

        isRedacted = true
    )

    override fun deleteBlanks(deleteBlanks: Boolean): Update = apply {
        this.deleteBlanks = deleteBlanks
    }

    override fun include(vararg fields: AbstractDataField) = include(fields.asSequence())

    override fun include(fields: Collection<AbstractDataField>) = include(fields.asSequence())

    override fun include(fields: Sequence<AbstractDataField>): Update = apply {
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

    override fun includeRawContactsFields(fields: Sequence<RawContactsField>): Update = apply {
        includeRawContactsFields = if (fields.isEmpty()) {
            DEFAULT_INCLUDE_RAW_CONTACTS_FIELDS
        } else {
            Include(fields + REQUIRED_INCLUDE_RAW_CONTACTS_FIELDS)
        }
    }

    override fun includeRawContactsFields(
        fields: RawContactsFields.() -> Collection<RawContactsField>
    ) = includeRawContactsFields(fields(RawContactsFields))

    override fun rawContacts(vararg rawContacts: ExistingRawContactEntity) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Collection<ExistingRawContactEntity>) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Sequence<ExistingRawContactEntity>): Update = apply {
        this.rawContacts.addAll(rawContacts.redactedCopiesOrThis(isRedacted))
    }

    override fun contacts(vararg contacts: ExistingContactEntity) =
        contacts(contacts.asSequence())

    override fun contacts(contacts: Collection<ExistingContactEntity>) =
        contacts(contacts.asSequence())

    override fun contacts(contacts: Sequence<ExistingContactEntity>): Update = apply {
        this.contacts.addAll(contacts.redactedCopiesOrThis(isRedacted))
    }

    override fun commit(): Update.Result = commit { false }

    override fun commit(cancel: () -> Boolean): Update.Result {
        onPreExecute()

        return if (
            (contacts.isEmpty() && rawContacts.isEmpty()) ||
            !permissions.canUpdateDelete() ||
            cancel()
        ) {
            UpdateFailed()
        } else {
            val contactIdsResultMap = mutableMapOf<Long, Boolean>()
            val rawContactIdsResultMap = mutableMapOf<Long, Boolean>()

            for (contact in contacts) {
                if (cancel()) {
                    break
                }

                contactIdsResultMap[contact.id] = if (contact.isProfile) {
                    // Intentionally fail the operation to ensure that this is only used for
                    // non-profile updates. Otherwise, operation can succeed. This is only done to
                    // enforce API design.
                    false
                } else if (contact.isBlank && deleteBlanks) {
                    contactsApi.deleteRawContactsWhere(RawContactsFields.ContactId equalTo contact.id)
                } else {
                    contactsApi.updateContact(
                        include.fields,
                        includeRawContactsFields.fields,
                        contact,
                        cancel
                    )
                }
            }

            for (rawContact in rawContacts) {
                if (cancel()) {
                    break
                }

                rawContactIdsResultMap[rawContact.id] = if (rawContact.isProfile) {
                    // Intentionally fail the operation to ensure that this is only used for
                    // non-profile updates. Otherwise, operation can succeed. This is only done to
                    // enforce API design.
                    false
                } else if (rawContact.isBlank && deleteBlanks) {
                    contactsApi.deleteRawContactsWhere(RawContactsFields.Id equalTo rawContact.id)
                } else {
                    contactsApi.updateRawContact(
                        include.fields,
                        includeRawContactsFields.fields,
                        rawContact,
                        cancel
                    )
                }
            }

            UpdateResult(
                contactIdsResultMap = contactIdsResultMap,
                rawContactIdsResultMap = rawContactIdsResultMap
            )
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }

    private companion object {
        val DEFAULT_INCLUDE_RAW_CONTACTS_FIELDS by lazy { Include(RawContactsFields.all) }
        val REQUIRED_INCLUDE_RAW_CONTACTS_FIELDS by lazy {
            RawContactsFields.Required.all.asSequence()
        }
    }
}

/**
 * Updates an existing profile or non-profile contact and all constituent raw contacts.
 */
internal fun Contacts.updateContact(
    includeFields: Set<AbstractDataField>,
    includeRawContactsFields: Set<RawContactsField>,
    contact: ExistingContactEntity,
    cancel: () -> Boolean
): Boolean {

    val operations = arrayListOf<ContentProviderOperation>()

    for (rawContact in contact.rawContacts) {
        operations.addAll(
            updateOperationsForRawContact(
                includeFields, includeRawContactsFields, rawContact, cancel
            )
        )
    }

    // Apply the Contacts option operations after RawContacts options because Contacts options
    // takes priority of RawContacts options. Users may exclude Fields.Contact.Options if they want
    // to update RawContact options.
    OptionsOperation().updateContactOptions(
        callerIsSyncAdapter = callerIsSyncAdapter,
        contact.id,
        contact.options,
        Fields.Contact.Options.intersect(includeFields)
    )?.let(operations::add)

    /*
     * Atomically perform all of the operations. All will either succeed or all will fail.
     */
    val success = contentResolver.applyBatch(operations) != null

    if (success) {
        // We will attempt to set or remove the photos, ignoring whether they fails or succeeds.
        // Users of this library can submit a request to change this behavior if they want =)
        for (rawContact in contact.rawContacts) {
            executePendingPhotoDataOperationFor(rawContact)
        }
        // Note that the contact photo operations are just propagated to the raw contacts.
    }

    return success
}

/**
 * Updates an existing profile or non-profile raw contact's data rows.
 *
 * If a raw contact attribute is null or the attribute's values are all null, then the
 * corresponding data row (if any) will be deleted.
 *
 * If only some of a raw contact's attribute's values are null, then a data row will be created
 * if it does not yet exist.
 */
internal fun Contacts.updateRawContact(
    includeFields: Set<AbstractDataField>,
    includeRawContactsFields: Set<RawContactsField>,
    rawContact: ExistingRawContactEntity,
    cancel: () -> Boolean
): Boolean {

    val operations = updateOperationsForRawContact(
        includeFields, includeRawContactsFields, rawContact, cancel
    )

    /*
     * Atomically perform all of the operations. All will either succeed or all will fail.
     */
    val success = contentResolver.applyBatch(operations) != null

    if (success) {
        // We will attempt to set or remove the photo, ignoring whether it fails or succeeds.
        // Users of this library can submit a request to change this behavior if they want =)
        executePendingPhotoDataOperationFor(rawContact)
    }

    return success
}

private fun Contacts.executePendingPhotoDataOperationFor(rawContact: ExistingRawContactEntity) {
    if (rawContact is MutableRawContact) {
        rawContact.photoDataOperation?.let {
            when (it) {
                is PhotoDataOperation.SetPhoto -> setRawContactPhotoDirect(
                    rawContact.id,
                    it.photoData
                )

                is PhotoDataOperation.RemovePhoto -> removeRawContactPhotoDirect(rawContact.id)
            }
        }
        // Perform the operation only once. Users can set a pending operation again if they'd like.
        rawContact.photoDataOperation = null
    }
}

private fun Contacts.updateOperationsForRawContact(
    includeFields: Set<AbstractDataField>,
    includeRawContactsFields: Set<RawContactsField>,
    rawContact: ExistingRawContactEntity,
    cancel: () -> Boolean
): ArrayList<ContentProviderOperation> {
    val isProfile = rawContact.isProfile

    val operations = arrayListOf<ContentProviderOperation>()

    RawContactsOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile
    ).update(rawContact, includeRawContactsFields)
        ?.let(operations::add)

    operations.addAll(
        AddressOperation(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            Fields.Address.intersect(includeFields)
        ).updateInsertOrDeleteDataForRawContact(
            rawContact.addresses, rawContact.id, contentResolver
        )
    )

    operations.addAll(
        EmailOperation(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            Fields.Email.intersect(includeFields)
        ).updateInsertOrDeleteDataForRawContact(
            rawContact.emails, rawContact.id, contentResolver
        )
    )

    operations.addAll(
        EventOperation(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            Fields.Event.intersect(includeFields)
        ).updateInsertOrDeleteDataForRawContact(
            rawContact.events, rawContact.id, contentResolver
        )
    )

    operations.addAll(
        GroupMembershipOperation(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            Fields.GroupMembership.intersect(includeFields)
        ).updateInsertOrDelete(
            rawContact.groupMemberships, rawContact.id, this, cancel
        )
    )

    operations.addAll(
        ImOperation(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            Fields.Im.intersect(includeFields)
        ).updateInsertOrDeleteDataForRawContact(
            rawContact.ims, rawContact.id, contentResolver
        )
    )

    NameOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        Fields.Name.intersect(includeFields)
    ).updateInsertOrDeleteDataForRawContact(
        rawContact.name, rawContact.id, contentResolver
    )?.let(operations::add)

    NicknameOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        Fields.Nickname.intersect(includeFields)
    ).updateInsertOrDeleteDataForRawContact(
        rawContact.nickname, rawContact.id, contentResolver
    )?.let(operations::add)

    NoteOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        Fields.Note.intersect(includeFields)
    ).updateInsertOrDeleteDataForRawContact(
        rawContact.note, rawContact.id, contentResolver
    )?.let(operations::add)

    // Apply the options operations after the group memberships operation.
    // Any remove or add membership operation to the favorites group will be overshadowed by the
    // value of Options.starred. If starred is true, the Contacts Provider will automatically add a
    // group membership to the favorites group (if exist). If starred is false, then the favorites
    // group membership will be removed.
    OptionsOperation().updateRawContactOptions(
        callerIsSyncAdapter = callerIsSyncAdapter,
        rawContact.options,
        rawContact.id,
        RawContactsFields.Options.all.intersect(includeRawContactsFields)
    )?.let(operations::add)

    OrganizationOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        Fields.Organization.intersect(includeFields)
    ).updateInsertOrDeleteDataForRawContact(
        rawContact.organization, rawContact.id, contentResolver
    )?.let(operations::add)

    operations.addAll(
        PhoneOperation(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            Fields.Phone.intersect(includeFields)
        ).updateInsertOrDeleteDataForRawContact(
            rawContact.phones, rawContact.id, contentResolver
        )
    )

    // Photo is intentionally excluded here. Use the ContactPhoto and RawContactPhoto extensions
    // to set full-sized and thumbnail photos.

    operations.addAll(
        RelationOperation(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            Fields.Relation.intersect(includeFields)
        ).updateInsertOrDeleteDataForRawContact(
            rawContact.relations, rawContact.id, contentResolver
        )
    )

    SipAddressOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        Fields.SipAddress.intersect(includeFields)
    ).updateInsertOrDeleteDataForRawContact(
        rawContact.sipAddress, rawContact.id, contentResolver
    )?.let(operations::add)

    operations.addAll(
        WebsiteOperation(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            Fields.Website.intersect(includeFields)
        ).updateInsertOrDeleteDataForRawContact(
            rawContact.websites, rawContact.id, contentResolver
        )
    )

    // Process custom data
    operations.addAll(
        rawContact.customDataUpdateInsertOrDeleteOperations(
            callerIsSyncAdapter = callerIsSyncAdapter,
            contentResolver, includeFields, customDataRegistry
        )
    )

    return operations
}

private fun ExistingRawContactEntity.customDataUpdateInsertOrDeleteOperations(
    callerIsSyncAdapter: Boolean,
    contentResolver: ContentResolver,
    includeFields: Set<AbstractDataField>,
    customDataRegistry: CustomDataRegistry
): List<ContentProviderOperation> = buildList {
    for ((mimeTypeValue, customDataEntityHolder) in customDataEntities) {
        val customDataEntry = customDataRegistry.entryOf(mimeTypeValue)

        val countRestriction = customDataEntry.countRestriction
        val customDataOperation = customDataEntry.operationFactory.create(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            includeFields = customDataEntry.fieldSet.intersect(includeFields)
        )

        when (countRestriction) {
            CustomDataCountRestriction.AT_MOST_ONE -> {
                customDataOperation
                    .updateInsertOrDeleteDataForRawContact(
                        customDataEntityHolder.entities.firstOrNull(), id, contentResolver
                    )?.let(::add)
            }

            CustomDataCountRestriction.NO_LIMIT -> {
                customDataOperation
                    .updateInsertOrDeleteDataForRawContact(
                        customDataEntityHolder.entities,
                        id,
                        contentResolver
                    )
                    .let(::addAll)
            }
        }
    }
}

private class UpdateResult private constructor(
    private val contactIdsResultMap: Map<Long, Boolean>,
    private val rawContactIdsResultMap: Map<Long, Boolean>,
    override val isRedacted: Boolean
) : Update.Result {

    constructor(
        contactIdsResultMap: Map<Long, Boolean>,
        rawContactIdsResultMap: Map<Long, Boolean>
    ) : this(contactIdsResultMap, rawContactIdsResultMap, false)

    override fun toString(): String =
        """
            Update.Result {
                isSuccessful: $isSuccessful
                contactIdsResultMap: $contactIdsResultMap
                rawContactIdsResultMap: $rawContactIdsResultMap
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): Update.Result = UpdateResult(
        contactIdsResultMap = contactIdsResultMap,
        rawContactIdsResultMap = rawContactIdsResultMap,
        isRedacted = true
    )

    override val isSuccessful: Boolean by lazy {
        if (rawContactIdsResultMap.isEmpty() && contactIdsResultMap.isEmpty()) {
            // Updating nothing is NOT successful.
            false
        } else {
            // A set has failure if it is NOT empty and one of its entries is false.
            val hasRawContactFailure = rawContactIdsResultMap.any { !it.value }
            val hasContactFailure = contactIdsResultMap.any { !it.value }
            !hasRawContactFailure && !hasContactFailure
        }
    }

    override fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean =
        rawContactIdsResultMap.getOrElse(rawContact.id) { false }

    override fun isSuccessful(contact: ExistingContactEntity): Boolean =
        contactIdsResultMap.getOrElse(contact.id) { false }
}

private class UpdateFailed private constructor(
    override val isRedacted: Boolean
) : Update.Result {

    constructor() : this(false)

    override fun toString(): String =
        """
            Update.Result {
                isSuccessful: $isSuccessful
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): Update.Result = UpdateFailed(true)

    override val isSuccessful: Boolean = false

    override fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean = false

    override fun isSuccessful(contact: ExistingContactEntity): Boolean = false
}