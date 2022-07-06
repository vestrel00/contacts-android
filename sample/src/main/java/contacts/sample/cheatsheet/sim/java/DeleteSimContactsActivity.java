package contacts.sample.cheatsheet.sim.java;

import android.app.Activity;

import contacts.core.ContactsFactory;
import contacts.core.entities.SimContact;
import contacts.core.sim.SimContactsDelete;

public class DeleteSimContactsActivity extends Activity {

    SimContactsDelete.Result deleteSimContact(SimContact simContact) {
        return ContactsFactory.create(this).sim().delete().simContacts(simContact).commit();
    }

    SimContactsDelete.Result deleteSimContactWithNameAndNumber(String name, String number) {
        return ContactsFactory.create(this).sim().delete().simContact(name, number).commit();
    }
}