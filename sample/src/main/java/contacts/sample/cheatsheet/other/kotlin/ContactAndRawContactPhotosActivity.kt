package contacts.sample.cheatsheet.other.kotlin

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import contacts.core.Contacts
import contacts.core.Insert
import contacts.core.Update
import contacts.core.entities.Contact
import contacts.core.entities.RawContact
import contacts.core.util.*

class ContactAndRawContactPhotosActivity : Activity() {

    fun getContactPhotoUri(contact: Contact): Uri? = contact.photoUri

    fun getContactThumbnailPhotoUri(contact: Contact): Uri? = contact.photoThumbnailUri

    fun getContactPhoto(contact: Contact): Bitmap? = contact.photoBitmap(Contacts(this))

    fun getContactPhotoThumbnail(contact: Contact): Bitmap? =
        contact.photoThumbnailBitmap(Contacts(this))

    fun getRawContactPhoto(rawContact: RawContact): Bitmap? = rawContact.photoBitmap(Contacts(this))

    fun getRawContactPhotoThumbnail(rawContact: RawContact): Bitmap? =
        rawContact.photoThumbnailBitmap(Contacts(this))


    fun insertNewRawContactWithPhoto(bitmap: Bitmap): Insert.Result = Contacts(this)
        .insert()
        .rawContact {
            setPhoto(PhotoData.from(bitmap))
        }
        .commit()

    fun setContactPhoto(contact: Contact, bitmap: Bitmap): Update.Result = Contacts(this)
        .update()
        .contacts(
            contact.mutableCopy {
                setPhoto(PhotoData.from(bitmap))
            }
        )
        .commit()

    fun setRawContactPhoto(rawContact: RawContact, bitmap: Bitmap): Update.Result = Contacts(this)
        .update()
        .rawContacts(
            rawContact.mutableCopy {
                setPhoto(PhotoData.from(bitmap))
            }
        )
        .commit()

    fun removeContactPhoto(contact: Contact): Update.Result = Contacts(this)
        .update()
        .contacts(
            contact.mutableCopy {
                removePhoto()
            }
        )
        .commit()

    fun removeRawContactPhoto(rawContact: RawContact): Update.Result = Contacts(this)
        .update()
        .rawContacts(
            rawContact.mutableCopy {
                removePhoto()
            }
        )
        .commit()

    fun setContactPhotoDirect(contact: Contact, bitmap: Bitmap): Boolean =
        contact.setPhotoDirect(Contacts(this), PhotoData.from(bitmap))

    fun setRawContactPhotoDirect(rawContact: RawContact, bitmap: Bitmap): Boolean =
        rawContact.setPhotoDirect(Contacts(this), PhotoData.from(bitmap))

    fun removeContactPhotoDirect(contact: Contact): Boolean =
        contact.removePhotoDirect(Contacts(this))

    fun removeRawContactPhotoDirect(rawContact: RawContact): Boolean =
        rawContact.removePhotoDirect(Contacts(this))
}