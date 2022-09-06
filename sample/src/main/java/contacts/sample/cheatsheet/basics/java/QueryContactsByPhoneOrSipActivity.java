package contacts.sample.cheatsheet.basics.java;

import android.app.Activity;

import java.util.List;

import contacts.core.ContactsFactory;
import contacts.core.PhoneLookupQuery;
import contacts.core.entities.Contact;

public class QueryContactsByPhoneOrSipActivity extends Activity {

    List<Contact> getContactsWithPhoneNumberThatExactlyMatches(String text) {
        return ContactsFactory.create(this)
                .phoneLookupQuery()
                .match(PhoneLookupQuery.Match.PHONE)
                .whereExactlyMatches(text)
                .find();
    }

    List<Contact> getContactsWithEmailOrDisplayNameThatPartiallyMatches(String text) {
        return ContactsFactory.create(this)
                .phoneLookupQuery()
                .match(PhoneLookupQuery.Match.SIP)
                .whereExactlyMatches(text)
                .find();
    }
}