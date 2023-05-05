package contacts.sample.cheatsheet.accounts.java;

import android.accounts.Account;
import android.app.Activity;

import contacts.core.ContactsFactory;
import contacts.core.accounts.MoveRawContactsAcrossAccounts;
import contacts.core.entities.RawContact;

public class MoveRawContactsAcrossAccountsActivity extends Activity {

    MoveRawContactsAcrossAccounts.Result moveRawContactToAccount(
            RawContact rawContact, Account account
    ) {
        return ContactsFactory.create(this)
                .accounts()
                .move()
                .rawContactsTo(account, rawContact)
                .commit();
    }
}