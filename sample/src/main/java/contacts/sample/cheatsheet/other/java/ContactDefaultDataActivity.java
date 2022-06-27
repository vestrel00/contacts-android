package contacts.sample.cheatsheet.other.java;

import android.app.Activity;

import contacts.core.ContactsFactory;
import contacts.core.entities.Phone;
import contacts.core.util.DefaultContactDataKt;

public class ContactDefaultDataActivity extends Activity {

    Boolean isPhoneTheDefaultPhone(Phone phone) {
        return phone.isDefault();
    }

    Boolean setPhoneAsDefault(Phone phone) {
        return DefaultContactDataKt.setAsDefault(phone, ContactsFactory.create(this));
    }

    Boolean clearDefaultPhone(Phone phone) {
        return DefaultContactDataKt.clearDefault(phone, ContactsFactory.create(this));
    }
}