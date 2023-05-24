package contacts.core.profile

import android.accounts.Account
import android.content.ContentResolver
import android.provider.ContactsContract
import contacts.core.*
import contacts.core.entities.NewRawContact
import contacts.core.entities.cursor.rawContactsCursor
import contacts.core.entities.table.ProfileUris
import contacts.core.util.*

/**
 * Inserts one (Profile) raw contact into the RawContacts table and all associated Data to the Data
 * table. The RawContact and Data table rows inserted here are stored in a special part of the
 * respective tables and are not visible via regular queries. Use [ProfileQuery] for retrieval.
 *
 * If the (Profile) Contact does not yet exist, one will be created. Otherwise, the raw contact will
 * be automatically associated with / belong to the (Profile) Contact upon creation. Note that there
 * is zero or one (Profile) Contact, which may have one or more RawContacts.
 *
 * As per documentation in [android.provider.ContactsContract.Profile],
 *
 * > The user's profile entry cannot be created explicitly (attempting to do so will throw an
 * > exception). When a raw contact is inserted into the profile, the provider will check for the
 * > existence of a profile on the device. If one is found, the raw contact's RawContacts.CONTACT_ID
 * > column gets the _ID of the profile Contact. If no match is found, the profile Contact is
 * > created and its _ID is put into the RawContacts.CONTACT_ID column of the newly inserted raw
 * > contact.
 *
 * ## Blank data are ignored
 *
 * Blank data will be ignored. For example, if all properties of an email are all null, empty, or
 * blank, then the email is not inserted. This is the same behavior as the AOSP Contacts app. This
 * behavior cannot be modified.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] and
 * [contacts.core.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION] are assumed to have been
 * granted already in these examples for brevity. All inserts will do nothing if these permissions
 * are not granted.
 *
 * For API 22 and below, the permission "android.permission.WRITE_PROFILE" is also required but only
 * at the manifest level. Prior to API 23 (Marshmallow), permissions needed to be granted prior
 * to installation instead of at runtime.
 *
 * ## Usage
 *
 * To insert a (Profile) raw contact with the name "john doe" with email "john@doe.com", not
 * allowing multiple raw contacts per account;
 *
 * In Kotlin,
 *
 * ```kotlin
 * val result = profileInsert
 *      .rawContact {
 *          name = NewName(
 *              givenName = "john"
 *              familyName = "doe"
 *          )
 *          emails.add(NewEmail(
 *              type = EmailEntity.Type.HOME
 *              address = "john@doe.com"
 *          ))
 *      }
 *      .commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * NewName name = new NewName();
 * name.setGivenName("john");
 * name.setFamilyName("doe");
 *
 * NewEmail email = new NewEmail();
 * email.setType(EmailEntity.Type.HOME);
 * email.setAddress("john@doe.com");
 *
 * List<NewEmail> emails = new ArrayList<>();
 * emails.add(email);
 *
 * NewRawContact rawContact = new NewRawContact();
 * rawContact.setName(name);
 * rawContact.setEmails(emails);
 *
 * ProfileInsert.Result result = profileInsert
 *      .rawContact(rawContact)
 *      .commit();
 * ```
 */
interface ProfileInsert : CrudApi {

    /**
     * If [allowBlanks] is set to true, then blank RawContacts ([NewRawContact.isBlank]) will
     * will be inserted. Otherwise, blanks will not be inserted and will result in a failed
     * operation. This flag is set to false by default.
     *
     * The Contacts Providers allows for RawContacts that have no rows in the Data table (let's call
     * them "blanks") to exist. The AOSP Contacts app does not allow insertion of new RawContacts
     * without at least one data row. It also deletes blanks on update. Despite seemingly not
     * allowing blanks, the AOSP Contacts app shows them.
     *
     * Note that blank data are ignored. For example, if all properties of an email are all null,
     * empty, or blank, then the email is not inserted. This is the same behavior as the AOSP
     * Contacts app. This is the same behavior as the AOSP Contacts app. This behavior cannot be
     * modified.
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
     * Specifies that only the given set of [fields] (data) will be inserted.
     *
     * If no fields are specified, then all fields will be inserted. Otherwise, only the specified
     * fields will be inserted.
     */
    fun include(vararg fields: AbstractDataField): ProfileInsert

    /**
     * See [ProfileInsert.include].
     */
    fun include(fields: Collection<AbstractDataField>): ProfileInsert

    /**
     * See [ProfileInsert.include].
     */
    fun include(fields: Sequence<AbstractDataField>): ProfileInsert

    /**
     * See [ProfileInsert.include].
     */
    fun include(fields: Fields.() -> Sequence<AbstractDataField>): ProfileInsert

    /**
     * Similar to [include] except this is really only used to specify
     * [contacts.core.entities.RawContact.options] fields. All other RawContact table fields
     * and the corresponding properties are immutable (exception for options).
     *
     * If no fields are specified, then all RawContacts fields ([RawContactsFields.all]) are
     * included. Otherwise, only the specified fields will be included in addition to required API
     * fields [RawContactsFields.Required].
     */
    fun includeRawContactsFields(vararg fields: RawContactsField): ProfileInsert

    /**
     * See [ProfileInsert.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: Collection<RawContactsField>): ProfileInsert

    /**
     * See [ProfileInsert.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: Sequence<RawContactsField>): ProfileInsert

    /**
     * See [ProfileInsert.includeRawContactsFields].
     */
    fun includeRawContactsFields(fields: RawContactsFields.() -> Collection<RawContactsField>): ProfileInsert

    /**
     * Configures a new [NewRawContact] for insertion, which will be inserted on [commit]. The
     * new instance is configured by the [configureRawContact] function.
     *
     * **Replaces any previously set RawContact in the insert queue.**
     */
    fun rawContact(configureRawContact: NewRawContact.() -> Unit): ProfileInsert

    /**
     * Sets the given [rawContact] for insertion, which will be inserted on [commit].
     *
     * **Replaces any previously set RawContact in the insert queue.**
     */
    fun rawContact(rawContact: NewRawContact): ProfileInsert

    /**
     * Inserts the [NewRawContact]s in the queue (added via [rawContact]) and returns the
     * [Result].
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.WRITE_PERMISSION] and
     * [contacts.core.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
     *
     * For API 22 and below, the permission "android.permission.WRITE_PROFILE" is also required but
     * only at the manifest level. Prior to API 23 (Marshmallow), permissions needed to be granted
     * prior to installation instead of at runtime.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

    /**
     * Inserts the [NewRawContact]s in the queue (added via [rawContact]) and returns the
     * [Result].
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.WRITE_PERMISSION] and
     * [contacts.core.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
     *
     * For API 22 and below, the permission "android.permission.WRITE_PROFILE" is also required but
     * only at the manifest level. Prior to API 23 (Marshmallow), permissions needed to be granted
     * prior to installation instead of at runtime.
     *
     * ## Cancellation
     *
     * To cancel at any time, the [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * **Cancelling does not undo insertions. This means that depending on when the cancellation
     * the RawContact in the insert queue may have already been inserted.**
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    // @JvmOverloads cannot be used in interface methods...
    // fun commit(cancel: () -> Boolean = { false }): Result
    fun commit(cancel: () -> Boolean): Result

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
    override fun redactedCopy(): ProfileInsert

    interface Result : CrudApi.Result {

        /**
         * The ID of the successfully created RawContact. Null if the insertion failed.
         */
        val rawContactId: Long?

        /**
         * True if the NewRawContact has successfully been inserted. False if insertion failed.
         */
        val isSuccessful: Boolean

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun ProfileInsert(contacts: Contacts): ProfileInsert = ProfileInsertImpl(contacts)

private class ProfileInsertImpl(
    override val contactsApi: Contacts,

    private var allowBlanks: Boolean = false,
    private var allowMultipleRawContactsPerAccount: Boolean = false,
    private var include: Include<AbstractDataField> = contactsApi.includeAllFields(),
    private var includeRawContactsFields: Include<RawContactsField> = DEFAULT_INCLUDE_RAW_CONTACTS_FIELDS,
    private var rawContact: NewRawContact? = null,

    override val isRedacted: Boolean = false
) : ProfileInsert {

    override fun toString(): String =
        """
            ProfileInsert {
                allowBlanks: $allowBlanks
                allowMultipleRawContactsPerAccount: $allowMultipleRawContactsPerAccount
                include: $include
                includeRawContactsFields: $includeRawContactsFields
                rawContact: $rawContact
                hasPermission: ${permissions.canInsert()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): ProfileInsert = ProfileInsertImpl(
        contactsApi,

        allowBlanks,
        allowMultipleRawContactsPerAccount,
        include,
        includeRawContactsFields,
        // Redact contact data.
        rawContact?.redactedCopy(),

        isRedacted = true
    )

    override fun allowBlanks(allowBlanks: Boolean): ProfileInsert = apply {
        this.allowBlanks = allowBlanks
    }

    override fun allowMultipleRawContactsPerAccount(
        allowMultipleRawContactsPerAccount: Boolean
    ): ProfileInsert = apply {
        this.allowMultipleRawContactsPerAccount = allowMultipleRawContactsPerAccount
    }

    override fun include(vararg fields: AbstractDataField) = include(fields.asSequence())

    override fun include(fields: Collection<AbstractDataField>) = include(fields.asSequence())

    override fun include(fields: Sequence<AbstractDataField>): ProfileInsert = apply {
        include = if (fields.isEmpty()) {
            contactsApi.includeAllFields()
        } else {
            Include(fields + Fields.Required.all.asSequence())
        }
    }

    override fun include(fields: Fields.() -> Sequence<AbstractDataField>) =
        include((fields(Fields)))

    override fun includeRawContactsFields(vararg fields: RawContactsField) =
        includeRawContactsFields(fields.asSequence())

    override fun includeRawContactsFields(fields: Collection<RawContactsField>) =
        includeRawContactsFields(fields.asSequence())

    override fun includeRawContactsFields(fields: Sequence<RawContactsField>): ProfileInsert =
        apply {
            includeRawContactsFields = if (fields.isEmpty()) {
                DEFAULT_INCLUDE_RAW_CONTACTS_FIELDS
            } else {
                Include(fields + REQUIRED_INCLUDE_RAW_CONTACTS_FIELDS)
            }
        }

    override fun includeRawContactsFields(
        fields: RawContactsFields.() -> Collection<RawContactsField>
    ) = includeRawContactsFields(fields(RawContactsFields))

    override fun rawContact(configureRawContact: NewRawContact.() -> Unit): ProfileInsert =
        rawContact(NewRawContact().apply(configureRawContact))

    override fun rawContact(rawContact: NewRawContact): ProfileInsert = apply {
        this.rawContact = rawContact.redactedCopyOrThis(isRedacted)
    }

    override fun commit(): ProfileInsert.Result = commit { false }

    override fun commit(cancel: () -> Boolean): ProfileInsert.Result {
        onPreExecute()

        val rawContact = rawContact
        return if (rawContact == null
            || (!allowBlanks && rawContact.isBlank)
            || !permissions.canInsert()
            || cancel()
        ) {
            ProfileInsertFailed()
        } else {
            // This ensures that a valid account is used. Otherwise, null is used.
            val account = rawContact.account.nullIfNotInSystem(contactsApi.applicationContext)

            if (
                (!allowMultipleRawContactsPerAccount
                        && contentResolver.profileRawContactExistFor(account))
                || cancel()
            ) {
                ProfileInsertFailed()
            } else {
                // No need to propagate the cancel function to within insertRawContact as that
                // operation should be fast and CPU time should be trivial.
                val rawContactId = contactsApi.insertRawContact(
                    include.fields, includeRawContactsFields.fields, rawContact, IS_PROFILE
                )

                return ProfileInsertResult(rawContactId)
            }
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }

    private companion object {
        const val IS_PROFILE = true

        val DEFAULT_INCLUDE_RAW_CONTACTS_FIELDS by lazy { Include(RawContactsFields.all) }
        val REQUIRED_INCLUDE_RAW_CONTACTS_FIELDS by lazy {
            RawContactsFields.Required.all.asSequence()
        }
    }
}

private fun ContentResolver.profileRawContactExistFor(account: Account?): Boolean = query(
    ProfileUris.RAW_CONTACTS.uri,
    Include(RawContactsFields.Id),
    // There may be RawContacts that are marked for deletion that have not yet been deleted.
    (RawContactsFields.Deleted notEqualTo true) and account.toRawContactsWhere()
) {
    it.getNextOrNull { it.rawContactsCursor().rawContactId } != null
} ?: false

private class ProfileInsertResult private constructor(
    override val rawContactId: Long?,
    override val isRedacted: Boolean
) : ProfileInsert.Result {

    constructor(rawContactId: Long?) : this(rawContactId, false)

    override fun toString(): String =
        """
            ProfileInsert.Result {
                isSuccessful: $isSuccessful
                rawContactId: $rawContactId
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): ProfileInsert.Result = ProfileInsertResult(
        rawContactId,
        isRedacted = true
    )

    override val isSuccessful: Boolean = rawContactId?.let(ContactsContract::isProfileId) == true
}

private class ProfileInsertFailed private constructor(
    override val isRedacted: Boolean
) : ProfileInsert.Result {

    constructor() : this(false)

    override fun toString(): String =
        """
            ProfileInsert.Result {
                isSuccessful: $isSuccessful
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): ProfileInsert.Result = ProfileInsertFailed(true)

    override val rawContactId: Long? = null

    override val isSuccessful: Boolean = false
}