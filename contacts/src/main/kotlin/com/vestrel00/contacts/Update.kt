package com.vestrel00.contacts

import android.content.ContentProviderOperation
import android.content.Context
import android.provider.ContactsContract
import com.vestrel00.contacts.entities.MutableContact
import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.operation.*

interface Update {

    fun rawContacts(vararg rawContacts: MutableRawContact): Update

    fun rawContacts(rawContacts: Collection<MutableRawContact>): Update

    fun rawContacts(rawContacts: Sequence<MutableRawContact>): Update

    fun contacts(vararg contacts: MutableContact): Update

    fun contacts(contacts: Collection<MutableContact>): Update

    fun contacts(contacts: Sequence<MutableContact>): Update

    fun commit(): Result

    interface Result {

        val isSuccessful: Boolean

        fun isSuccessful(rawContact: MutableRawContact): Boolean

        fun isSuccessful(contact: MutableContact): Boolean
    }
}

@Suppress("FunctionName")
internal fun Update(context: Context): Update = UpdateImpl(
    context,
    ContactsPermissions(context)
)

private class UpdateImpl(
    private val context: Context,
    private val permissions: ContactsPermissions,
    private val rawContacts: MutableSet<MutableRawContact> = mutableSetOf()
) : Update {

    override fun rawContacts(vararg rawContacts: MutableRawContact): Update =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Collection<MutableRawContact>): Update =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Sequence<MutableRawContact>): Update = apply {
        val existingRawContacts = rawContacts.filter { it.hasValidId() }
        this.rawContacts.addAll(existingRawContacts)
    }

    override fun contacts(vararg contacts: MutableContact): Update =
        rawContacts(contacts.asSequence().flatMap { it.rawContacts.asSequence() })

    override fun contacts(contacts: Collection<MutableContact>): Update =
        rawContacts(contacts.asSequence().flatMap { it.rawContacts.asSequence() })

    override fun contacts(contacts: Sequence<MutableContact>): Update =
        rawContacts(contacts.asSequence().flatMap { it.rawContacts.asSequence() })

    override fun commit(): Update.Result {
        if (rawContacts.isEmpty() || !permissions.canInsertUpdateDelete()) {
            return UpdateFailed
        }

        val notBlankRawContacts = rawContacts.asSequence().filter { !it.isBlank() }
        val notBlankRawContactsResults = mutableMapOf<Long, Boolean>()
        for (rawContact in notBlankRawContacts) {
            notBlankRawContactsResults[rawContact.id] = updateRawContact(rawContact)
        }

        val blankRawContacts = rawContacts.asSequence().filter { it.isBlank() }
        val blankRawContactsResults = mutableMapOf<Long, Boolean>()
        for (rawContact in blankRawContacts) {
            blankRawContactsResults[rawContact.id] =
                deleteRawContactWithId(rawContact.id, context.contentResolver)
        }

        return UpdateResult(notBlankRawContactsResults + blankRawContactsResults)
    }

    private fun updateRawContact(rawContact: MutableRawContact): Boolean {
        val operations = arrayListOf<ContentProviderOperation>()
        val contentResolver = context.contentResolver

        val addressOperations = AddressOperation().updateInsertOrDelete(
            rawContact.addresses, rawContact.id, contentResolver
        )
        operations.addAll(addressOperations)

        operations.add(
            CompanyOperation().updateInsertOrDelete(
                rawContact.company, rawContact.id, contentResolver
            )
        )

        val emailOperations = EmailOperation().updateInsertOrDelete(
            rawContact.emails, rawContact.id, contentResolver
        )
        operations.addAll(emailOperations)

        val eventOperations = EventOperation().updateInsertOrDelete(
            rawContact.events, rawContact.id, contentResolver
        )
        operations.addAll(eventOperations)

        val groupMembershipOperations = GroupMembershipOperation().updateInsertOrDelete(
            rawContact.groupMemberships, rawContact.id, context
        )
        operations.addAll(groupMembershipOperations)

        val imOperations = ImOperation().updateInsertOrDelete(
            rawContact.ims, rawContact.id, contentResolver
        )
        operations.addAll(imOperations)

        operations.add(
            NameOperation().updateInsertOrDelete(
                rawContact.name, rawContact.id, contentResolver
            )
        )

        operations.add(
            NicknameOperation().updateInsertOrDelete(
                rawContact.nickname, rawContact.id, contentResolver
            )
        )

        operations.add(
            NoteOperation().updateInsertOrDelete(
                rawContact.note, rawContact.id, contentResolver
            )
        )

        val phoneOperations = PhoneOperation().updateInsertOrDelete(
            rawContact.phones, rawContact.id, contentResolver
        )
        operations.addAll(phoneOperations)

        val relationOperations = RelationOperation().updateInsertOrDelete(
            rawContact.relations, rawContact.id, contentResolver
        )
        operations.addAll(relationOperations)

        operations.add(
            SipAddressOperation().updateInsertOrDelete(
                rawContact.sipAddress, rawContact.id, contentResolver
            )
        )

        val websiteOperations = WebsiteOperation().updateInsertOrDelete(
            rawContact.websites, rawContact.id, contentResolver
        )
        operations.addAll(websiteOperations)

        /*
         * Atomically update all of the associated Data rows. All of the above operations will
         * either succeed or fail.
         */
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
        } catch (exception: Exception) {
            return false
        }

        return true
    }
}

private class UpdateResult(private val rawContactIdsResultMap: Map<Long, Boolean>) : Update.Result {

    override val isSuccessful: Boolean by lazy { rawContactIdsResultMap.all { it.value } }

    override fun isSuccessful(rawContact: MutableRawContact): Boolean = isSuccessful(rawContact.id)

    override fun isSuccessful(contact: MutableContact): Boolean {
        for (rawContactId in contact.rawContacts.asSequence().map { it.id }) {
            if (!isSuccessful(rawContactId)) {
                return false
            }
        }

        return true
    }

    private fun isSuccessful(rawContactId: Long): Boolean =
        rawContactIdsResultMap.getOrElse(rawContactId) { false }
}

private object UpdateFailed : Update.Result {

    override val isSuccessful: Boolean = false

    override fun isSuccessful(rawContact: MutableRawContact): Boolean = false

    override fun isSuccessful(contact: MutableContact): Boolean = false
}