package contacts.sample.cheatsheet.groups.java;

import static contacts.core.WhereKt.*;

import android.accounts.Account;
import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import contacts.core.*;
import contacts.core.entities.*;

public class QueryGroupsActivity extends Activity {

    List<Group> getAllGroupsFromAllAccounts() {
        return ContactsFactory.create(this).groups().query().find();
    }

    List<Group> getGroupsFromAccount(Account account) {
        return ContactsFactory.create(this).groups().query().accounts(account).find();
    }

    List<Group> getGroupsById(List<Long> groupsIds) {
        return ContactsFactory.create(this)
                .groups()
                .query()
                .where(in(GroupsFields.Id, groupsIds))
                .find();
    }

    List<Group> getGroupsByTitle(String title) {
        return ContactsFactory.create(this)
                .groups()
                .query()
                .where(contains(GroupsFields.Title, title))
                .find();
    }

    List<Group> getGroupsByGroupMembership(List<GroupMembership> groupMemberships) {
        List<Long> groupsIds = new ArrayList<>();
        for (GroupMembership groupMembership : groupMemberships) {
            if (groupMembership.getGroupId() != null) {
                groupsIds.add(groupMembership.getGroupId());
            }
        }

        return ContactsFactory.create(this)
                .groups()
                .query()
                .where(in(GroupsFields.Id, groupsIds))
                .find();
    }

    List<Group> getSystemGroups(Account account) {
        return ContactsFactory.create(this)
                .groups()
                .query()
                .accounts(account)
                .where(isNotNull(GroupsFields.SystemId))
                .find();
    }

    List<Group> getUserCreatedGroups(Account account) {
        List<Group> groups = ContactsFactory.create(this)
                .groups()
                .query()
                .accounts(account)
                .find();

        List<Group> userCreatedGroups = new ArrayList<>();
        for (Group group : groups) {
            if (!group.isSystemGroup()) {
                userCreatedGroups.add(group);
            }
        }

        return userCreatedGroups;
    }
}