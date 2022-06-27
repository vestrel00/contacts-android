package contacts.sample.cheatsheet.other.java;

import android.app.Activity;
import android.content.Intent;

import java.util.List;

import contacts.core.entities.Contact;
import contacts.core.util.ContactShareKt;

public class ContactShareActivity extends Activity {

    void shareContact(Contact contact) {
        Intent shareIntent = ContactShareKt.shareVCardIntent(contact);
        if (shareIntent != null) {
            startActivity(Intent.createChooser(shareIntent, null));
        }
    }

    void shareContacts(List<Contact> contacts) {
        Intent shareIntent = ContactShareKt.shareMultiVCardIntent(contacts);
        if (shareIntent != null) {
            startActivity(Intent.createChooser(shareIntent, null));
        }
    }
}