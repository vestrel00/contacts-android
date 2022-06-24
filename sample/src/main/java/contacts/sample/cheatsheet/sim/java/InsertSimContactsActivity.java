package contacts.sample.cheatsheet.sim.java;

import android.app.Activity;

import contacts.core.ContactsFactory;
import contacts.core.entities.NewSimContact;
import contacts.core.sim.SimContactsInsert;

public class InsertSimContactsActivity extends Activity {

    SimContactsInsert.Result insertSimContact() {
        return ContactsFactory.create(this)
                .sim()
                .insert()
                .simContacts(new NewSimContact("Mr. Joe", "5555555555"))
                .commit();
    }
}