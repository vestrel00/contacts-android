package contacts.sample.cheatsheet.basics.java;

import android.app.Activity;

import contacts.core.ContactsFactory;
import contacts.core.Update;
import contacts.core.entities.*;
import contacts.core.util.ContactDataKt;

public class UpdateContactsActivity extends Activity {

    Update.Result addEmail(Contact contact) {
        MutableContact mutableContact = contact.mutableCopy();
        ContactDataKt.addEmail(mutableContact, new NewEmail(
                EmailEntity.Type.CUSTOM,
                "Personal",
                "321@xyz.com"
        ));

        return ContactsFactory.create(this)
                .update()
                .contacts(mutableContact)
                .commit();
    }

    Update.Result addEmail(RawContact rawContact) {
        MutableRawContact mutableRawContact = rawContact.mutableCopy();
        mutableRawContact.getEmails().add(new NewEmail(
                EmailEntity.Type.CUSTOM,
                "Personal",
                "321@xyz.com"
        ));

        return ContactsFactory.create(this)
                .update()
                .rawContacts(mutableRawContact)
                .commit();
    }

    Update.Result addAnniversary(Contact contact) {
        MutableContact mutableContact = contact.mutableCopy();
        ContactDataKt.addEvent(mutableContact, new NewEvent(
                EventEntity.Type.ANNIVERSARY,
                null,
                EventDate.from(2016, 6, 14)
        ));

        return ContactsFactory.create(this)
                .update()
                .contacts(mutableContact)
                .commit();
    }

    Update.Result setFullName(RawContact rawContact) {
        NewName name = new NewName();
        name.setPrefix("Mr.");
        name.setGivenName("Small");
        name.setMiddleName("Bald");
        name.setFamilyName("Eagle");
        name.setSuffix("Sr");

        MutableRawContact mutableRawContact = rawContact.mutableCopy();
        mutableRawContact.setName(name);

        return ContactsFactory.create(this)
                .update()
                .rawContacts(mutableRawContact)
                .commit();
    }

    Update.Result setGivenName(RawContact rawContact) {
        MutableRawContact mutableRawContact = rawContact.mutableCopy();
        if (mutableRawContact.getName() != null) {
            mutableRawContact.getName().setGivenName("Greg");
        } else {
            NewName name = new NewName();
            name.setGivenName("Greg");
            mutableRawContact.setName(name);
        }

        return ContactsFactory.create(this)
                .update()
                .rawContacts(mutableRawContact)
                .commit();
    }

    Update.Result removeGmailEmails(Contact contact) {
        MutableContact mutableContact = contact.mutableCopy();
        for (MutableEmailEntity email : ContactDataKt.emailList(mutableContact)) {
            String emailAddress = email.getAddress();
            if (emailAddress != null && emailAddress.toLowerCase().endsWith("@gmail.com")) {
                ContactDataKt.removeEmail(mutableContact, email);
            }
        }

        return ContactsFactory.create(this)
                .update()
                .contacts(mutableContact)
                .commit();
    }

    Update.Result removeEmailsAndPhones(Contact contact) {
        MutableContact mutableContact = contact.mutableCopy();
        ContactDataKt.removeAllEmails(mutableContact);
        ContactDataKt.removeAllPhones(mutableContact);

        return ContactsFactory.create(this)
                .update()
                .contacts(mutableContact)
                .commit();
    }
}