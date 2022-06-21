package contacts.sample.cheatsheet.profile.java;

import android.app.Activity;

import contacts.core.ContactsFactory;
import contacts.core.entities.Contact;

public class QueryProfileActivity extends Activity {

    Contact getProfile() {
        return ContactsFactory.create(this).profile().query().find().getContact();
    }
}