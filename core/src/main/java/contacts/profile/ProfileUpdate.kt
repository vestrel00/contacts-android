package contacts.profile

import android.content.Context
import contacts.ContactsPermissions
import contacts.deleteRawContactWithId
import contacts.entities.MutableContact
import contacts.entities.MutableRawContact
import contacts.entities.custom.CustomCommonDataRegistry
import contacts.updateRawContact
import contacts.util.unsafeLazy

/**
 * Updates one or more (Profile) raw contacts' rows in the data table.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All updates will do nothing if these permissions are not granted.
 *
 * ## Accounts
 *
 * These operations ensure that only [MutableRawContact.groupMemberships] belonging to the same
 * account as the raw contact are inserted.
 *
 * ## Usage
 *
 * To update a (profile) raw contact's name to "john doe" and add an email "john@doe.com";
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
 * val result = profileUpdate
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
     * ([MutableRawContact.isBlank]) or blank a profile Contact ([MutableContact.isBlank]) will
     * result in their deletion. Otherwise, blanks will not be deleted and will result in a failed
     * operation. This flag is set to true by default.
     *
     * The Contacts Providers allows for RawContacts that have no rows in the Data table (let's call
     * them "blanks") to exist. The native Contacts app does not allow insertion of new RawContacts
     * without at least one data row. It also deletes blanks on update. Despite seemingly not
     * allowing blanks, the native Contacts app shows them.
     */
    fun deleteBlanks(deleteBlanks: Boolean): ProfileUpdate

    /**
     * Adds the given [rawContacts] to the update queue, which will be updated on [commit].
     *
     * Only existing profile ([MutableRawContact.isProfile]) [rawContacts] that have been retrieved
     * via a query will be added to the update queue. Those that have been manually created via a
     * constructor will be ignored and result in a failed operation.
     */
    fun rawContacts(vararg rawContacts: MutableRawContact): ProfileUpdate

    /**
     * See [ProfileUpdate.rawContacts].
     */
    fun rawContacts(rawContacts: Collection<MutableRawContact>): ProfileUpdate

    /**
     * See [ProfileUpdate.rawContacts].
     */
    fun rawContacts(rawContacts: Sequence<MutableRawContact>): ProfileUpdate

    /**
     * Adds the profile ([MutableRawContact.isProfile]) [MutableContact.rawContacts]s of the given
     * [contact] to the update queue, which will be updated on [commit].
     */
    fun contact(contact: MutableContact): ProfileUpdate

    /**
     * Updates the [MutableRawContact]s in the queue (added via [rawContacts] and [contact]) and
     * returns the [Result].
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

    interface Result {

        /**
         * True if all of the RawContacts have successfully been updated. False if even one
         * update failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [rawContact] has been successfully updated. False otherwise.
         */
        fun isSuccessful(rawContact: MutableRawContact): Boolean
    }
}

@Suppress("FunctionName")
internal fun ProfileUpdate(
    context: Context, customDataRegistry: CustomCommonDataRegistry
): ProfileUpdate = ProfileUpdateImpl(
    context.applicationContext,
    ContactsPermissions(context),
    customDataRegistry
)

private class ProfileUpdateImpl(
    private val applicationContext: Context,
    private val permissions: ContactsPermissions,
    private val customDataRegistry: CustomCommonDataRegistry,

    private var deleteBlanks: Boolean = true,
    private val rawContacts: MutableSet<MutableRawContact> = mutableSetOf()
) : ProfileUpdate {

    override fun toString(): String =
        """
            ProfileUpdate {
                deleteBlanks: $deleteBlanks
                rawContacts: $rawContacts
            }
        """.trimIndent()

    override fun deleteBlanks(deleteBlanks: Boolean): ProfileUpdate = apply {
        this.deleteBlanks = deleteBlanks
    }

    override fun rawContacts(vararg rawContacts: MutableRawContact) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Collection<MutableRawContact>) =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Sequence<MutableRawContact>): ProfileUpdate = apply {
        this.rawContacts.addAll(rawContacts)
    }

    override fun contact(contact: MutableContact): ProfileUpdate = rawContacts(contact.rawContacts)

    override fun commit(): ProfileUpdate.Result {
        if (rawContacts.isEmpty() || !permissions.canUpdateDelete()) {
            return ProfileUpdateFailed
        }

        val results = mutableMapOf<Long, Boolean>()
        for (rawContact in rawContacts) {
            if (rawContact.id != null) {
                results[rawContact.id] = if (!rawContact.isProfile) {
                    // Intentionally fail the operation to ensure that this is only used for profile
                    // updates. Otherwise, operation can succeed. This is only done to enforce API
                    // design.
                    false
                } else if (rawContact.isBlank && deleteBlanks) {
                    applicationContext.contentResolver.deleteRawContactWithId(rawContact.id)
                } else {
                    applicationContext.updateRawContact(customDataRegistry, rawContact)
                }
            } else {
                results[INVALID_ID] = false
            }
        }
        return ProfileUpdateResult(results)
    }

    private companion object {
        // A failed entry in the results so that Result.isSuccessful returns false.
        const val INVALID_ID = -1L
    }
}

private class ProfileUpdateResult(private val rawContactIdsResultMap: Map<Long, Boolean>) :
    ProfileUpdate.Result {

    override val isSuccessful: Boolean by unsafeLazy {
        rawContactIdsResultMap.isNotEmpty() && rawContactIdsResultMap.all { it.value }
    }

    override fun isSuccessful(rawContact: MutableRawContact): Boolean {
        val rawContactId = rawContact.id
        return rawContactId != null && rawContactIdsResultMap.getOrElse(rawContactId) { false }
    }
}


private object ProfileUpdateFailed : ProfileUpdate.Result {

    override val isSuccessful: Boolean = false

    override fun isSuccessful(rawContact: MutableRawContact): Boolean = false
}