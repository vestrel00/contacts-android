package contacts.sample.cheatsheet.accounts.java;

import android.accounts.Account;
import android.app.Activity;

import java.util.List;

import contacts.core.ContactsFactory;

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
}