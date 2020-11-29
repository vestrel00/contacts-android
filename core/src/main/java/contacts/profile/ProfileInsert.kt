package contacts.profile

import android.accounts.Account
import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import contacts.*
import contacts.entities.MutableRawContact
import contacts.entities.cursor.rawContactsCursor
import contacts.entities.table.ProfileUris
import contacts.util.nullIfNotInSystem
import contacts.util.query
import contacts.util.toRawContactsWhere

/**
 * Inserts one (Profile) raw contact into the RawContacts table and all associated Data to the Data
 * table. The RawContact and Data table rows inserted here are stored in a special part of the
 * respective tables and are not visible via regular queries. Use [ProfileQuery] for retrieval.
 *
 * If the (Profile) Contact does not yet exist, one will be created. Otherwise, the raw contact will
 * be automatically associated with / belong to the (Profile) Contact upon creation. Note that there
 * is zero or one (Profile) Contact, which may have one or more RawContacts.
 *
 * The native Contacts app typically only maintains one local (no account) RawContact when
 * configuring the user's profile.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION] are assumed to have
 * been granted already in these examples for brevity. All inserts will do nothing if these
 * permissions are not granted.
 *
 * ## Accounts
 *
 * The Contacts Provider does not associate local contacts to an account when an account is or
 * becomes available (regardless of API level).
 *
 * **Account removal**
 *
 * Removing the Account will delete all of the associated rows in the RawContact and Data tables.
 *
 * ## Usage
 *
 * To insert a (Profile) raw contact with the name "john doe" with email "john@doe.com" for the
 * local account (no account), not allowing multiple raw contacts per account;
 *
 * In Kotlin,
 *
 * ```kotlin
 * val result = profileInsert
 *      .rawContact {
 *          name = MutableName().apply {
 *              givenName = "john"
 *              familyName = "doe"
 *          }
 *          emails.add(MutableEmail().apply {
 *              type = Email.Type.HOME
 *              address = "john@doe.com"
 *          })
 *      }
 *      .commit()
 * ```
 *
 * In Java,
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
 * List<MutableEmail> emails = new ArrayList<>();
 * emails.add(email);
 *
 * MutableRawContact rawContact = new MutableRawContact();
 * rawContact.setName(name);
 * rawContact.setEmails(emails);
 *
 * ProfileInsert.Result result = profileInsert
 *      .rawContact(rawContact)
 *      .commit();
 * ```
 */
interface ProfileInsert {

    /**
     * If [allowBlanks] is set to true, then blank RawContacts ([MutableRawContact.isBlank]) will
     * will be inserted. Otherwise, blanks will not be inserted and will result in a failed
     * operation. This flag is set to false by default.
     *
     * The Contacts Providers allows for RawContacts that have no rows in the Data table (let's call
     * them "blanks") to exist. The native Contacts app does not allow insertion of new RawContacts
     * without at least one data row. It also deletes blanks on update. Despite seemingly not
     * allowing blanks, the native Contacts app shows them.
     */
    fun allowBlanks(allowBlanks: Boolean): ProfileInsert

    /**
     * If [allowMultipleRawContactsPerAccount] is set to true, then inserting a profile RawContact
     * with an Account that already has a profile RawContact is allowed. Otherwise, this will result
     * in a failed operation. This flag is set to false by default.
     *
     * According to the `ContactsContract.Profile` documentation; "... each account (including data
     * set, if applicable) on the device may contribute a single raw contact representing the user's
     * personal profile data from that source." In other words, one account can have one profile
     * RawContact.
     *
     * Despite the documentation of "one profile RawContact per one Account", the Contacts Provider
     * allows for multiple RawContacts per Account, including multiple local RawContacts (no
     * Account).
     */
    fun allowMultipleRawContactsPerAccount(
        allowMultipleRawContactsPerAccount: Boolean
    ): ProfileInsert

    /**
     * The RawContact that is inserted on [commit] will belong to the given [account].
     *
     * If not provided, or null is provided, or if an incorrect account is provided, the raw
     * contacts inserted here will not be associated with an account. RawContacts inserted without
     * an associated account are considered local or device-only contacts, which are not synced.
     *
     * Creating / setting up the profile in the native Contacts app results in the creation of a
     * local RawContact (not associated with an Account) even if there are available Accounts.
     *
     * If you want to mimic the native Contacts app behavior, do not call this method or do call
     * it with null.
     */
    fun forAccount(account: Account?): ProfileInsert

    /**
     * Configures a new [MutableRawContact] for insertion, which will be inserted on [commit]. The
     * new instance is configured by the [configureRawContact] function.
     *
     * Replaces any previously set RawContact in the insert queue.
     */
    fun rawContact(configureRawContact: MutableRawContact.() -> Unit): ProfileInsert

    /**
     * Sets the given [rawContact] for insertion, which will be inserted on [commit].
     *
     * Replaces any previously set RawContact in the insert queue.
     */
    fun rawContact(rawContact: MutableRawContact): ProfileInsert

    /**
     * Inserts the [MutableRawContact]s in the queue (added via [rawContact]) and returns the
     * [Result].
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.WRITE_PERMISSION] and
     * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

    interface Result {

        /**
         * The ID of the successfully created RawContact. Null if the insertion failed.
         */
        val rawContactId: Long?

        /**
         * True if the MutableRawContact has successfully been inserted. False if insertion failed.
         */
        val isSuccessful: Boolean
    }
}

@Suppress("FunctionName")
internal fun ProfileInsert(context: Context): ProfileInsert = ProfileInsertImpl(
    context.applicationContext,
    ContactsPermissions(context)
)

private class ProfileInsertImpl(
    private val applicationContext: Context,
    private val permissions: ContactsPermissions,

    private var allowBlanks: Boolean = false,
    private var allowMultipleRawContactsPerAccount: Boolean = false,
    private var account: Account? = null,
    private var rawContact: MutableRawContact? = null
) : ProfileInsert {

    override fun toString(): String =
        """
            ProfileInsert {
                allowBlanks: $allowBlanks
                allowMultipleRawContactsPerAccount: $allowMultipleRawContactsPerAccount
                account: $account
                rawContact: $rawContact
            }
        """.trimIndent()

    override fun allowBlanks(allowBlanks: Boolean): ProfileInsert = apply {
        this.allowBlanks = allowBlanks
    }

    override fun allowMultipleRawContactsPerAccount(
        allowMultipleRawContactsPerAccount: Boolean
    ): ProfileInsert = apply {
        this.allowMultipleRawContactsPerAccount = allowMultipleRawContactsPerAccount
    }

    override fun forAccount(account: Account?): ProfileInsert = apply {
        this.account = account
    }

    override fun rawContact(configureRawContact: MutableRawContact.() -> Unit): ProfileInsert =
        rawContact(MutableRawContact().apply(configureRawContact))

    override fun rawContact(rawContact: MutableRawContact): ProfileInsert = apply {
        this.rawContact = rawContact
    }

    override fun commit(): ProfileInsert.Result {
        val rawContact = rawContact

        if (rawContact == null
            || (!allowBlanks && rawContact.isBlank)
            || !permissions.canInsert()
        ) {
            return ProfileInsertFailed
        }

        // This ensures that a valid account is used. Otherwise, null is used.
        account = account?.nullIfNotInSystem(applicationContext)

        if (
            !allowMultipleRawContactsPerAccount
            && applicationContext.contentResolver.hasProfileRawContactForAccount(account)
        ) {
            return ProfileInsertFailed
        }

        val rawContactId =
            applicationContext.insertRawContactForAccount(account, rawContact, IS_PROFILE)

        return ProfileInsertResult(rawContactId)
    }

    private companion object {
        const val IS_PROFILE = true
    }
}

private class ProfileInsertResult(override val rawContactId: Long?) : ProfileInsert.Result {

    override val isSuccessful: Boolean = rawContactId?.let(ContactsContract::isProfileId) == true
}

private object ProfileInsertFailed : ProfileInsert.Result {

    override val rawContactId: Long? = null

    override val isSuccessful: Boolean = false
}

private fun ContentResolver.hasProfileRawContactForAccount(account: Account?): Boolean = query(
    ProfileUris.RAW_CONTACTS.uri,
    Include(RawContactsFields.Id),
    // There may be lingering RawContacts whose associated contact was already deleted.
    // Such RawContacts have contact id column value as null.
    RawContactsFields.ContactId.isNotNull() and account.toRawContactsWhere()
) {
    it.getNextOrNull { it.rawContactsCursor().rawContactId } != null
} ?: false