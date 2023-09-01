package contacts.core.groups

import android.content.ContentProviderOperation
import android.content.ContentResolver
import contacts.core.*
import contacts.core.entities.ExistingGroupEntity
import contacts.core.entities.operation.withSelection
import contacts.core.entities.table.Table
import contacts.core.util.applyBatch
import contacts.core.util.deleteSuccess

/**
 * Deletes one or more groups from the groups table.
 *
 * ## Group memberships are automatically deleted
 *
 * When a group is deleted, any memberships to that group are deleted automatically by the
 * Contacts Provider.
 *
 * ## Deletion is not guaranteed to be immediate
 *
 * **Groups may not immediately be deleted**. They are marked for deletion and get deleted  in the
 * background by the Contacts Provider depending on sync settings and network availability.
 *
 * Group **memberships** to those groups marked for deletion are immediately deleted!
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
 * Raw contacts that are not associated with an account may or may not have any group memberships.
 * Even though these RawContacts may not have a membership to a favorites group, they may still be
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
 * In Kotlin,
 *
 * ```kotlin
 * val result = groupsDelete.groups(groups).commit()
 * ```
 *
 * In Java,
 *
 * ```java
 * GroupsDelete.Result result = groupsDelete.groups(groups).commit();
 * ```
 */
interface GroupsDelete : CrudApi {

    /**
     * Adds the given [groups] to the delete queue, which will be deleted on [commit] or
     * [commitInOneTransaction].
     *
     * Attempting to delete a read-only group will result in a failed operation.
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
     * Adds the given [groupsIds] to the delete queue, which will be deleted on [commit] or
     * [commitInOneTransaction].
     *
     * Attempting to delete a read-only group will result in a failed operation.
     */
    fun groupsWithId(vararg groupsIds: Long): GroupsDelete

    /**
     * See [GroupsDelete.groupsWithId].
     */
    fun groupsWithId(groupsIds: Collection<Long>): GroupsDelete

    /**
     * See [GroupsDelete.groupsWithId].
     */
    fun groupsWithId(groupsIds: Sequence<Long>): GroupsDelete

    /**
     * Deletes all of the groups that match the given [where].
     */
    fun groupsWhere(where: Where<GroupsField>?): GroupsDelete

    /**
     * Same as [GroupsDelete.groupsWhere] except you have direct access to all properties of
     * [GroupsFields] in the function parameter. Use this to shorten your code.
     */
    fun groupsWhere(where: GroupsFields.() -> Where<GroupsField>?): GroupsDelete

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
         *
         * ## [commit] vs [commitInOneTransaction]
         *
         * If you used several of the following in one call,
         *
         * - [groups]
         * - [groupsWithId]
         * - [groupsWhere]
         *
         * then this value may be false even if the groups were actually deleted if you used
         * [commit]. Using [commitInOneTransaction] does not have this "issue".
         */
        val isSuccessful: Boolean

        /**
         * True if the [group] has been successfully deleted. False otherwise.
         */
        fun isSuccessful(group: ExistingGroupEntity): Boolean

        /**
         * True if the group with the given [groupId] has been successfully deleted. False otherwise.
         *
         * This is used in conjunction with [GroupsDelete.groupsWithId].
         */
        fun isSuccessful(groupId: Long): Boolean

        /**
         * True if the delete operation using the given [where] was successful.
         *
         * This is used in conjunction with [GroupsDelete.groupsWhere].
         */
        fun isSuccessful(where: Where<GroupsField>): Boolean

        // We have to cast the return type because we are not using recursive generic types.
        override fun redactedCopy(): Result
    }
}

@Suppress("FunctionName")
internal fun GroupsDelete(contacts: Contacts): GroupsDelete = GroupsDeleteImpl(contacts)

private class GroupsDeleteImpl(
    override val contactsApi: Contacts,

    private val groupsIds: MutableSet<Long> = mutableSetOf(),
    private var groupsWhere: Where<GroupsField>? = null,

    override val isRedacted: Boolean = false
) : GroupsDelete {

    private val hasNothingToCommit: Boolean
        get() = groupsIds.isEmpty() && groupsWhere == null

    override fun toString(): String =
        """
            GroupsDelete {
                groupsIds: $groupsIds
                groupsWhere: $groupsWhere
                hasPermission: ${permissions.canUpdateDelete()}
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): GroupsDelete = GroupsDeleteImpl(
        contactsApi, groupsIds, groupsWhere, isRedacted = true
    )

    override fun groups(vararg groups: ExistingGroupEntity) = groups(groups.asSequence())

    override fun groups(groups: Collection<ExistingGroupEntity>) = groups(groups.asSequence())

    override fun groups(groups: Sequence<ExistingGroupEntity>) = groupsWithId(groups.map { it.id })

    override fun groupsWithId(vararg groupsIds: Long) = groupsWithId(groupsIds.asSequence())

    override fun groupsWithId(groupsIds: Collection<Long>) = groupsWithId(groupsIds.asSequence())

    override fun groupsWithId(groupsIds: Sequence<Long>): GroupsDelete = apply {
        this.groupsIds.addAll(groupsIds)
    }

    override fun groupsWhere(where: Where<GroupsField>?): GroupsDelete = apply {
        groupsWhere = where?.redactedCopyOrThis(isRedacted)
    }

    override fun groupsWhere(where: GroupsFields.() -> Where<GroupsField>?) =
        groupsWhere(where(GroupsFields))

    override fun commit(): GroupsDelete.Result {
        onPreExecute()

        return if (!permissions.canUpdateDelete() || hasNothingToCommit) {
            GroupsDeleteAllResult(isSuccessful = false)
        } else {
            val results = mutableMapOf<Long, Boolean>()
            for (groupId in groupsIds) {
                results[groupId] = contentResolver.deleteGroupsWhere(
                    // Attempting to delete a read-only group will result in a "successful" result
                    // even though the group was not actually deleted. The group will be marked as
                    // "deleted" in the local Contacts Provider database but will cause sync adapter
                    // failures. Ultimately, deletion of read-only groups will not be propagated
                    // to the remote sync servers.
                    //
                    // Thus, we manually add the check to match only non-read-only groups.
                    (GroupsFields.Id equalTo groupId) and (GroupsFields.GroupIsReadOnly equalTo false)
                )
            }

            val whereResultMap = mutableMapOf<String, Boolean>()
            groupsWhere?.let {
                whereResultMap[it.toString()] = contentResolver.deleteGroupsWhere(
                    // Attempting to delete a read-only group will result in a "successful" result
                    // even though the group was not actually deleted. The group will be marked as
                    // "deleted" in the local Contacts Provider database but will cause sync adapter
                    // failures. Ultimately, deletion of read-only groups will not be propagated
                    // to the remote sync servers.
                    //
                    // Thus, we manually add the check to match only non-read-only groups.
                    it and (GroupsFields.GroupIsReadOnly equalTo false)
                )
            }

            GroupsDeleteResult(results, whereResultMap)
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }

    override fun commitInOneTransaction(): GroupsDelete.Result {
        onPreExecute()

        return if (!permissions.canUpdateDelete() || hasNothingToCommit) {
            GroupsDeleteAllResult(isSuccessful = false)
        } else {
            val operations = arrayListOf<ContentProviderOperation>()

            if (groupsIds.isNotEmpty()) {
                deleteOperationFor(
                    // Attempting to delete a read-only group will result in a "successful" result
                    // even though the group was not actually deleted. The group will be marked as
                    // "deleted" in the local Contacts Provider database but will cause sync adapter
                    // failures. Ultimately, deletion of read-only groups will not be propagated
                    // to the remote sync servers.
                    //
                    // Thus, we manually add the check to match only non-read-only groups.
                    (GroupsFields.Id `in` groupsIds) and (GroupsFields.GroupIsReadOnly equalTo false)
                ).let(operations::add)
            }

            groupsWhere?.let {
                deleteOperationFor(
                    // Attempting to delete a read-only group will result in a "successful" result
                    // even though the group was not actually deleted. The group will be marked as
                    // "deleted" in the local Contacts Provider database but will cause sync adapter
                    // failures. Ultimately, deletion of read-only groups will not be propagated
                    // to the remote sync servers.
                    //
                    // Thus, we manually add the check to match only non-read-only groups.
                    it and (GroupsFields.GroupIsReadOnly equalTo false)
                ).let(operations::add)
            }

            GroupsDeleteAllResult(
                isSuccessful = contentResolver.applyBatch(operations).deleteSuccess
            )
        }
            .redactedCopyOrThis(isRedacted)
            .also { onPostExecute(contactsApi, it) }
    }
}

private fun ContentResolver.deleteGroupsWhere(where: Where<GroupsField>): Boolean =
    applyBatch(deleteOperationFor(where)).deleteSuccess

private fun deleteOperationFor(where: Where<GroupsField>): ContentProviderOperation =
    ContentProviderOperation.newDelete(Table.Groups.uri)
        .withSelection(where)
        .build()

private class GroupsDeleteResult private constructor(
    private val groupIdsResultMap: Map<Long, Boolean>,
    private var whereResultMap: Map<String, Boolean>,
    override val isRedacted: Boolean
) : GroupsDelete.Result {

    constructor(
        groupIdsResultMap: Map<Long, Boolean>,
        whereResultMap: Map<String, Boolean>
    ) : this(groupIdsResultMap, whereResultMap, false)

    override fun toString(): String =
        """
            GroupsDelete.Result {
                isSuccessful: $isSuccessful
                groupIdsResultMap: $groupIdsResultMap
                whereResultMap: $whereResultMap
                isRedacted: $isRedacted
            }
        """.trimIndent()

    override fun redactedCopy(): GroupsDelete.Result = GroupsDeleteResult(
        groupIdsResultMap = groupIdsResultMap,
        whereResultMap = whereResultMap.redactedStringKeys(),
        isRedacted = true
    )

    override val isSuccessful: Boolean by lazy {
        if (groupIdsResultMap.isEmpty() && whereResultMap.isEmpty()
        ) {
            // Deleting nothing is NOT successful.
            false
        } else {
            // A set has failure if it is NOT empty and one of its entries is false.
            val hasIdFailure = groupIdsResultMap.any { !it.value }
            val hasWhereFailure = whereResultMap.any { !it.value }
            !hasIdFailure && !hasWhereFailure
        }
    }

    override fun isSuccessful(group: ExistingGroupEntity): Boolean = isSuccessful(group.id)

    override fun isSuccessful(groupId: Long): Boolean =
        groupIdsResultMap.getOrElse(groupId) { false }

    override fun isSuccessful(where: Where<GroupsField>): Boolean =
        whereResultMap.getOrElse(where.toString()) { false }
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

    override fun isSuccessful(groupId: Long): Boolean = isSuccessful

    override fun isSuccessful(where: Where<GroupsField>): Boolean = isSuccessful
}
