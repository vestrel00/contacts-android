package contacts.sample.cheatsheet.basics.java;

import android.app.Activity;

import contacts.core.ContactsFactory;
import contacts.core.LookupQuery;
import contacts.core.entities.Contact;

public class QueryContactsByLookupKeyActivity extends Activity {

    Contact getContactByLookupKey(String lookupKey) {
        LookupQuery.Result result = ContactsFactory.create(this)
                .lookupQuery()
                .whereLookupKeyMatches(lookupKey)
                .find();
        return !result.isEmpty() ? result.get(0) : null;
    }

    Contact getContactByLookupKeyWithId(String lookupKey, long contactId) {
        LookupQuery.Result result = ContactsFactory.create(this)
                .lookupQuery()
                .whereLookupKeyWithIdMatches(new LookupQuery.LookupKeyWithId(lookupKey, contactId))
                .find();
        return !result.isEmpty() ? result.get(0) : null;
    }
}