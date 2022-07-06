package contacts.sample.cheatsheet.groups.java;

import static contacts.core.WhereKt.equalTo;

import android.app.Activity;

import java.util.List;

import contacts.core.ContactsFactory;
import contacts.core.GroupsFields;
import contacts.core.entities.Group;
import contacts.core.groups.GroupsDelete;

public class DeleteGroupsActivity extends Activity {

    GroupsDelete.Result deleteGroups(List<Group> groups) {
        return ContactsFactory.create(this).groups().delete().groups(groups).commit();
    }

    GroupsDelete.Result deleteGroupWithId(long groupId) {
        return ContactsFactory.create(this).groups().delete().groupsWithId(groupId).commit();
    }

    GroupsDelete.Result deleteUserCreatedGroupFromAllGoogleAccounts() {
        return ContactsFactory.create(this)
                .groups()
                .delete()
                .groupsWhere(equalTo(GroupsFields.AccountType, "com.google"))
                .commit();
    }
}