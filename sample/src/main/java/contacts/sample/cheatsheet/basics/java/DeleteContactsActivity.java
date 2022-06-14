package contacts.sample.cheatsheet.basics.java;

import android.app.Activity;

import contacts.core.ContactsFactory;
import contacts.core.Delete;
import contacts.core.entities.Contact;
import contacts.core.entities.RawContact;

public class DeleteContactsActivity extends Activity {

    Delete.Result deleteContact(Contact contact) {
        return ContactsFactory.create(this)
                .delete()
                .contacts(contact)
                .commit();
    }

    Delete.Result deleteContactWithId(Long contactId) {
        return ContactsFactory.create(this)
                .delete()
                .contactsWithId(contactId)
                .commit();
    }

    Delete.Result deleteRawContact(RawContact rawContact) {
        return ContactsFactory.create(this)
                .delete()
                .rawContacts(rawContact)
                .commit();
    }

    Delete.Result deleteRawContactWithId(Long rawContactId) {
        return ContactsFactory.create(this)
                .delete()
                .rawContactsWithId(rawContactId)
                .commit();
    }
}