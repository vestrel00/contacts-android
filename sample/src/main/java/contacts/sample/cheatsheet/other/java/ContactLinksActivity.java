package contacts.sample.cheatsheet.other.java;

import android.app.Activity;

import java.util.List;

import contacts.core.ContactsFactory;
import contacts.core.aggregationexceptions.ContactLink;
import contacts.core.aggregationexceptions.ContactUnlink;
import contacts.core.entities.Contact;
import contacts.core.util.ContactLinksKt;

public class ContactLinksActivity extends Activity {

    ContactLink.Result link(List<Contact> contacts) {
        return ContactsFactory
                .create(this)
                .aggregationExceptions()
                .link()
                .contacts(contacts)
                .commit();
    }

    ContactUnlink.Result unlink(Contact contact) {
        return ContactsFactory
                .create(this)
                .aggregationExceptions()
                .unlink()
                .contact(contact)
                .commit();
    }

    ContactLink.Result linkDirect(List<Contact> contacts) {
        return ContactLinksKt.linkDirect(contacts, ContactsFactory.create(this));
    }

    ContactUnlink.Result unlinkDirect(Contact contact) {
        return ContactLinksKt.unlinkDirect(contact, ContactsFactory.create(this));
    }
}