package contacts.sample.cheatsheet.accounts.java;

import static contacts.core.WhereKt.equalTo;

import android.accounts.Account;
import android.app.Activity;

import java.util.List;

import contacts.core.*;
import contacts.core.entities.BlankRawContact;

public class QueryAccountsRawContactsActivity extends Activity {

    List<BlankRawContact> getAllRawContacts() {
        return ContactsFactory.create(this).accounts().queryRawContacts().find();
    }

    List<BlankRawContact> getRawContactsForAccount(Account account) {
        return ContactsFactory.create(this)
                .accounts()
                .queryRawContacts()
                .accounts(account)
                .find();
    }

    List<BlankRawContact> getRawContactsForAllGoogleAccounts() {
        return ContactsFactory.create(this)
                .accounts()
                .queryRawContacts()
                .where(equalTo(RawContactsFields.AccountType, "com.google"))
                .find();
    }

    BlankRawContact getRawContactById(Long rawContactId) {
        return ContactsFactory.create(this)
                .accounts()
                .queryRawContacts()
                .where(equalTo(RawContactsFields.Id, rawContactId))
                .find()
                .get(0);
    }
}