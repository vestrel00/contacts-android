package com.vestrel00.contacts.profile

import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.entities.ContactEntity
import com.vestrel00.contacts.entities.RawContactEntity
import com.vestrel00.contacts.util.unsafeLazy

interface ProfileDelete {

    /**
     * Adds the given profile [rawContacts] ([RawContactEntity.isProfile]) to the delete queue,
     * which will be deleted on [commit].
     *
     * Profile RawContacts that have not yet been inserted ([RawContactEntity.id] is null) will be
     * ignored and result in a failed operation.
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

    interface Result {

        /**
         * True if the profile [ContactEntity] has been successfully deleted (via [contact]),
         * regardless of the outcome of profile [RawContactEntity] deletions (via [rawContacts]).
         */
        val isSuccessful: Boolean

        /**
         * True if the [rawContact] has been successfully deleted. False otherwise.
         *
         * This does not indicate whether the parent profile [ContactEntity] has been deleted or
         * not. This may return false even if the parent profile [ContactEntity] has been
         * successfully deleted. This is used in conjunction with [ProfileDelete.rawContacts].
         */
        fun isSuccessful(rawContact: RawContactEntity): Boolean
    }
}

private class ProfileDeleteResult(
    private val rawContactIdsResultMap: Map<Long, Boolean>,

    /**
     * Null if not attempted.
     *
     * Got lazy here. I know I can use an something else (like an enum) for ternary options instead
     * of using a null value as the third option. Sue me.
     */
    private val profileContactDeleteSuccess: Boolean?
) : ProfileDelete.Result {

    override val isSuccessful: Boolean by unsafeLazy {
        (profileContactDeleteSuccess == true) || rawContactIdsResultMap.all { it.value }
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