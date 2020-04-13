package com.vestrel00.contacts

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.RawContact
import com.vestrel00.contacts.entities.operation.RawContactOperation
import com.vestrel00.contacts.entities.table.Table

/**
 * Deletes one or more raw contacts or contacts from the contacts table. All associated raw contacts
 * and data rows are also deleted.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All deletes will do nothing if the permission is not granted.
 *
 * ## Usage
 *
 * To delete a [Contact] and all associated [RawContact]s;
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
     * ## IMPORTANT
     *
     * Deleting all [RawContact]s of a [Contact] will result in the deletion of the associated
     * [Contact]! However, the [Contact] will remain as long as it has at least has one associated
     * [RawContact].
     */
    fun rawContacts(vararg rawContacts: RawContact): Delete

    /**
     * See [Delete.rawContacts].
     */
    fun rawContacts(rawContacts: Collection<RawContact>): Delete

    /**
     * See [Delete.rawContacts].
     */
    fun rawContacts(rawContacts: Sequence<RawContact>): Delete

    /**
     * Adds the given [contacts] to the delete queue, which will be deleted on [commit].
     *
     * ## IMPORTANT
     *
     * Deleting a [Contact] will result in the deletion of all associated [RawContact]s even those
     * that are not in the [Contact.rawContacts] set!
     */
    fun contacts(vararg contacts: Contact): Delete

    /**
     * See [Delete.contacts].
     */
    fun contacts(contacts: Collection<Contact>): Delete

    /**
     * See [Delete.contacts].
     */
    fun contacts(contacts: Sequence<Contact>): Delete

    /**
     * Deletes the [Contact]s and [RawContact]s in the queue (added via [contacts] and
     * [rawContacts]) and returns the [Result].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

    interface Result {

        /**
         * True if all Contacts and RawContacts have successfully been deleted. False if even one
         * delete failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [rawContact] has been successfully deleted. False otherwise.
         */
        fun isSuccessful(rawContact: RawContact): Boolean

        /**
         * True the [Contact] (and all of its associated [RawContact]s has been successfully
         * deleted). False otherwise.
         *
         * ## Important
         *
         * This will return false even if all associated [RawContact]s have been deleted. This
         * should only be used in conjunction with [Delete.contacts] to avoid incorrect results.
         */
        fun isSuccessful(contact: Contact): Boolean
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
    private val rawContacts: MutableSet<RawContact> = mutableSetOf(),
    private val contacts: MutableSet<Contact> = mutableSetOf()
) : Delete {

    override fun rawContacts(vararg rawContacts: RawContact): Delete =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Collection<RawContact>): Delete =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Sequence<RawContact>): Delete = apply {
        this.rawContacts.addAll(rawContacts.filter { it.hasValidId() })
    }

    override fun contacts(vararg contacts: Contact): Delete = contacts(contacts.asSequence())

    override fun contacts(contacts: Collection<Contact>): Delete = contacts(contacts.asSequence())

    override fun contacts(contacts: Sequence<Contact>): Delete = apply {
        this.contacts.addAll(contacts.filter { it.hasValidId() })
    }

    override fun commit(): Delete.Result {
        if ((contacts.isEmpty() && rawContacts.isEmpty()) || !permissions.canInsertUpdateDelete()) {
            return DeleteFailed
        }

        val rawContactsResult = mutableMapOf<Long, Boolean>()
        for (rawContact in rawContacts) {
            rawContactsResult[rawContact.id] =
                deleteRawContactWithId(rawContact.id, contentResolver)
        }

        val contactsResults = mutableMapOf<Long, Boolean>()
        for (contact in contacts) {
            contactsResults[contact.id] = deleteContactWithId(contact.id, contentResolver)
        }

        return DeleteResult(rawContactsResult, contactsResults)
    }
}

internal fun deleteRawContactWithId(rawContactId: Long, contentResolver: ContentResolver): Boolean {
    val operation = RawContactOperation(Table.RAW_CONTACTS.uri).deleteRawContact(rawContactId)

    /*
     * Perform this single operation in a batch to be consistent with the other CRUD functions.
     */
    try {
        contentResolver.applyBatch(ContactsContract.AUTHORITY, arrayListOf(operation))
    } catch (exception: Exception) {
        return false
    }

    return true
}

internal fun deleteContactWithId(contactId: Long, contentResolver: ContentResolver): Boolean {
    val operation = RawContactOperation(Table.RAW_CONTACTS.uri).deleteContact(contactId)

    /*
     * Perform this single operation in a batch to be consistent with the other CRUD functions.
     */
    try {
        contentResolver.applyBatch(ContactsContract.AUTHORITY, arrayListOf(operation))
    } catch (exception: Exception) {
        return false
    }

    return true
}

private class DeleteResult(
    private val rawContactIdsResultMap: Map<Long, Boolean>,
    private val contactIdsResultMap: Map<Long, Boolean>
) : Delete.Result {

    override val isSuccessful: Boolean by lazy {
        rawContactIdsResultMap.all { it.value } && contactIdsResultMap.all { it.value }
    }

    override fun isSuccessful(rawContact: RawContact): Boolean =
        rawContactIdsResultMap.getOrElse(rawContact.id) { false }

    override fun isSuccessful(contact: Contact): Boolean =
        contactIdsResultMap.getOrElse(contact.id) { false }
}

private object DeleteFailed : Delete.Result {

    override val isSuccessful: Boolean = false

    override fun isSuccessful(rawContact: RawContact): Boolean = false

    override fun isSuccessful(contact: Contact): Boolean = false
}