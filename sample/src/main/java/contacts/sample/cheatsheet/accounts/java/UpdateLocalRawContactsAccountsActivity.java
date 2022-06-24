package contacts.sample.cheatsheet.accounts.java;

import android.accounts.Account;
import android.app.Activity;

import contacts.core.ContactsFactory;
import contacts.core.accounts.AccountsLocalRawContactsUpdate;
import contacts.core.entities.RawContact;

public class UpdateLocalRawContactsAccountsActivity extends Activity {

    AccountsLocalRawContactsUpdate.Result associateLocalRawContactToAccount(
            RawContact localRawContact, Account account
    ) {
        return ContactsFactory.create(this)
                .accounts()
                .updateLocalRawContactsAccount()
                .addToAccount(account)
                .localRawContacts(localRawContact)
                .commit();
    }
}