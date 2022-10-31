package contacts.sample.cheatsheet.other.java;

import android.app.Activity;

import contacts.core.entities.*;
import contacts.core.util.*;

public class ContactAndRawContactOptionsActivity extends Activity {

    Options getContactOptions(Contact contact) {
        return contact.getOptions();
    }

    void setContactOptions(Contact contact) {
        NewOptions newOptions = new NewOptions();
        newOptions.setStarred(true);
        newOptions.setCustomRingtone(null);
        newOptions.setSendToVoicemail(false);

        MutableContact mutableContact = contact.mutableCopy();

        ContactDataKt.setOptions(mutableContact, newOptions);
    }

    Options getRawContactOptions(RawContact rawContact) {
        return rawContact.getOptions();
    }

    void setRawContactOptions(RawContact rawContact) {
        NewOptions newOptions = new NewOptions();
        newOptions.setStarred(true);
        newOptions.setCustomRingtone(null);
        newOptions.setSendToVoicemail(false);

        MutableRawContact mutableRawContact = rawContact.mutableCopy();

        MutableRawContactDataKt.setOptions(mutableRawContact, newOptions);
    }
}