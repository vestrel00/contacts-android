package contacts.sample.cheatsheet.other.java;

import android.app.Activity;

import java.util.List;

import contacts.core.ContactsFactory;
import contacts.core.aggregationexceptions.ContactLink;
import contacts.core.aggregationexceptions.ContactUnlink;
import contacts.core.entities.Contact;
import contacts.core.util.ContactLinksKt;

public class ContactLinksActivity extends Activity {

    ContactLink.Result linkContacts(List<Contact> contacts) {
        return ContactsFactory
                .create(this)
                .aggregationExceptions()
                .linkContacts()
                .contacts(contacts)
                .commit();
    }

    ContactUnlink.Result unlinkContact(Contact contact) {
        return ContactsFactory
                .create(this)
                .aggregationExceptions()
                .unlinkContact()
                .contact(contact)
                .commit();
    }

    ContactLink.Result linkContactsDirect(List<Contact> contacts) {
        return ContactLinksKt.linkDirect(contacts, ContactsFactory.create(this));
    }

    ContactUnlink.Result unlinkContactDirect(Contact contact) {
        return ContactLinksKt.unlinkDirect(contact, ContactsFactory.create(this));
    }
}