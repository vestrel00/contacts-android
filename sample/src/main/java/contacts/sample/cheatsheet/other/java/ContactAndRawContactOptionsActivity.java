package contacts.sample.cheatsheet.other.java;

import android.app.Activity;

import contacts.core.ContactsFactory;
import contacts.core.entities.*;
import contacts.core.util.*;

public class ContactAndRawContactOptionsActivity extends Activity {

    Options getContactOptions(Contact contact) {
        return contact.getOptions();
    }

    Options getContactOptionsFromDb(Contact contact) {
        return ContactOptionsKt.options(contact, ContactsFactory.create(this));
    }

    Options getRawContactOptionsFromDb(RawContact rawContact) {
        return RawContactOptionsKt.options(rawContact, ContactsFactory.create(this));
    }

    Boolean setContactOptions(Contact contact, MutableOptions options) {
        return ContactOptionsKt.setOptions(contact, ContactsFactory.create(this), options);
    }

    Boolean setRawContactOptions(RawContact rawContact, MutableOptions options) {
        return RawContactOptionsKt.setOptions(rawContact, ContactsFactory.create(this), options);
    }
}