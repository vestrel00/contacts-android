package contacts.sample.cheatsheet.profile.java;

import android.app.Activity;

import contacts.core.ContactsFactory;
import contacts.core.profile.ProfileDelete;

public class DeleteProfileActivity extends Activity {

    ProfileDelete.Result deleteProfile() {
        return ContactsFactory.create(this).profile().delete().contact().commit();
    }
}