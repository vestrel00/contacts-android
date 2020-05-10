package com.vestrel00.contacts.groups

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.entities.MutableGroup
import com.vestrel00.contacts.entities.operation.GroupOperation

/**
 * Updates one or more groups rows in the groups table.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] and
 * [com.vestrel00.contacts.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION] are assumed to have
 * been granted already in these examples for brevity. All updates will do nothing if these
 * permissions are not granted.
 *
 * ## Usage
 *
 * To update a groups's name to "Best Friends";
 *
 * In Kotlin,
 *
 * ```kotlin
 * val result = groupsUpdate
 *      .groups(group.toMutableGroup()?.apply {
 *          title = "Best Friends"
 *      })
 *      .commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * MutableGroup mutableGroup = group.toMutableGroup();
 *
 * if (mutableGroup != null) {
 *   mutableGroup.setTitle("Best Friends");
 *
 *   GroupsUpdate.Result result = groupsUpdate
 *        .groups(mutableGroup)
 *        .commit();
 * }
 * ```
 */
interface GroupsUpdate {

    /**
     * Adds the given [groups] to the update queue, which will be updated on [commit].
     *
     * Only existing [groups] that have been retrieved via a query will be added to the update
     * queue. Those that have been manually created via a constructor will be ignored.
     *
     * ## Null [MutableGroup]s
     *
     * Null groups are ignored. The only reason null is allowed to be passed here is for consumer
     * convenience because the group's `toMutable` returns null if the `readOnly` property is true.
     */
    fun groups(vararg groups: MutableGroup?): GroupsUpdate

    /**
     * See [GroupsUpdate.groups].
     */
    fun groups(groups: Collection<MutableGroup?>): GroupsUpdate

    /**
     * See [GroupsUpdate.groups].
     */
    fun groups(groups: Sequence<MutableGroup?>): GroupsUpdate

    /**
     * Updates the [MutableGroup]s in the queue (added via [groups]) and returns the [Result].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commit(): Result

    interface Result {

        /**
         * True if all Groups have successfully been updated. False if even one update failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [group] has been successfully updated. False otherwise.
         */
        fun isSuccessful(group: MutableGroup): Boolean
    }
}

@Suppress("FunctionName")
internal fun GroupsUpdate(context: Context): GroupsUpdate = GroupsUpdateImpl(
    context.contentResolver,
    ContactsPermissions(context)
)

private class GroupsUpdateImpl(
    private val contentResolver: ContentResolver,
    private val permissions: ContactsPermissions,
    private val groups: MutableSet<MutableGroup> = mutableSetOf()
) : GroupsUpdate {

    override fun groups(vararg groups: MutableGroup?): GroupsUpdate =
        groups(groups.asSequence())

    override fun groups(groups: Collection<MutableGroup?>): GroupsUpdate =
        groups(groups.asSequence())

    override fun groups(groups: Sequence<MutableGroup?>): GroupsUpdate = apply {
        this.groups.addAll(groups.filterNotNull())
    }

    override fun commit(): GroupsUpdate.Result {
        if (groups.isEmpty() || !permissions.canInsertUpdateDelete()) {
            return GroupsUpdateFailed
        }

        val results = mutableMapOf<Long, Boolean>()
        for (group in groups) {
            if (group.id != null && !group.readOnly) {
                results[group.id] = contentResolver.updateGroup(group)
            }
        }
        return GroupsUpdateResult(results)
    }
}

private fun ContentResolver.updateGroup(group: MutableGroup): Boolean {
    val operation = GroupOperation().update(group) ?: return false

    /*
     * Atomically update the group row.
     *
     * Perform this single operation in a batch to be consistent with the other CRUD functions.
     */
    try {
        applyBatch(ContactsContract.AUTHORITY, arrayListOf(operation))
    } catch (exception: Exception) {
        return false
    }

    return true
}

private class GroupsUpdateResult(private val groupIdsResultMap: Map<Long, Boolean>) :
    GroupsUpdate.Result {

    override val isSuccessful: Boolean by lazy { groupIdsResultMap.all { it.value } }

    override fun isSuccessful(group: MutableGroup): Boolean = group.id != null
            && groupIdsResultMap.getOrElse(group.id) { false }
}

private object GroupsUpdateFailed : GroupsUpdate.Result {

    override val isSuccessful: Boolean = false

    override fun isSuccessful(group: MutableGroup): Boolean = false
}
