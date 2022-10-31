package contacts.sample.cheatsheet.groups.kotlin

import android.accounts.Account
import android.app.Activity
import contacts.core.*
import contacts.core.entities.*

class QueryGroupsActivity : Activity() {

    fun getAllGroupsFromAllAccounts(): List<Group> = Contacts(this).groups().query().find()

    fun getGroupsFromAccount(account: Account): List<Group> =
        Contacts(this).groups().query().accounts(account).find()

    fun getGroupsById(groupsIds: List<Long>): List<Group> = Contacts(this)
        .groups()
        .query()
        .where { Id `in` groupsIds }
        .find()

    fun getGroupsByTitle(title: String): List<Group> = Contacts(this)
        .groups()
        .query()
        .where { Title contains title }
        .find()

    fun getGroupsOfGroupMemberships(groupMemberships: List<GroupMembership>): List<Group> =
        Contacts(this)
            .groups()
            .query()
            .where { Id `in` groupMemberships.mapNotNull { it.groupId } }
            .find()

    fun getSystemGroups(account: Account): List<Group> = Contacts(this)
        .groups()
        .query()
        .accounts(account)
        .where { SystemId.isNotNull() }
        .find()

    fun getUserCreatedGroups(account: Account): List<Group> = Contacts(this)
        .groups()
        .query()
        .accounts(account)
        .find()
        .filter { !it.isSystemGroup }
}