package contacts.sample.cheatsheet.customdata.java;

import static contacts.core.WhereKt.isNotNull;

import android.app.Activity;

import java.util.List;

import contacts.core.*;
import contacts.core.data.*;
import contacts.core.entities.*;
import contacts.core.entities.custom.CustomDataRegistry;
import contacts.entities.custom.gender.*;

public class IntegrateGenderCustomDataActivity extends Activity {

    Contacts contacts = ContactsFactory.create(
            this, false, new CustomDataRegistry().register(new GenderRegistration())
    );

    List<Contact> getContactsWithGenderCustomData() {
        return contacts
                .query()
                .where(isNotNull(GenderFields.Type))
                .find();
    }

    Insert.Result insertRawContactWithGenderCustomData() {
        NewGender newGender = new NewGender(GenderEntity.Type.MALE);

        NewRawContact newRawContact = new NewRawContact();
        RawContactGenderKt.setGender(newRawContact, contacts, newGender);

        return contacts
                .insert()
                .rawContacts(newRawContact)
                .commit();
    }

    Update.Result updateRawContactGenderCustomData(RawContact rawContact) {
        MutableRawContact mutableRawContact = rawContact.mutableCopy();
        MutableGenderEntity mutableGender = RawContactGenderKt.gender(mutableRawContact, contacts);
        if (mutableGender != null) {
            mutableGender.setType(GenderEntity.Type.FEMALE);
        }

        return contacts
                .update()
                .rawContacts(mutableRawContact)
                .commit();
    }

    Update.Result deleteGenderCustomDataFromRawContact(RawContact rawContact) {
        MutableRawContact mutableRawContact = rawContact.mutableCopy();
        RawContactGenderKt.setGender(mutableRawContact, contacts, (MutableGenderEntity) null);

        return contacts
                .update()
                .rawContacts(mutableRawContact)
                .commit();
    }

    List<Gender> getAllGenders() {
        return GenderDataQueryKt.genders(contacts.data().query()).find();
    }

    DataUpdate.Result updateGender(MutableGender gender) {
        return contacts.data().update().data(gender).commit();
    }

    DataDelete.Result deleteGender(Gender gender) {
        return contacts.data().delete().data(gender).commit();
    }
}