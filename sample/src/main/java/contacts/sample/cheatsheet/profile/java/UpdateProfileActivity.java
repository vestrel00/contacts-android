package contacts.sample.cheatsheet.profile.java;

import android.app.Activity;

import contacts.core.ContactsFactory;
import contacts.core.entities.*;
import contacts.core.profile.ProfileUpdate;
import contacts.core.util.ContactDataKt;

public class UpdateProfileActivity extends Activity {

    ProfileUpdate.Result updateProfile(Contact profile) {
        MutableContact mutableProfile = profile.mutableCopy();
        ContactDataKt.setName(mutableProfile, new NewName("I am the phone owner"));
        ContactDataKt.addEmail(mutableProfile, new NewEmail(
                EmailEntity.Type.CUSTOM,
                "Profile Email",
                "phone@owner.com"
        ));
        ContactDataKt.removeAllPhones(mutableProfile);
        ContactDataKt.setOrganization(mutableProfile, (MutableOrganizationEntity) null);

        return ContactsFactory.create(this)
                .profile()
                .update()
                .contact(mutableProfile)
                .commit();
    }
}