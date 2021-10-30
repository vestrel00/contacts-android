package contacts.core

import android.content.ContentProviderOperation
import android.content.ContentResolver
import contacts.core.entities.ContactEntity
import contacts.core.entities.RawContactEntity
import contacts.core.entities.operation.RawContactsOperation
import contacts.core.util.applyBatch
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
 * To delete a [ContactEntity] and all associated [RawContactEntity]s;
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
interface Delete {

    /**
     * Adds the given [rawContacts] to the delete queue, which will be deleted on [commit].
     *
     * RawContacts that have not yet been inserted ([RawContactEntity.id] is null) will be ignored
     * and result in a failed operation.
     *
     * ## IMPORTANT
     *
     * Deleting all [RawContactEntity]s of a [ContactEntity] will result in the deletion of the
     * associated [ContactEntity]! However, the [ContactEntity] will remain as long as it has at
     * least has one associated [RawContactEntity].
     */
    fun rawContacts(vararg rawContacts: RawContactEntity): Delete

    /**
     * See [Delete.rawContacts].
     */
    fun rawContacts(rawContacts: Collection<RawContactEntity>): Delete

    /**
     * See [Delete.rawContacts].
     */
    fun rawContacts(rawContacts: Sequence<RawContactEntity>): Delete

    /**
     * Adds the given [contacts] to the delete queue, which will be deleted on [commit].
     *
     * Contacts that have not yet been inserted ([ContactEntity.id] is null) will be ignored and
     * result in a failed operation.
     *
     * ## IMPORTANT
     *
     * Deleting a [ContactEntity] will result in the deletion of all associated [RawContactEntity]s
     * even those that are not in the [ContactEntity.rawContacts] set!
     */
    fun contacts(vararg contacts: ContactEntity): Delete

    /**
     * See [Delete.contacts].
     */
    fun contacts(contacts: Collection<ContactEntity>): Delete

    /**
     * See [Delete.contacts].
     */
    fun contacts(contacts: Sequence<ContactEntity>): Delete

    /**
     * Deletes the [ContactEntity]s and [RawContactEntity]s in the queue (added via [contacts] and
     * [rawContacts]) and returns the [Result].
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
     * Deletes the [ContactEntity]s and [RawContactEntity]s in the queue (added via [contacts] and
     * [rawContacts]) in one transaction. Either ALL deletes succeed or ALL fail.
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
    fun commitInOneTransaction(): Boolean

    interface Result {

        /**
         * True if all Contacts and RawContacts have successfully been deleted. False if even one
         * delete failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [rawContact] has been successfully deleted. False otherwise.
         *
         * This does not indicate whether the parent [ContactEntity] has been deleted or not. This
         * may return false even if the parent [ContactEntity] has been deleted. This is used in
         * conjunction with [Delete.rawContacts].
         */
        fun isSuccessful(rawContact: RawContactEntity): Boolean

        /**
         * True the [ContactEntity] (and all of its associated [RawContactEntity]s has been
         * successfully deleted). False otherwise.
         *
         * This does not indicate whether the chile [RawContactEntity]s has been deleted or not.
         * This may return false even if all associated [RawContactEntity]s have been deleted. This
         * is used in conjunction with [Delete.contacts].
         */
        fun isSuccessful(contact: ContactEntity): Boolean
    }
}

@Suppress("FunctionName")
internal fun Delete(contacts: Contacts): Delete = DeleteImpl(
    contacts.applicationContext.contentResolver,
    contacts.permissions
)

private class DeleteImpl(
    private val contentResolver: ContentResolver,
    private val permissions: ContactsPermissions,
    private val rawContactIds: MutableSet<Long> = mutableSetOf(),
    private val contactIds: MutableSet<Long> = mutableSetOf()
) : Delete {

    override fun toString(): String =
        """
            Delete {
                rawContactIds: $rawContactIds
                contactIds: $contactIds
            }
        """.trimIndent()

    override fun rawContacts(vararg rawContacts: RawContactEntity) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Collection<RawContactEntity>) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Sequence<RawContactEntity>): Delete = apply {
        this.rawContactIds.addAll(rawContacts.map { it.id ?: INVALID_ID })
    }

    override fun contacts(vararg contacts: ContactEntity) = contacts(contacts.asSequence())

    override fun contacts(contacts: Collection<ContactEntity>) =
        contacts(contacts.asSequence())

    override fun contacts(contacts: Sequence<ContactEntity>): Delete = apply {
        this.contactIds.addAll(contacts.map { it.id ?: INVALID_ID })
    }

    override fun commit(): Delete.Result {
        if ((contactIds.isEmpty() && rawContactIds.isEmpty()) || !permissions.canUpdateDelete) {
            return DeleteFailed()
        }

        val rawContactsResult = mutableMapOf<Long, Boolean>()
        for (rawContactId in rawContactIds) {
            rawContactsResult[rawContactId] =
                if (rawContactId == INVALID_ID || rawContactId.isProfileId) {
                    // Intentionally fail the operation to ensure that this is only used for
                    // non-profile updates. Otherwise, operation can succeed. This is only done to
                    // enforce API design.
                    false
                } else {
                    contentResolver.deleteRawContactWithId(rawContactId)
                }
        }

        val contactsResults = mutableMapOf<Long, Boolean>()
        for (contactId in contactIds) {
            contactsResults[contactId] = if (contactId == INVALID_ID || contactId.isProfileId) {
                // Intentionally fail the operation to ensure that this is only used for
                // non-profile deletes. Otherwise, operation can succeed. This is only done to
                // enforce API design.
                false
            } else {
                contentResolver.deleteContactWithId(contactId)
            }
        }

        return DeleteResult(rawContactsResult, contactsResults)
    }

    override fun commitInOneTransaction(): Boolean {
        if ((rawContactIds.isEmpty() && contactIds.isEmpty()) || !permissions.canUpdateDelete) {
            return false
        }

        val nonProfileRawContactIds = rawContactIds.filter { it != INVALID_ID && !it.isProfileId }
        val nonProfileContactIds = contactIds.filter { it != INVALID_ID && !it.isProfileId }

        if (rawContactIds.size != nonProfileRawContactIds.size ||
            contactIds.size != nonProfileContactIds.size
        ) {
            // There are some invalid ids or profile RawContacts, fail without performing operation.
            return false
        }

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

        return contentResolver.applyBatch(operations) != null
    }

    private companion object {
        // A failed entry in the results so that Result.isSuccessful returns false.
        const val INVALID_ID = -1L
    }
}

internal fun ContentResolver.deleteRawContactWithId(rawContactId: Long): Boolean =
    applyBatch(
        RawContactsOperation(rawContactId.isProfileId).deleteRawContact(rawContactId)
    ) != null

private fun ContentResolver.deleteContactWithId(contactId: Long): Boolean =
    applyBatch(
        RawContactsOperation(contactId.isProfileId).deleteRawContactsWithContactId(contactId)
    ) != null

private class DeleteResult(
    private val rawContactIdsResultMap: Map<Long, Boolean>,
    private val contactIdsResultMap: Map<Long, Boolean>
) : Delete.Result {

    override val isSuccessful: Boolean by unsafeLazy {
        rawContactIdsResultMap.all { it.value } || contactIdsResultMap.all { it.value }
    }

    override fun isSuccessful(rawContact: RawContactEntity): Boolean =
        rawContact.id?.let { rawContactId ->
            rawContactIdsResultMap.getOrElse(rawContactId) { false }
        } ?: false

    override fun isSuccessful(contact: ContactEntity): Boolean =
        contact.id?.let { contactId ->
            contactIdsResultMap.getOrElse(contactId) { false }
        } ?: false
}

private class DeleteFailed : Delete.Result {

    override val isSuccessful: Boolean = false

    override fun isSuccessful(rawContact: RawContactEntity): Boolean = false

    override fun isSuccessful(contact: ContactEntity): Boolean = false
}