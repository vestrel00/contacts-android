package contacts.core

import android.content.ContentProviderOperation
import android.content.ContentResolver
import contacts.core.accounts.accountForRawContactWithId
import contacts.core.entities.ExistingContactEntity
import contacts.core.entities.ExistingRawContactEntity
import contacts.core.entities.custom.CustomDataCountRestriction
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.entities.operation.*
import contacts.core.util.applyBatch
import contacts.core.util.isEmpty
import contacts.core.util.unsafeLazy

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
 * deleted. This is the same behavior as the native Contacts app.
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
 * Java,
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
     * them "blanks") to exist. The native Contacts app does not allow insertion of new RawContacts
     * without at least one data row. It also deletes blanks on update. Despite seemingly not
     * allowing blanks, the native Contacts app shows them.
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
     * Adds the given [rawContacts] to the update queue, which will be updated on [commit].
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
     * Adds the [ExistingRawContactEntity]s of the given [contacts] to the update queue, which will
     * be updated on [commit].
     *
     * See [rawContacts].
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
         */
        fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean

        /**
         * True if all of the [ExistingContactEntity.rawContacts] has been successfully updated.
         * False otherwise.
         *
         * ## Important
         *
         * If this [contact] has as [ExistingRawContactEntity] that has not been updated, then this
         * will return false. This may occur if only some (not all) of the
         * [ExistingRawContactEntity] in [ExistingContactEntity.rawContacts] have been added to the
         * update queue via [Update.rawContacts].
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
    private val rawContacts: MutableSet<ExistingRawContactEntity> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : Update {

    override fun toString(): String =
        """
            Update {
                deleteBlanks: $deleteBlanks
                include: $include
                rawContacts: $rawContacts
                hasPermission: ${permissions.canUpdateDelete()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): Update = UpdateImpl(
        contactsApi,

        deleteBlanks,
        include,
        // Redact contact data.
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

    override fun contacts(contacts: Sequence<ExistingContactEntity>): Update =
        rawContacts(contacts.flatMap { it.rawContacts.asSequence() })

    override fun commit(): Update.Result = commit { false }

    override fun commit(cancel: () -> Boolean): Update.Result {
        onPreExecute()

        return if (rawContacts.isEmpty() || !permissions.canUpdateDelete() || cancel()) {
            UpdateFailed()
        } else {
            val results = mutableMapOf<Long, Boolean>()
            for (rawContact in rawContacts) {
                if (cancel()) {
                    break
                }

                results[rawContact.id] = if (rawContact.isProfile) {
                    // Intentionally fail the operation to ensure that this is only used for
                    // non-profile updates. Otherwise, operation can succeed. This is only done to
                    // enforce API design.
                    false
                } else if (rawContact.isBlank && deleteBlanks) {
                    contentResolver.deleteRawContactWithId(rawContact.id)
                } else {
                    contactsApi.updateRawContact(include.fields, rawContact)
                }
            }
            UpdateResult(results)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }
}

/**
 * Updates an existing raw contact's data rows.
 *
 * If a raw contact attribute is null or the attribute's values are all null, then the
 * corresponding data row (if any) will be deleted.
 *
 * If only some of a raw contact's attribute's values are null, then a data row will be created
 * if it does not yet exist.
 */
internal fun Contacts.updateRawContact(
    includeFields: Set<AbstractDataField>,
    rawContact: ExistingRawContactEntity
): Boolean {
    val isProfile = rawContact.isProfile
    val account = contentResolver.accountForRawContactWithId(rawContact.id)
    val hasAccount = account != null

    val operations = arrayListOf<ContentProviderOperation>()

    operations.addAll(
        AddressOperation(isProfile, Fields.Address.intersect(includeFields))
            .updateInsertOrDeleteDataForRawContact(
                rawContact.addresses, rawContact.id, contentResolver
            )
    )

    operations.addAll(
        EmailOperation(isProfile, Fields.Email.intersect(includeFields))
            .updateInsertOrDeleteDataForRawContact(
                rawContact.emails, rawContact.id, contentResolver
            )
    )

    if (hasAccount) {
        // I'm not sure why the native Contacts app hides events from the UI for local raw contacts.
        // The Contacts Provider does support having events for local raw contacts. Anyways, let's
        // follow in the footsteps of the native Contacts app...
        operations.addAll(
            EventOperation(isProfile, Fields.Event.intersect(includeFields))
                .updateInsertOrDeleteDataForRawContact(
                    rawContact.events, rawContact.id, contentResolver
                )
        )
    }

    if (hasAccount) {
        // Groups require an Account. Therefore, memberships to groups cannot exist without groups.
        // It should not be possible for consumers to get access to group memberships.
        // The Contacts Provider does support having events for local raw contacts.
        operations.addAll(
            GroupMembershipOperation(
                isProfile,
                Fields.GroupMembership.intersect(includeFields),
                groups()
            ).updateInsertOrDelete(
                rawContact.groupMemberships, rawContact.id, applicationContext
            )
        )
    }

    operations.addAll(
        ImOperation(isProfile, Fields.Im.intersect(includeFields))
            .updateInsertOrDeleteDataForRawContact(
                rawContact.ims, rawContact.id, contentResolver
            )
    )

    NameOperation(isProfile, Fields.Name.intersect(includeFields))
        .updateInsertOrDeleteDataForRawContact(
            rawContact.name, rawContact.id, contentResolver
        )?.let(operations::add)

    NicknameOperation(isProfile, Fields.Nickname.intersect(includeFields))
        .updateInsertOrDeleteDataForRawContact(
            rawContact.nickname, rawContact.id, contentResolver
        )?.let(operations::add)

    NoteOperation(isProfile, Fields.Note.intersect(includeFields))
        .updateInsertOrDeleteDataForRawContact(
            rawContact.note, rawContact.id, contentResolver
        )?.let(operations::add)

    OrganizationOperation(isProfile, Fields.Organization.intersect(includeFields))
        .updateInsertOrDeleteDataForRawContact(
            rawContact.organization, rawContact.id, contentResolver
        )?.let(operations::add)

    operations.addAll(
        PhoneOperation(isProfile, Fields.Phone.intersect(includeFields))
            .updateInsertOrDeleteDataForRawContact(
                rawContact.phones, rawContact.id, contentResolver
            )
    )

    // Photo is intentionally excluded here. Use the ContactPhoto and RawContactPhoto extensions
    // to set full-sized and thumbnail photos.

    if (hasAccount) {
        // I'm not sure why the native Contacts app hides relations from the UI for local raw
        // contacts. The Contacts Provider does support having events for local raw contacts.
        // Anyways, let's follow in the footsteps of the native Contacts app...
        operations.addAll(
            RelationOperation(isProfile, Fields.Relation.intersect(includeFields))
                .updateInsertOrDeleteDataForRawContact(
                    rawContact.relations, rawContact.id, contentResolver
                )
        )
    }

    SipAddressOperation(isProfile, Fields.SipAddress.intersect(includeFields))
        .updateInsertOrDeleteDataForRawContact(
            rawContact.sipAddress, rawContact.id, contentResolver
        )?.let(operations::add)

    operations.addAll(
        WebsiteOperation(isProfile, Fields.Website.intersect(includeFields))
            .updateInsertOrDeleteDataForRawContact(
                rawContact.websites, rawContact.id, contentResolver
            )
    )

    // Process custom data
    operations.addAll(
        rawContact.customDataUpdateInsertOrDeleteOperations(
            contentResolver, includeFields, customDataRegistry
        )
    )

    /*
     * Atomically update all of the associated Data rows. All of the above operations will
     * either succeed or fail.
     *
     * Note that the returned result on success is ContentProviderResult(count=0). Therefore, we
     * cannot use the count to determine if the operation succeeded or not.
     */
    return contentResolver.applyBatch(operations) != null
}

private fun ExistingRawContactEntity.customDataUpdateInsertOrDeleteOperations(
    contentResolver: ContentResolver,
    includeFields: Set<AbstractDataField>,
    customDataRegistry: CustomDataRegistry
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
    private val rawContactIdsResultMap: Map<Long, Boolean>,
    override val isRedacted: Boolean
) : Update.Result {

    constructor(rawContactIdsResultMap: Map<Long, Boolean>) : this(rawContactIdsResultMap, false)

    override fun toString(): String =
        """
            Update.Result {
                isSuccessful: $isSuccessful
                rawContactIdsResultMap: $rawContactIdsResultMap
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): Update.Result = UpdateResult(
        rawContactIdsResultMap,
        isRedacted = true
    )

    override val isSuccessful: Boolean by unsafeLazy {
        // By default, all returns true when the collection is empty. So, we override that.
        rawContactIdsResultMap.run { isNotEmpty() && all { it.value } }
    }

    override fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean =
        isSuccessful(rawContact.id)

    override fun isSuccessful(contact: ExistingContactEntity): Boolean {
        for (rawContactId in contact.rawContacts.asSequence().map { it.id }) {
            if (!isSuccessful(rawContactId)) {
                return false
            }
        }
        return true
    }

    private fun isSuccessful(rawContactId: Long?): Boolean = rawContactId != null
            && rawContactIdsResultMap.getOrElse(rawContactId) { false }
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