# Android Contacts, Reborn (CHEATSHEET)

This page gives you basic sample copy-paste code showcasing how to use all of the **core APIs** 
provided in this library in both **Kotlin** and **Java**!

The examples provided here show the most basic usage of each **`core` API**. Click on the section 
heading explore each API in full detail. You may also find these samples in the `sample` module's
`contacts.sample.cheatsheet` package.

> ⚠️ Executing `find()` and `commit()` functions in the UI thread may result in choppy UI. Those
> should be invoked in background threads instead.
> For more info, read [Execute work outside of the UI thread using coroutines](./async/async-execution-coroutines.md).

----------------------------------------------------------------------------------------------------

## Basics

### [Query contacts](./basics/query-contacts.md)

=== "Kotlin"

    ```kotlin
    import android.app.Activity
    import contacts.core.*
    import contacts.core.entities.Contact
    
    class QueryContactsActivity : Activity() {
    
        fun getAllContacts(): List<Contact> = Contacts(this).broadQuery().find()
    
        fun getAllContactsWithFavoritesFirstOrderedByDisplayName(): List<Contact> = Contacts(this)
            .broadQuery()
            .orderBy(
                ContactsFields.Options.Starred.desc(),
                ContactsFields.DisplayNamePrimary.asc(ignoreCase = true)
            )
            .find()
    
        fun getContactsWithEmailOrDisplayNameThatPartiallyMatches(text: String?): List<Contact> =
            Contacts(this)
                .broadQuery()
                .match(BroadQuery.Match.EMAIL)
                .wherePartiallyMatches(text)
                .find()
    
        fun getContactsWithPhoneOrDisplayNameThatPartiallyMatches(text: String?): List<Contact> =
            Contacts(this)
                .broadQuery()
                .match(BroadQuery.Match.PHONE)
                .wherePartiallyMatches(text)
                .find()
    
        fun getAllContactsIncludingOnlyDisplayNameAndEmailAddresses(): List<Contact> = Contacts(this)
            .broadQuery()
            .include(
                Fields.Contact.DisplayNamePrimary,
                Fields.Email.Address
            )
            .find()
    
        fun get25Contacts(): List<Contact> = Contacts(this)
            .broadQuery()
            .limit(25)
            .find()
    
        fun get25ContactsSkippingTheFirst25(): List<Contact> = Contacts(this)
            .broadQuery()
            .offset(25)
            .limit(25)
            .find()
    }
    ```

=== "Java"

    ```java
    import static contacts.core.OrderByKt.*;
    
    import android.app.Activity;
    
    import java.util.List;
    
    import contacts.core.BroadQuery;
    import contacts.core.ContactsFactory;
    import contacts.core.ContactsFields;
    import contacts.core.Fields;
    import contacts.core.entities.Contact;
    
    public class QueryContactsActivity extends Activity {
    
        List<Contact> getAllContacts() {
            return ContactsFactory.create(this).broadQuery().find();
        }
    
        List<Contact> getAllContactsWithFavoritesFirstOrderedByDisplayName() {
            return ContactsFactory.create(this)
                    .broadQuery()
                    .orderBy(
                            desc(ContactsFields.Options.Starred),
                            asc(ContactsFields.DisplayNamePrimary, true)
                    )
                    .find();
        }
    
        List<Contact> getContactsWithAnyDataThatPartiallyMatches(String text) {
            return ContactsFactory.create(this)
                    .broadQuery()
                    .match(BroadQuery.Match.ANY)
                    .wherePartiallyMatches(text)
                    .find();
        }
    
        List<Contact> getContactsWithEmailOrDisplayNameThatPartiallyMatches(String text) {
            return ContactsFactory.create(this)
                    .broadQuery()
                    .match(BroadQuery.Match.EMAIL)
                    .wherePartiallyMatches(text)
                    .find();
        }
    
        List<Contact> getContactsWithPhoneOrDisplayNameThatPartiallyMatches(String text) {
            return ContactsFactory.create(this)
                    .broadQuery()
                    .match(BroadQuery.Match.PHONE)
                    .wherePartiallyMatches(text)
                    .find();
        }
    
        List<Contact> getAllContactsIncludingOnlyDisplayNameAndEmailAddresses() {
            return ContactsFactory.create(this)
                    .broadQuery()
                    .include(
                            Fields.Contact.DisplayNamePrimary,
                            Fields.Email.Address
                    )
                    .find();
        }
    
        List<Contact> get25Contacts() {
            return ContactsFactory.create(this)
                    .broadQuery()
                    .limit(25)
                    .find();
        }
    
        List<Contact> get25ContactsSkippingTheFirst25() {
            return ContactsFactory.create(this)
                    .broadQuery()
                    .offset(25)
                    .limit(25)
                    .find();
        }
    }
    ```

### [Query contacts (advanced)](./basics/query-contacts-advanced.md)

=== "Kotlin"

    ```kotlin
    import android.accounts.Account
    import android.app.Activity
    import contacts.core.*
    import contacts.core.entities.Contact
    import contacts.core.util.lookupKeyIn
    
    class QueryContactsAdvanced : Activity() {
    
        fun getContactById(contactId: Long): Contact? = Contacts(this)
            .query()
            .where { Contact.Id equalTo contactId }
            .find()
            .firstOrNull()
    
        fun getContactByLookupKey(lookupKey: String): List<Contact> = Contacts(this)
            .query()
            .where { Contact.lookupKeyIn(lookupKey) }
            .find()
    
        fun getAllContactsForAGoogleAccount(): List<Contact> = Contacts(this)
            .query()
            .accounts(Account("email@gmail.com", "com.google"))
            .find()
    
        fun getOnlyFavoriteContacts(): List<Contact> = Contacts(this)
            .query()
            .where {
                Contact.Options.Starred equalTo true
            }
            .find()
    
        fun getContactsPartiallyMatchingDisplayName(): List<Contact> = Contacts(this)
            .query()
            .where {
                Contact.DisplayNamePrimary contains "alex"
            }
            .find()
    
        fun getContactsWithAtLeastOneGmailEmail(): List<Contact> = Contacts(this)
            .query()
            .where {
                Email.Address endsWith "@gmail.com"
            }
            .find()
    
        fun getContactsWithAtLeastOnePhoneNumber(): List<Contact> = Contacts(this)
            .query()
            .where {
                Phone.Number.isNotNullOrEmpty()
                // or Contact.HasPhoneNumber equalTo true
            }
            .find()
    
        fun getContactsWithAtLeastOnePhoneNumberAndEmail(): List<Contact> = Contacts(this)
            .query()
            .where {
                Phone.Number.isNotNullOrEmpty() and Email.Address.isNotNullOrEmpty()
                // or Contact.HasPhoneNumber equalTo true and Email.Address.isNotNullOrEmpty()
            }
            .find()
    }
    ```

=== "Java"

    ```java
    import static contacts.core.WhereKt.*;
    import static contacts.core.util.ContactLookupKeyKt.lookupKeyIn;
    
    import android.accounts.Account;
    import android.app.Activity;
    
    import java.util.List;
    
    import contacts.core.ContactsFactory;
    import contacts.core.Fields;
    import contacts.core.entities.Contact;
    
    public class QueryContactsAdvanced extends Activity {
    
        Contact getContactById(Long contactId) {
            return ContactsFactory.create(this)
                    .query()
                    .where(
                            equalTo(Fields.Contact.Id, contactId)
                    )
                    .find()
                    .get(0);
        }
    
        List<Contact> getContactByLookupKey(String lookupKey) {
            return ContactsFactory.create(this)
                    .query()
                    .where(
                            lookupKeyIn(Fields.Contact, lookupKey)
                    )
                    .find();
        }
    
        List<Contact> getAllContactsForAGoogleAccount() {
            return ContactsFactory.create(this)
                    .query()
                    .accounts(new Account("email@gmail.com", "com.google"))
                    .find();
        }
    
        List<Contact> getOnlyFavoriteContacts() {
            return ContactsFactory.create(this)
                    .query()
                    .where(
                            equalTo(Fields.Contact.Options.Starred, true)
                    )
                    .find();
        }
    
        List<Contact> getContactsPartiallyMatchingDisplayName() {
            return ContactsFactory.create(this)
                    .query()
                    .where(
                            contains(Fields.Contact.DisplayNamePrimary, "alex")
                    )
                    .find();
        }
    
        List<Contact> getContactsWithAtLeastOneGmailEmail() {
            return ContactsFactory.create(this)
                    .query()
                    .where(
                            endsWith(Fields.Email.Address, "@gmail.com")
                    )
                    .find();
        }
    
        List<Contact> getContactsWithAtLeastOnePhoneNumber() {
            return ContactsFactory.create(this)
                    .query()
                    .where(
                            isNotNullOrEmpty(Fields.Phone.Number)
                            // or equalTo(Fields.Contact.HasPhoneNumber, true)
                    )
                    .find();
        }
    
        List<Contact> getContactsWithAtLeastOnePhoneNumberAndEmail() {
            return ContactsFactory.create(this)
                    .query()
                    .where(
                            and(
                                    isNotNullOrEmpty(Fields.Phone.Number),
                                    // or equalTo(Fields.Contact.HasPhoneNumber, true),
                                    isNotNullOrEmpty(Fields.Email.Address)
                            )
                    )
                    .find();
        }
    }
    ```

### [Insert contacts](./basics/insert-contacts.md)

=== "Kotlin"

    ```kotlin
    import android.accounts.Account
    import android.app.Activity
    import contacts.core.Contacts
    import contacts.core.Insert
    import contacts.core.entities.*
    import contacts.core.util.*
    
    class InsertContactsActivity : Activity() {
    
        fun insertContact(account: Account?, groupMembership: NewGroupMembership?): Insert.Result =
            Contacts(this)
                .insert()
                .forAccount(account)
                .rawContact {
                    addAddress {
                        street = "123 Abc street"
                        city = "Brooklyn"
                        region = "New York"
                        postcode = "11207"
                        country = "US"
                        type = AddressEntity.Type.WORK
                    }
                    addEmail {
                        address = "123@abc.com"
                        type = EmailEntity.Type.WORK
                    }
                    addEvent {
                        date = EventDate.from(1990, 0, 1)
                        type = EventEntity.Type.BIRTHDAY
                    }
                    if (groupMembership != null) {
                        addGroupMembership(groupMembership)
                    }
                    addIm {
                        data = "im@aol.com"
                        protocol = ImEntity.Protocol.CUSTOM
                        customProtocol = "AOL"
                    }
                    setName {
                        prefix = "Mr."
                        givenName = "Big"
                        middleName = "Bad"
                        familyName = "Fox"
                        suffix = "Jr"
                    }
                    setNickname {
                        name = "BIG BAD FOX"
                    }
                    setNote {
                        note = "This is one big bad fox!"
                    }
                    setOrganization {
                        company = "Bad company"
                        title = "Boss"
                        department = "The bad one"
                        jobDescription = "Be a big bad boss"
                        officeLocation = "It's a secret"
                    }
                    addPhone {
                        number = "(888) 123-4567"
                        type = PhoneEntity.Type.WORK
                    }
                    addRelation {
                        name = "Bro"
                        type = RelationEntity.Type.BROTHER
                    }
                    setSipAddress {
                        sipAddress = "sip:user@domain:port"
                    }
                    addWebsite {
                        url = "www.bigbadfox.com"
                    }
                }
                .commit()
    }
    ```

=== "Java"

    ```java
    import android.accounts.Account;
    import android.app.Activity;
    
    import contacts.core.ContactsFactory;
    import contacts.core.Insert;
    import contacts.core.entities.*;

    public class InsertContactsActivity extends Activity {
    
        Insert.Result insertContact(Account account, NewGroupMembership groupMembership) {
            NewAddress address = new NewAddress();
            address.setStreet("123 Abc street");
            address.setCity("Brooklyn");
            address.setRegion("New York");
            address.setPostcode("11207");
            address.setCountry("US");
            address.setType(AddressEntity.Type.WORK);
    
            NewEmail email = new NewEmail();
            email.setAddress("123@abc.com");
            email.setType(EmailEntity.Type.WORK);
    
            NewEvent event = new NewEvent();
            event.setDate(EventDate.from(1990, 0, 1));
            event.setType(EventEntity.Type.BIRTHDAY);
    
            NewIm im = new NewIm();
            im.setData("im@aol.com");
            im.setProtocol(ImEntity.Protocol.CUSTOM);
            im.setCustomProtocol("AOL");
    
            NewName name = new NewName();
            name.setPrefix("Mr.");
            name.setGivenName("Big");
            name.setMiddleName("Bad");
            name.setFamilyName("Fox");
            name.setSuffix("Jr");
    
            NewNickname nickname = new NewNickname();
            nickname.setName("BIG BAD FOX");
    
            NewNote note = new NewNote();
            note.setNote("This is one big bad fox!");
    
            NewOrganization organization = new NewOrganization();
            organization.setCompany("Bad company");
            organization.setTitle("Boss");
            organization.setDepartment("The bad one");
            organization.setJobDescription("Be a big bad boss");
            organization.setOfficeLocation("It's a secret");
    
            NewPhone phone = new NewPhone();
            phone.setNumber("(888) 123-4567");
            phone.setType(PhoneEntity.Type.WORK);
    
            NewRelation relation = new NewRelation();
            relation.setName("Bro");
            relation.setType(RelationEntity.Type.BROTHER);
    
            NewSipAddress sipAddress = new NewSipAddress();
            sipAddress.setSipAddress("sip:user@domain:port");
    
            NewWebsite website = new NewWebsite();
            website.setUrl("www.bigbadfox.com");
    
            NewRawContact rawContact = new NewRawContact();
            rawContact.getAddresses().add(address);
            rawContact.getEmails().add(email);
            rawContact.getEvents().add(event);
            if (groupMembership != null) {
                rawContact.getGroupMemberships().add(groupMembership);
            }
            rawContact.getIms().add(im);
            rawContact.setName(name);
            rawContact.setNickname(nickname);
            rawContact.setNote(note);
            rawContact.setOrganization(organization);
            rawContact.getPhones().add(phone);
            rawContact.getRelations().add(relation);
            rawContact.setSipAddress(sipAddress);
            rawContact.getWebsites().add(website);
    
            return ContactsFactory.create(this)
                    .insert()
                    .forAccount(account)
                    .rawContacts(rawContact)
                    .commit();
        }
    }
    ```

### [Update contacts](./basics/update-contacts.md)

=== "Kotlin"

    ```kotlin
    import android.app.Activity
    import contacts.core.Contacts
    import contacts.core.Update
    import contacts.core.entities.*
    import contacts.core.util.*
    
    class UpdateContactsActivity : Activity() {
    
        fun addEmail(contact: Contact): Update.Result =
            Contacts(this)
                .update()
                .contacts(contact.mutableCopy {
                    addEmail {
                        address = "321@xyz.com"
                        type = EmailEntity.Type.CUSTOM
                        label = "Personal"
                    }
                })
                .commit()
    
        fun addEmail(rawContact: RawContact): Update.Result =
            Contacts(this)
                .update()
                .rawContacts(rawContact.mutableCopy {
                    addEmail {
                        address = "321@xyz.com"
                        type = EmailEntity.Type.CUSTOM
                        label = "Personal"
                    }
                })
                .commit()
    
        fun addAnniversary(contact: Contact): Update.Result =
            Contacts(this)
                .update()
                .contacts(contact.mutableCopy {
                    addEvent {
                        date = EventDate.from(2016, 6, 14)
                        type = EventEntity.Type.ANNIVERSARY
                    }
                })
                .commit()
    
        fun setFullName(rawContact: RawContact): Update.Result =
            Contacts(this)
                .update()
                .rawContacts(rawContact.mutableCopy {
                    setName {
                        prefix = "Mr."
                        givenName = "Small"
                        middleName = "Bald"
                        familyName = "Eagle"
                        suffix = "Sr"
                    }
                })
                .commit()
    
        fun setGivenName(rawContact: RawContact): Update.Result =
            Contacts(this)
                .update()
                .rawContacts(rawContact.mutableCopy {
                    name = (name ?: NewName()).also { it.givenName = "Greg" }
                })
                .commit()
    
        fun removeGmailEmails(contact: Contact): Update.Result =
            Contacts(this)
                .update()
                .contacts(contact.mutableCopy {
                    emails()
                        .filter { it.address?.endsWith("@gmail.com", ignoreCase = true) == true }
                        .forEach { removeEmail(it) }
                })
                .commit()
    
        fun removeEmailsAndPhones(contact: Contact): Update.Result =
            Contacts(this)
                .update()
                .contacts(contact.mutableCopy {
                    removeAllEmails()
                    removeAllPhones()
                })
                .commit()
    }
    ```

=== "Java"

    ```java
    import android.app.Activity;
    
    import contacts.core.ContactsFactory;
    import contacts.core.Update;
    import contacts.core.entities.*;
    import contacts.core.util.ContactDataKt;
    
    public class UpdateContactsActivity extends Activity {
    
        Update.Result addEmail(Contact contact) {
            MutableContact mutableContact = contact.mutableCopy();
            ContactDataKt.addEmail(mutableContact, new NewEmail(
                    EmailEntity.Type.CUSTOM,
                    "Personal",
                    "321@xyz.com"
            ));
    
            return ContactsFactory.create(this)
                    .update()
                    .contacts(mutableContact)
                    .commit();
        }
    
        Update.Result addEmail(RawContact rawContact) {
            MutableRawContact mutableRawContact = rawContact.mutableCopy();
            mutableRawContact.getEmails().add(new NewEmail(
                    EmailEntity.Type.CUSTOM,
                    "Personal",
                    "321@xyz.com"
            ));
    
            return ContactsFactory.create(this)
                    .update()
                    .rawContacts(mutableRawContact)
                    .commit();
        }
    
        Update.Result addAnniversary(Contact contact) {
            MutableContact mutableContact = contact.mutableCopy();
            ContactDataKt.addEvent(mutableContact, new NewEvent(
                    EventEntity.Type.ANNIVERSARY,
                    null,
                    EventDate.from(2016, 6, 14)
            ));
    
            return ContactsFactory.create(this)
                    .update()
                    .contacts(mutableContact)
                    .commit();
        }
    
        Update.Result setFullName(RawContact rawContact) {
            NewName name = new NewName();
            name.setPrefix("Mr.");
            name.setGivenName("Small");
            name.setMiddleName("Bald");
            name.setFamilyName("Eagle");
            name.setSuffix("Sr");
    
            MutableRawContact mutableRawContact = rawContact.mutableCopy();
            mutableRawContact.setName(name);
    
            return ContactsFactory.create(this)
                    .update()
                    .rawContacts(mutableRawContact)
                    .commit();
        }
    
        Update.Result setGivenName(RawContact rawContact) {
            MutableRawContact mutableRawContact = rawContact.mutableCopy();
            if (mutableRawContact.getName() != null) {
                mutableRawContact.getName().setGivenName("Greg");
            } else {
                NewName name = new NewName();
                name.setGivenName("Greg");
                mutableRawContact.setName(name);
            }
    
            return ContactsFactory.create(this)
                    .update()
                    .rawContacts(mutableRawContact)
                    .commit();
        }
    
        Update.Result removeGmailEmails(Contact contact) {
            MutableContact mutableContact = contact.mutableCopy();
            for (MutableEmailEntity email : ContactDataKt.emailList(mutableContact)) {
                String emailAddress = email.getAddress();
                if (emailAddress != null && emailAddress.toLowerCase().endsWith("@gmail.com")) {
                    ContactDataKt.removeEmail(mutableContact, email);
                }
            }
    
            return ContactsFactory.create(this)
                    .update()
                    .contacts(mutableContact)
                    .commit();
        }
    
        Update.Result removeEmailsAndPhones(Contact contact) {
            MutableContact mutableContact = contact.mutableCopy();
            ContactDataKt.removeAllEmails(mutableContact);
            ContactDataKt.removeAllPhones(mutableContact);
    
            return ContactsFactory.create(this)
                    .update()
                    .contacts(mutableContact)
                    .commit();
        }
    }
    ```

### [Delete contacts](./basics/delete-contacts.md)

=== "Kotlin"

    ```kotlin
    import android.app.Activity
    import contacts.core.Contacts
    import contacts.core.Delete
    import contacts.core.entities.Contact
    import contacts.core.entities.RawContact
    
    class DeleteContactsActivity : Activity() {
    
        fun deleteContact(contact: Contact): Delete.Result = Contacts(this)
            .delete()
            .contacts(contact)
            .commit()
    
        fun deleteRawContact(rawContact: RawContact): Delete.Result = Contacts(this)
            .delete()
            .rawContacts(rawContact)
            .commit()
    }
    ```

=== "Java"

    ```java
    import android.app.Activity;
    
    import contacts.core.ContactsFactory;
    import contacts.core.Delete;
    import contacts.core.entities.Contact;
    import contacts.core.entities.RawContact;
    
    public class DeleteContactsActivity extends Activity {
    
        Delete.Result deleteContact(Contact contact) {
            return ContactsFactory.create(this)
                    .delete()
                    .contacts(contact)
                    .commit();
        }
    
        Delete.Result deleteRawContact(RawContact rawContact) {
            return ContactsFactory.create(this)
                    .delete()
                    .rawContacts(rawContact)
                    .commit();
        }
    }
    ```

----------------------------------------------------------------------------------------------------

## Data

### [Query specific data kinds](./data/query-data-sets.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Insert data into new or existing contacts](./data/insert-data-sets.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Update existing sets of data](./data/update-data-sets.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Delete existing sets of data](./data/delete-data-sets.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

----------------------------------------------------------------------------------------------------

## Custom data

### [Query custom data](./customdata/query-custom-data.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Insert custom data into new or existing contacts](./customdata/insert-custom-data.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Update custom data](./customdata/update-custom-data.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Delete custom data](./customdata/delete-custom-data.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Integrate the Google Contacts custom data](./customdata/integrate-googlecontacts-custom-data.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Integrate the Gender custom data](./customdata/integrate-gender-custom-data.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Integrate the Handle Name custom data](./customdata/integrate-handlename-custom-data.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Integrate the Pokemon custom data](./customdata/integrate-pokemon-custom-data.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Integrate the RPG custom data](./customdata/integrate-rpg-custom-data.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

----------------------------------------------------------------------------------------------------

## Groups

### [Query groups](./groups/query-groups.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Insert groups](./groups/insert-groups.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Update groups](./groups/update-groups.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Delete groups](./groups/delete-groups.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

----------------------------------------------------------------------------------------------------

## Profile

### [Query device owner Contact profile](./profile/query-profile.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Insert device owner Contact profile](./profile/insert-profile.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Update device owner Contact profile](./profile/update-profile.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Delete device owner Contact profile](./profile/delete-profile.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

----------------------------------------------------------------------------------------------------

## Accounts

### [Query for Accounts](./accounts/query-accounts.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Query for RawContacts](./accounts/query-raw-contacts.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Associate a local RawContact to an Account](./accounts/associate-device-local-raw-contacts-to-an-account.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

----------------------------------------------------------------------------------------------------

## SIM card

### [Query contacts in SIM card](./sim/query-sim-contacts.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Insert contacts into SIM card](./sim/insert-sim-contacts.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Update contacts in SIM card](./sim/update-sim-contacts.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Delete contacts from SIM card](./sim/delete-sim-contacts.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

----------------------------------------------------------------------------------------------------

## Blocked numbers

### [Query blocked numbers](./blockednumbers/query-blocked-numbers.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Insert blocked numbers](./blockednumbers/insert-blocked-numbers.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Delete blocked numbers](./blockednumbers/delete-blocked-numbers.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

----------------------------------------------------------------------------------------------------

## Other

### [Get set remove full-sized and thumbnail contact photos](./other/get-set-remove-contact-raw-contact-photo.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Get set Contact options](./other/get-set-clear-contact-raw-contact-options.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Get set clear default Contact data](./other/get-set-clear-default-data.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Link unlink Contacts](./other/link-unlink-contacts.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Share Contacts vCard (.VCF)](./other/share-contacts-vcard.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Convenience functions](./other/convenience-functions.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

----------------------------------------------------------------------------------------------------

## Logging

### [Log API input and output](./logging/log-api-input-output.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

----------------------------------------------------------------------------------------------------

## Testing

### [Contacts API Testing](./testing/test-contacts-api.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

----------------------------------------------------------------------------------------------------

## Debug

### [Debug the Contacts Provider tables](./debug/debug-contacts-provider-tables.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Debug the BlockedNumber Provider tables](./debug/debug-blockednumber-provider-tables.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```

### [Debug the Sim Contacts table](./debug/debug-sim-contacts-tables.md)

=== "Kotlin"

    ```kotlin
    TODO
    ```

=== "Java"

    ```java
    TODO
    ```