package contacts.sample.cheatsheet.groups.java;

import android.accounts.Account;
import android.app.Activity;

import java.util.List;

import contacts.core.ContactsFactory;
import contacts.core.entities.NewGroup;
import contacts.core.groups.GroupsInsert;

public class InsertGroupsActivity extends Activity {

    GroupsInsert.Result insertGroup(String title, Account account) {
        return ContactsFactory.create(this).groups().insert().group(title, account).commit();
    }

    GroupsInsert.Result insertGroups(List<NewGroup> groups) {
        return ContactsFactory.create(this).groups().insert().groups(groups).commit();
    }
}