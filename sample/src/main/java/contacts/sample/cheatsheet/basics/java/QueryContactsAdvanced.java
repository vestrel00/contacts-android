package contacts.sample.cheatsheet.basics.java;

import static contacts.core.WhereKt.*;
import static contacts.core.util.ContactLookupKeyKt.lookupKeyIn;

import android.accounts.Account;
import android.app.Activity;

import java.util.List;

import contacts.core.ContactsFactory;
import contacts.core.Fields;
import contacts.core.entities.Contact;

public class QueryContactsAdvanced extends Activity {

    Contact getContactById(Long contactId) {
        return ContactsFactory.create(this)
                .query()
                .where(
                        equalTo(Fields.Contact.Id, contactId)
                )
                .find()
                .get(0);
    }

    List<Contact> getContactByLookupKey(String lookupKey) {
        return ContactsFactory.create(this)
                .query()
                .where(
                        lookupKeyIn(Fields.Contact, lookupKey)
                )
                .find();
    }

    List<Contact> getAllContactsForAGoogleAccount() {
        return ContactsFactory.create(this)
                .query()
                .accounts(new Account("email@gmail.com", "com.google"))
                .find();
    }

    List<Contact> getOnlyFavoriteContacts() {
        return ContactsFactory.create(this)
                .query()
                .where(
                        equalTo(Fields.Contact.Options.Starred, true)
                )
                .find();
    }

    List<Contact> getContactsPartiallyMatchingDisplayName() {
        return ContactsFactory.create(this)
                .query()
                .where(
                        contains(Fields.Contact.DisplayNamePrimary, "alex")
                )
                .find();
    }

    List<Contact> getContactsWithAtLeastOneGmailEmail() {
        return ContactsFactory.create(this)
                .query()
                .where(
                        endsWith(Fields.Email.Address, "@gmail.com")
                )
                .find();
    }

    List<Contact> getContactsWithAtLeastOnePhoneNumber() {
        return ContactsFactory.create(this)
                .query()
                .where(
                        isNotNullOrEmpty(Fields.Phone.Number)
                        // or equalTo(Fields.Contact.HasPhoneNumber, true)
                )
                .find();
    }

    List<Contact> getContactsWithAtLeastOnePhoneNumberAndEmail() {
        return ContactsFactory.create(this)
                .query()
                .where(
                        and(
                                isNotNullOrEmpty(Fields.Phone.Number),
                                // or equalTo(Fields.Contact.HasPhoneNumber, true),
                                isNotNullOrEmpty(Fields.Email.Address)
                        )
                )
                .find();
    }
}