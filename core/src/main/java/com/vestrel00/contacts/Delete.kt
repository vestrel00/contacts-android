package com.vestrel00.contacts

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.Context
import com.vestrel00.contacts.entities.ContactEntity
import com.vestrel00.contacts.entities.RawContactEntity
import com.vestrel00.contacts.entities.operation.RawContactsOperation
import com.vestrel00.contacts.util.applyBatch
import com.vestrel00.contacts.util.unsafeLazy

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
internal fun Delete(context: Context): Delete = DeleteImpl(
    context.contentResolver,
    ContactsPermissions(context)
)

private class DeleteImpl(
    private val contentResolver: ContentResolver,
    private val permissions: ContactsPermissions,
    private val rawContacts: MutableSet<RawContactEntity> = mutableSetOf(),
    private val contacts: MutableSet<ContactEntity> = mutableSetOf()
) : Delete {

    override fun toString(): String =
        """
            Delete {
                rawContacts: $rawContacts
                contacts: $contacts
            }
        """.trimIndent()

    override fun rawContacts(vararg rawContacts: RawContactEntity) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Collection<RawContactEntity>) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Sequence<RawContactEntity>): Delete = apply {
        this.rawContacts.addAll(rawContacts)
    }

    override fun contacts(vararg contacts: ContactEntity) = contacts(contacts.asSequence())

    override fun contacts(contacts: Collection<ContactEntity>) =
        contacts(contacts.asSequence())

    override fun contacts(contacts: Sequence<ContactEntity>): Delete = apply {
        this.contacts.addAll(contacts)
    }

    override fun commit(): Delete.Result {
        if ((contacts.isEmpty() && rawContacts.isEmpty()) || !permissions.canUpdateDelete()) {
            return DeleteFailed
        }

        val rawContactsResult = mutableMapOf<Long, Boolean>()
        for (rawContact in rawContacts) {
            val rawContactId = rawContact.id
            if (rawContactId != null) {
                rawContactsResult[rawContactId] = if (rawContact.isProfile != IS_PROFILE) {
                    false
                } else {
                    contentResolver.deleteRawContactWithId(rawContactId, IS_PROFILE)
                }
            } else {
                rawContactsResult[INVALID_ID] = false
            }
        }

        val contactsResults = mutableMapOf<Long, Boolean>()
        for (contact in contacts) {
            val contactId = contact.id
            if (contactId != null) {
                contactsResults[contactId] = if (contact.isProfile != IS_PROFILE) {
                    false
                } else {
                    contentResolver.deleteContactWithId(contactId, IS_PROFILE)
                }
            } else {
                contactsResults[INVALID_ID] = false
            }
        }

        return DeleteResult(rawContactsResult, contactsResults)
    }

    override fun commitInOneTransaction(): Boolean {
        if ((rawContacts.isEmpty() && contacts.isEmpty()) || !permissions.canUpdateDelete()) {
            return false
        }

        val rawContactIds = rawContacts.filter { it.isProfile == IS_PROFILE }.mapNotNull { it.id }
        val contactIds = contacts.filter { it.isProfile == IS_PROFILE }.mapNotNull { it.id }

        if (rawContactIds.size != rawContacts.size || contactIds.size != contacts.size) {
            // There are some null ids or IS_PROFILE mismatch, fail without performing operation.
            return false
        }

        val operations = arrayListOf<ContentProviderOperation>()

        if (rawContacts.isNotEmpty()) {
            operations.add(RawContactsOperation(IS_PROFILE).deleteRawContacts(rawContactIds))
        }

        if (contacts.isNotEmpty()) {
            operations.add(
                RawContactsOperation(IS_PROFILE).deleteRawContactsWithContactIds(
                    contactIds
                )
            )
        }

        return contentResolver.applyBatch(operations) != null
    }

    private companion object {
        // A failed entry in the results so that Result.isSuccessful returns false.
        const val INVALID_ID = -1L
        const val IS_PROFILE = false
    }
}

internal fun ContentResolver.deleteRawContactWithId(
    rawContactId: Long, isProfile: Boolean
): Boolean = applyBatch(RawContactsOperation(isProfile).deleteRawContact(rawContactId)) != null

private fun ContentResolver.deleteContactWithId(contactId: Long, isProfile: Boolean): Boolean =
    applyBatch(RawContactsOperation(isProfile).deleteRawContactsWithContactId(contactId)) != null

private class DeleteResult(
    private val rawContactIdsResultMap: Map<Long, Boolean>,
    private val contactIdsResultMap: Map<Long, Boolean>
) : Delete.Result {

    override val isSuccessful: Boolean by unsafeLazy {
        (rawContactIdsResultMap.isNotEmpty() && rawContactIdsResultMap.all { it.value }) ||
                (contactIdsResultMap.isNotEmpty() && contactIdsResultMap.all { it.value })
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

private object DeleteFailed : Delete.Result {

    override val isSuccessful: Boolean = false

    override fun isSuccessful(rawContact: RawContactEntity): Boolean = false

    override fun isSuccessful(contact: ContactEntity): Boolean = false
}