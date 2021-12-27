package contacts.core

import android.content.ContentProviderOperation
import android.content.ContentResolver
import contacts.core.entities.ExistingContactEntity
import contacts.core.entities.ExistingRawContactEntity
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
 * To delete a [ExistingContactEntity] and all associated [ExistingRawContactEntity]s;
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
interface Delete : Redactable {

    /**
     * Adds the given [rawContacts] to the delete queue, which will be deleted on [commit].
     *
     * ## IMPORTANT
     *
     * Deleting all [ExistingRawContactEntity]s of a [ExistingContactEntity] will result in the
     * deletion of the associated [ExistingContactEntity]! However, the [ExistingContactEntity]
     * will remain as long as it has at least has one associated [ExistingRawContactEntity].
     */
    fun rawContacts(vararg rawContacts: ExistingRawContactEntity): Delete

    /**
     * See [Delete.rawContacts].
     */
    fun rawContacts(rawContacts: Collection<ExistingRawContactEntity>): Delete

    /**
     * See [Delete.rawContacts].
     */
    fun rawContacts(rawContacts: Sequence<ExistingRawContactEntity>): Delete

    /**
     * Adds the given [contacts] to the delete queue, which will be deleted on [commit].
     *
     * ## IMPORTANT
     *
     * Deleting a [ExistingContactEntity] will result in the deletion of all associated
     * [ExistingRawContactEntity]s even those that are not in the
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
     * Deletes the [ExistingContactEntity]s and [ExistingRawContactEntity]s in the queue (added via
     * [contacts] and [rawContacts]) and returns the [Result].
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
     * Deletes the [ExistingContactEntity]s and [ExistingRawContactEntity]s in the queue (added via
     * [contacts] and [rawContacts]) in one transaction. Either ALL deletes succeed or ALL fail.
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

    interface Result : Redactable {

        /**
         * True if all Contacts and RawContacts have successfully been deleted. False if even one
         * delete failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [rawContact] has been successfully deleted. False otherwise.
         *
         * This does not indicate whether the parent [ExistingContactEntity] has been deleted or
         * not. This may return false even if the parent [ExistingContactEntity] has been deleted.
         * This is used in conjunction with [Delete.rawContacts].
         */
        fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean

        /**
         * True the [ExistingContactEntity] (and all of its associated [ExistingRawContactEntity]s
         * has been successfully deleted). False otherwise.
         *
         * This does not indicate whether the chile [ExistingRawContactEntity]s has been deleted or
         * not. This may return false even if all associated [ExistingRawContactEntity]s have been
         * deleted. This is used in conjunction with [Delete.contacts].
         */
        fun isSuccessful(contact: ExistingContactEntity): Boolean

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
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
    private val contactIds: MutableSet<Long> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : Delete {

    override fun toString(): String =
        """
            Delete {
                rawContactIds: $rawContactIds
                contactIds: $contactIds
                isRedacted: $isRedacted
            }
        """.trimIndent()

    // There isn't really anything to redact =)
    override fun redactedCopy(): Delete = DeleteImpl(
        contentResolver, permissions,

        rawContactIds, contactIds,

        isRedacted = true
    )

    override fun rawContacts(vararg rawContacts: ExistingRawContactEntity) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Collection<ExistingRawContactEntity>) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Sequence<ExistingRawContactEntity>): Delete = apply {
        this.rawContactIds.addAll(rawContacts.map { it.id })
    }

    override fun contacts(vararg contacts: ExistingContactEntity) = contacts(contacts.asSequence())

    override fun contacts(contacts: Collection<ExistingContactEntity>) =
        contacts(contacts.asSequence())

    override fun contacts(contacts: Sequence<ExistingContactEntity>): Delete = apply {
        this.contactIds.addAll(contacts.map { it.id })
    }

    override fun commit(): Delete.Result {
        if ((contactIds.isEmpty() && rawContactIds.isEmpty()) || !permissions.canUpdateDelete()) {
            return DeleteFailed(isRedacted)
        }

        val rawContactsResult = mutableMapOf<Long, Boolean>()
        for (rawContactId in rawContactIds) {
            rawContactsResult[rawContactId] =
                if (rawContactId.isProfileId) {
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
            contactsResults[contactId] = if (contactId.isProfileId) {
                // Intentionally fail the operation to ensure that this is only used for
                // non-profile deletes. Otherwise, operation can succeed. This is only done to
                // enforce API design.
                false
            } else {
                contentResolver.deleteContactWithId(contactId)
            }
        }

        return DeleteResult(rawContactsResult, contactsResults, isRedacted)
    }

    override fun commitInOneTransaction(): Boolean {
        if ((rawContactIds.isEmpty() && contactIds.isEmpty()) || !permissions.canUpdateDelete()) {
            return false
        }

        val nonProfileRawContactIds = rawContactIds.filter { !it.isProfileId }
        val nonProfileContactIds = contactIds.filter { !it.isProfileId }

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
    private val contactIdsResultMap: Map<Long, Boolean>,
    override val isRedacted: Boolean
) : Delete.Result {

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
        rawContactIdsResultMap.all { it.value } || contactIdsResultMap.all { it.value }
    }

    override fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean =
        rawContactIdsResultMap.getOrElse(rawContact.id) { false }

    override fun isSuccessful(contact: ExistingContactEntity): Boolean =
        contactIdsResultMap.getOrElse(contact.id) { false }
}

private class DeleteFailed(
    override val isRedacted: Boolean
) : Delete.Result {

    override fun toString(): String =
        """
            Delete.Result {
                isSuccessful: $isSuccessful
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): Delete.Result = DeleteFailed(true)

    override val isSuccessful: Boolean = false

    override fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean = false

    override fun isSuccessful(contact: ExistingContactEntity): Boolean = false
}