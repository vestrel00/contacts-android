package contacts.core.profile

import contacts.core.*
import contacts.core.entities.ExistingContactEntity
import contacts.core.entities.ExistingRawContactEntity
import contacts.core.util.isEmpty
import contacts.core.util.unsafeLazy

/**
 * Updates the Profile contact in the Contacts Provider database to ensure that it contains the same
 * data as the contact and raw contacts provided in [contact] and [rawContacts].
 *
 * As per documentation in []android.provider.ContactsContract.Profile],
 *
 * > The profile Contact has the same update restrictions as Contacts in general...
 * > Only certain columns of Contact are modifiable: STARRED, CUSTOM_RINGTONE, SEND_TO_VOICEMAIL.
 * > Changing any of these columns on the Contact also changes them on all constituent raw contacts.
 *
 * ## Blank data are deleted
 *
 * Blank data will be deleted, unless the corresponding fields are not provided in [include].
 * For example, if all properties of an email are all null, empty, or blank, then the email is
 * deleted. This is the same behavior as the native Contacts app.
 *
 * Note that in cases where blank data are deleted, existing RawContact instances (in memory) will
 * still have references to the deleted data instance. The RawContact instances (in memory) must be
 * refreshed to get the most up-to-date data sets.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All updates will do nothing if these permissions are not granted.
 *
 * For API 22 and below, the permission "android.permission.WRITE_PROFILE" is also required but
 * only at the manifest level. Prior to API 23 (Marshmallow), permissions needed to be granted
 * prior to installation instead of at runtime.
 *
 * ## Usage
 *
 * To update a (profile) raw contact's name to "john doe" and add an email "john@doe.com";
 *
 * In Kotlin,
 *
 * ```kotlin
 * val mutableRawContact = rawContact.mutableCopy {
 *      name = NewName(
 *          givenName = "john"
 *          familyName = "doe"
 *      )
 *      emails.add(NewEmail(
 *          type = EmailEntity.Type.HOME
 *          address = "john@doe.com"
 *      ))
 * }
 *
 * val result = profileUpdate
 *      .rawContacts(mutableRawContact)
 *      .commit()
 * ```
 *
 * Java,
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
 * MutableRawContact mutableRawContact = rawContact.mutableCopy();
 * mutableRawContact.setName(name);
 * mutableRawContact.getEmails().add(email);
 *
 * Update.Result result = profileUpdate
 *      .rawContacts(mutableRawContact)
 *      .commit();
 * ```
 *
 * ## Developer notes
 *
 * This is so similar to Update that we could just use Update to handle profile entities too.
 * However, keeping it separate like this gives us the most flexibility and cohesiveness of
 * profile APIs.
 */
interface ProfileUpdate : CrudApi {

    /**
     * If [deleteBlanks] is set to true, then updating blank profile RawContacts
     * ([ExistingRawContactEntity.isBlank]) or blank a profile Contact
     * ([ExistingContactEntity.isBlank]) will result in their deletion. Otherwise, blanks will not
     * be deleted and will result in a failed operation. This flag is set to true by default.
     *
     * The Contacts Providers allows for RawContacts that have no rows in the Data table (let's call
     * them "blanks") to exist. The native Contacts app does not allow insertion of new RawContacts
     * without at least one data row. It also deletes blanks on update. Despite seemingly not
     * allowing blanks, the native Contacts app shows them.
     *
     * Note that this DOES NOT refer to blank data, which are deleted regardless of the value passed
     * to this function.
     */
    fun deleteBlanks(deleteBlanks: Boolean): ProfileUpdate

    /**
     * Specifies that only the given set of [fields] (data) will be updated.
     *
     * If no fields are specified, then all fields will be updated. Otherwise, only the specified
     * fields will be updating in addition to required API fields [Fields.Required] (e.g. IDs),
     * which are always included.
     *
     * Blank data are deleted on update, unless the corresponding fields are NOT included.
     *
     * Note that this may affect performance. It is recommended to only include fields that will be
     * used to save CPU and memory.
     */
    fun include(vararg fields: AbstractDataField): ProfileUpdate

    /**
     * See [ProfileUpdate.include].
     */
    fun include(fields: Collection<AbstractDataField>): ProfileUpdate

    /**
     * See [ProfileUpdate.include].
     */
    fun include(fields: Sequence<AbstractDataField>): ProfileUpdate

    /**
     * See [ProfileUpdate.include].
     */
    fun include(fields: Fields.() -> Collection<AbstractDataField>): ProfileUpdate

    /**
     * Adds the given [rawContacts] to the update queue, which will be updated on [commit].
     */
    fun rawContacts(vararg rawContacts: ExistingRawContactEntity): ProfileUpdate

    /**
     * See [ProfileUpdate.rawContacts].
     */
    fun rawContacts(rawContacts: Collection<ExistingRawContactEntity>): ProfileUpdate

    /**
     * See [ProfileUpdate.rawContacts].
     */
    fun rawContacts(rawContacts: Sequence<ExistingRawContactEntity>): ProfileUpdate

    /**
     * Adds the profile ([ExistingRawContactEntity.isProfile]) [ExistingContactEntity.rawContacts]s
     * of the given [contact] to the update queue, which will be updated on [commit].
     */
    fun contact(contact: ExistingContactEntity): ProfileUpdate

    /**
     * Updates the [ExistingRawContactEntity]s in the queue (added via [rawContacts] and [contact])
     * and returns the [Result].
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.WRITE_PERMISSION].
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
     * Updates the [ExistingRawContactEntity]s in the queue (added via [rawContacts] and [contact])
     * and returns the [Result].
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.WRITE_PERMISSION].
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
     * **Cancelling does not undo updates. This means that depending on when the cancellation
     * occurs, the RawContact in the update queue may have already been updated.**
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
    override fun redactedCopy(): ProfileUpdate

    interface Result : CrudApi.Result {

        /**
         * True if all of the RawContacts have successfully been updated. False if even one
         * update failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [rawContact] has been successfully updated. False otherwise.
         */
        fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun ProfileUpdate(contacts: Contacts): ProfileUpdate = ProfileUpdateImpl(contacts)

private class ProfileUpdateImpl(
    override val contactsApi: Contacts,

    private var deleteBlanks: Boolean = true,
    private var include: Include<AbstractDataField> = contactsApi.includeAllFields(),
    private val rawContacts: MutableSet<ExistingRawContactEntity> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : ProfileUpdate {

    override fun toString(): String =
        """
            ProfileUpdate {
                deleteBlanks: $deleteBlanks
                include: $include
                rawContacts: $rawContacts
                hasPermission: ${permissions.canUpdateDelete()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): ProfileUpdate = ProfileUpdateImpl(
        contactsApi,

        deleteBlanks,
        include,
        // Redact contact data.
        rawContacts.asSequence().redactedCopies().toMutableSet(),

        isRedacted = true
    )

    override fun deleteBlanks(deleteBlanks: Boolean): ProfileUpdate = apply {
        this.deleteBlanks = deleteBlanks
    }

    override fun include(vararg fields: AbstractDataField) = include(fields.asSequence())

    override fun include(fields: Collection<AbstractDataField>) = include(fields.asSequence())

    override fun include(fields: Sequence<AbstractDataField>): ProfileUpdate = apply {
        include = if (fields.isEmpty()) {
            contactsApi.includeAllFields()
        } else {
            Include(fields + Fields.Required.all.asSequence())
        }
    }

    override fun include(fields: Fields.() -> Collection<AbstractDataField>) =
        include(fields(Fields))

    override fun rawContacts(vararg rawContacts: ExistingRawContactEntity) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Collection<ExistingRawContactEntity>) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Sequence<ExistingRawContactEntity>): ProfileUpdate =
        apply {
            this.rawContacts.addAll(rawContacts.redactedCopiesOrThis(isRedacted))
        }

    override fun contact(contact: ExistingContactEntity): ProfileUpdate =
        rawContacts(contact.rawContacts)

    override fun commit(): ProfileUpdate.Result = commit { false }

    override fun commit(cancel: () -> Boolean): ProfileUpdate.Result {
        onPreExecute()

        return if (rawContacts.isEmpty() || !permissions.canUpdateDelete() || cancel()) {
            ProfileUpdateFailed()
        } else {
            val results = mutableMapOf<Long, Boolean>()
            for (rawContact in rawContacts) {
                if (cancel()) {
                    break
                }

                results[rawContact.id] = if (!rawContact.isProfile) {
                    // Intentionally fail the operation to ensure that this is only used for profile
                    // updates. Otherwise, operation can succeed. This is only done to enforce API
                    // design.
                    false
                } else if (rawContact.isBlank && deleteBlanks) {
                    contentResolver.deleteRawContactWithId(rawContact.id)
                } else {
                    contactsApi.updateRawContact(include.fields, rawContact)
                }
            }

            ProfileUpdateResult(results)
        }
            .redactedCopyOrThis(isRedacted)
            .apply { onPostExecute(contactsApi) }
    }
}

private class ProfileUpdateResult private constructor(
    private val rawContactIdsResultMap: Map<Long, Boolean>,
    override val isRedacted: Boolean
) : ProfileUpdate.Result {

    constructor(rawContactIdsResultMap: Map<Long, Boolean>) : this(rawContactIdsResultMap, false)

    override fun toString(): String =
        """
            ProfileUpdate.Result {
                isSuccessful: $isSuccessful
                rawContactIdsResultMap: $rawContactIdsResultMap
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): ProfileUpdate.Result = ProfileUpdateResult(
        rawContactIdsResultMap,
        isRedacted = true
    )

    override val isSuccessful: Boolean by unsafeLazy {
        // By default, all returns true when the collection is empty. So, we override that.
        rawContactIdsResultMap.run { isNotEmpty() && all { it.value } }
    }

    override fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean =
        rawContactIdsResultMap.getOrElse(rawContact.id) { false }
}


private class ProfileUpdateFailed private constructor(
    override val isRedacted: Boolean
) : ProfileUpdate.Result {

    constructor() : this(false)

    override fun toString(): String =
        """
            ProfileUpdate.Result {
                isSuccessful: $isSuccessful
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): ProfileUpdate.Result = ProfileUpdateFailed(true)

    override val isSuccessful: Boolean = false

    override fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean = false
}