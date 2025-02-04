package contacts.sample.cheatsheet.customdata.java;

import static contacts.core.WhereKt.isNotNull;

import android.app.Activity;

import java.util.List;

import contacts.core.*;
import contacts.core.data.*;
import contacts.core.entities.*;
import contacts.core.entities.custom.CustomDataRegistry;
import contacts.entities.custom.multiplenotes.*;

public class IntegrateMultipleNotesCustomDataActivity extends Activity {

    Contacts contacts = ContactsFactory.create(
            this, false, new CustomDataRegistry().register(new MultipleNotesRegistration())
    );

    List<Contact> getContactsWithMultipleNotesCustomData() {
        return contacts
                .query()
                .where(isNotNull(MultipleNotesFields.Note))
                .find();
    }

    Insert.Result insertRawContactWithMultipleNotesCustomData() {
        NewMultipleNotes newMultipleNotes1 = new NewMultipleNotes("First note");
        NewMultipleNotes newMultipleNotes2 = new NewMultipleNotes("Second note");

        NewRawContact newRawContact = new NewRawContact();
        RawContactMultipleNotesKt.addMultipleNotes(newRawContact, contacts, newMultipleNotes1);
        RawContactMultipleNotesKt.addMultipleNotes(newRawContact, contacts, newMultipleNotes2);

        return contacts
                .insert()
                .rawContacts(newRawContact)
                .commit();
    }

    Update.Result updateRawContactMultipleNotesCustomData(RawContact rawContact) {
        MutableRawContact mutableRawContact = rawContact.mutableCopy();
        MutableMultipleNotesEntity mutableMultipleNotes =
                RawContactMultipleNotesKt.multipleNotesList(mutableRawContact, contacts).get(0);
        if (mutableMultipleNotes != null) {
            mutableMultipleNotes.setNote("A note");
        }

        return contacts
                .update()
                .rawContacts(mutableRawContact)
                .commit();
    }

    Update.Result deleteMultipleNotesCustomDataFromRawContact(RawContact rawContact) {
        MutableRawContact mutableRawContact = rawContact.mutableCopy();
        RawContactMultipleNotesKt.removeAllMultipleNotes(mutableRawContact, contacts);

        return contacts
                .update()
                .rawContacts(mutableRawContact)
                .commit();
    }

    List<MultipleNotes> getAllMultipleNotes() {
        return MultipleNotesDataQueryKt.multipleNotes(contacts.data().query()).find();
    }

    DataUpdate.Result updateMultipleNotes(MutableMultipleNotes multipleNotes) {
        return contacts.data().update().data(multipleNotes).commit();
    }

    DataDelete.Result deleteMultipleNotes(MultipleNotes multipleNotes) {
        return contacts.data().delete().data(multipleNotes).commit();
    }
}