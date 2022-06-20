package contacts.sample.cheatsheet.groups.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.entities.Group
import contacts.core.groups.GroupsUpdate

class UpdateGroupsActivity : Activity() {

    fun updateGroup(group: Group): GroupsUpdate.Result = Contacts(this)
        .groups()
        .update()
        .groups(
            group.mutableCopy {
                title = "Bad love"
            }
        )
        .commit()
}