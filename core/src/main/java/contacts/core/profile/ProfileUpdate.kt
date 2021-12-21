package contacts.core.profile

import contacts.core.*
import contacts.core.entities.ExistingContactEntity
import contacts.core.entities.ExistingRawContactEntity
import contacts.core.util.isEmpty
import contacts.core.util.unsafeLazy

/**
 * Updates one or more (Profile) raw contacts' rows in the data table.
 *
 * As per documentation in []android.provider.ContactsContract.Profile],
 *
 * > The profile Contact has the same update restrictions as Contacts in general...
 * > Only certain columns of Contact are modifiable: STARRED, CUSTOM_RINGTONE, SEND_TO_VOICEMAIL.
 * > Changing any of these columns on the Contact also changes them on all constituent raw contacts.
 *
 * ## Blank data are deleted
 *
 * Blank data will be deleted. For example, if all properties of an email are all null, empty, or
 * blank, then the email is deleted. This is the same behavior as the native Contacts app. This
 * behavior cannot be modified.
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
interface ProfileUpdate {

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
     * Note that blank data are deleted. For example, if all properties of an email are all null,
     * empty, or blank, then the email is deleted. This is the same behavior as the native Contacts
     * app. This is the same behavior as the native Contacts app. This behavior cannot be modified.
     */
    fun deleteBlanks(deleteBlanks: Boolean): ProfileUpdate

    /**
     * Specifies that only the given set of [fields] (data) will be updated.
     *
     * If no fields are specified, then all fields will be updated. Otherwise, only the specified
     * fields will be updating in addition to required API fields [Fields.Required] (e.g. IDs),
     * which are always included.
     *
     * Note that this may affect performance. It is recommended to only include fields that will be
     * used to save CPU and memory.
     *
     * ## Performing updates on entities with partial includes
     *
     * When the query include function is used, only certain data will be included in the returned
     * entities. All other data are guaranteed to be null (except for those in [Fields.Required]).
     * When performing updates on entities that have only partial data included, make sure to use
     * the same included fields in the update operation as the included fields used in the query.
     * This will ensure that the set of data queried and updated are the same. For example, in order
     * to get and set only email addresses and leave everything the same in the database...
     *
     * ```kotlin
     * val profile = query.include(Fields.Email.Address).find()
     * val mutableProfile = setEmailAddresses(profile)
     * update.contact(mutableProfile).include(Fields.Email.Address).commit()
     * ```
     *
     * On the other hand, you may intentionally include only some data and perform updates without
     * on all data (not just the included ones) to effectively delete all non-included data. This
     * is, currently, a feature- not a bug! For example, in order to get and set only email
     * addresses and set all other data to null (such as phone numbers, name, etc) in the database..
     *
     * ```kotlin
     * val profile = query.include(Fields.Email.Address).find()
     * val mutableProfile = setEmailAddresses(profile)
     * update.contact(mutableProfile).include(Fields.all).commit()
     * ```
     *
     * This gives you the most flexibility when it comes to specifying what fields to
     * include/exclude in queries, inserts, and update, which will allow you to do things beyond
     * your wildest imagination!
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

    interface Result {

        /**
         * True if all of the RawContacts have successfully been updated. False if even one
         * update failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [rawContact] has been successfully updated. False otherwise.
         */
        fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean
    }
}

@Suppress("FunctionName")
internal fun ProfileUpdate(contacts: Contacts): ProfileUpdate = ProfileUpdateImpl(contacts)

private class ProfileUpdateImpl(
    private val contacts: Contacts,

    private var deleteBlanks: Boolean = true,
    private var include: Include<AbstractDataField> = allDataFields(contacts.customDataRegistry),
    private val rawContacts: MutableSet<ExistingRawContactEntity> = mutableSetOf()
) : ProfileUpdate {

    override fun toString(): String =
        """
            ProfileUpdate {
                deleteBlanks: $deleteBlanks
                include: $include
                rawContacts: $rawContacts
            }
        """.trimIndent()

    override fun deleteBlanks(deleteBlanks: Boolean): ProfileUpdate = apply {
        this.deleteBlanks = deleteBlanks
    }

    override fun include(vararg fields: AbstractDataField) = include(fields.asSequence())

    override fun include(fields: Collection<AbstractDataField>) = include(fields.asSequence())

    override fun include(fields: Sequence<AbstractDataField>): ProfileUpdate = apply {
        include = if (fields.isEmpty()) {
            allDataFields(contacts.customDataRegistry)
        } else {
            Include(fields + Fields.Required.all.asSequence())
        }
    }

    override fun rawContacts(vararg rawContacts: ExistingRawContactEntity) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Collection<ExistingRawContactEntity>) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Sequence<ExistingRawContactEntity>): ProfileUpdate =
        apply {
            this.rawContacts.addAll(rawContacts)
        }

    override fun contact(contact: ExistingContactEntity): ProfileUpdate =
        rawContacts(contact.rawContacts)

    override fun commit(): ProfileUpdate.Result = commit { false }

    override fun commit(cancel: () -> Boolean): ProfileUpdate.Result {
        if (rawContacts.isEmpty() || !contacts.permissions.canUpdateDelete() || cancel()) {
            return ProfileUpdateFailed()
        }

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
                contacts.applicationContext.contentResolver
                    .deleteRawContactWithId(rawContact.id)
            } else {
                contacts.updateRawContact(include.fields, rawContact)
            }
        }
        return ProfileUpdateResult(results)
    }
}

private class ProfileUpdateResult(private val rawContactIdsResultMap: Map<Long, Boolean>) :
    ProfileUpdate.Result {

    override val isSuccessful: Boolean by unsafeLazy { rawContactIdsResultMap.all { it.value } }

    override fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean =
        rawContactIdsResultMap.getOrElse(rawContact.id) { false }
}


private class ProfileUpdateFailed : ProfileUpdate.Result {

    override val isSuccessful: Boolean = false

    override fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean = false
}