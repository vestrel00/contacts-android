package contacts.core

import android.content.ContentProviderOperation
import android.content.ContentResolver
import contacts.core.entities.ExistingContactEntity
import contacts.core.entities.ExistingRawContactEntityWithContactId
import contacts.core.entities.operation.RawContactsOperation
import contacts.core.util.applyBatch
import contacts.core.util.deleteSuccess
import contacts.core.util.isProfileId
import contacts.core.util.unsafeLazy

/**
 * Deletes one or more raw contacts or contacts from the raw contacts and contacts tables
 * respectively. All associated data rows are also deleted.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All deletes will do nothing if the permission is not granted.
 *
 * ## Usage
 *
 * To delete a [ExistingContactEntity] and all associated [ExistingRawContactEntityWithContactId]s;
 *
 * ```kotlin
 * val result = delete
 *      .contacts(contact)
 *      .commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * Delete.Result result = delete
 *      .contacts(contact)
 *      .commit()
 * ```
 */
interface Delete : CrudApi {

    /**
     * Adds the given [rawContacts] to the delete queue, which will be deleted on [commit].
     *
     * ## Note
     *
     * Deleting all [ExistingRawContactEntityWithContactId]s of a [ExistingContactEntity] will
     * result in the deletion of the associated [ExistingContactEntity]! However, the
     * [ExistingContactEntity] will remain as long as it has at least has one associated
     * [ExistingRawContactEntityWithContactId].
     */
    fun rawContacts(vararg rawContacts: ExistingRawContactEntityWithContactId): Delete

    /**
     * See [Delete.rawContacts].
     */
    fun rawContacts(rawContacts: Collection<ExistingRawContactEntityWithContactId>): Delete

    /**
     * See [Delete.rawContacts].
     */
    fun rawContacts(rawContacts: Sequence<ExistingRawContactEntityWithContactId>): Delete

    /**
     * Adds the given [rawContactsIds] to the delete queue, which will be deleted on [commit].
     *
     * ## Note
     *
     * Deleting all RawContacts of an existing Contact will result in the deletion of the
     * associated Contact! However, the Contact will remain as long as it has at least has one
     * associated RawContact.
     */
    fun rawContactsWithId(vararg rawContactsIds: Long): Delete

    /**
     * See [Delete.rawContactsWithId].
     */
    fun rawContactsWithId(rawContactsIds: Collection<Long>): Delete

    /**
     * See [Delete.rawContactsWithId].
     */
    fun rawContactsWithId(rawContactsIds: Sequence<Long>): Delete

    /**
     * Adds the given [contacts] to the delete queue, which will be deleted on [commit].
     *
     * ## Note
     *
     * Deleting a [ExistingContactEntity] will result in the deletion of all associated
     * [ExistingRawContactEntityWithContactId]s even those that are not in the
     * [ExistingContactEntity.rawContacts] set!
     */
    fun contacts(vararg contacts: ExistingContactEntity): Delete

    /**
     * See [Delete.contacts].
     */
    fun contacts(contacts: Collection<ExistingContactEntity>): Delete

    /**
     * See [Delete.contacts].
     */
    fun contacts(contacts: Sequence<ExistingContactEntity>): Delete

    /**
     * Adds the given [contactsIds] to the delete queue, which will be deleted on [commit].
     *
     * ## Note
     *
     * Deleting a Contact will result in the deletion of all associated RawContacts!
     */
    fun contactsWithId(vararg contactsIds: Long): Delete

    /**
     * See [Delete.contactsWithId].
     */
    fun contactsWithId(contactsIds: Collection<Long>): Delete

    /**
     * See [Delete.contactsWithId].
     */
    fun contactsWithId(contactsIds: Sequence<Long>): Delete

    /**
     * Deletes the [ExistingContactEntity]s and [ExistingRawContactEntityWithContactId]s in the
     * queue (added via [contacts] and [rawContacts]) and returns the [Result].
     *
     * ## Permissions
     *
     * Requires the [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

    /**
     * Deletes the [ExistingContactEntity]s and [ExistingRawContactEntityWithContactId]s in the
     * queue (added via [contacts] and [rawContacts]) in one transaction. Either ALL deletes
     * succeed or ALL fail.
     *
     * ## Permissions
     *
     * Requires the [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commitInOneTransaction(): Result

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
    override fun redactedCopy(): Delete

    interface Result : CrudApi.Result {

        /**
         * True if all Contacts and RawContacts have successfully been deleted. False if even one
         * delete failed.
         *
         * ## Limitation
         *
         * If you specified RawContacts in [rawContacts] or [rawContactsWithId] that belong to a
         * Contact that you also passed in [contacts] or [contactsWithId], then this may be false
         * even if the Contact and RawContacts were actually deleted IF you used [commit].
         * Using [commitInOneTransaction] does not have this limitation.
         */
        val isSuccessful: Boolean

        /**
         * True if the [rawContact] has been successfully deleted. False otherwise.
         *
         * This does not indicate whether the parent [ExistingContactEntity] has been deleted or
         * not. This may return false even if the parent [ExistingContactEntity] has been deleted.
         *
         * This is used in conjunction with [Delete.rawContacts].
         */
        fun isSuccessful(rawContact: ExistingRawContactEntityWithContactId): Boolean

        /**
         * True if the RawContact with the given [rawContactId] has been successfully deleted.
         * False otherwise.
         *
         * This does not indicate whether the parent [ExistingContactEntity] has been deleted or
         * not. This may return false even if the parent [ExistingContactEntity] has been deleted.
         *
         * This is used in conjunction with [Delete.rawContactsWithId].
         */
        fun isRawContactDeleteSuccessful(rawContactId: Long): Boolean

        /**
         * True the [ExistingContactEntity] (and all of its associated
         * [ExistingRawContactEntityWithContactId]s) has been successfully deleted. False otherwise.
         *
         * This does not indicate whether child [ExistingRawContactEntityWithContactId]s have
         * been deleted or not. This may return false even if all associated
         * [ExistingRawContactEntityWithContactId]s have been deleted.
         *
         * This is used in conjunction with [Delete.contacts].
         */
        fun isSuccessful(contact: ExistingContactEntity): Boolean

        /**
         * True the Contact with the given [contactId] (and all of its associated RawContacts) has
         * been successfully deleted. False otherwise.
         *
         * This does not indicate whether child RawContacts have been deleted or not. This may
         * return false even if all associated RawContacts have been deleted.
         *
         * This is used in conjunction with [Delete.contactsWithId].
         */
        fun isContactDeleteSuccessful(contactId: Long): Boolean

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun Delete(contacts: Contacts): Delete = DeleteImpl(contacts)

private class DeleteImpl(
    override val contactsApi: Contacts,

    private val rawContactIds: MutableSet<Long> = mutableSetOf(),
    private val contactIds: MutableSet<Long> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : Delete {

    override fun toString(): String =
        """
            Delete {
                rawContactIds: $rawContactIds
                contactIds: $contactIds
                hasPermission: ${permissions.canUpdateDelete()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    // There isn't really anything to redact =)
    override fun redactedCopy(): Delete = DeleteImpl(
        contactsApi,

        rawContactIds,
        contactIds,

        isRedacted = true
    )

    override fun rawContacts(vararg rawContacts: ExistingRawContactEntityWithContactId) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Collection<ExistingRawContactEntityWithContactId>) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Sequence<ExistingRawContactEntityWithContactId>) =
        rawContactsWithId(rawContacts.asSequence().map { it.id })

    override fun rawContactsWithId(vararg rawContactsIds: Long) =
        rawContactsWithId(rawContactsIds.asSequence())

    override fun rawContactsWithId(rawContactsIds: Collection<Long>) =
        rawContactsWithId(rawContactsIds.asSequence())

    override fun rawContactsWithId(rawContactsIds: Sequence<Long>): Delete = apply {
        this.rawContactIds.addAll(rawContactsIds)
    }

    override fun contacts(vararg contacts: ExistingContactEntity) = contacts(contacts.asSequence())

    override fun contacts(contacts: Collection<ExistingContactEntity>) =
        contacts(contacts.asSequence())

    override fun contacts(contacts: Sequence<ExistingContactEntity>) =
        contactsWithId(contacts.map { it.id })

    override fun contactsWithId(vararg contactsIds: Long) = contactsWithId(contactsIds.asSequence())

    override fun contactsWithId(contactsIds: Collection<Long>) =
        contactsWithId(contactsIds.asSequence())

    override fun contactsWithId(contactsIds: Sequence<Long>): Delete = apply {
        this.contactIds.addAll(contactsIds)
    }

    override fun commit(): Delete.Result {
        onPreExecute()

        return if (
            (contactIds.isEmpty() && rawContactIds.isEmpty()) || !permissions.canUpdateDelete()
        ) {
            DeleteAllResult(isSuccessful = false)
        } else {
            val rawContactsResult = mutableMapOf<Long, Boolean>()
            for (rawContactId in rawContactIds) {
                rawContactsResult[rawContactId] =
                    if (rawContactId.isProfileId) {
                        // Intentionally fail the operation to ensure that this is only used for
                        // non-profile updates. Otherwise, operation can succeed. This is only done
                        // to enforce API design.
                        false
                    } else {
                        contentResolver.deleteRawContactWithId(rawContactId)
                    }
            }

            val contactsResults = mutableMapOf<Long, Boolean>()
            for (contactId in contactIds) {
                contactsResults[contactId] = if (contactId.isProfileId) {
                    // Intentionally fail the operation to ensure that this is only used for
                    // non-profile deletes. Otherwise, operation can succeed. This is only done to
                    // enforce API design.
                    false
                } else {
                    contentResolver.deleteContactWithId(contactId)
                }
            }

            DeleteResult(rawContactsResult, contactsResults)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }

    override fun commitInOneTransaction(): Delete.Result {
        onPreExecute()

        return if (
            (rawContactIds.isEmpty() && contactIds.isEmpty()) || !permissions.canUpdateDelete()
        ) {
            DeleteAllResult(isSuccessful = false)
        } else {
            val nonProfileRawContactIds = rawContactIds.filter { !it.isProfileId }
            val nonProfileContactIds = contactIds.filter { !it.isProfileId }

            if (rawContactIds.size != nonProfileRawContactIds.size ||
                contactIds.size != nonProfileContactIds.size
            ) {
                // There are some invalid ids or profile RawContacts, fail no-op.
                DeleteAllResult(isSuccessful = false)
            } else {
                val operations = arrayListOf<ContentProviderOperation>()

                if (nonProfileRawContactIds.isNotEmpty()) {
                    RawContactsOperation(false)
                        .deleteRawContacts(nonProfileRawContactIds)
                        .let(operations::add)
                }

                if (nonProfileContactIds.isNotEmpty()) {
                    RawContactsOperation(false)
                        .deleteRawContactsWithContactIds(nonProfileContactIds)
                        .let(operations::add)
                }

                DeleteAllResult(isSuccessful = contentResolver.applyBatch(operations).deleteSuccess)
            }
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }
}

internal fun ContentResolver.deleteRawContactWithId(rawContactId: Long): Boolean = applyBatch(
    RawContactsOperation(rawContactId.isProfileId).deleteRawContact(rawContactId)
).deleteSuccess

private fun ContentResolver.deleteContactWithId(contactId: Long): Boolean =
    applyBatch(
        RawContactsOperation(contactId.isProfileId).deleteRawContactsWithContactId(contactId)
    ).deleteSuccess

private class DeleteResult private constructor(
    private val rawContactIdsResultMap: Map<Long, Boolean>,
    private val contactIdsResultMap: Map<Long, Boolean>,
    override val isRedacted: Boolean
) : Delete.Result {

    constructor(
        rawContactIdsResultMap: Map<Long, Boolean>,
        contactIdsResultMap: Map<Long, Boolean>
    ) : this(rawContactIdsResultMap, contactIdsResultMap, false)

    override fun toString(): String =
        """
            Delete.Result {
                isSuccessful: $isSuccessful
                rawContactIdsResultMap: $rawContactIdsResultMap
                contactIdsResultMap: $contactIdsResultMap
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): Delete.Result = DeleteResult(
        rawContactIdsResultMap, contactIdsResultMap,
        isRedacted = true
    )

    override val isSuccessful: Boolean by unsafeLazy {
        // By default, all returns true when the collection is empty. So, we override that.
        rawContactIdsResultMap.run { isNotEmpty() && all { it.value } }
                && contactIdsResultMap.run { isNotEmpty() && all { it.value } }
    }

    override fun isSuccessful(rawContact: ExistingRawContactEntityWithContactId): Boolean =
        isRawContactDeleteSuccessful(rawContact.id)

    override fun isRawContactDeleteSuccessful(rawContactId: Long): Boolean =
        rawContactIdsResultMap.getOrElse(rawContactId) { false }

    override fun isSuccessful(contact: ExistingContactEntity): Boolean =
        isContactDeleteSuccessful(contact.id)

    override fun isContactDeleteSuccessful(contactId: Long): Boolean =
        contactIdsResultMap.getOrElse(contactId) { false }
}

private class DeleteAllResult private constructor(
    override val isSuccessful: Boolean,
    override val isRedacted: Boolean
) : Delete.Result {

    constructor(isSuccessful: Boolean) : this(
        isSuccessful = isSuccessful,
        isRedacted = false
    )

    override fun toString(): String =
        """
            Delete.Result {
                isSuccessful: $isSuccessful
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): Delete.Result = DeleteAllResult(
        isSuccessful = isSuccessful,
        isRedacted = true
    )

    override fun isSuccessful(rawContact: ExistingRawContactEntityWithContactId): Boolean =
        isSuccessful

    override fun isRawContactDeleteSuccessful(rawContactId: Long): Boolean = isSuccessful

    override fun isSuccessful(contact: ExistingContactEntity): Boolean = isSuccessful

    override fun isContactDeleteSuccessful(contactId: Long): Boolean = isSuccessful
}