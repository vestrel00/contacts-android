package contacts.sample.cheatsheet.accounts.java;

import android.accounts.Account;
import android.app.Activity;

import java.util.List;

import contacts.core.ContactsFactory;
import contacts.core.entities.RawContact;

public class QueryAccountsActivity extends Activity {

    List<Account> getAllAccounts() {
        return ContactsFactory.create(this).accounts().query().find();
    }

    List<Account> getAllGoogleAccounts() {
        return ContactsFactory.create(this)
                .accounts()
                .query()
                .withTypes("com.google")
                .find();
    }

    Account getRawContactAccount(RawContact rawContact) {
        return ContactsFactory.create(this)
                .accounts()
                .query()
                .associatedWith(rawContact)
                .find()
                .get(0);
    }
}