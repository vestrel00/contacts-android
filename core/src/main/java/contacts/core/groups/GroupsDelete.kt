package contacts.core.groups

import contacts.core.*
import contacts.core.entities.ExistingGroupEntity
import contacts.core.entities.operation.GroupsOperation
import contacts.core.util.applyBatch
import contacts.core.util.unsafeLazy

/**
 * Deletes one or more groups from the groups table.
 *
 * ## Group memberships are automatically deleted
 *
 * When a group is deleted, any membership to that group is deleted automatically by the
 * Contacts Provider.
 *
 * ## Deletion is not immediate
 *
 * **Groups are not immediately deleted**. However, they are marked for deletion and they do get
 * deleted in the background by the Contacts Provider depending on sync settings.
 *
 * However, group memberships to those groups marked for deletion are immediately deleted!
 *
 * ### Starred in Android (Favorites)
 *
 * When a Contact is starred, the Contacts Provider automatically adds a group membership to the
 * favorites group for all RawContacts linked to the Contact. Setting the Contact starred to false
 * removes all group memberships to the favorites group.
 *
 * The Contact's "starred" value is interdependent with group memberships to the favorites group.
 * Adding a group membership to the favorites group results in starred being set to true. Removing
 * the membership sets it to false.
 *
 * Raw contacts that are not associated with an account do not have any group memberships. Even
 * though these RawContacts may not have a membership to the favorites group, they may still be
 * "starred" (favorited), which is not dependent on the existence of a favorites group membership.
 *
 * **Refresh RawContact instances after changing the starred value.** Otherwise, performing an
 * update on the RawContact with a stale set of group memberships may revert the star/unstar
 * operation. For example,
 *
 * -> query returns a starred RawContact
 * -> set starred to false
 * -> update RawContact (still containing a group membership to the favorites group)
 * -> starred will be set back to true.
 *
 * ## Permissions
 *
 * The [ContactsPermissions.WRITE_PERMISSION] is assumed to have been granted already in these
 * examples for brevity. All deletes will do nothing if the permission is not granted.
 *
 * ## Usage
 *
 * To delete the given groups,
 *
 * ```kotlin
 * groupsDelete
 *      .groups(groups)
 *      .commit()
 * ```
 */
interface GroupsDelete : CrudApi {

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

    interface Result : CrudApi.Result {

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
internal fun GroupsDelete(contacts: Contacts): GroupsDelete = GroupsDeleteImpl(contacts)

private class GroupsDeleteImpl(
    override val contactsApi: Contacts,

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
        contactsApi,

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
        onPreExecute()

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
        }
            .redactedCopyOrThis(isRedacted)
            .apply { onPostExecute(contactsApi) }
    }

    override fun commitInOneTransaction(): GroupsDelete.Result {
        onPreExecute()

        val isSuccessful = permissions.canUpdateDelete()
                && groups.isNotEmpty()
                // Fail immediately if the set contains a read-only group.
                && groups.find { it.readOnly } == null
                && contentResolver.applyBatch(GroupsOperation().delete(groups.map { it.id })) != null

        return GroupsDeleteAllResult(isSuccessful)
            .redactedCopyOrThis(isRedacted)
            .apply { onPostExecute(contactsApi) }
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

    override val isSuccessful: Boolean by unsafeLazy {
        // By default, all returns true when the collection is empty. So, we override that.
        groupIdsResultMap.run { isNotEmpty() && all { it.value } }
    }

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
