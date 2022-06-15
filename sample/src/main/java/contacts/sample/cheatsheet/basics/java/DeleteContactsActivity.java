package contacts.sample.cheatsheet.basics.java;

import static contacts.core.WhereKt.*;

import android.app.Activity;

import java.util.Set;

import contacts.core.ContactsFactory;
import contacts.core.Delete;
import contacts.core.Fields;
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

    Delete.Result deleteNonFavoriteContactsThatHaveANote() {
        return ContactsFactory.create(this)
                .delete()
                .contactsWhereData(
                        and(
                                equalTo(Fields.Contact.Options.Starred, false),
                                isNotNullOrEmpty(Fields.Note.Note)
                        )
                )
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

    Delete.Result deleteRawContactsInTheSetThatHaveANote(Set<Long> rawContactIds) {
        return ContactsFactory.create(this)
                .delete()
                .rawContactsWhereData(
                        and(
                                in(Fields.RawContact.Id, rawContactIds),
                                isNotNullOrEmpty(Fields.Note.Note)
                        )
                )
                .commit();
    }
}