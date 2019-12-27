package com.vestrel00.contacts

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.RawContact
import com.vestrel00.contacts.entities.operation.RawContactOperation

interface Delete {

    fun rawContacts(vararg rawContacts: RawContact): Delete

    fun rawContacts(rawContacts: Collection<RawContact>): Delete

    fun rawContacts(rawContacts: Sequence<RawContact>): Delete

    fun contacts(vararg contacts: Contact): Delete

    fun contacts(contacts: Collection<Contact>): Delete

    fun contacts(contacts: Sequence<Contact>): Delete

    fun commit(): Result

    interface Result {

        val isSuccessful: Boolean

        fun isSuccessful(rawContact: RawContact): Boolean

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
    val operation = RawContactOperation().deleteRawContact(rawContactId)

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
    val operation = RawContactOperation().deleteContact(contactId)

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