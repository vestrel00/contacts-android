package contacts.sample.cheatsheet.sim.java;

import android.app.Activity;

import contacts.core.ContactsFactory;
import contacts.core.entities.*;
import contacts.core.sim.SimContactsUpdate;

public class UpdateSimContactsActivity extends Activity {

    SimContactsUpdate.Result updateSimContact(SimContact simContact) {
        MutableSimContact mutableSimContact = simContact.mutableCopy();
        mutableSimContact.setName("Vandolf");
        mutableSimContact.setNumber("1234567890");

        return ContactsFactory.create(this)
                .sim()
                .update()
                .simContact(simContact, mutableSimContact)
                .commit();
    }
}