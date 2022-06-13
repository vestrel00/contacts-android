package contacts.sample.cheatsheet.basics.java;

import static contacts.core.OrderByKt.*;

import android.app.Activity;

import java.util.List;

import contacts.core.BroadQuery;
import contacts.core.ContactsFactory;
import contacts.core.ContactsFields;
import contacts.core.Fields;
import contacts.core.entities.Contact;

public class QueryContactsActivity extends Activity {

    List<Contact> getAllContacts() {
        return ContactsFactory.create(this).broadQuery().find();
    }

    List<Contact> getAllContactsWithFavoritesFirstOrderedByDisplayName() {
        return ContactsFactory.create(this)
                .broadQuery()
                .orderBy(
                        desc(ContactsFields.Options.Starred),
                        asc(ContactsFields.DisplayNamePrimary, true)
                )
                .find();
    }

    List<Contact> getContactsWithAnyDataThatPartiallyMatches(String text) {
        return ContactsFactory.create(this)
                .broadQuery()
                .match(BroadQuery.Match.ANY)
                .wherePartiallyMatches(text)
                .find();
    }

    List<Contact> getContactsWithEmailOrDisplayNameThatPartiallyMatches(String text) {
        return ContactsFactory.create(this)
                .broadQuery()
                .match(BroadQuery.Match.EMAIL)
                .wherePartiallyMatches(text)
                .find();
    }

    List<Contact> getContactsWithPhoneOrDisplayNameThatPartiallyMatches(String text) {
        return ContactsFactory.create(this)
                .broadQuery()
                .match(BroadQuery.Match.PHONE)
                .wherePartiallyMatches(text)
                .find();
    }

    List<Contact> getAllContactsIncludingOnlyDisplayNameAndEmailAddresses() {
        return ContactsFactory.create(this)
                .broadQuery()
                .include(
                        Fields.Contact.DisplayNamePrimary,
                        Fields.Email.Address
                )
                .find();
    }

    List<Contact> get25Contacts() {
        return ContactsFactory.create(this)
                .broadQuery()
                .limit(25)
                .find();
    }

    List<Contact> get25ContactsSkippingTheFirst25() {
        return ContactsFactory.create(this)
                .broadQuery()
                .offset(25)
                .limit(25)
                .find();
    }
}