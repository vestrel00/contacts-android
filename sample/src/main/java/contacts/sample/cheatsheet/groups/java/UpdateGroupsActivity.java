package contacts.sample.cheatsheet.groups.java;

import android.app.Activity;

import contacts.core.ContactsFactory;
import contacts.core.entities.*;
import contacts.core.groups.GroupsUpdate;

public class UpdateGroupsActivity extends Activity {

    GroupsUpdate.Result updateGroup(Group group) {
        MutableGroup mutableGroup = group.mutableCopy();
        mutableGroup.setTitle("Bad love");

        return ContactsFactory.create(this)
                .groups()
                .update()
                .groups(mutableGroup)
                .commit();
    }
}