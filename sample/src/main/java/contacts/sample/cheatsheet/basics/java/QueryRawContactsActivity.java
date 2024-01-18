package contacts.sample.cheatsheet.basics.java;

import static contacts.core.WhereKt.equalTo;
import static contacts.core.WhereKt.isNotNullOrEmpty;

import android.accounts.Account;
import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import contacts.core.ContactsFactory;
import contacts.core.Fields;
import contacts.core.RawContactsFields;
import contacts.core.entities.RawContact;

public class QueryRawContactsActivity extends Activity {

    List<RawContact> getAllRawContacts() {
        return ContactsFactory.create(this).rawContactsQuery().find();
    }

    List<RawContact> getAllFavoriteRawContacts() {
        return ContactsFactory.create(this)
                .rawContactsQuery()
                .rawContactsWhere(
                        new ArrayList<>(),
                        equalTo(RawContactsFields.Options.Starred, true)
                )
                .find();
    }

    List<RawContact> getRawContactsForAccount(Account account) {
        List<Account> accounts = new ArrayList<>();
        accounts.add(account);
        return ContactsFactory.create(this)
                .rawContactsQuery()
                .rawContactsWhere(accounts, null)
                .find();
    }

    List<RawContact> getRawContactsForAllGoogleAccounts() {
        return ContactsFactory.create(this)
                .rawContactsQuery()
                .rawContactsWhere(
                        new ArrayList<>(),
                        equalTo(RawContactsFields.AccountType, "com.google")
                )
                .find();
    }

    List<RawContact> getRawContactsThatHasANote() {
        return ContactsFactory.create(this)
                .rawContactsQuery()
                .where(isNotNullOrEmpty(Fields.Note.Note))
                .find();
    }
    
    @Nullable
    RawContact getRawContactById(Long rawContactId) {
        RawContactsQuery.Result result = ContactsFactory.create(this)
                .rawContactsQuery()
                .rawContactsWhere(
                        new ArrayList<>(),
                        equalTo(RawContactsFields.Id, rawContactId)
                )
                // alternatively, .where(equalTo(Fields.RawContact.Id, rawContactId))
                .limit(1)
                .find();
        return !result.isEmpty() ? result.get(0) : null;
    }
}