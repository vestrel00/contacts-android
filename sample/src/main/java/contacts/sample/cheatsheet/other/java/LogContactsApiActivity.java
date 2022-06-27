package contacts.sample.cheatsheet.other.java;

import android.app.Activity;

import contacts.core.*;
import contacts.core.log.AndroidLogger;

public class LogContactsApiActivity extends Activity {

    Contacts createContactsApiWithLoggingEnabled(Boolean redactLogMessages) {
        return ContactsFactory.create(this, new AndroidLogger(redactLogMessages));
    }
}