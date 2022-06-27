package contacts.sample.cheatsheet.other.java;

import android.app.Activity;

import java.util.List;

import contacts.core.ContactsFactory;
import contacts.core.entities.Contact;
import contacts.core.util.*;

public class ContactLinksActivity extends Activity {

    ContactLinkResult linkContacts(List<Contact> contacts) {
        return ContactLinksKt.link(contacts, ContactsFactory.create(this));
    }

    ContactUnlinkResult unlinkContact(Contact contact) {
        return ContactLinksKt.unlink(contact, ContactsFactory.create(this));
    }
}