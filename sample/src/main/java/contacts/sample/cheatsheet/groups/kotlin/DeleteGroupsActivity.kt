package contacts.sample.cheatsheet.groups.kotlin

import android.app.Activity
import contacts.core.*
import contacts.core.entities.Group
import contacts.core.groups.GroupsDelete

class DeleteGroupsActivity : Activity() {

    fun deleteGroups(groups: List<Group>): GroupsDelete.Result =
        Contacts(this).groups().delete().groups(groups).commit()

    fun deleteGroupWithId(groupId: Long): GroupsDelete.Result =
        Contacts(this).groups().delete().groupsWithId(groupId).commit()

    fun deleteUserCreatedGroupFromAllGoogleAccounts(): GroupsDelete.Result = Contacts(this)
        .groups()
        .delete()
        .groupsWhere { AccountType equalTo "com.google" }
        .commit()
}