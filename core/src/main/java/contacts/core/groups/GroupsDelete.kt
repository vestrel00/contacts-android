package contacts.core.groups

import android.content.ContentResolver
import contacts.core.*
import contacts.core.entities.ExistingGroupEntity
import contacts.core.entities.operation.GroupsOperation
import contacts.core.util.applyBatch
import contacts.core.util.unsafeLazy

/**
 * Deletes one or more groups from the groups table.
 *
 * Note that groups are not immediately deleted. It is deleted in the background by the Contacts
 * Provider depending on sync settings.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All deletes will do nothing if the permission is not granted.
 *
 * ## Usage
 *
 * To delete the given groups;
 *
 * ```kotlin
 * groupsDelete
 *      .groups(groups)
 *      .commit()
 * ```
 *
 * ## Notes
 *
 * Prior to Android 8.0 (Oreo, API 26), group deletion is unpredictable. Groups that are marked for
 * deletion remain in the DB and is still shown in the native Contacts app. Sometimes they do get
 * deleted at some point but the trigger for the actual deletion eludes me.
 *
 * The native Contacts app (prior to API 26) does NOT support group deletion perhaps because groups
 * syncing isn't implemented or at least not to the same extent as contacts syncing. Therefore, this
 * library will also not support group deletion for API versions lower than 26.
 *
 * DO NOT USE THIS ON API VERSION BELOW 26! Or use at your own peril =)
 */
interface GroupsDelete : Redactable {

    /**
     * Adds the given [groups] to the delete queue, which will be deleted on [commit].
     *
     * Read-only groups will be ignored and result in a failed operation.
     */
    fun groups(vararg groups: ExistingGroupEntity): GroupsDelete

    /**
     * See [GroupsDelete.groups].
     */
    fun groups(groups: Collection<ExistingGroupEntity>): GroupsDelete

    /**
     * See [GroupsDelete.groups].
     */
    fun groups(groups: Sequence<ExistingGroupEntity>): GroupsDelete

    /**
     * Deletes the [ExistingGroupEntity]s in the queue (added via [groups]) and returns the [Result].
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

    /**
     * Deletes the [ExistingGroupEntity]s in the queue (added via [groups]) in one transaction.
     * Either ALL deletes succeed or ALL fail.
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Thread Safety

     * This should be called in a background thread to avoid blocking the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun commitInOneTransaction(): Result

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
    override fun redactedCopy(): GroupsDelete

    interface Result : Redactable {

        /**
         * True if all Groups have successfully been deleted. False if even one delete failed.
         */
        val isSuccessful: Boolean

        /**
         * True if the [group] has been successfully deleted. False otherwise.
         */
        fun isSuccessful(group: ExistingGroupEntity): Boolean

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun GroupsDelete(contacts: Contacts): GroupsDelete = GroupsDeleteImpl(
    contacts.applicationContext.contentResolver,
    contacts.permissions
)

private class GroupsDeleteImpl(
    private val contentResolver: ContentResolver,
    private val permissions: ContactsPermissions,

    private val groups: MutableSet<ExistingGroupEntity> = mutableSetOf(),

    override val isRedacted: Boolean = false
) : GroupsDelete {

    override fun toString(): String =
        """
            GroupsDelete {
                groups: $groups
                hasPermission: ${permissions.canUpdateDelete()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): GroupsDelete = GroupsDeleteImpl(
        contentResolver, permissions,

        // Redact group data.
        groups.asSequence().redactedCopies().toMutableSet(),

        isRedacted = true
    )

    override fun groups(vararg groups: ExistingGroupEntity) = groups(groups.asSequence())

    override fun groups(groups: Collection<ExistingGroupEntity>) = groups(groups.asSequence())

    override fun groups(groups: Sequence<ExistingGroupEntity>): GroupsDelete = apply {
        this.groups.addAll(groups.redactedCopiesOrThis(isRedacted))
    }

    override fun commit(): GroupsDelete.Result {
        // TODO issue #144 log this
        return if (groups.isEmpty() || !permissions.canUpdateDelete()) {
            GroupsDeleteResult(emptyMap())
        } else {
            val results = mutableMapOf<Long, Boolean>()
            for (group in groups) {
                results[group.id] = if (group.readOnly) {
                    // Do not attempt to delete read-only groups.
                    false
                } else {
                    contentResolver.applyBatch(GroupsOperation().delete(group.id)) != null
                }
            }
            GroupsDeleteResult(results)
        }.redactedCopyOrThis(isRedacted)
        // TODO issue #144 log result
    }

    override fun commitInOneTransaction(): GroupsDelete.Result {
        // TODO issue #144 log this
        val isSuccessful = permissions.canUpdateDelete()
                && groups.isNotEmpty()
                // Fail immediately if the set contains a read-only group.
                && groups.find { it.readOnly } == null
                && contentResolver.applyBatch(GroupsOperation().delete(groups.map { it.id })) != null

        return GroupsDeleteAllResult(isSuccessful).redactedCopyOrThis(isRedacted)
        // TODO issue #144 log result
    }
}

private class GroupsDeleteResult private constructor(
    private val groupIdsResultMap: Map<Long, Boolean>,
    override val isRedacted: Boolean
) : GroupsDelete.Result {

    constructor(groupIdsResultMap: Map<Long, Boolean>) : this(groupIdsResultMap, false)

    override fun toString(): String =
        """
            GroupsDelete.Result {
                isSuccessful: $isSuccessful
                groupIdsResultMap: $groupIdsResultMap
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): GroupsDelete.Result = GroupsDeleteResult(
        groupIdsResultMap, true
    )

    override val isSuccessful: Boolean by unsafeLazy { groupIdsResultMap.all { it.value } }

    override fun isSuccessful(group: ExistingGroupEntity): Boolean {
        return groupIdsResultMap.getOrElse(group.id) { false }
    }
}

private class GroupsDeleteAllResult private constructor(
    override val isSuccessful: Boolean,
    override val isRedacted: Boolean
) : GroupsDelete.Result {

    constructor(isSuccessful: Boolean) : this(
        isSuccessful = isSuccessful,
        isRedacted = false
    )

    override fun toString(): String =
        """
            GroupsDelete.Result {
                isSuccessful: $isSuccessful
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): GroupsDelete.Result = GroupsDeleteAllResult(
        isSuccessful = isSuccessful,
        isRedacted = true
    )

    override fun isSuccessful(group: ExistingGroupEntity): Boolean = isSuccessful
}
