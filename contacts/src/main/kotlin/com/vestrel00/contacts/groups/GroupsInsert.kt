package com.vestrel00.contacts.groups

import android.accounts.Account
import android.content.Context
import android.provider.ContactsContract
import com.vestrel00.contacts.ContactsPermissions
import com.vestrel00.contacts.accounts.Accounts
import com.vestrel00.contacts.entities.MutableGroup
import com.vestrel00.contacts.entities.operation.GroupOperation

interface GroupsInsert {

    fun groups(vararg groups: MutableGroup): GroupsInsert

    fun groups(groups: Collection<MutableGroup>): GroupsInsert

    fun groups(groups: Sequence<MutableGroup>): GroupsInsert

    fun commit(): Result

    interface Result {

        val groupIds: List<Long>

        val isSuccessful: Boolean

        fun isSuccessful(group: MutableGroup): Boolean

        fun groupId(group: MutableGroup): Long?
    }
}

@Suppress("FunctionName")
internal fun GroupsInsert(context: Context): GroupsInsert = GroupsInsertImpl(
    context,
    Accounts(),
    ContactsPermissions(context)
)

private class GroupsInsertImpl(
    private val context: Context,
    private val accounts: Accounts,
    private val permissions: ContactsPermissions,
    private val groups: MutableSet<MutableGroup> = mutableSetOf()
) : GroupsInsert {

    override fun groups(vararg groups: MutableGroup): GroupsInsert =
        groups(groups.asSequence())

    override fun groups(groups: Collection<MutableGroup>): GroupsInsert =
        groups(groups.asSequence())

    override fun groups(groups: Sequence<MutableGroup>): GroupsInsert = apply {
        this.groups.addAll(groups)
    }

    override fun commit(): GroupsInsert.Result {
        val accounts = accounts.allAccounts(context)
        if (accounts.isEmpty() || groups.isEmpty() || !permissions.canInsertUpdateDelete()) {
            return GroupsInsertFailed
        }

        val results = mutableMapOf<MutableGroup, Long?>()
        for (group in groups) {
            results[group] = insertGroup(group.withValidAccount(accounts))
        }
        return GroupsInsertResult(results)
    }

    private fun insertGroup(group: MutableGroup): Long? {
        val operation = GroupOperation().insert(group)

        val results = try {
            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, arrayListOf(operation))
        } catch (exception: Exception) {
            null
        }

        return results?.firstOrNull()?.let { result ->
            val groupUri = result.uri
            val groupId = groupUri.lastPathSegment?.toLongOrNull()
            groupId
        }
    }

    private fun MutableGroup.withValidAccount(accounts: List<Account>): MutableGroup {
        if (!accounts.contains(account)) {
            // We dissuade consumers from using the copy method. However, we know what we are doing
            // so we make an exception here =)
            return this.copy(account = accounts.first())
        }

        return this
    }
}

private class GroupsInsertResult(private val groupsMap: Map<MutableGroup, Long?>) :
    GroupsInsert.Result {

    override val groupIds: List<Long> by lazy {
        groupsMap.asSequence()
            .filter { it.value != null }
            .map { it.value!! }
            .toList()
    }

    override val isSuccessful: Boolean by lazy { groupsMap.all { it.value != null } }

    override fun isSuccessful(group: MutableGroup): Boolean = groupId(group) != null

    override fun groupId(group: MutableGroup): Long? = groupsMap.getOrElse(group) { null }
}

private object GroupsInsertFailed : GroupsInsert.Result {

    override val groupIds: List<Long> = emptyList()

    override val isSuccessful: Boolean = false

    override fun isSuccessful(group: MutableGroup): Boolean = false

    override fun groupId(group: MutableGroup): Long? = null
}