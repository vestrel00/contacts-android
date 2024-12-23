package contacts.core.profile

import android.accounts.Account
import android.provider.ContactsContract
import contacts.core.*
import contacts.core.entities.Group
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
 * ## Invalid or invisible Accounts
 *
 * Attempting to insert new RawContacts using an [android.accounts.Account] that your app does not
 * have access/visibility to or a completely invalid/bogus Account will result in the RawContact to
 * be associated with the local/device-only Account.
 *
 * For example, Samsung and Xiaomi accounts in Samsung and Xiaomi devices respectively are not
 * returned by [android.accounts.AccountManager.getAccounts] if the calling app is a 3rd party app
 * (does not come pre-installed with the OS).
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
     * operation.
     *
     * This flag is set to false by default.
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
     *
     * ## Performance
     *
     * When this is set to false, the API executes extra lines of code to perform the validation,
     * which may result in a slight performance hit. You can disable this internal check, perhaps
     * increasing insertion speed, by setting this to true.
     */
    fun allowBlanks(allowBlanks: Boolean): ProfileInsert

    /**
     * If [allowMultipleRawContactsPerAccount] is set to true, then inserting a profile RawContact
     * with an Account that already has a profile RawContact is allowed. Otherwise, this will result
     * in a failed operation.
     *
     * This flag is set to false by default.
     *
     * According to the `ContactsContract.Profile` documentation; "... each account (including data
     * set, if applicable) on the device may contribute a single raw contact representing the user's
     * personal profile data from that source." In other words, one account can have one profile
     * RawContact.
     *
     * Despite the documentation of "one profile RawContact per one Account", the Contacts Provider
     * allows for multiple RawContacts per Account, including multiple local RawContacts (no
     * Account).
     *
     * ## Performance
     *
     * When this is set to false, the API executes extra lines of code to perform the validation,
     * which may result in a slight performance hit. You can disable this internal check, perhaps
     * increasing insertion speed, by setting this to true.
     */
    fun allowMultipleRawContactsPerAccount(
        allowMultipleRawContactsPerAccount: Boolean
    ): ProfileInsert

    /**
     * If [validateAccounts] is set to true, then all Accounts in the system are queried to ensure
     * that each [NewRawContact.account] is in the system. For Accounts that are not in the system,
     * null is used instead. This guards against invalid accounts.
     *
     * This flag is set to true by default.
     *
     * ## Performance
     *
     * When this is set to true, the API executes extra lines of code to perform the validation,
     * which may result in a slight performance hit. You can disable this internal check, perhaps
     * increasing insertion speed, by setting this to false.
     */
    fun validateAccounts(validateAccounts: Boolean): ProfileInsert

    /**
     * If [validateGroupMemberships] is set to true, then all Groups belonging to the
     * [NewRawContact.account] are queried to ensure that each [NewRawContact.groupMemberships]
     * points to a Group in that list. Group memberships that are not pointing to a group that
     * belong to the [NewRawContact.account] are not inserted. This guards against invalid accounts.
     *
     * This flag is set to true by default.
     *
     * ## Performance
     *
     * When this is set to true, the API executes extra lines of code to perform the validation,
     * which may result in a slight performance hit. You can disable this internal check, perhaps
     * increasing insertion speed, by setting this to false.
     */
    fun validateGroupMemberships(validateGroupMemberships: Boolean): ProfileInsert

    /**
     * Specifies that only the given set of [fields] (data) will be inserted.
     *
     * If no fields are specified (empty list), then all fields will be inserted. Otherwise, only
     * the specified fields will be inserted.
     *
     * ## Including all fields
     *
     * If you want to include all fields, including custom data fields, then passing in an empty
     * list or not invoking this function is the most performant way to do it because internal
     * checks will be disabled (less lines of code executed).
     *
     * ## Developer notes
     *
     * Passing in an empty list here should set the reference to the internal field set to null to
     * indicate that include field checks should be disabled. Implementations of
     * [contacts.core.entities.operation.AbstractDataOperation] and other similar operations classes
     * treat empty list vs null field sets differently. If the included field set is...
     *
     * - null, then the included field checks are disabled. This means that any non-blank data will
     *   be processed. This is a more optimal, recommended way of including all fields.
     * - not null but empty, then data will be skipped (no-op).
     *
     * Note that internal operations class instances may receive an empty list of fields instead of
     * null when the **intersection** of the corresponding set of all fields and the
     * non-null&non-empty set of included fields... is empty.
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
     * Similar to [include] except this is used to specify fields that are specific to the
     * RawContacts table.
     *
     * If no fields are specified (empty list), then all RawContacts fields are included. Otherwise,
     * only the specified fields will be included.
     *
     * ## Including all fields
     *
     * If you want to include all RawContacts fields, then passing in an empty list or not invoking
     * this function is the most performant way to do it because internal checks will be disabled
     * (less lines of code executed).
     *
     * ## Developer notes
     *
     * Passing in an empty list here should set the reference to the internal RawContacts field set
     * to null to indicate that include RawContacts field checks should be disabled. Operations
     * such as [contacts.core.entities.operation.RawContactsOperation] and
     * [contacts.core.entities.operation.OptionsOperation] treat empty list vs null field sets
     * differently. If the included field set is...
     *
     * - null, then the included field checks are disabled. This means that any non-blank data will
     *   be processed. This is a more optimal, recommended way of including all fields.
     * - not null but empty, then data will be skipped (no-op).
     *
     * Note that internal operations class instances may receive an empty list of fields instead of
     * null when the **intersection** of the corresponding set of all fields and the
     * non-null&non-empty set of included fields... is empty.
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

internal fun ProfileInsert(contacts: Contacts): ProfileInsert = ProfileInsertImpl(contacts)

private class ProfileInsertImpl(
    override val contactsApi: Contacts,

    private var allowBlanks: Boolean = false,
    private var allowMultipleRawContactsPerAccount: Boolean = false,
    private var validateAccounts: Boolean = true,
    private var validateGroupMemberships: Boolean = true,
    private var include: Include<AbstractDataField>? = null,
    private var includeRawContactsFields: Include<RawContactsField>? = null,
    private var rawContact: NewRawContact? = null,

    override val isRedacted: Boolean = false
) : ProfileInsert {

    override fun toString(): String =
        """
            ProfileInsert {
                allowBlanks: $allowBlanks
                allowMultipleRawContactsPerAccount: $allowMultipleRawContactsPerAccount
                validateAccounts: $validateAccounts
                validateGroupMemberships: $validateGroupMemberships
                include: $include
                includeRawContactsFields: $includeRawContactsFields
                rawContact: $rawContact
                hasPermission: ${permissions.canInsert()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): ProfileInsert = ProfileInsertImpl(
        contactsApi,

        allowBlanks = allowBlanks,
        allowMultipleRawContactsPerAccount = allowMultipleRawContactsPerAccount,
        validateAccounts = validateAccounts,
        validateGroupMemberships = validateGroupMemberships,
        include = include,
        includeRawContactsFields = includeRawContactsFields,
        // Redact contact data.
        rawContact = rawContact?.redactedCopy(),

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

    override fun validateAccounts(validateAccounts: Boolean): ProfileInsert = apply {
        this.validateAccounts = validateAccounts
    }

    override fun validateGroupMemberships(validateGroupMemberships: Boolean): ProfileInsert =
        apply {
            this.validateGroupMemberships = validateGroupMemberships
        }

    override fun include(vararg fields: AbstractDataField) = include(fields.asSequence())

    override fun include(fields: Collection<AbstractDataField>) = include(fields.asSequence())

    override fun include(fields: Sequence<AbstractDataField>): ProfileInsert = apply {
        include = if (fields.isEmpty()) {
            null // Set to null to disable include field checks, for optimization purposes.
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
                null // Set to null to disable include field checks, for optimization purposes.
            } else {
                Include(fields + RawContactsFields.Required.all.asSequence())
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
            if (
                (!allowMultipleRawContactsPerAccount
                        && contactsApi.profileRawContactExistFor(rawContact.account))
                || cancel()
            ) {
                ProfileInsertFailed()
            } else {
                // Query all accounts outside of the for-loop to minimize performance hit!
                val accountsInSystem: Collection<Account>? = if (validateAccounts) {
                    contactsApi.accounts().query().find(cancel)
                } else {
                    null
                }

                // Query groups for all of the NewRawContacts' Accounts outside of the for-loop to
                // minimize performance hit!
                val accountsGroupsMap: Map<Account?, Map<Long, Group>>? =
                    if (validateGroupMemberships) {
                        contactsApi.accountsGroupsMapFor(listOf(rawContact), cancel)
                    } else {
                        null
                    }

                // No need to propagate the cancel function to within insertRawContact as that
                // operation should be fast and CPU time should be trivial.
                val rawContactId = contactsApi.insertRawContact(
                    accountsInSystem,
                    accountsGroupsMap?.get(rawContact.account),
                    include?.fields, includeRawContactsFields?.fields,
                    rawContact,
                    IS_PROFILE
                )

                return ProfileInsertResult(rawContactId)
            }
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }

    private companion object {
        const val IS_PROFILE = true
    }
}

private fun Contacts.profileRawContactExistFor(account: Account?): Boolean = contentResolver.query(
    ProfileUris.RAW_CONTACTS.uri(callerIsSyncAdapter),
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