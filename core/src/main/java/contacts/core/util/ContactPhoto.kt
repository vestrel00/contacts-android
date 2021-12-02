package contacts.core.util

import android.content.ContentProviderOperation.newDelete
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.ContactsContract
import contacts.core.*
import contacts.core.entities.ContactEntity
import contacts.core.entities.MimeType
import contacts.core.entities.TempRawContact
import contacts.core.entities.cursor.contactsCursor
import contacts.core.entities.cursor.dataCursor
import contacts.core.entities.mapper.tempRawContactMapper
import contacts.core.entities.operation.withSelection
import contacts.core.entities.table.ProfileUris
import contacts.core.entities.table.Table
import java.io.IOException
import java.io.InputStream

// region GET PHOTO

/**
 * Returns the full-sized photo as an [InputStream]. Returns null if a photo has not yet been set.
 *
 * This does not use the [ContactEntity.photoUri] from this instance. Instead, a query is made to
 * retrieve the latest photo uri from the database in order to ensure validity.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ContactEntity.rawContacts] depending
 * on query filters.
 *
 * It is up to the caller to close the [InputStream].
 *
 * Supports profile and non-profile Contacts.
 *
 * ## For existing (inserted) entities only
 *
 * This function will only work for entities that have already been inserted into the Contacts
 * Provider database. This means that this is only for entities that have been retrieved using
 * query or result APIs.
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
fun ContactEntity.photoInputStream(contacts: Contacts): InputStream? {
    val contactId = id

    if (!contacts.permissions.canQuery() || contactId == null) {
        return null
    }

    return contacts.applicationContext.contentResolver.query(
        if (isProfile) ProfileUris.CONTACTS.uri else Table.Contacts.uri,
        Include(ContactsFields.PhotoUri),
        ContactsFields.Id equalTo contactId
    ) {
        val photoUri = it.getNextOrNull { it.contactsCursor().photoUri }
        uriInputStream(contacts, photoUri)
    }
}

/**
 * Returns the full-sized photo as a [ByteArray]. Returns null if a photo has not yet been set.
 *
 * This does not use the [ContactEntity.photoUri] from this instance. Instead, a query is made to
 * retrieve the latest photo uri from the database in order to ensure validity.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ContactEntity.rawContacts] depending
 * on query filters.
 *
 * Supports profile and non-profile Contacts.
 *
 * ## For existing (inserted) entities only
 *
 * This function will only work for entities that have already been inserted into the Contacts
 * Provider database. This means that this is only for entities that have been retrieved using
 * query or result APIs.
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
fun ContactEntity.photoBytes(contacts: Contacts): ByteArray? = photoInputStream(contacts)?.apply {
    it.readBytes()
}

/**
 * Returns the full-sized photo as a [Bitmap]. Returns null if a photo has not yet been set.
 *
 * This does not use the [ContactEntity.photoUri] from this instance. Instead, a query is made to
 * retrieve the latest photo uri from the database in order to ensure validity.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ContactEntity.rawContacts] depending
 * on query filters.
 *
 * Supports profile and non-profile Contacts.
 *
 * ## For existing (inserted) entities only
 *
 * This function will only work for entities that have already been inserted into the Contacts
 * Provider database. This means that this is only for entities that have been retrieved using
 * query or result APIs.
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
fun ContactEntity.photoBitmap(contacts: Contacts): Bitmap? = photoInputStream(contacts)?.apply {
    BitmapFactory.decodeStream(it)
}

/**
 * Returns the full-sized photo as a [BitmapDrawable]. Returns null if a photo has not yet been set.
 *
 * This does not use the [ContactEntity.photoUri] from this instance. Instead, a query is made to
 * retrieve the latest photo uri from the database in order to ensure validity.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ContactEntity.rawContacts] depending
 * on query filters.
 *
 * Supports profile and non-profile Contacts.
 *
 * ## For existing (inserted) entities only
 *
 * This function will only work for entities that have already been inserted into the Contacts
 * Provider database. This means that this is only for entities that have been retrieved using
 * query or result APIs.
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
fun ContactEntity.photoBitmapDrawable(contacts: Contacts): BitmapDrawable? =
    photoInputStream(contacts)?.apply {
        BitmapDrawable(contacts.applicationContext.resources, it)
    }

private fun uriInputStream(contacts: Contacts, uri: Uri?): InputStream? {
    if (uri == null) {
        return null
    }

    var inputStream: InputStream? = null
    try {
        val fd = contacts.applicationContext.contentResolver.openAssetFileDescriptor(uri, "r")
        inputStream = fd?.createInputStream()
    } catch (ioe: IOException) {
        // do nothing
    }
    return inputStream
}

// endregion

// region GET PHOTO THUMBNAIL

/**
 * Returns the photo thumbnail as an [InputStream]. Returns null if a photo has not yet been set.
 *
 * This does not use the [ContactEntity.photoThumbnailUri] from this instance. Instead, a query is
 * made to retrieve the latest photo thumbnail uri from the database in order to ensure validity.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ContactEntity.rawContacts] depending
 * on query filters.
 *
 * It is up to the caller to close the [InputStream].
 *
 * Supports profile and non-profile Contacts.
 *
 * ## For existing (inserted) entities only
 *
 * This function will only work for entities that have already been inserted into the Contacts
 * Provider database. This means that this is only for entities that have been retrieved using
 * query or result APIs.
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
fun ContactEntity.photoThumbnailInputStream(contacts: Contacts): InputStream? {
    val contactId = id

    if (!contacts.permissions.canQuery() || contactId == null) {
        return null
    }

    return contacts.applicationContext.contentResolver.query(
        if (isProfile) ProfileUris.CONTACTS.uri else Table.Contacts.uri,
        Include(ContactsFields.PhotoThumbnailUri),
        ContactsFields.Id equalTo contactId
    ) {
        val photoThumbnailUri = it.getNextOrNull { it.contactsCursor().photoThumbnailUri }
        uriInputStream(contacts, photoThumbnailUri)
    }
}

/**
 * Returns the photo thumbnail as a [ByteArray]. Returns null if a photo has not yet been set.
 *
 * This does not use the [ContactEntity.photoThumbnailUri] from this instance. Instead, a query is
 * made to retrieve the latest photo thumbnail uri from the database in order to ensure validity.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ContactEntity.rawContacts] depending
 * on query filters.
 *
 * The stream should be closed after use.
 *
 * Supports profile and non-profile Contacts.
 *
 * ## For existing (inserted) entities only
 *
 * This function will only work for entities that have already been inserted into the Contacts
 * Provider database. This means that this is only for entities that have been retrieved using
 * query or result APIs.
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
fun ContactEntity.photoThumbnailBytes(contacts: Contacts): ByteArray? =
    photoThumbnailInputStream(contacts)?.apply {
        it.readBytes()
    }

/**
 * Returns the full-sized photo as a [Bitmap]. Returns null if a photo has not yet been set.
 *
 * This does not use the [ContactEntity.photoThumbnailUri] from this instance. Instead, a query is
 * made to retrieve the latest photo thumbnail uri from the database in order to ensure validity.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ContactEntity.rawContacts] depending
 * on query filters.
 *
 * Supports profile and non-profile Contacts.
 *
 * ## For existing (inserted) entities only
 *
 * This function will only work for entities that have already been inserted into the Contacts
 * Provider database. This means that this is only for entities that have been retrieved using
 * query or result APIs.
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
fun ContactEntity.photoThumbnailBitmap(contacts: Contacts): Bitmap? =
    photoThumbnailInputStream(contacts)?.apply {
        BitmapFactory.decodeStream(it)
    }

/**
 * Returns the full-sized photo as a [BitmapDrawable]. Returns null if a photo has not yet been set.
 *
 * This does not use the [ContactEntity.photoThumbnailUri] from this instance. Instead, a query is
 * made to retrieve the latest photo thumbnail uri from the database in order to ensure validity.
 *
 * This photo is picked from one of the associated [android.provider.ContactsContract.RawContacts]s
 * by the Contacts Provider, which may not be in the list of [ContactEntity.rawContacts] depending
 * on query filters.
 *
 * Supports profile and non-profile Contacts.
 *
 * ## For existing (inserted) entities only
 *
 * This function will only work for entities that have already been inserted into the Contacts
 * Provider database. This means that this is only for entities that have been retrieved using
 * query or result APIs.
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
fun ContactEntity.photoThumbnailBitmapDrawable(contacts: Contacts): BitmapDrawable? =
    photoThumbnailInputStream(contacts)?.apply {
        BitmapDrawable(contacts.applicationContext.resources, it)
    }

// endregion

// region SET PHOTO

/**
 * Sets the photo of this [ContactEntity] (and the [contacts.core.entities.RawContact] that the
 * Contacts Provider has chosen to hold the primary photo). If a photo already exists, it will be
 * overwritten. The Contacts Provider automatically creates a downsized version of this as the
 * thumbnail.
 *
 * If a photo has not yet been set and the Contacts Provider has not yet chosen the RawContact that
 * will be used as the primary photo holder, then this will use the first RawContact in the list of
 * [ContactEntity.rawContacts].
 *
 * Returns true if the operation succeeds.
 *
 * Supports profile and non-profile Contacts.
 *
 * ## For existing (inserted) entities only
 *
 * This function will only work for entities that have already been inserted into the Contacts
 * Provider database. This means that this is only for entities that have been retrieved using
 * query or result APIs.
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
fun ContactEntity.setPhoto(contacts: Contacts, photoBytes: ByteArray): Boolean {
    val contactId = id

    if (!contacts.permissions.canUpdateDelete() || contactId == null) {
        return false
    }

    val photoFileId = photoFileId(contacts, contactId)

    val rawContact = if (photoFileId != null) {
        // A photo exists for the Contact. Get the RawContact in the Data table that holds the photo
        // row with the same photo file id. Keep in mind that there may be multiple RawContacts
        // associated with a single Contact.
        rawContactWithPhotoFileId(contacts, photoFileId)
    } else {
        // No photo exists for the Contact or any of its associated RawContacts. Use the
        // first RawContact as the default or fail if also not available.
        rawContacts.firstOrNull()
    }

    return rawContact?.doSetPhoto(contacts, photoBytes) == true
}

/**
 * See [ContactEntity.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ContactEntity.setPhoto(contacts: Contacts, photoInputStream: InputStream): Boolean =
    setPhoto(contacts, photoInputStream.readBytes())

/**
 * See [ContactEntity.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ContactEntity.setPhoto(contacts: Contacts, photoBitmap: Bitmap): Boolean =
    setPhoto(contacts, photoBitmap.bytes())

/**
 * See [ContactEntity.setPhoto].
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
fun ContactEntity.setPhoto(contacts: Contacts, photoDrawable: BitmapDrawable): Boolean =
    setPhoto(contacts, photoDrawable.bitmap.bytes())

private fun ContactEntity.photoFileId(contacts: Contacts, contactId: Long): Long? =
    contacts.applicationContext.contentResolver.query(
        if (isProfile) ProfileUris.CONTACTS.uri else Table.Contacts.uri,
        Include(ContactsFields.PhotoFileId),
        ContactsFields.Id equalTo contactId
    ) {
        it.getNextOrNull { it.contactsCursor().photoFileId }
    }

private fun ContactEntity.rawContactWithPhotoFileId(
    contacts: Contacts, photoFileId: Long
): TempRawContact? =
    contacts.applicationContext.contentResolver.query(
        if (isProfile) ProfileUris.DATA.uri else Table.Data.uri,
        Include(Fields.RawContact.Id),
        Fields.Photo.PhotoFileId equalTo photoFileId
    ) {
        it.getNextOrNull { it.dataCursor().tempRawContactMapper().value }
    }

// endregion

// region REMOVE PHOTO

/**
 * Removes the photos of all the RawContacts associated with this [ContactEntity], if any exists.
 *
 * Returns true if the operation succeeds.
 *
 * The native Contacts app actually does not provide the option to remove the photo of a Contact
 * with at least 2 associated RawContacts.
 *
 * Supports profile and non-profile Contacts.
 *
 * ## For existing (inserted) entities only
 *
 * This function will only work for entities that have already been inserted into the Contacts
 * Provider database. This means that this is only for entities that have been retrieved using
 * query or result APIs.
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
fun ContactEntity.removePhoto(contacts: Contacts): Boolean {
    val contactId = id

    if (!contacts.permissions.canUpdateDelete() || contactId == null) {
        return false
    }

    return contacts.applicationContext.contentResolver.applyBatch(
        newDelete(if (isProfile) ProfileUris.DATA.uri else Table.Data.uri)
            .withSelection(
                (Fields.Contact.Id equalTo contactId)
                        and (Fields.MimeType equalTo MimeType.Photo)
            )
            .build()
    ) != null
}

// endregion