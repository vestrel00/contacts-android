package contacts.sample.cheatsheet.customdata.java;

import static contacts.core.WhereKt.isNotNull;

import android.app.Activity;

import java.util.List;

import contacts.core.*;
import contacts.core.data.*;
import contacts.core.entities.*;
import contacts.core.entities.custom.CustomDataRegistry;
import contacts.entities.custom.handlename.*;

public class IntegrateHandleNameCustomDataActivity extends Activity {

    Contacts contacts = ContactsFactory.create(
            this, false, new CustomDataRegistry().register(new HandleNameRegistration())
    );

    List<Contact> getContactsWithHandleNameCustomData() {
        return contacts
                .query()
                .where(isNotNull(HandleNameFields.Handle))
                .find();
    }

    Insert.Result insertRawContactWithHandleNameCustomData() {
        NewHandleName newHandleName = new NewHandleName("The Beauty");

        NewRawContact newRawContact = new NewRawContact();
        RawContactHandleNameKt.addHandleName(newRawContact, contacts, newHandleName);

        return contacts
                .insert()
                .rawContacts(newRawContact)
                .commit();
    }

    Update.Result updateRawContactHandleNameCustomData(RawContact rawContact) {
        MutableRawContact mutableRawContact = rawContact.mutableCopy();

        List<MutableHandleNameEntity> mutableHandleNameList =
                RawContactHandleNameKt.handleNameList(mutableRawContact, contacts);
        MutableHandleNameEntity mutableHandleName = null;
        if (!mutableHandleNameList.isEmpty()) {
            mutableHandleName = mutableHandleNameList.get(0);
        }

        if (mutableHandleName != null) {
            mutableHandleName.setHandle("The Beast");
        }

        return contacts
                .update()
                .rawContacts(mutableRawContact)
                .commit();
    }

    Update.Result deleteHandleNameCustomDataFromRawContact(RawContact rawContact) {
        MutableRawContact mutableRawContact = rawContact.mutableCopy();
        RawContactHandleNameKt.removeAllHandleNames(mutableRawContact, contacts);

        return contacts
                .update()
                .rawContacts(mutableRawContact)
                .commit();
    }

    List<HandleName> getAllHandleNames() {
        return HandleNameDataQueryKt.handleNames(contacts.data().query()).find();
    }

    DataUpdate.Result updateHandleName(MutableHandleName handleName) {
        return contacts.data().update().data(handleName).commit();
    }

    DataDelete.Result deleteHandleName(HandleName handleName) {
        return contacts.data().delete().data(handleName).commit();
    }
}