package contacts.sample.cheatsheet.other.java;

import android.app.Activity;

import java.util.*;

import contacts.core.*;
import contacts.core.entities.*;
import contacts.core.util.*;

public class ConvenienceFunctionsActivity extends Activity {

    void getSetRawContactDataThroughContact(Contact contact) {
        NewEmail newEmail = new NewEmail();
        newEmail.setAddress("test@email.com");
        newEmail.setType(EmailEntity.Type.WORK);
        ContactDataKt.addEmail(contact.mutableCopy(), newEmail);
    }

    void setDataUsingExtensions(MutableRawContact mutableRawContact) {
        NewEmail newEmail = new NewEmail();
        newEmail.setAddress("test@email.com");
        newEmail.setType(EmailEntity.Type.WORK);
        MutableRawContactDataKt.addEmail(mutableRawContact, newEmail);
    }

    Contact getParentContactOfRawContact(RawContact rawContact) {
        return RawContactContactKt.contact(rawContact, ContactsFactory.create(this));
    }

    Contact getParentContactOfData(ExistingDataEntity data) {
        return DataContactKt.contact(data, ContactsFactory.create(this));
    }

    RawContact getParentRawContactOfData(ExistingDataEntity data) {
        return DataRawContactKt.rawContact(data, ContactsFactory.create(this));
    }

    Contact refreshContact(Contact contact) {
        return ContactRefreshKt.refresh(contact, ContactsFactory.create(this));
    }

    RawContact refreshRawContact(RawContact rawContact) {
        return RawContactRefreshKt.refresh(rawContact, ContactsFactory.create(this));
    }

    ExistingDataEntity refreshData(ExistingDataEntity data) {
        return DataRefreshKt.refresh(data, ContactsFactory.create(this));
    }

    boolean isDataReadOnly(ExistingDataEntity data) {
        return DataIsReadOnlyKt.isReadOnly(data, ContactsFactory.create(this));
    }

    Map<Long, Boolean> isDataReadOnlyMap(Collection<ExistingDataEntity> data) {
        return DataIsReadOnlyKt.isReadOnlyMap(data, ContactsFactory.create(this));
    }

    List<Contact> sortContactsUsingDataTableFields(List<Contact> contacts) {
        List<OrderBy<AbstractDataField>> orderByFields = new ArrayList<>();
        orderByFields.add(OrderByKt.desc(Fields.Contact.Options.Starred));
        orderByFields.add(OrderByKt.asc(Fields.Email.Type));

        List<Contact> sortedContacts = new ArrayList<>(contacts);
        Collections.sort(sortedContacts, ContactsComparatorKt.contactsComparator(orderByFields));
        return sortedContacts;
    }

    List<Group> getGroupOfGroupMemberships(List<GroupMembership> groupMemberships) {
        return GroupMembershipGroupKt.groups(groupMemberships, ContactsFactory.create(this));
    }
}