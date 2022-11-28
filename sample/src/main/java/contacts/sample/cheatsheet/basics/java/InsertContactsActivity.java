package contacts.sample.cheatsheet.basics.java;

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
        rawContact.setAccount(account);
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
                .rawContacts(rawContact)
                .commit();
    }
}
