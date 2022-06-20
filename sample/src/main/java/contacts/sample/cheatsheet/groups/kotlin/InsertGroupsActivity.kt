package contacts.sample.cheatsheet.groups.kotlin

import android.accounts.Account
import android.app.Activity
import contacts.core.Contacts
import contacts.core.entities.NewGroup
import contacts.core.groups.GroupsInsert

class InsertGroupsActivity : Activity() {

    fun insertGroup(title: String, account: Account): GroupsInsert.Result =
        Contacts(this).groups().insert().group(title, account).commit()

    fun insertGroups(groups: List<NewGroup>): GroupsInsert.Result =
        Contacts(this).groups().insert().groups(groups).commit()
}