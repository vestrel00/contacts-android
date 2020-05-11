package com.vestrel00.contacts

import android.content.ContentProviderOperation
import android.content.Context
import com.vestrel00.contacts.entities.MutableContact
import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.operation.*
import com.vestrel00.contacts.util.applyBatch

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
 * These operations ensure that only [MutableRawContact.groupMemberships] belonging to the same
 * account as the raw contact are inserted.
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
     */
    fun deleteBlanks(deleteBlanks: Boolean): Update

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

    private var deleteBlanks: Boolean = true,
    private val rawContacts: MutableSet<MutableRawContact> = mutableSetOf()
) : Update {

    override fun deleteBlanks(deleteBlanks: Boolean): Update = apply {
        this.deleteBlanks = deleteBlanks
    }

    override fun rawContacts(vararg rawContacts: MutableRawContact): Update =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Collection<MutableRawContact>): Update =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Sequence<MutableRawContact>): Update = apply {
        this.rawContacts.addAll(rawContacts)
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

        val results = mutableMapOf<Long, Boolean>()
        for (rawContact in rawContacts) {
            if (rawContact.id != null) {
                results[rawContact.id] = if (rawContact.isBlank() && deleteBlanks) {
                    context.contentResolver.deleteRawContactWithId(rawContact.id)
                } else {
                    context.updateRawContact(rawContact)
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
private fun Context.updateRawContact(rawContact: MutableRawContact): Boolean {
    if (rawContact.id == null) {
        return false
    }

    val operations = arrayListOf<ContentProviderOperation>()

    operations.addAll(
        AddressOperation().updateInsertOrDelete(
            rawContact.addresses, rawContact.id, contentResolver
        )
    )

    operations.addAll(
        EmailOperation().updateInsertOrDelete(
            rawContact.emails, rawContact.id, contentResolver
        )
    )

    operations.addAll(
        EventOperation().updateInsertOrDelete(
            rawContact.events, rawContact.id, contentResolver
        )
    )

    operations.addAll(
        GroupMembershipOperation().updateInsertOrDelete(
            rawContact.groupMemberships, rawContact.id, this
        )
    )

    operations.addAll(
        ImOperation().updateInsertOrDelete(
            rawContact.ims, rawContact.id, contentResolver
        )
    )

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

    operations.add(
        OrganizationOperation().updateInsertOrDelete(
            rawContact.organization, rawContact.id, contentResolver
        )
    )

    operations.addAll(
        PhoneOperation().updateInsertOrDelete(
            rawContact.phones, rawContact.id, contentResolver
        )
    )

    operations.addAll(
        RelationOperation().updateInsertOrDelete(
            rawContact.relations, rawContact.id, contentResolver
        )
    )

    operations.add(
        SipAddressOperation().updateInsertOrDelete(
            rawContact.sipAddress, rawContact.id, contentResolver
        )
    )

    operations.addAll(
        WebsiteOperation().updateInsertOrDelete(
            rawContact.websites, rawContact.id, contentResolver
        )
    )

    /*
     * Atomically update all of the associated Data rows. All of the above operations will
     * either succeed or fail.
     */
    return contentResolver.applyBatch(operations) != null
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

    private fun isSuccessful(rawContactId: Long?): Boolean = rawContactId != null
            && rawContactIdsResultMap.getOrElse(rawContactId) { false }
}

private object UpdateFailed : Update.Result {

    override val isSuccessful: Boolean = false

    override fun isSuccessful(rawContact: MutableRawContact): Boolean = false

    override fun isSuccessful(contact: MutableContact): Boolean = false
}