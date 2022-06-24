package contacts.sample.cheatsheet.sim.java;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import contacts.core.ContactsFactory;
import contacts.core.entities.SimContact;

public class QuerySimContactsActivity extends Activity {

    List<SimContact> getAllSimContacts() {
        return ContactsFactory.create(this).sim().query().find();
    }

    List<SimContact> getAllSimContactsWithPhoneNumber() {
        List<SimContact> simContacts = ContactsFactory.create(this).sim().query().find();
        List<SimContact> simContactsWithPhoneNumber = new ArrayList<>();
        for (SimContact simContact : simContacts) {
            if (simContact.getNumber() != null && !simContact.getNumber().isEmpty()) {
                simContactsWithPhoneNumber.add(simContact);
            }
        }
        return simContactsWithPhoneNumber;
    }
}