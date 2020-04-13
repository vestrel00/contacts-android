package com.vestrel00.contacts

import android.content.ContentProviderOperation
import android.content.Context
import android.provider.ContactsContract
import com.vestrel00.contacts.entities.MutableContact
import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.operation.*

/**
 * Updates one or more raw contacts' rows in the data table.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION] are assumed to have
 * been granted already in these  examples for brevity. All updates will do nothing if these
 * permissions are not granted.
 *
 * ## Accounts
 *
 * The get accounts permission is required here in order to ensure that only
 * [MutableRawContact.groupMemberships] belonging to the same account as the raw contact are
 * inserted.
 *
 * ## Contacts & RawContacts may be deleted automatically in some cases!
 *
 * When a RawContact does not have an associated Account, it is automatically deleted when all of
 * its data (e.g. emails, phones, name, ...) are deleted (set to null or removed from the list).
 * On the contrary, when a RawContact has an associated Account, it does not get deleted when all of
 * its data rows are deleted.
 *
 * Contacts with no associated RawContacts are automatically deleted.
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
     * Adds the given [rawContacts] to the update queue, which will be updated on [commit].
     *
     * Only existing [rawContacts] that have been retrieved via a query will be added to the
     * update queue. Those that have been manually created via a constructor will be ignored.
     *
     * Null (or empty collection) raw contact attributes will be deleted. Non-null (and non-empty
     * collection) raw contact attributes will be inserted.
     *
     * Raw contacts with only null and empty attributes, blanks ([MutableRawContact.isBlank]), will
     * be deleted instead! This mimics the native Contacts app behavior deleting an existing contact
     * when saved without a single piece of non-null data.
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
     * Only existing raw contacts that have been retrieved via a query will be added to the
     * update queue. Those that have been manually created via a constructor will be ignored.
     *
     * Null (or empty collection) raw contact attributes will be deleted. Non-null (and non-empty
     * collection) raw contact attributes will be inserted.
     *
     * Raw contacts with only null and empty attributes, blanks ([MutableRawContact.isBlank]), will
     * be deleted instead! This mimics the native Contacts app behavior deleting an existing contact
     * when saved without a single piece of non-null data.
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
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

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
        contacts(contacts.asSequence())

    override fun contacts(contacts: Collection<MutableContact>): Update =
        contacts(contacts.asSequence())

    override fun contacts(contacts: Sequence<MutableContact>): Update =
        rawContacts(contacts.flatMap { it.rawContacts.asSequence() })

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

    /**
     * Updates a raw contact's data rows.
     *
     * If a raw contact attribute is null or the attribute's values are all null, then the
     * corresponding data row (if any) will be deleted.
     *
     * If only some of a raw contact's attribute's values are null, then a data row will be created
     * if it does not yet exist.
     */
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