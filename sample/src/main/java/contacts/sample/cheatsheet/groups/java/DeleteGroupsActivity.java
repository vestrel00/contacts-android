package contacts.sample.cheatsheet.groups.java;

import android.app.Activity;

import java.util.List;

import contacts.core.ContactsFactory;
import contacts.core.entities.Group;
import contacts.core.groups.GroupsDelete;

public class DeleteGroupsActivity extends Activity {

    GroupsDelete.Result deleteGroups(List<Group> groups) {
        return ContactsFactory.create(this).groups().delete().groups(groups).commit();
    }
}