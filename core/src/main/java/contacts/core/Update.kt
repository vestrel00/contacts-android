package contacts.core

import android.content.ContentProviderOperation
import android.content.ContentResolver
import contacts.core.entities.MutableContact
import contacts.core.entities.MutableRawContact
import contacts.core.entities.custom.CustomDataCountRestriction
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.entities.operation.*
import contacts.core.util.applyBatch
import contacts.core.util.isEmpty
import contacts.core.util.unsafeLazy

/**
 * Updates one or more raw contacts' rows in the data table.
 *
 * This does not support updating user Profile Contact. For Profile updates, use
 * [contacts.core.profile.ProfileUpdate].
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
 * val mutableRawContact = rawContact.toMutableRawContact().apply {
 *      name = MutableName().apply {
 *          givenName = "john"
 *          familyName = "doe"
 *      }
 *      emails.add(MutableEmail().apply {
 *          type = Email.Type.HOME
 *          address = "john@doe.com"
 *      })
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
 * MutableName name = new MutableName();
 * name.setGivenName("john");
 * name.setFamilyName("doe");
 *
 * MutableEmail email = new MutableEmail();
 * email.setType(Email.Type.HOME);
 * email.setAddress("john@doe.com");
 *
 * MutableRawContact mutableRawContact = rawContact.toMutableRawContact();
 * mutableRawContact.setName(name);
 * mutableRawContact.getEmails().add(email);
 *
 * Update.Result result = update
 *      .rawContacts(mutableRawContact)
 *      .commit();
 * ```
 */
interface Update {

    /**
     * If [deleteBlanks] is set to true, then updating blank RawContacts
     * ([MutableRawContact.isBlank]) or blank Contacts ([MutableContact.isBlank]) will result in
     * their deletion. Otherwise, blanks will not be deleted and will result in a failed operation.
     * This flag is set to true by default.
     *
     * The Contacts Providers allows for RawContacts that have no rows in the Data table (let's call
     * them "blanks") to exist. The native Contacts app does not allow insertion of new RawContacts
     * without at least one data row. It also deletes blanks on update. Despite seemingly not
     * allowing blanks, the native Contacts app shows them.
     *
     * Note that blank data are always deleted. For example, if all properties of an email are all
     * null or blank, then the email is deleted. This behavior cannot be modified.
     */
    fun deleteBlanks(deleteBlanks: Boolean): Update

    /**
     * Specifies that only the given set of [fields] (data) will be updated.
     *
     * If no fields are specified, then all fields will be updated. Otherwise, only the specified
     * fields will be updated in addition to required API fields [Fields.Required] (e.g. IDs),
     * which are always included.
     *
     * Note that this may affect performance. It is recommended to only include fields that will be
     * used to save CPU and memory.
     *
     * ## Performing updates on entities with partial includes
     *
     * When the query include function is used, only certain data will be included in the returned
     * entities. All other data are guaranteed to be null (except for those in [Fields.Required]).
     * When performing updates on entities that have only partial data included, make sure to use
     * the same included fields in the update operation as the included fields used in the query.
     * This will ensure that the set of data queried and updated are the same. For example, in order
     * to get and set only email addresses and leave everything the same in the database...
     *
     * ```kotlin
     * val contacts = query.include(Fields.Email.Address).find()
     * val mutableContacts = setEmailAddresses(contacts)
     * update.contacts(mutableContacts).include(Fields.Email.Address).commit()
     * ```
     *
     * On the other hand, you may intentionally include only some data and perform updates without
     * on all data (not just the included ones) to effectively delete all non-included data. This
     * is, currently, a feature- not a bug! For example, in order to get and set only email
     * addresses and set all other data to null (such as phone numbers, name, etc) in the database..
     *
     * ```kotlin
     * val contacts = query.include(Fields.Email.Address).find()
     * val mutableContacts = setEmailAddresses(contacts)
     * update.contacts(mutableContacts).include(Fields.all).commit()
     * ```
     *
     * This gives you the most flexibility when it comes to specifying what fields to
     * include/exclude in queries, inserts, and update, which will allow you to do things beyond
     * your wildest imagination!
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
     * Adds the given [rawContacts] to the update queue, which will be updated on [commit].
     *
     * Only existing [rawContacts] that have been retrieved via a query will be added to the
     * update queue. Those that have been manually created via a constructor will be ignored and
     * result in a failed operation.
     */
    fun rawContacts(vararg rawContacts: MutableRawContact): Update

    /**
     * See [Update.rawContacts].
     */
    fun rawContacts(rawContacts: Collection<MutableRawContact>): Update

    /**
     * See [Update.rawContacts].
     */
    fun rawContacts(rawContacts: Sequence<MutableRawContact>): Update

    /**
     * Adds the [MutableRawContact]s of the given [contacts] to the update queue, which will be
     * updated on [commit].
     *
     * See [rawContacts].
     */
    fun contacts(vararg contacts: MutableContact): Update

    /**
     * See [Update.contacts].
     */
    fun contacts(contacts: Collection<MutableContact>): Update

    /**
     * See [Update.contacts].
     */
    fun contacts(contacts: Sequence<MutableContact>): Update

    /**
     * Updates the [MutableRawContact]s in the queue (added via [rawContacts] and [contacts]) and
     * returns the [Result].
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
     * Updates the [MutableRawContact]s in the queue (added via [rawContacts] and [contacts]) and
     * returns the [Result].
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

    interface Result {

        /**
         * True if all Contacts and RawContacts have successfully been updated. False if even one
         * update failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [rawContact] has been successfully updated. False otherwise.
         */
        fun isSuccessful(rawContact: MutableRawContact): Boolean

        /**
         * True if all of the [MutableContact.rawContacts] has been successfully updated. False
         * otherwise.
         *
         * ## Important
         *
         * If this [contact] has as [MutableRawContact] that has not been updated, then this will
         * return false. This may occur if only some (not all) of the [MutableRawContact] in
         * [MutableContact.rawContacts] has been added to the update queue via [Update.rawContacts].
         */
        fun isSuccessful(contact: MutableContact): Boolean
    }
}

@Suppress("FunctionName")
internal fun Update(contacts: Contacts): Update = UpdateImpl(contacts)

private class UpdateImpl(
    private val contacts: Contacts,

    private var deleteBlanks: Boolean = true,
    private var include: Include<AbstractDataField> = allDataFields(contacts.customDataRegistry),
    private val rawContacts: MutableSet<MutableRawContact> = mutableSetOf()
) : Update {

    override fun toString(): String =
        """
            Update {
                deleteBlanks: $deleteBlanks
                include: $include
                rawContacts: $rawContacts
            }
        """.trimIndent()

    override fun deleteBlanks(deleteBlanks: Boolean): Update = apply {
        this.deleteBlanks = deleteBlanks
    }

    override fun include(vararg fields: AbstractDataField) = include(fields.asSequence())

    override fun include(fields: Collection<AbstractDataField>) = include(fields.asSequence())

    override fun include(fields: Sequence<AbstractDataField>): Update = apply {
        include = if (fields.isEmpty()) {
            allDataFields(contacts.customDataRegistry)
        } else {
            Include(fields + Fields.Required.all.asSequence())
        }
    }

    override fun rawContacts(vararg rawContacts: MutableRawContact) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Collection<MutableRawContact>) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Sequence<MutableRawContact>): Update = apply {
        this.rawContacts.addAll(rawContacts)
    }

    override fun contacts(vararg contacts: MutableContact) =
        contacts(contacts.asSequence())

    override fun contacts(contacts: Collection<MutableContact>) =
        contacts(contacts.asSequence())

    override fun contacts(contacts: Sequence<MutableContact>): Update =
        rawContacts(contacts.flatMap { it.rawContacts.asSequence() })

    override fun commit(): Update.Result = commit { false }

    override fun commit(cancel: () -> Boolean): Update.Result {
        if (rawContacts.isEmpty() || !contacts.permissions.canUpdateDelete() || cancel()) {
            return UpdateFailed()
        }

        val results = mutableMapOf<Long, Boolean>()
        for (rawContact in rawContacts) {
            if (cancel()) {
                break
            }

            if (rawContact.id != null) {
                results[rawContact.id] = if (rawContact.isProfile) {
                    // Intentionally fail the operation to ensure that this is only used for
                    // non-profile updates. Otherwise, operation can succeed. This is only done to
                    // enforce API design.
                    false
                } else if (rawContact.isBlank && deleteBlanks) {
                    contacts.applicationContext.contentResolver
                        .deleteRawContactWithId(rawContact.id)
                } else {
                    contacts.updateRawContact(include.fields, rawContact)
                }
            } else {
                results[INVALID_ID] = false
            }
        }
        return UpdateResult(results)
    }

    private companion object {
        // A failed entry in the results so that Result.isSuccessful returns false.
        const val INVALID_ID = -1L
    }
}

/**
 * Updates a raw contact's data rows.
 *
 * If a raw contact attribute is null or the attribute's values are all null, then the
 * corresponding data row (if any) will be deleted.
 *
 * If only some of a raw contact's attribute's values are null, then a data row will be created
 * if it does not yet exist.
 */
internal fun Contacts.updateRawContact(
    includeFields: Set<AbstractDataField>,
    rawContact: MutableRawContact
): Boolean {
    if (rawContact.id == null) {
        return false
    }

    val isProfile = rawContact.isProfile
    val account = accounts(isProfile).query().accountFor(rawContact)
    val hasAccount = account != null

    val operations = arrayListOf<ContentProviderOperation>()

    operations.addAll(
        AddressOperation(isProfile, Fields.Address.intersect(includeFields)).updateInsertOrDelete(
            rawContact.addresses, rawContact.id, applicationContext.contentResolver
        )
    )

    operations.addAll(
        EmailOperation(isProfile, Fields.Email.intersect(includeFields)).updateInsertOrDelete(
            rawContact.emails, rawContact.id, applicationContext.contentResolver
        )
    )

    if (hasAccount) {
        // I'm not sure why the native Contacts app hides events from the UI for local raw contacts.
        // The Contacts Provider does support having events for local raw contacts. Anyways, let's
        // follow in the footsteps of the native Contacts app...
        operations.addAll(
            EventOperation(isProfile, Fields.Event.intersect(includeFields)).updateInsertOrDelete(
                rawContact.events, rawContact.id, applicationContext.contentResolver
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
        ImOperation(isProfile, Fields.Im.intersect(includeFields)).updateInsertOrDelete(
            rawContact.ims, rawContact.id, applicationContext.contentResolver
        )
    )

    NameOperation(isProfile, Fields.Name.intersect(includeFields)).updateInsertOrDelete(
        rawContact.name, rawContact.id, applicationContext.contentResolver
    )?.let(operations::add)

    NicknameOperation(isProfile, Fields.Nickname.intersect(includeFields)).updateInsertOrDelete(
        rawContact.nickname, rawContact.id, applicationContext.contentResolver
    )?.let(operations::add)

    NoteOperation(isProfile, Fields.Note.intersect(includeFields)).updateInsertOrDelete(
        rawContact.note, rawContact.id, applicationContext.contentResolver
    )?.let(operations::add)

    OrganizationOperation(isProfile, Fields.Organization.intersect(includeFields))
        .updateInsertOrDelete(
            rawContact.organization, rawContact.id, applicationContext.contentResolver
        )?.let(operations::add)

    operations.addAll(
        PhoneOperation(isProfile, Fields.Phone.intersect(includeFields)).updateInsertOrDelete(
            rawContact.phones, rawContact.id, applicationContext.contentResolver
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
                .updateInsertOrDelete(
                    rawContact.relations, rawContact.id, applicationContext.contentResolver
                )
        )
    }

    SipAddressOperation(isProfile, Fields.SipAddress.intersect(includeFields))
        .updateInsertOrDelete(
            rawContact.sipAddress, rawContact.id, applicationContext.contentResolver
        )?.let(operations::add)

    operations.addAll(
        WebsiteOperation(isProfile, Fields.Website.intersect(includeFields)).updateInsertOrDelete(
            rawContact.websites, rawContact.id, applicationContext.contentResolver
        )
    )

    // Process custom data
    operations.addAll(
        rawContact.customDataUpdateInsertOrDeleteOperations(
            applicationContext.contentResolver, includeFields, customDataRegistry
        )
    )

    /*
     * Atomically update all of the associated Data rows. All of the above operations will
     * either succeed or fail.
     */
    return applicationContext.contentResolver.applyBatch(operations) != null
}

private fun MutableRawContact.customDataUpdateInsertOrDeleteOperations(
    contentResolver: ContentResolver,
    includeFields: Set<AbstractDataField>,
    customDataRegistry: CustomDataRegistry
): List<ContentProviderOperation> = mutableListOf<ContentProviderOperation>().apply {
    if (id == null) {
        return@apply
    }

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
                    .updateInsertOrDelete(
                        customDataEntityHolder.entities.firstOrNull(), id, contentResolver
                    )?.let(::add)
            }
            CustomDataCountRestriction.NO_LIMIT -> {
                customDataOperation
                    .updateInsertOrDelete(customDataEntityHolder.entities, id, contentResolver)
                    .let(::addAll)
            }
        }
    }
}

private class UpdateResult(private val rawContactIdsResultMap: Map<Long, Boolean>) : Update.Result {

    override val isSuccessful: Boolean by unsafeLazy { rawContactIdsResultMap.all { it.value } }

    override fun isSuccessful(rawContact: MutableRawContact): Boolean = isSuccessful(rawContact.id)

    override fun isSuccessful(contact: MutableContact): Boolean {
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

private class UpdateFailed : Update.Result {

    override val isSuccessful: Boolean = false

    override fun isSuccessful(rawContact: MutableRawContact): Boolean = false

    override fun isSuccessful(contact: MutableContact): Boolean = false
}