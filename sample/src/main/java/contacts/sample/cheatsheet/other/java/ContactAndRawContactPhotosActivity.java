package contacts.sample.cheatsheet.other.java;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;

import contacts.core.ContactsFactory;
import contacts.core.Insert;
import contacts.core.Update;
import contacts.core.entities.Contact;
import contacts.core.entities.MutableContact;
import contacts.core.entities.MutableRawContact;
import contacts.core.entities.NewRawContact;
import contacts.core.entities.RawContact;
import contacts.core.util.ContactPhotoKt;
import contacts.core.util.PhotoData;
import contacts.core.util.RawContactPhotoKt;

public class ContactAndRawContactPhotosActivity extends Activity {

    Uri getContactPhotoUri(Contact contact) {
        return contact.getPhotoUri();
    }

    Uri getContactThumbnailPhotoUri(Contact contact) {
        return contact.getPhotoThumbnailUri();
    }

    Bitmap getContactPhoto(Contact contact) {
        return ContactPhotoKt.photoBitmap(contact, ContactsFactory.create(this));
    }

    Bitmap getContactPhotoThumbnail(Contact contact) {
        return ContactPhotoKt.photoThumbnailBitmap(contact, ContactsFactory.create(this));
    }

    Bitmap getRawContactPhoto(RawContact rawContact) {
        return RawContactPhotoKt.photoBitmap(rawContact, ContactsFactory.create(this));
    }

    Bitmap getRawContactPhotoThumbnail(RawContact rawContact) {
        return RawContactPhotoKt.photoThumbnailBitmap(rawContact, ContactsFactory.create(this));
    }

    Insert.Result insertNewRawContactWithPhoto(Bitmap bitmap) {
        NewRawContact rawContact = new NewRawContact();
        RawContactPhotoKt.setPhoto(rawContact, PhotoData.from(bitmap));

        return ContactsFactory.create(this)
                .insert()
                .rawContacts(rawContact)
                .commit();
    }

    Update.Result setContactPhoto(Contact contact, Bitmap bitmap) {
        MutableContact mutableContact = contact.mutableCopy();
        ContactPhotoKt.setPhoto(mutableContact, PhotoData.from(bitmap));

        return ContactsFactory.create(this)
                .update()
                .contacts(mutableContact)
                .commit();
    }

    Update.Result setRawContactPhoto(RawContact rawContact, Bitmap bitmap) {
        MutableRawContact mutableRawContact = rawContact.mutableCopy();
        RawContactPhotoKt.setPhoto(mutableRawContact, PhotoData.from(bitmap));

        return ContactsFactory.create(this)
                .update()
                .rawContacts(mutableRawContact)
                .commit();
    }

    Update.Result removeContactPhoto(Contact contact) {
        MutableContact mutableContact = contact.mutableCopy();
        ContactPhotoKt.removePhoto(mutableContact);

        return ContactsFactory.create(this)
                .update()
                .contacts(mutableContact)
                .commit();
    }

    Update.Result removeRawContactPhoto(RawContact rawContact) {
        MutableRawContact mutableRawContact = rawContact.mutableCopy();
        RawContactPhotoKt.removePhoto(mutableRawContact);

        return ContactsFactory.create(this)
                .update()
                .rawContacts(mutableRawContact)
                .commit();
    }

    Boolean setContactPhotoDirect(Contact contact, Bitmap bitmap) {
        return ContactPhotoKt.setPhotoDirect(contact, ContactsFactory.create(this), PhotoData.from(bitmap));
    }

    Boolean setRawContactPhotoDirect(RawContact rawContact, Bitmap bitmap) {
        return RawContactPhotoKt.setPhotoDirect(rawContact, ContactsFactory.create(this), PhotoData.from(bitmap));
    }

    Boolean removeContactPhotoDirect(Contact contact) {
        return ContactPhotoKt.removePhotoDirect(contact, ContactsFactory.create(this));
    }

    Boolean removeRawContactPhotoDirect(RawContact rawContact) {
        return RawContactPhotoKt.removePhotoDirect(rawContact, ContactsFactory.create(this));
    }
}