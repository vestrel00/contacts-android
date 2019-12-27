package com.vestrel00.contacts

import android.accounts.Account
import android.content.ContentProviderOperation
import android.content.Context
import android.provider.ContactsContract
import com.vestrel00.contacts.accounts.Accounts
import com.vestrel00.contacts.entities.MutableRawContact
import com.vestrel00.contacts.entities.operation.*

interface Insert {

    fun forAccount(account: Account?): Insert

    fun rawContacts(vararg rawContacts: MutableRawContact): Insert

    fun rawContacts(rawContacts: Collection<MutableRawContact>): Insert

    fun rawContacts(rawContacts: Sequence<MutableRawContact>): Insert

    fun commit(): Result

    interface Result {

        val rawContactIds: List<Long>

        val isSuccessful: Boolean

        fun isSuccessful(rawContact: MutableRawContact): Boolean

        fun rawContactId(rawContact: MutableRawContact): Long?
    }
}

@Suppress("FunctionName")
internal fun Insert(context: Context): Insert = InsertImpl(
    context,
    Accounts(),
    ContactsPermissions(context)
)

private class InsertImpl(
    private val context: Context,
    private val accounts: Accounts,
    private val permissions: ContactsPermissions,
    private val rawContacts: MutableSet<MutableRawContact> = mutableSetOf(),
    private var account: Account? = null
) : Insert {

    override fun forAccount(account: Account?): Insert = apply {
        this.account = account
    }

    override fun rawContacts(vararg rawContacts: MutableRawContact): Insert =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Collection<MutableRawContact>): Insert =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Sequence<MutableRawContact>): Insert = apply {
        // Do not insert blank contacts.
        this.rawContacts.addAll(rawContacts.filter { !it.isBlank() })
    }

    override fun commit(): Insert.Result {
        if (rawContacts.isEmpty() || !permissions.canInsertUpdateDelete()) {
            return InsertFailed
        }

        setValidAccount()

        val results = mutableMapOf<MutableRawContact, Long?>()
        for (rawContact in rawContacts) {
            results[rawContact] = insertRawContactForAccount(account, rawContact)
        }
        return InsertResult(results)
    }

    private fun setValidAccount() {
        val accounts = accounts.allAccounts(context)

        val account = this.account
        if (account == null || !accounts.contains(account)) {
            this.account = accounts.firstOrNull()
        }
    }

    private fun insertRawContactForAccount(
        account: Account?,
        rawContact: MutableRawContact
    ): Long? {
        val operations = arrayListOf<ContentProviderOperation>()

        /*
         * Like with the native Android Contacts app, a new RawContact row is created for each new
         * raw contact.
         *
         * This needs to be the first operation in the batch as it will be used by all subsequent
         * Data table insert operations.
         */
        operations.add(RawContactOperation().insert(account))

        operations.addAll(AddressOperation().insert(rawContact.addresses))

        rawContact.company?.let {
            CompanyOperation().insert(it)?.let(operations::add)
        }

        operations.addAll(EmailOperation().insert(rawContact.emails))

        operations.addAll(EventOperation().insert(rawContact.events))

        // The account can only be null if there are no available accounts. In this case, it should
        // not be possible for consumers to obtain group memberships unless they have saved them
        // as parcelables and then restored them after the accounts have been removed.
        // Still we should have this null check just in case.
        if (account != null) {
            operations.addAll(
                GroupMembershipOperation().insert(rawContact.groupMemberships, account, context)
            )
        }

        operations.addAll(ImOperation().insert(rawContact.ims))

        rawContact.name?.let {
            NameOperation().insert(it)?.let(operations::add)
        }

        rawContact.nickname?.let {
            NicknameOperation().insert(it)?.let(operations::add)
        }

        rawContact.note?.let {
            NoteOperation().insert(it)?.let(operations::add)
        }

        operations.addAll(PhoneOperation().insert(rawContact.phones))

        operations.addAll(RelationOperation().insert(rawContact.relations))

        rawContact.sipAddress?.let {
            SipAddressOperation().insert(it)?.let(operations::add)
        }

        operations.addAll(WebsiteOperation().insert(rawContact.websites))

        /*
         * Atomically create the RawContact row and all of the associated Data rows. All of the
         * above operations will either succeed or fail.
         */
        val results = try {
            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
        } catch (exception: Exception) {
            null
        }

        /*
         * The ContentProviderResult[0] contains the first result of the batch, which is the
         * RawContactOperation. The uri contains the RawContact._ID as the last path segment.
         *
         * E.G. "content://com.android.contacts/raw_contacts/18"
         * In this case, 18 is the RawContacts._ID.
         *
         * It is formed by the Contacts Provider using
         * Uri.withAppendedPath(ContactsContract.RawContacts.CONTENT_URI, "18")
         */
        return results?.firstOrNull()?.let { result ->
            val rawContactUri = result.uri
            val rawContactId = rawContactUri.lastPathSegment?.toLongOrNull()
            rawContactId
        }
    }
}

private class InsertResult(private val rawContactMap: Map<MutableRawContact, Long?>) :
    Insert.Result {

    override val rawContactIds: List<Long> by lazy {
        rawContactMap.asSequence()
            .filter { it.value != null }
            .map { it.value!! }
            .toList()
    }

    override val isSuccessful: Boolean by lazy { rawContactMap.all { it.value != null } }

    override fun isSuccessful(rawContact: MutableRawContact): Boolean =
        rawContactId(rawContact) != null

    override fun rawContactId(rawContact: MutableRawContact): Long? =
        rawContactMap.getOrElse(rawContact) { null }
}

private object InsertFailed : Insert.Result {

    override val rawContactIds: List<Long> = emptyList()

    override val isSuccessful: Boolean = false

    override fun isSuccessful(rawContact: MutableRawContact): Boolean = false

    override fun rawContactId(rawContact: MutableRawContact): Long? = null
}