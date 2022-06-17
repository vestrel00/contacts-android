package contacts.sample.cheatsheet.customdata.java;

import static contacts.core.WhereKt.*;

import android.app.Activity;

import java.util.List;

import contacts.core.*;
import contacts.core.data.*;
import contacts.core.entities.*;
import contacts.core.entities.custom.CustomDataRegistry;
import contacts.entities.custom.googlecontacts.*;
import contacts.entities.custom.googlecontacts.fileas.*;
import contacts.entities.custom.googlecontacts.userdefined.*;

public class IntegrateGoogleContactsCustomDataActivity extends Activity {

    Contacts contacts = ContactsFactory.create(
            this, new CustomDataRegistry().register(new GoogleContactsRegistration())
    );

    List<Contact> getContactsWithGoogleContactsCustomData() {
        return contacts
                .query()
                .where(
                        or(
                                isNotNull(GoogleContactsFields.FileAs.Name),
                                isNotNull(GoogleContactsFields.UserDefined.Field)
                        )
                )
                .find();
    }

    Insert.Result insertRawContactWithGoogleContactsCustomData() {
        NewFileAs newFileAs = new NewFileAs("Lucky");
        NewUserDefined newUserDefined = new NewUserDefined("Lucky Field", "Lucky Label");

        NewRawContact newRawContact = new NewRawContact();
        RawContactFileAsKt.setFileAs(newRawContact, contacts, newFileAs);
        RawContactUserDefinedKt.addUserDefined(newRawContact, contacts, newUserDefined);

        return contacts
                .insert()
                .rawContacts(newRawContact)
                .commit();
    }

    Update.Result updateRawContactGoogleContactsCustomData(RawContact rawContact) {
        NewFileAs fileAs = new NewFileAs("Unfortunate");
        NewUserDefined userDefined = new NewUserDefined("Unfortunate Field", "Unfortunate Label");

        MutableRawContact mutableRawContact = rawContact.mutableCopy();
        RawContactFileAsKt.setFileAs(mutableRawContact, contacts, fileAs);
        RawContactUserDefinedKt.addUserDefined(mutableRawContact, contacts, userDefined);

        return contacts
                .update()
                .rawContacts(mutableRawContact)
                .commit();
    }

    Update.Result deleteGoogleContactsCustomDataFromRawContact(RawContact rawContact) {
        MutableRawContact mutableRawContact = rawContact.mutableCopy();
        RawContactFileAsKt.setFileAs(mutableRawContact, contacts, (MutableFileAsEntity) null);
        RawContactUserDefinedKt.removeAllUserDefined(mutableRawContact, contacts);

        return contacts
                .update()
                .rawContacts(mutableRawContact)
                .commit();
    }

    List<FileAs> getAllFileAs() {
        return FileAsDataQueryKt.fileAs(contacts.data().query()).find();
    }

    List<UserDefined> getAllUserDefined() {
        return UserDefinedDataQueryKt.userDefined(contacts.data().query()).find();
    }

    DataUpdate.Result updateFileAsAndUserDefined(
            MutableFileAs fileAs, MutableUserDefined userDefined
    ) {
        return contacts.data().update().data(fileAs, userDefined).commit();
    }

    DataDelete.Result updateFileAsAndUserDefined(
            FileAs fileAs, UserDefined userDefined
    ) {
        return contacts.data().delete().data(fileAs, userDefined).commit();
    }
}