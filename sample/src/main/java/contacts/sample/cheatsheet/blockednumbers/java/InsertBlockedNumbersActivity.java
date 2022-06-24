package contacts.sample.cheatsheet.blockednumbers.java;

import android.app.Activity;

import contacts.core.ContactsFactory;
import contacts.core.blockednumbers.BlockedNumbersInsert;
import contacts.core.entities.NewBlockedNumber;

public class InsertBlockedNumbersActivity extends Activity {

    BlockedNumbersInsert.Result insertBlockedNumber() {
        return ContactsFactory.create(this)
                .blockedNumbers()
                .insert()
                .blockedNumbers(new NewBlockedNumber("555-555-5555"))
                .commit();
    }
}