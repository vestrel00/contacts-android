package contacts.sample.cheatsheet.profile.java;

import android.app.Activity;

import contacts.core.ContactsFactory;
import contacts.core.entities.*;
import contacts.core.profile.ProfileInsert;

public class InsertProfileActivity extends Activity {

    ProfileInsert.Result insertProfile() {
        NewAddress address = new NewAddress();
        address.setStreet("Xyz Abc street");
        address.setCity("Brooklyn");
        address.setRegion("New York");
        address.setPostcode("11207");
        address.setCountry("US");
        address.setType(AddressEntity.Type.WORK);

        NewEmail email = new NewEmail();
        email.setAddress("321@xyz.com");
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
        name.setGivenName("Small");
        name.setMiddleName("Goody");
        name.setFamilyName("Dog");
        name.setSuffix("Jr");

        NewNickname nickname = new NewNickname();
        nickname.setName("TINY DOMESTICATED ANIMAL");

        NewNote note = new NewNote();
        note.setNote("This is one furry friend!");

        NewOrganization organization = new NewOrganization();
        organization.setCompany("Good company");
        organization.setTitle("Teammate");
        organization.setDepartment("The good one");
        organization.setJobDescription("Be a good citizen");
        organization.setOfficeLocation("It's public");

        NewPhone phone = new NewPhone();
        phone.setNumber("(888) 321-7654");
        phone.setType(PhoneEntity.Type.WORK);

        NewRelation relation = new NewRelation();
        relation.setName("Bro");
        relation.setType(RelationEntity.Type.BROTHER);

        NewSipAddress sipAddress = new NewSipAddress();
        sipAddress.setSipAddress("sip:user@domain:port");

        NewWebsite website = new NewWebsite();
        website.setUrl("www.smalltinycompany.com");

        NewRawContact rawContact = new NewRawContact();
        rawContact.getAddresses().add(address);
        rawContact.getEmails().add(email);
        rawContact.getEvents().add(event);
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
                .profile()
                .insert()
                .rawContact(rawContact)
                .commit();
    }
}
