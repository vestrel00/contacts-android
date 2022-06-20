package contacts.sample.cheatsheet.groups.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.entities.Group
import contacts.core.groups.GroupsDelete

class DeleteGroupsActivity : Activity() {

    fun deleteGroups(groups: List<Group>): GroupsDelete.Result =
        Contacts(this).groups().delete().groups(groups).commit()
}