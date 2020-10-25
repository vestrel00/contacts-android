package com.vestrel00.contacts.profile

import android.content.ContentProviderOperation.newDelete
import android.content.ContentResolver
import android.content.Context
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.deleteRawContactWithId
import com.vestrel00.contacts.entities.ContactEntity
import com.vestrel00.contacts.entities.RawContactEntity
import com.vestrel00.contacts.entities.operation.RawContactsOperation
import com.vestrel00.contacts.entities.table.ProfileUris
import com.vestrel00.contacts.util.applyBatch
import com.vestrel00.contacts.util.unsafeLazy

/**
 * TODO documentation
 */
interface ProfileDelete {

    /**
     * Adds the given profile [rawContacts] ([RawContactEntity.isProfile]) to the delete queue,
     * which will be deleted on [commit].
     *
     * Profile RawContacts that have not yet been inserted ([RawContactEntity.id] is null) will be
     * ignored and result in a failed operation.
     *
     * If [contact] is called, then any [RawContactEntity]s provided here will be ignored.
     *
     * ## IMPORTANT
     *
     * Deleting all profile [RawContactEntity]s of a profile [ContactEntity] will result in the
     * deletion of the associated profile [ContactEntity]! However, the profile [ContactEntity]
     * will remain as long as it has at least has one associated profile [RawContactEntity].
     */
    fun rawContacts(vararg rawContacts: RawContactEntity): ProfileDelete

    /**
     * See [ProfileDelete.rawContacts].
     */
    fun rawContacts(rawContacts: Collection<RawContactEntity>): ProfileDelete

    /**
     * See [ProfileDelete.rawContacts].
     */
    fun rawContacts(rawContacts: Sequence<RawContactEntity>): ProfileDelete

    /**
     * Adds the existing profile contact (if any) to the delete queue, which will be deleted on
     * [commit].
     *
     * If this is called, then any [RawContactEntity]s provided via [rawContacts] will be ignored.
     *
     * ## IMPORTANT
     *
     * Deleting the profile [ContactEntity] will result in the deletion of all associated profile
     * [RawContactEntity]s!
     */
    fun contact(): ProfileDelete

    /**
     * Deletes the profile [ContactEntity] or profile [RawContactEntity]s in the queue (added via
     * [contact] and [rawContacts]) and returns the [Result].
     *
     * ## Permissions
     *
     * Requires the [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

    /**
     * Deletes the profile [ContactEntity] or [RawContactEntity]s in the queue in one transaction.
     *
     * True if the profile [ContactEntity] has been successfully deleted (via [contact]).
     *
     * If [rawContacts] is used instead of [contact], then this is true only if all
     * [RawContactEntity]s provided in [rawContacts] have been successfully deleted.
     *
     * ## Permissions
     *
     * Requires the [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commitInOneTransaction(): Boolean

    interface Result {

        /**
         * True if the profile [ContactEntity] has been successfully deleted (via [contact]).
         *
         * If [rawContacts] is used instead of [contact], then this is true only if all
         * [RawContactEntity]s provided in [rawContacts] have been successfully deleted.
         */
        val isSuccessful: Boolean

        /**
         * True if the [rawContact] has been successfully deleted provided via [rawContacts].
         *
         * This will always be true if the profile [ContactEntity] has been successfully deleted
         * (via [contact]).
         */
        fun isSuccessful(rawContact: RawContactEntity): Boolean
    }
}

@Suppress("FunctionName")
internal fun ProfileDelete(context: Context): ProfileDelete = ProfileDeleteImpl(
    context.contentResolver,
    ContactsPermissions(context)
)

private class ProfileDeleteImpl(
    private val contentResolver: ContentResolver,
    private val permissions: ContactsPermissions,
    private val rawContacts: MutableSet<RawContactEntity> = mutableSetOf(),
    private var deleteProfileContact: Boolean = false
) : ProfileDelete {

    override fun toString(): String =
        """
            ProfileDeleteImpl {
                rawContacts: $rawContacts
                deleteProfileContact: $deleteProfileContact
            }
        """.trimIndent()

    override fun rawContacts(vararg rawContacts: RawContactEntity): ProfileDelete =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Collection<RawContactEntity>): ProfileDelete =
        rawContacts(rawContacts.asSequence())

    override fun rawContacts(rawContacts: Sequence<RawContactEntity>): ProfileDelete = apply {
        this.rawContacts.addAll(rawContacts)
    }

    override fun contact(): ProfileDelete = apply {
        deleteProfileContact = true
    }

    override fun commit(): ProfileDelete.Result {
        if ((rawContacts.isEmpty() && !deleteProfileContact) || !permissions.canUpdateDelete()) {
            return ProfileDeleteFailed
        }

        if (deleteProfileContact) {
            return ProfileDeleteResult(
                emptyMap(),
                contentResolver.deleteProfileContact()
            )
        }

        val rawContactsResult = mutableMapOf<Long, Boolean>()
        for (rawContact in rawContacts) {
            val rawContactId = rawContact.id
            if (rawContactId != null) {
                rawContactsResult[rawContactId] = if (rawContact.isProfile != IS_PROFILE) {
                    false
                } else {
                    contentResolver.deleteRawContactWithId(rawContactId, IS_PROFILE)
                }
            } else {
                rawContactsResult[INVALID_ID] = false
            }

        }

        return ProfileDeleteResult(rawContactsResult, false)
    }

    override fun commitInOneTransaction(): Boolean {
        if ((rawContacts.isEmpty() && !deleteProfileContact) || !permissions.canUpdateDelete()) {
            return false
        }

        if (deleteProfileContact) {
            return contentResolver.deleteProfileContact()
        }

        val rawContactIds = rawContacts.filter { it.isProfile == IS_PROFILE }.mapNotNull { it.id }

        if (rawContactIds.size != rawContacts.size) {
            // There are some null ids or IS_PROFILE mismatch, fail without performing operation.
            return false
        }

        return contentResolver.applyBatch(
            RawContactsOperation(IS_PROFILE).deleteRawContacts(rawContactIds)
        ) != null
    }

    private companion object {
        // A failed entry in the results so that Result.isSuccessful returns false.
        const val INVALID_ID = -1L
        const val IS_PROFILE = true
    }
}

private fun ContentResolver.deleteProfileContact(): Boolean =
    applyBatch(newDelete(ProfileUris.RAW_CONTACTS.uri).build()) != null

private class ProfileDeleteResult(
    private val rawContactIdsResultMap: Map<Long, Boolean>,
    private val profileContactDeleteSuccess: Boolean
) : ProfileDelete.Result {

    override val isSuccessful: Boolean by unsafeLazy {
        profileContactDeleteSuccess ||
                (rawContactIdsResultMap.isNotEmpty() && rawContactIdsResultMap.all { it.value })
    }

    override fun isSuccessful(rawContact: RawContactEntity): Boolean =
        rawContact.id?.let { rawContactId ->
            rawContactIdsResultMap.getOrElse(rawContactId) { false }
        } ?: false
}

private object ProfileDeleteFailed :
    ProfileDelete.Result {

    override val isSuccessful: Boolean = false

    override fun isSuccessful(rawContact: RawContactEntity): Boolean = false
}