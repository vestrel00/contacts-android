package contacts.sample.cheatsheet.other.java;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;

import contacts.core.ContactsFactory;
import contacts.core.entities.*;
import contacts.core.util.*;

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

    Boolean setContactPhoto(Contact contact, Bitmap photo) {
        return ContactPhotoKt.setPhotoDirect(contact, ContactsFactory.create(this), photo);
    }

    Boolean setRawContactPhoto(RawContact rawContact, Bitmap photo) {
        return RawContactPhotoKt.setPhotoDirect(rawContact, ContactsFactory.create(this), photo);
    }

    Boolean removeContactPhoto(Contact contact) {
        return ContactPhotoKt.removePhotoDirect(contact, ContactsFactory.create(this));
    }

    Boolean removeRawContactPhoto(RawContact rawContact) {
        return RawContactPhotoKt.removePhotoDirect(rawContact, ContactsFactory.create(this));
    }
}