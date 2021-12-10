package contacts.core.profile

import android.content.ContentProviderOperation.newDelete
import android.content.ContentResolver
import contacts.core.Contacts
import contacts.core.ContactsPermissions
import contacts.core.deleteRawContactWithId
import contacts.core.entities.ExistingContactEntity
import contacts.core.entities.ExistingRawContactEntity
import contacts.core.entities.operation.RawContactsOperation
import contacts.core.entities.table.ProfileUris
import contacts.core.util.applyBatch
import contacts.core.util.isProfileId
import contacts.core.util.unsafeLazy

/**
 * Deletes one or more (Profile) raw contacts or the (Profile) contact from the raw contacts and
 * contacts tables respectively. All associated (Profile) data rows are also deleted.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All deletes will do nothing if the permission is not granted.
 *
 * For API 22 and below, the permission "android.permission.WRITE_PROFILE" is also required but
 * only at the manifest level. Prior to API 23 (Marshmallow), permissions needed to be granted
 * prior to installation instead of at runtime.
 *
 * ## Usage
 *
 * To delete the profile [ExistingContactEntity] and all associated [ExistingRawContactEntity]s;
 *
 * ```kotlin
 * val result = profileDelete
 *      .contact()
 *      .commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * ProfileDelete.Result result = profileDelete
 *      .contact()
 *      .commit()
 * ```
 */
interface ProfileDelete {

    /**
     * Adds the given profile [rawContacts] ([ExistingRawContactEntity.isProfile]) to the delete
     * queue, which will be deleted on [commit].
     *
     * If [contact] is called, then any [ExistingRawContactEntity]s provided here will be ignored.
     *
     * ## IMPORTANT
     *
     * Deleting all profile [ExistingRawContactEntity]s of a profile [ExistingContactEntity] will
     * result in the deletion of the associated profile [ExistingContactEntity]! However, the
     * profile [ExistingContactEntity] will remain as long as it has at least has one associated
     * profile [ExistingRawContactEntity].
     */
    fun rawContacts(vararg rawContacts: ExistingRawContactEntity): ProfileDelete

    /**
     * See [ProfileDelete.rawContacts].
     */
    fun rawContacts(rawContacts: Collection<ExistingRawContactEntity>): ProfileDelete

    /**
     * See [ProfileDelete.rawContacts].
     */
    fun rawContacts(rawContacts: Sequence<ExistingRawContactEntity>): ProfileDelete

    /**
     * Adds the existing profile contact (if any) to the delete queue, which will be deleted on
     * [commit].
     *
     * If this is called, then any [ExistingRawContactEntity]s provided via [rawContacts] will be
     * ignored.
     *
     * ## IMPORTANT
     *
     * Deleting the profile [ExistingContactEntity] will result in the deletion of all associated
     * profile [ExistingRawContactEntity]s!
     */
    fun contact(): ProfileDelete

    /**
     * Deletes the profile [ExistingContactEntity] or profile [ExistingRawContactEntity]s in the
     * queue (added via [contact] and [rawContacts]) and returns the [Result].
     *
     * ## Permissions
     *
     * Requires the [ContactsPermissions.WRITE_PERMISSION].
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
     * Deletes the profile [ExistingContactEntity] or [ExistingRawContactEntity]s in the queue in
     * one transaction.
     *
     * True if the profile [ExistingContactEntity] has been successfully deleted (via [contact]).
     *
     * If [rawContacts] is used instead of [contact], then this is true only if all
     * [ExistingRawContactEntity]s provided in [rawContacts] have been successfully deleted.
     *
     * ## Permissions
     *
     * Requires the [ContactsPermissions.WRITE_PERMISSION].
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
    fun commitInOneTransaction(): Boolean

    interface Result {

        /**
         * True if the profile [ExistingContactEntity] has been successfully deleted
         * (via [contact]).
         *
         * If [rawContacts] is used instead of [contact], then this is true only if all
         * [ExistingRawContactEntity]s provided in [rawContacts] have been successfully deleted.
         */
        val isSuccessful: Boolean

        /**
         * True if the [rawContact] has been successfully deleted provided via [rawContacts].
         *
         * This will always be true if the profile [ExistingContactEntity] has been successfully
         * deleted (via [contact]).
         */
        fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean
    }
}

@Suppress("FunctionName")
internal fun ProfileDelete(contacts: Contacts): ProfileDelete = ProfileDeleteImpl(
    contacts.applicationContext.contentResolver,
    contacts.permissions
)

private class ProfileDeleteImpl(
    private val contentResolver: ContentResolver,
    private val permissions: ContactsPermissions,
    private val rawContactIds: MutableSet<Long> = mutableSetOf(),
    private var deleteProfileContact: Boolean = false
) : ProfileDelete {

    override fun toString(): String =
        """
            ProfileDeleteImpl {
                rawContactIds: $rawContactIds
                deleteProfileContact: $deleteProfileContact
            }
        """.trimIndent()

    override fun rawContacts(vararg rawContacts: ExistingRawContactEntity): ProfileDelete =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Collection<ExistingRawContactEntity>): ProfileDelete =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Sequence<ExistingRawContactEntity>): ProfileDelete =
        apply {
            rawContactIds.addAll(rawContacts.map { it.id })
        }

    override fun contact(): ProfileDelete = apply {
        deleteProfileContact = true
    }

    override fun commit(): ProfileDelete.Result {
        if ((rawContactIds.isEmpty() && !deleteProfileContact) || !permissions.canUpdateDelete()) {
            return ProfileDeleteFailed()
        }

        if (deleteProfileContact) {
            return ProfileDeleteResult(
                emptyMap(),
                contentResolver.deleteProfileContact()
            )
        }

        val rawContactsResult = mutableMapOf<Long, Boolean>()
        for (rawContactId in rawContactIds) {
            rawContactsResult[rawContactId] =
                if (!rawContactId.isProfileId) {
                    // Intentionally fail the operation to ensure that this is only used for profile
                    // deletes. Otherwise, operation can succeed. This is only done to enforce API
                    // design.
                    false
                } else {
                    contentResolver.deleteRawContactWithId(rawContactId)
                }
        }

        return ProfileDeleteResult(rawContactsResult, false)
    }

    override fun commitInOneTransaction(): Boolean {
        if ((rawContactIds.isEmpty() && !deleteProfileContact) || !permissions.canUpdateDelete()) {
            return false
        }

        if (deleteProfileContact) {
            return contentResolver.deleteProfileContact()
        }

        val profileRawContactIds = rawContactIds.filter { it.isProfileId }

        if (rawContactIds.size != profileRawContactIds.size) {
            // There are some non-profile RawContact Ids, fail without performing operation.
            return false
        }

        return contentResolver.applyBatch(
            RawContactsOperation(true).deleteRawContacts(profileRawContactIds)
        ) != null
    }
}

private fun ContentResolver.deleteProfileContact(): Boolean =
    applyBatch(newDelete(ProfileUris.RAW_CONTACTS.uri).build()) != null

private class ProfileDeleteResult(
    private val rawContactIdsResultMap: Map<Long, Boolean>,
    private val profileContactDeleteSuccess: Boolean
) : ProfileDelete.Result {

    override val isSuccessful: Boolean by unsafeLazy {
        profileContactDeleteSuccess || rawContactIdsResultMap.all { it.value }
    }

    override fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean =
        rawContactIdsResultMap.getOrElse(rawContact.id) { false }
}

private class ProfileDeleteFailed : ProfileDelete.Result {

    override val isSuccessful: Boolean = false

    override fun isSuccessful(rawContact: ExistingRawContactEntity): Boolean = false
}