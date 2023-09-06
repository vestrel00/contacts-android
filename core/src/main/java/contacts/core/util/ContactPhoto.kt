package contacts.core.util

import android.annotation.SuppressLint
import android.content.ContentProviderOperation.newDelete
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.ContactsContract
import contacts.core.*
import contacts.core.entities.ExistingContactEntity
import contacts.core.entities.MimeType
import contacts.core.entities.MutableContact
import contacts.core.entities.cursor.contactsCursor
import contacts.core.entities.operation.withSelection
import java.io.IOException
import java.io.InputStream

// region GET PHOTO

/**
 * Returns the full-sized photo as an [InputStream].
 *
 * Returns null if a photo has not yet been set or if permissions have not been granted or if the
 * contact no longer exists.
 *
 * This does not use the [ExistingContactEntity.photoUri] from this instance. Instead, a query is
 * made to retrieve the latest photo uri from the database in order to ensure validity.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ExistingContactEntity.rawContacts]
 * depending on query filters.
 *
 * It is up to the caller to close the [InputStream].
 *
 * Supports profile and non-profile Contacts.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingContactEntity.photoInputStream(contacts: Contacts): InputStream? {
    if (!contacts.permissions.canQuery()) {
        return null
    }

    return contacts.contentResolver.query(
        contacts.contactsUri(isProfile = isProfile),
        Include(ContactsFields.PhotoUri),
        ContactsFields.Id equalTo id
    ) {
        val photoUri = it.getNextOrNull { it.contactsCursor().photoUri }
        uriInputStream(contacts, photoUri)
    }
}

/**
 * Returns the full-sized photo as a [ByteArray].
 *
 * Returns null if a photo has not yet been set or if permissions have not been granted or if the
 * contact no longer exists.
 *
 * This does not use the [ExistingContactEntity.photoUri] from this instance. Instead, a query is
 * made to retrieve the latest photo uri from the database in order to ensure validity.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ExistingContactEntity.rawContacts]
 * depending on query filters.
 *
 * Supports profile and non-profile Contacts.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingContactEntity.photoBytes(contacts: Contacts): ByteArray? =
    photoInputStream(contacts)?.apply {
        it.readBytes()
    }

/**
 * Returns the full-sized photo as a [Bitmap].
 *
 * Returns null if a photo has not yet been set or if permissions have not been granted or if the
 * contact no longer exists.
 *
 * This does not use the [ExistingContactEntity.photoUri] from this instance. Instead, a query is
 * made to retrieve the latest photo uri from the database in order to ensure validity.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ExistingContactEntity.rawContacts]
 * depending on query filters.
 *
 * Supports profile and non-profile Contacts.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingContactEntity.photoBitmap(contacts: Contacts): Bitmap? =
    photoInputStream(contacts)?.apply {
        BitmapFactory.decodeStream(it)
    }

/**
 * Returns the full-sized photo as a [BitmapDrawable].
 *
 * Returns null if a photo has not yet been set or if permissions have not been granted or if the
 * contact no longer exists.
 *
 * This does not use the [ExistingContactEntity.photoUri] from this instance. Instead, a query is
 * made to retrieve the latest photo uri from the database in order to ensure validity.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ExistingContactEntity.rawContacts]
 * depending on query filters.
 *
 * Supports profile and non-profile Contacts.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingContactEntity.photoBitmapDrawable(contacts: Contacts): BitmapDrawable? =
    photoInputStream(contacts)?.apply {
        BitmapDrawable(contacts.resources, it)
    }

private fun uriInputStream(contacts: Contacts, uri: Uri?): InputStream? {
    if (uri == null) {
        return null
    }

    var inputStream: InputStream? = null
    try {
        @SuppressLint("Recycle")
        val fd = contacts.contentResolver.openAssetFileDescriptor(uri, "r")
        inputStream = fd?.createInputStream()
    } catch (ioe: IOException) {
        // do nothing
    }
    return inputStream
}

// endregion

// region GET PHOTO THUMBNAIL

/**
 * Returns the photo thumbnail as an [InputStream].
 *
 * Returns null if a photo has not yet been set or if permissions have not been granted or if the
 * contact no longer exists.
 *
 * This does not use the [ExistingContactEntity.photoThumbnailUri] from this instance. Instead, a
 * query is made to retrieve the latest photo uri from the database in order to ensure validity.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ExistingContactEntity.rawContacts]
 * depending on query filters.
 *
 * It is up to the caller to close the [InputStream].
 *
 * Supports profile and non-profile Contacts.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingContactEntity.photoThumbnailInputStream(contacts: Contacts): InputStream? {
    if (!contacts.permissions.canQuery()) {
        return null
    }

    return contacts.contentResolver.query(
        contacts.contactsUri(isProfile = isProfile),
        Include(ContactsFields.PhotoThumbnailUri),
        ContactsFields.Id equalTo id
    ) {
        val photoThumbnailUri = it.getNextOrNull { it.contactsCursor().photoThumbnailUri }
        uriInputStream(contacts, photoThumbnailUri)
    }
}

/**
 * Returns the photo thumbnail as a [ByteArray].
 *
 * Returns null if a photo has not yet been set or if permissions have not been granted or if the
 * contact no longer exists.
 *
 * This does not use the [ExistingContactEntity.photoThumbnailUri] from this instance. Instead, a
 * query is made to retrieve the latest photo uri from the database in order to ensure validity.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ExistingContactEntity.rawContacts]
 * depending on query filters.
 *
 * Supports profile and non-profile Contacts.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingContactEntity.photoThumbnailBytes(contacts: Contacts): ByteArray? =
    photoThumbnailInputStream(contacts)?.apply {
        it.readBytes()
    }

/**
 * Returns the full-sized photo as a [Bitmap].
 *
 * Returns null if a photo has not yet been set or if permissions have not been granted or if the
 * contact no longer exists.
 *
 * This does not use the [ExistingContactEntity.photoThumbnailUri] from this instance. Instead, a
 * query is made to retrieve the latest photo uri from the database in order to ensure validity.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ExistingContactEntity.rawContacts]
 * depending on query filters.
 *
 * Supports profile and non-profile Contacts.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingContactEntity.photoThumbnailBitmap(contacts: Contacts): Bitmap? =
    photoThumbnailInputStream(contacts)?.apply {
        BitmapFactory.decodeStream(it)
    }

/**
 * Returns the full-sized photo as a [BitmapDrawable].
 *
 * Returns null if a photo has not yet been set or if permissions have not been granted or if the
 * contact no longer exists.
 *
 * This does not use the [ExistingContactEntity.photoThumbnailUri] from this instance. Instead, a
 * query is made to retrieve the latest photo uri from the database in order to ensure validity.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ExistingContactEntity.rawContacts]
 * depending on query filters.
 *
 * Supports profile and non-profile Contacts.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.READ_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingContactEntity.photoThumbnailBitmapDrawable(contacts: Contacts): BitmapDrawable? =
    photoThumbnailInputStream(contacts)?.apply {
        BitmapDrawable(contacts.resources, it)
    }

// endregion

// region SET PHOTO

/**
 * Sets the photo of this [MutableContact.primaryPhotoHolder], pending an update API call. If a
 * photo already exists, it will be overwritten. The Contacts Provider automatically creates a
 * downsized version of this as the thumbnail.
 *
 * Make sure that the [MutableContact] came from a query that included all fields from
 * [contacts.core.Fields.PrimaryPhotoHolder]. Otherwise, the incorrect child RawContact's photo may
 * be set (in case the Contact has more than one child RawContact).
 *
 * The given [photoData] will not be set until the update API call is committed successfully.
 *
 * If you want to directly set the photo into the database, without an update API call, use
 * [MutableContact.setPhotoDirect].
 *
 * ## Not parcelable
 *
 * The [photoData] is ignored on parcel. The [photoData] will not be carried over across activities,
 * fragments, or views during creation/recreation.
 */
fun MutableContact.setPhoto(photoData: PhotoData) {
    primaryPhotoHolder?.photoDataOperation = PhotoDataOperation.SetPhoto(photoData)
}

// endregion

// region SET PHOTO DIRECT

/**
 * Sets the photo of this [ExistingContactEntity] (and the [contacts.core.entities.RawContact] that
 * the Contacts Provider has chosen to hold the primary photo) directly to the database. If a photo
 * already exists, it will be overwritten. The Contacts Provider automatically creates a downsized
 * version of this as the thumbnail.
 *
 * Make sure that the [ExistingContactEntity] came from a query that included all fields from
 * [contacts.core.Fields.PrimaryPhotoHolder]. Otherwise, the incorrect child RawContact's photo may
 * be set (in case the Contact has more than one child RawContact).
 *
 * Returns true if the operation succeeds.
 *
 * Supports profile and non-profile Contacts.
 *
 * ## Changes are immediate
 *
 * This function will make the changes to the Contacts Provider database immediately. You do not
 * need to use update APIs to commit the changes.
 *
 * ## Changes are not applied to the receiver
 *
 * This function call does NOT mutate immutable or mutable receivers. Therefore, you should use
 * query APIs or refresh extensions or process the result of this function call to get the most
 * up-to-date reference to mutable or immutable entity that contains the changes in the Contacts
 * Provider database.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.WRITE_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 *
 * ## DEV NOTES
 *
 * The function body is mostly taken from the sample code from the
 * [ContactsContract.RawContacts.DisplayPhoto] class documentation.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ExistingContactEntity.setPhotoDirect(contacts: Contacts, photoData: PhotoData): Boolean {
    if (!contacts.permissions.canUpdateDelete()) {
        return false
    }

    return primaryPhotoHolder?.setPhotoDirect(contacts, photoData) == true
}

// endregion

// region REMOVE PHOTO

/**
 * Removes the photos, pending an update API call.
 *
 * If [fromAllRawContacts] is true, the photos of all RawContacts associated with this
 * [ExistingContactEntity] will be removed. Otherwise, only the photo of the
 * [ExistingContactEntity.primaryPhotoHolder] will be removed.
 *
 * Make sure that the [MutableContact] came from a query that included all fields from
 * [contacts.core.Fields.PrimaryPhotoHolder]. Otherwise, the incorrect child RawContact's photo may
 * be removed (in case the Contact has more than one child RawContact).
 *
 * The photo will not be removed until the update API call is committed successfully.
 *
 * If you want to directly remove the photo from the database, without an update API call, use
 * [MutableContact.removePhotoDirect].
 *
 * ## Not parcelable
 *
 * This action is ignored on parcel. This action will not be carried over across activities,
 * fragments, or views during creation/recreation.
 */
@JvmOverloads
fun MutableContact.removePhoto(fromAllRawContacts: Boolean = false) {
    if (fromAllRawContacts) {
        for (rawContact in rawContacts) {
            rawContact.photoDataOperation = PhotoDataOperation.RemovePhoto
        }
    } else {
        primaryPhotoHolder?.photoDataOperation = PhotoDataOperation.RemovePhoto
    }
}

// endregion

// region REMOVE PHOTO DIRECT

/**
 * Removes the photos directly from the database.
 *
 * If [fromAllRawContacts] is true, the photos of all RawContacts associated with this
 * [ExistingContactEntity] will be removed. Otherwise, only the photo of the
 * [ExistingContactEntity.primaryPhotoHolder] will be removed.
 *
 * Make sure that the [ExistingContactEntity] came from a query that included all fields from
 * [contacts.core.Fields.PrimaryPhotoHolder]. Otherwise, the incorrect child RawContact's photo may
 * be removed (in case the Contact has more than one child RawContact).
 *
 * Returns true if the operation succeeds.
 *
 * Supports profile and non-profile Contacts.
 *
 * ## Changes are immediate
 *
 * This function will make the changes to the Contacts Provider database immediately. You do not
 * need to use update APIs to commit the changes.
 *
 * ## Changes are not applied to the receiver
 *
 * This function call does NOT mutate immutable or mutable receivers. Therefore, you should use
 * query APIs or refresh extensions or process the result of this function call to get the most
 * up-to-date reference to mutable or immutable entity that contains the changes in the Contacts
 * Provider database.
 *
 * ## Permissions
 *
 * This requires the [ContactsPermissions.WRITE_PERMISSION].
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun ExistingContactEntity.removePhotoDirect(
    contacts: Contacts, fromAllRawContacts: Boolean = false
): Boolean {
    if (!contacts.permissions.canUpdateDelete()) {
        return false
    }

    val whereId = if (fromAllRawContacts) {
        Fields.Contact.Id equalTo id
    } else {
        val primaryPhotoHolderId = primaryPhotoHolder?.id
        if (primaryPhotoHolderId != null) {
            Fields.RawContact.Id equalTo primaryPhotoHolderId
        } else {
            return false
        }
    }

    return contacts.contentResolver.applyBatch(
        newDelete(
            contacts.dataUri(isProfile = isProfile)
        ).withSelection(whereId and (Fields.MimeType equalTo MimeType.Photo))
            .build()
    ) != null
}

// endregion